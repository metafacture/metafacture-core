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

import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.metafix.fix.Expression;
import org.metafacture.metafix.fix.Fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
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
public class Metafix implements StreamPipe<StreamReceiver> {

    public static final String VAR_START = "$[";
    public static final String VAR_END = "]";
    public static final Map<String, String> NO_VARS = Collections.emptyMap();
    private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";

    private static final Logger LOG = LoggerFactory.getLogger(Metafix.class);

    // TODO: Use SimpleRegexTrie / WildcardTrie for wildcard, alternation and character class support
    private Map<String, Object> currentRecord = new LinkedHashMap<>();
    private Fix fix;
    private final List<Expression> expressions = new ArrayList<>();
    private Map<String, String> vars = NO_VARS;
    private final StreamFlattener flattener = new StreamFlattener();
    private final Deque<Integer> entityCountStack = new LinkedList<>();
    private int entityCount;
    private StreamReceiver outputStreamReceiver;
    private String recordIdentifier;
    private List<Map<String, Object>> entities = new ArrayList<>();

    public Metafix() {
        init();
    }

    public Metafix(final String fixDef) throws FileNotFoundException {
        this(fixDef, NO_VARS);
    }

    public Metafix(final String fixDef, final Map<String, String> vars) throws FileNotFoundException {
        this(fixDef.endsWith(".fix") ? new FileReader(fixDef) : new StringReader(fixDef), vars);
    }

    public Metafix(final Reader morphDef) {
        this(morphDef, NO_VARS);
    }

    public Metafix(final Reader fixDef, final Map<String, String> vars) {
        buildPipeline(fixDef, vars);
        init();
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    private void init() {
        flattener.setReceiver(new DefaultStreamReceiver() {
            @Override
            public void literal(final String name, final String value) {
                // TODO: keep flattener as option?
                // add(currentRecord, name, value);
            }
        });
    }

    private void buildPipeline(final Reader fixDef, final Map<String, String> theVars) {
        final Fix f = FixStandaloneSetup.parseFix(fixDef);
        this.fix = f;
        this.vars = theVars;
    }

    @Override
    public void startRecord(final String identifier) {
        currentRecord = new LinkedHashMap<>();
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
        final RecordTransformer transformer = new RecordTransformer(currentRecord, vars, fix);
        currentRecord = transformer.transform();
        if (!currentRecord.containsKey("__reject")) {
            outputStreamReceiver.startRecord(recordIdentifier);
            LOG.debug("Sending results to {}", outputStreamReceiver);
            currentRecord.keySet().forEach(k -> {
                emit(k, currentRecord.get(k));
            });
            outputStreamReceiver.endRecord();
        }
    }

    private void emit(final Object key, final Object val) {
        if (val == null) {
            return;
        }
        final List<?> vals = asList(val);
        final boolean isMulti = vals.size() > 1 || val instanceof List;
        if (isMulti) {
            outputStreamReceiver.startEntity(key.toString() + "[]");
        }
        for (int i = 0; i < vals.size(); ++i) {
            final Object value = vals.get(i);
            if (value instanceof Map) {
                final Map<?, ?> nested = (Map<?, ?>) value;
                outputStreamReceiver.startEntity(isMulti ? "" : key.toString());
                nested.entrySet().forEach(nestedEntry -> {
                    emit(nestedEntry.getKey(), nestedEntry.getValue());
                });
                outputStreamReceiver.endEntity();
            }
            else {
                outputStreamReceiver.literal(isMulti ? (i + 1) + "" : key.toString(), value.toString());
            }
        }
        if (isMulti) {
            outputStreamReceiver.endEntity();
        }
    }

    @Override
    public void startEntity(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity name must not be null.");
        }
        ++entityCount;
        final Integer currentEntityIndex = entityCountStack.peek() - 1;
        final Map<String, Object> previousEntity = currentEntityIndex < 0 ||
                entities.size() <= currentEntityIndex ? null : entities.get(currentEntityIndex);
        entityCountStack.push(Integer.valueOf(entityCount));
        flattener.startEntity(name);
        entities.add(currentEntity(name, previousEntity == null && entities.size() >= 0 ? currentRecord : previousEntity));
    }

    private Map<String, Object> currentEntity(final String name, final Map<String, Object> previousEntity) {
        final Object existingValue = previousEntity != null ? previousEntity.get(name) : null;
        final Map<String, Object> currentEntity;
        if (existingValue != null && existingValue instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> existingEntity = (Map<String, Object>) previousEntity.get(name);
            currentEntity = existingEntity;
        }
        else {
            currentEntity = new LinkedHashMap<>();
            add(previousEntity != null ? previousEntity : currentRecord, name, currentEntity);
        }
        return currentEntity;
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
        final Map<String, Object> currentEntity = currentEntityIndex < 0 ||
                entities.size() <= currentEntityIndex ? null : entities.get(currentEntityIndex);
        add(currentEntity != null ? currentEntity : currentRecord, name, value);
        // TODO: keep flattener as option?
        // flattener.literal(name, value);
    }

    @Override
    public void resetStream() {
        outputStreamReceiver.resetStream();
    }

    @Override
    public void closeStream() {
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

    public Map<String, Object> getCurrentRecord() {
        return currentRecord;
    }

    static void addAll(final Map<String, Object> record, final String fieldName, final List<String> values) {
        values.forEach(value -> {
            add(record, fieldName, value);
        });
    }

    static void addAll(final Map<String, Object> record, final Map<String, Object> values) {
        values.entrySet().forEach(value -> {
            add(record, value.getKey(), value.getValue());
        });
    }

    static void add(final Map<String, Object> record, final String name, final Object newValue) {
        final Object oldValue = record.get(name);
        record.put(name, oldValue == null ? newValue : merged(oldValue, newValue));
    }

    @SuppressWarnings("unchecked")
    static Object merged(final Object object1, final Object object2) {
        if (object1 instanceof Map && object2 instanceof Map) {
            ((Map<String, Object>) object1).putAll((Map<String, Object>) object2);
            return object1;
        }
        final List<Object> list = asList(object1);
        asList(object2).forEach(e -> {
            list.add(e);
        });
        return list;
    }

    @SuppressWarnings("unchecked")
    static List<Object> asList(final Object object) {
        return new ArrayList<>(
                object instanceof List ? (List<Object>) object : Arrays.asList(object));
    }

}
