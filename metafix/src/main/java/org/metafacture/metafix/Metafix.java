/*
 * Copyright 2013, 2023 Deutsche Nationalbibliothek and others
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StandardEventNames;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.metafix.fix.Expression;
import org.metafacture.metamorph.api.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Transforms a data stream sent via the {@link StreamReceiver} interface. Uses
 * {@link RecordTransformer} to transform records based on a Fix DSL description.
 *
 * @author Markus Michael Geipel (Metamorph)
 * @author Christoph Böhme (Metamorph)
 * @author Fabian Steeg (Metafix)
 */
@Description("Applies a fix transformation to the event stream, given as the path to a fix file or the fixes themselves.") // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("fix")
public class Metafix implements StreamPipe<StreamReceiver>, Maps {
    public static final String ARRAY_MARKER = "[]";
    public static final String FIX_EXTENSION = ".fix";
    public static final String VAR_END = "]";
    public static final String VAR_START = "$[";

    public static final Strictness DEFAULT_STRICTNESS = Strictness.PROCESS;
    public static final String DEFAULT_ENTITY_MEMBER_NAME = "%d";

    public static final Map<String, String> NO_VARS = Collections.emptyMap();

    private static final Logger LOG = LoggerFactory.getLogger(Metafix.class);

    private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";

    private final Deque<Integer> entityCountStack = new LinkedList<>();
    private final List<Closeable> resources = new ArrayList<>();
    private final List<Expression> expressions = new ArrayList<>();
    private final Map<String, Map<String, String>> maps = new HashMap<>();
    private final Map<String, RecordTransformer> fixCache = new HashMap<>();
    private final Map<String, RecordTransformer> macros = new HashMap<>();
    private final Map<String, String> pathCache = new HashMap<>();
    private final Map<String, String> vars = new HashMap<>();
    private final RecordTransformer recordTransformer;
    private final StreamFlattener flattener = new StreamFlattener();

    private List<Value> entities = new ArrayList<>();
    private Record currentRecord = new Record();
    private StreamReceiver outputStreamReceiver;
    private Strictness strictness = DEFAULT_STRICTNESS;
    private String fixFile;
    private String recordIdentifier;
    private String entityMemberName = DEFAULT_ENTITY_MEMBER_NAME;
    private boolean repeatedFieldsToEntities;
    private boolean strictnessHandlesProcessExceptions;
    private int entityCount;

    public Metafix() {
        this(NO_VARS);
    }

    public Metafix(final Map<String, String> newVars) {
        init(newVars);
        recordTransformer = null;
    }

    public Metafix(final String fixDef) throws IOException {
        this(fixDef, NO_VARS);
    }

    public Metafix(final String fixDef, final Map<String, String> vars) throws IOException {
        init(vars);

        if (isFixFile(fixDef)) {
            fixFile = fixDef;
            recordTransformer = getRecordTransformer(fixDef);
        }
        else {
            try (Reader reader = new StringReader(fixDef)) {
                recordTransformer = getRecordTransformer(reader);
            }
        }
    }

    public Metafix(final Reader fixDef) {
        this(fixDef, NO_VARS);
    }

    public Metafix(final Reader fixDef, final Map<String, String> vars) {
        init(vars);
        recordTransformer = getRecordTransformer(fixDef);
    }

    private void init(final Map<String, String> newVars) {
        flattener.setReceiver(new DefaultStreamReceiver() {

            @Override
            public void literal(final String name, final String value) {
                final String[] split = Value.split(name);
                addValue(split[split.length - 1], new Value(value));
                // TODO use full path here to insert only once?
                // new FixPath(name).insertInto(currentRecord, InsertMode.APPEND, new Value(value));
            }

        });

        vars.putAll(newVars);
    }

    /*package-private*/ static boolean isFixFile(final String fixDef) {
        return fixDef.endsWith(FIX_EXTENSION);
    }

    public String resolvePath(final String path) {
        return pathCache.computeIfAbsent(path, this::resolvePathInternal);
    }

    private String resolvePathInternal(final String path) {
        final String resolvedPath;

        if (isValidUrl(path)) {
            resolvedPath = path;
            LOG.debug("Resolved path: url = '{}'", resolvedPath);
        }
        else {
            final Path basePath;

            if (path.startsWith(".")) {
                if (fixFile != null) {
                    basePath = getPath(fixFile).getParent();
                }
                else {
                    throw new IllegalArgumentException("Cannot resolve relative path: " + path);
                }
            }
            else {
                basePath = getPath("");
            }

            resolvedPath = basePath.resolve(path).normalize().toString();
            LOG.debug("Resolved path: base = '{}', path = '{}', result = '{}'", basePath, path, resolvedPath);
        }

        return resolvedPath;
    }

    private boolean isValidUrl(final String url) {
        try {
            new URL(url);
            return true;
        }
        catch (final MalformedURLException e) {
            return false;
        }
    }

    private Path getPath(final String path) {
        return Paths.get(path).toAbsolutePath().normalize();
    }

    public RecordTransformer getRecordTransformer(final String fixDef) {
        return fixCache.computeIfAbsent(fixDef, k -> new RecordTransformer(this, FixStandaloneSetup.parseFix(k)));
    }

    private RecordTransformer getRecordTransformer(final Reader fixDef) {
        return new RecordTransformer(this, FixStandaloneSetup.parseFix(fixDef));
    }

    public void putMacro(final String name, final RecordTransformer macro) {
        macros.put(name, macro);
    }

    public RecordTransformer getMacro(final String name) {
        final RecordTransformer macro = macros.get(name);

        if (macro != null) {
            macro.setParentExceptionMessageFrom(recordTransformer);
        }

        return macro;
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
        LOG.debug("End record, walking Fix: {}", currentRecord);
        recordTransformer.transform(currentRecord);
        if (!currentRecord.getReject()) {
            outputStreamReceiver.startRecord(recordIdentifier);
            LOG.debug("Sending results to {}", outputStreamReceiver);
            currentRecord.forEach(this::emit);
            outputStreamReceiver.endRecord();
        }
    }

    private void emit(final String field, final Value value) {
        Value.asList(value, array -> {
            final boolean isMulti = repeatedFieldsToEntities && array.size() > 1 || isArrayName(field);
            if (isMulti) {
                outputStreamReceiver.startEntity(field);
            }

            for (int i = 0; i < array.size(); ++i) {
                final Value currentValue = array.get(i);
                final String fieldName = isMulti ? String.format(entityMemberName, i + 1) : field;

                currentValue.matchType()
                    .ifArray(a -> emit(isMulti ? fieldName + ARRAY_MARKER : fieldName, currentValue))
                    .ifHash(h -> {
                        outputStreamReceiver.startEntity(fieldName);
                        h.forEach(this::emit);
                        outputStreamReceiver.endEntity();
                    })
                    .ifString(s -> outputStreamReceiver.literal(fieldName, s));
            }

            if (isMulti) {
                outputStreamReceiver.endEntity();
            }
        });
    }

    private boolean isArrayName(final String name) {
        return name.endsWith(ARRAY_MARKER);
    }

    private void addValue(final String name, final Value value) {
        final int index = entityCountStack.peek() - 1;
        if (index < 0 || entities.size() <= index) {
            currentRecord.add(name, value);
        }
        else {
            final Value entity = entities.get(index);
            value.withPathSet(entity.getPath());
            entity.matchType()
                .ifArray(a -> a.add(value))
                .ifHash(h -> h.add(name, value))
                .orElseThrow();
        }
    }

    @Override
    public void startEntity(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity name must not be null.");
        }

        final Value value = isArrayName(name) ? Value.newArray() : Value.newHash();
        addValue(name, value);
        entities.add(value);

        entityCountStack.push(Integer.valueOf(++entityCount));
        flattener.startEntity(name);
    }

    @Override
    public void endEntity() {
        entityCountStack.pop().intValue();
        flattener.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        LOG.debug("Putting '{}': '{}'", name, value);
        flattener.literal(name, value);
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
                throw new UncheckedIOException(e);
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

    public void setStrictness(final Strictness strictness) {
        this.strictness = strictness != null ? strictness : DEFAULT_STRICTNESS;
    }

    public Strictness getStrictness() {
        return strictness;
    }

    public void setStrictnessHandlesProcessExceptions(final boolean strictnessHandlesProcessExceptions) {
        this.strictnessHandlesProcessExceptions = strictnessHandlesProcessExceptions;
    }

    public boolean getStrictnessHandlesProcessExceptions() {
        return strictnessHandlesProcessExceptions;
    }

    public void setRepeatedFieldsToEntities(final boolean repeatedFieldsToEntities) {
        this.repeatedFieldsToEntities = repeatedFieldsToEntities;
    }

    public boolean getRepeatedFieldsToEntities() {
        return repeatedFieldsToEntities;
    }

    public void setEntityMemberName(final String entityMemberName) {
        this.entityMemberName = entityMemberName;
    }

    public String getEntityMemberName() {
        return entityMemberName;
    }

    public enum Strictness {

        /**
         * Aborts process by throwing an exception.
         */
        PROCESS {
            @Override
            protected void handleInternal(final MetafactureException exception, final Record record) {
                throw exception;
            }
        },

        /**
         * Ignores (skips) record and logs an error.
         */
        RECORD {
            @Override
            protected void handleInternal(final MetafactureException exception, final Record record) {
                log(exception, LOG::error);
                record.setReject(true); // TODO: Skip remaining expressions?
            }
        },

        /**
         * Ignores (skips) expression and logs a warning.
         */
        EXPRESSION {
            @Override
            protected void handleInternal(final MetafactureException exception, final Record record) {
                log(exception, LOG::warn);
            }
        };

        public void handle(final MetafactureException exception, final Record record) {
            LOG.info("Current record: {}", record);
            handleInternal(exception, record);
        }

        protected abstract void handleInternal(MetafactureException exception, Record record);

        protected void log(final MetafactureException exception, final BiConsumer<String, Throwable> logger) {
            logger.accept(exception.getMessage(), exception.getCause());
        }

    }
}
