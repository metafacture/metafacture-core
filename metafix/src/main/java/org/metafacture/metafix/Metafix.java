/*
 * Copyright 2013, 2019 Deutsche Nationalbibliothek and others
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//TODO: move all classes here to fix package

package org.metafacture.metafix;

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StandardEventNames;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.metafix.fix.Expression;
import org.metafacture.metafix.fix.Fix;
import org.metafacture.metamorph.api.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Transforms a data stream sent via the {@link StreamReceiver} interface. Use
 * {@link RecordTransformer} to transform based on a Fix DSL description.
 *
 * @author Markus Michael Geipel (Metamorph)
 * @author Christoph Böhme (Metamorph)
 * @author Fabian Steeg (Metafix)
 */
public class Metafix implements StreamPipe<StreamReceiver>, Maps { // checkstyle-disable-line ClassDataAbstractionCoupling

    public static final String VAR_START = "$[";
    public static final String VAR_END = "]";
    public static final Map<String, String> NO_VARS = Collections.emptyMap();
    public static final String ARRAY_MARKER = "[]";

    private static final Logger LOG = LoggerFactory.getLogger(Metafix.class);

    private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";

    private final Deque<Integer> entityCountStack = new LinkedList<>();
    private final List<Closeable> resources = new ArrayList<>();
    private final List<Expression> expressions = new ArrayList<>();
    private final Map<String, Map<String, String>> maps = new HashMap<>();
    private final StreamFlattener flattener = new StreamFlattener();

    // TODO: Use SimpleRegexTrie / WildcardTrie for wildcard, alternation and character class support
    private Record currentRecord = new Record();
    private Fix fix;
    private Map<String, String> vars = new HashMap<>();
    private int entityCount;
    private StreamReceiver outputStreamReceiver;
    private String recordIdentifier;
    private List<Value.Hash> entities = new ArrayList<>();

    public Metafix() {
        flattener.setReceiver(new DefaultStreamReceiver() {
            @Override
            public void literal(final String name, final String value) {
                // TODO: keep flattener as option?
                // add(currentRecord, name, value);
            }
        });
    }

    public Metafix(final Map<String, String> newVars) {
        this();
        vars.putAll(newVars);
    }

    public Metafix(final String fixDef) throws FileNotFoundException {
        this(fixDef, NO_VARS);
    }

    public Metafix(final String fixDef, final Map<String, String> vars) throws FileNotFoundException {
        this(fixDef.endsWith(".fix") ? new FileReader(fixDef) : new StringReader(fixDef), vars);
    }

    public Metafix(final Reader fixDef) {
        this(fixDef, NO_VARS);
    }

    public Metafix(final Reader fixDef, final Map<String, String> vars) {
        this(vars);
        fix = FixStandaloneSetup.parseFix(fixDef);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public void startRecord(final String identifier) {
        currentRecord = new Record();
        currentRecord.putVirtualField(StandardEventNames.ID, new Value(identifier));
        LOG.debug("Start record: {}", identifier);
        flattener.startRecord(identifier);
        entityCountStack.clear();
        entityCount = 0;
        entityCountStack.add(Integer.valueOf(entityCount));
        recordIdentifier = identifier;
        entities = new ArrayList<>();
    }

    @Override
    public void endRecord() {
        entityCountStack.removeLast();
        if (!entityCountStack.isEmpty()) {
            throw new IllegalStateException(ENTITIES_NOT_BALANCED);
        }
        flattener.endRecord();
        LOG.debug("End record, walking fix: {}", currentRecord);
        currentRecord = new RecordTransformer(this, fix).transform();
        if (!currentRecord.getReject()) {
            outputStreamReceiver.startRecord(recordIdentifier);
            LOG.debug("Sending results to {}", outputStreamReceiver);
            currentRecord.forEach(this::emit);
            outputStreamReceiver.endRecord();
        }
    }

    private void emit(final String field, final Value value) {
        Value.asList(value, array -> {
            final boolean isMulti = array.size() > 1 || value.isArray();
            if (isMulti) {
                outputStreamReceiver.startEntity(field);
            }

            for (int i = 0; i < array.size(); ++i) {
                final Value arrayValue = array.get(i);

                if (arrayValue.isHash()) {
                    outputStreamReceiver.startEntity(isMulti ? (i + 1) + "" : field);
                    arrayValue.asHash().forEach(this::emit);
                    outputStreamReceiver.endEntity();
                }
                else {
                    outputStreamReceiver.literal(isMulti ? (i + 1) + "" : field, arrayValue.toString());
                }
            }

            if (isMulti) {
                outputStreamReceiver.endEntity();
            }
        });
    }

    @Override
    public void startEntity(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity name must not be null.");
        }
        ++entityCount;
        final Integer currentEntityIndex = entityCountStack.peek() - 1;
        final Value.Hash previousEntity = currentEntityIndex < 0 ||
                entities.size() <= currentEntityIndex ? null : entities.get(currentEntityIndex);
        entityCountStack.push(Integer.valueOf(entityCount));
        flattener.startEntity(name);
        final Value value = Value.newHash();
        (previousEntity != null ? previousEntity : currentRecord).add(name, value);
        entities.add(value.asHash());
    }

    @Override
    public void endEntity() {
        entityCountStack.pop().intValue();
        flattener.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        LOG.debug("Putting '{}': '{}'", name, value);
        final Integer currentEntityIndex = entityCountStack.peek() - 1;
        final Value.Hash currentEntity = currentEntityIndex < 0 ||
                entities.size() <= currentEntityIndex ? null : entities.get(currentEntityIndex);
        (currentEntity != null ? currentEntity : currentRecord).add(name, new Value(value));
        // TODO: keep flattener as option?
        // flattener.literal(name, value);
    }

    @Override
    public void resetStream() {
        outputStreamReceiver.resetStream();
    }

    @Override
    public void closeStream() {
        for (final Closeable closeable : resources) {
            try {
                closeable.close();
            }
            catch (final IOException e) {
                throw new MetafactureException("Error while executing the Metafix transformation pipeline: " + e.getMessage(), e);
            }
        }

        outputStreamReceiver.closeStream();
    }

    /**
     * @param streamReceiver the outputHandler to set
     */
    @Override
    public <R extends StreamReceiver> R setReceiver(final R streamReceiver) {
        if (streamReceiver == null) {
            throw new IllegalArgumentException("'streamReceiver' must not be null");
        }
        outputStreamReceiver = streamReceiver;
        return streamReceiver;
    }

    public StreamReceiver getStreamReceiver() {
        return outputStreamReceiver;
    }

    public Map<String, String> getVars() {
        return vars;
    }

    public Record getCurrentRecord() {
        return currentRecord;
    }

    @Override
    public Collection<String> getMapNames() {
        return Collections.unmodifiableSet(maps.keySet());
    }

    @Override
    public Map<String, String> getMap(final String mapName) {
        return maps.getOrDefault(mapName, Collections.emptyMap());
    }

    @Override
    public String getValue(final String mapName, final String key) {
        final Map<String, String> map = getMap(mapName);
        return map.containsKey(key) ? map.get(key) : map.get(Maps.DEFAULT_MAP_KEY);
    }

    @Override
    public Map<String, String> putMap(final String mapName, final Map<String, String> map) {
        if (map instanceof Closeable) {
            resources.add((Closeable) map);
        }

        return maps.put(mapName, map);
    }

    @Override
    public String putValue(final String mapName, final String key, final String value) {
        return maps.computeIfAbsent(mapName, k -> new HashMap<>()).put(key, value);
    }

}
