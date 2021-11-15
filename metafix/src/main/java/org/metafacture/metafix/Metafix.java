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

import org.metafacture.framework.StandardEventNames;
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
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
    private Record currentRecord = new Record();
    private Fix fix;
    private final List<Expression> expressions = new ArrayList<>();
    private Map<String, String> vars = NO_VARS;
    private final StreamFlattener flattener = new StreamFlattener();
    private final Deque<Integer> entityCountStack = new LinkedList<>();
    private int entityCount;
    private StreamReceiver outputStreamReceiver;
    private String recordIdentifier;
    private List<Value.Hash> entities = new ArrayList<>();

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
        currentRecord = new Record();
        LOG.debug("Start record: {}", identifier);
        flattener.startRecord(identifier);
        entityCountStack.clear();
        entityCount = 0;
        entityCountStack.add(Integer.valueOf(entityCount));
        recordIdentifier = identifier;
        entities = new ArrayList<>();
        literal(StandardEventNames.ID, identifier);
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
        if (!currentRecord.getReject()) {
            outputStreamReceiver.startRecord(recordIdentifier);
            LOG.debug("Sending results to {}", outputStreamReceiver);
            currentRecord.forEach((k, v) -> {
                if (!k.startsWith("_")) {
                    emit(k, v);
                }
            });
            outputStreamReceiver.endRecord();
        }
    }

    private void emit(final String key, final Value val) {
        asList(val, vals -> {
            final boolean isMulti = vals.size() > 1 || val.isArray();
            if (isMulti) {
                outputStreamReceiver.startEntity(key + "[]");
            }

            for (int i = 0; i < vals.size(); ++i) {
                final Value value = vals.get(i);

                if (value.isHash()) {
                    outputStreamReceiver.startEntity(isMulti ? "" : key);
                    value.asHash().forEach(this::emit);
                    outputStreamReceiver.endEntity();
                }
                else {
                    outputStreamReceiver.literal(isMulti ? (i + 1) + "" : key, value.toString());
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
        entities.add(currentEntity(name, previousEntity != null ? previousEntity : currentRecord));
    }

    private Value.Hash currentEntity(final String name, final Value.Hash previousEntity) {
        final Value existingValue = previousEntity != null ? previousEntity.get(name) : null;
        final Value.Hash currentEntity;
        if (existingValue != null && existingValue.isHash()) {
            currentEntity = previousEntity.get(name).asHash();
        }
        else {
            final Value value = Value.newHash();
            currentEntity = value.asHash();
            add(previousEntity != null ? previousEntity : currentRecord, name, value);
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
        final Value.Hash currentEntity = currentEntityIndex < 0 ||
                entities.size() <= currentEntityIndex ? null : entities.get(currentEntityIndex);
        add(currentEntity != null ? currentEntity : currentRecord, name, new Value(value));
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

    public Record getCurrentRecord() {
        return currentRecord;
    }

    static void addAll(final Value.Hash record, final String fieldName, final List<String> values) {
        values.forEach(value -> add(record, fieldName, new Value(value)));
    }

    static void addAll(final Value.Hash record, final Value.Hash values) {
        values.forEach((fieldName, value) -> add(record, fieldName, value));
    }

    static void add(final Value.Hash record, final String name, final Value newValue) {
        final Value oldValue = record.get(name);
        record.put(name, oldValue == null ? newValue : merged(oldValue, newValue));
    }

    static Value merged(final Value value1, final Value value2) {
        if (value1.isHash() && value2.isHash()) {
            final Value.Hash hash = value1.asHash();
            value2.asHash().forEach(hash::put);
            return value1;
        }
        else {
            return asList(value1, a1 -> asList(value2, a2 -> a2.forEach(a1::add)));
        }
    }

    static Value asList(final Value value, final Consumer<Value.Array> consumer) {
        final Value result;

        if (Value.isNull(value)) {
            result = null;
        }
        else if (value.isArray()) {
            if (consumer != null) {
                consumer.accept(value.asArray());
            }

            result = value;
        }
        else {
            result = Value.newArray(a -> {
                a.add(value);

                if (consumer != null) {
                    consumer.accept(a);
                }
            });
        }

        return result;
    }

}
