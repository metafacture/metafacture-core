/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.metamorph;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StandardEventNames;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.metamorph.api.FlushListener;
import org.metafacture.metamorph.api.InterceptorFactory;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.MorphBuildException;
import org.metafacture.metamorph.api.MorphErrorHandler;
import org.metafacture.metamorph.api.NamedValuePipe;
import org.metafacture.metamorph.api.NamedValueReceiver;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.SourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Transforms a data stream sent via the {@link StreamReceiver} interface. Use
 * {@link MorphBuilder} to create an instance based on an XML description.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 */
@Description("Applies a metamorph transformation to the event stream. Metamorph definition is given in brackets.") // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("morph")
public final class Metamorph implements StreamPipe<StreamReceiver>, NamedValuePipe, Maps {

    public static final String ELSE_KEYWORD = "_else";
    public static final String ELSE_NESTED_KEYWORD = "_elseNested";
    public static final String ELSE_FLATTENED_KEYWORD = "_elseFlattened";
    public static final char FEEDBACK_CHAR = '@';
    public static final char ESCAPE_CHAR = '\\';
    public static final String METADATA = "__meta";
    public static final String VAR_START = "$[";
    public static final String VAR_END = "]";

    private static final Logger LOG = LoggerFactory.getLogger(Metamorph.class);

    private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";
    private static final String COULD_NOT_LOAD_MORPH_FILE = "Could not load morph file";

    private static final InterceptorFactory NULL_INTERCEPTOR_FACTORY = new NullInterceptorFactory();
    private static final Map<String, String> NO_VARS = Collections.emptyMap();

    private final Registry<NamedValueReceiver> dataRegistry = new WildcardRegistry<>();
    private final List<NamedValueReceiver> elseSources = new ArrayList<>();

    private final Map<String, Map<String, String>> maps = new HashMap<>();
    private final List<Closeable> resources = new ArrayList<>();

    private final StreamFlattener flattener = new StreamFlattener();

    private final Deque<Integer> entityCountStack = new LinkedList<>();
    private int entityCount;
    private int currentEntityCount;

    private StreamReceiver outputStreamReceiver;
    private MorphErrorHandler errorHandler = new DefaultErrorHandler();
    private int recordCount;
    private final List<FlushListener> recordEndListener = new ArrayList<>();

    private final Deque<EntityEntry> elseNestedEntities = new LinkedList<>();
    private boolean elseNested;
    private String currentLiteralName;

    protected Metamorph() {
        // package private
        init();
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link String}.
     *
     * @param morphDef the {@link String}
     */
    public Metamorph(final String morphDef) {
        this(morphDef, NO_VARS);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link String} and morph variables as a Map.
     *
     * @param morphDef the {@link String}
     * @param vars     the morph variables as a Map
     */
    public Metamorph(final String morphDef, final Map<String, String> vars) {
        this(morphDef, vars, NULL_INTERCEPTOR_FACTORY);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link String} and an {@link InterceptorFactory}.
     *
     * @param morphDef           the {@link String}
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final String morphDef, final InterceptorFactory interceptorFactory) {
        this(morphDef, NO_VARS, interceptorFactory);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link String} and morph variables as a Map and an
     * {@link InterceptorFactory}.
     *
     * @param morphDef           the {@link String}
     * @param vars               the morph variables as a Map
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final String morphDef, final Map<String, String> vars,
            final InterceptorFactory interceptorFactory) {
        this(getInputSource(morphDef), vars, interceptorFactory);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link Reader}.
     *
     * @param morphDef the {@link Reader}
     */
    public Metamorph(final Reader morphDef) {
        this(morphDef, NO_VARS);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link Reader} and the morph variables as a Map.
     *
     * @param morphDef the {@link Reader}
     * @param vars     the morph variables as a Map
     */
    public Metamorph(final Reader morphDef, final Map<String, String> vars) {
        this(morphDef, vars, NULL_INTERCEPTOR_FACTORY);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link Reader} and an {@link InterceptorFactory}.
     *
     * @param morphDef           the {@link Reader}
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final Reader morphDef, final InterceptorFactory interceptorFactory) {
        this(morphDef, NO_VARS, interceptorFactory);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link Reader} and morph variables as a Map and an
     * {@link InterceptorFactory}.
     *
     * @param morphDef           the {@link Reader}
     * @param vars               the morph variables as a Map
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final Reader morphDef, final Map<String, String> vars,
            final InterceptorFactory interceptorFactory) {
        this(new InputSource(morphDef), vars, interceptorFactory);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputStream}.
     *
     * @param morphDef the {@link InputStream}
     */
    public Metamorph(final InputStream morphDef) {
        this(morphDef, NO_VARS);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputStream} and morph variables as a Map.
     *
     * @param morphDef the {@link InputStream}
     * @param vars     the morph variables as a Map
     */
    public Metamorph(final InputStream morphDef, final Map<String, String> vars) {
        this(morphDef, vars, NULL_INTERCEPTOR_FACTORY);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputStream} and an {@link InterceptorFactory}.
     *
     * @param morphDef           the {@link InputStream}
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final InputStream morphDef, final InterceptorFactory interceptorFactory) {
        this(morphDef, NO_VARS, interceptorFactory);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputStream}, morph variables as a Map and an
     * {@link InterceptorFactory}.
     *
     * @param morphDef           the {@link InputStream}
     * @param vars               the morph variables as a Map
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final InputStream morphDef, final Map<String, String> vars,
            final InterceptorFactory interceptorFactory) {
        this(new InputSource(morphDef), vars, interceptorFactory);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputSource}.
     *
     * @param inputSource the {@link InputSource}
     */
    public Metamorph(final InputSource inputSource) {
        this(inputSource, NO_VARS);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputSource} and morph variables as a Map.
     *
     * @param inputSource the {@link InputSource}
     * @param vars        the morph variables as a Map
     */
    public Metamorph(final InputSource inputSource, final Map<String, String> vars) {
        this(inputSource, vars, NULL_INTERCEPTOR_FACTORY);
    }

    /**
     * Creates an instance of {@link Metamorph} given by a morph definition as
     * {@link InputSource} and an {@link InterceptorFactory}.
     *
     * @param inputSource        the {@link InputSource}
     * @param interceptorFactory the {@link InterceptorFactory}
     */
    public Metamorph(final InputSource inputSource, final InterceptorFactory interceptorFactory) {
        this(inputSource, NO_VARS, interceptorFactory);
    }

    /**
     * Constructs a Metamorph by setting a morph definition as {@link InputSource},
     * a Map of variables and an {@link InterceptorFactory}.
     *
     * @param inputSource        the InputSource
     * @param vars               the Map of variables
     * @param interceptorFactory the InterceptorFactory
     */
    public Metamorph(final InputSource inputSource, final Map<String, String> vars,
            final InterceptorFactory interceptorFactory) {
        buildPipeline(inputSource, vars, interceptorFactory);
        init();
    }

    private void buildPipeline(final InputSource inputSource, final Map<String, String> vars, final InterceptorFactory interceptorFactory) {
        try {
            final MorphBuilder builder = new MorphBuilder(this, interceptorFactory);
            builder.walk(inputSource, vars);
        }
        catch (final RuntimeException e) { // checkstyle-disable-line IllegalCatch
            throw new MetamorphException("Error while building the Metamorph transformation pipeline: " + e.getMessage(), e);
        }
    }

    private static InputSource getInputSource(final String morphDef) {
        try {
            return new InputSource(ResourceUtil.getUrl(morphDef).toExternalForm());
        }
        catch (final MalformedURLException e) {
            throw new MorphBuildException(COULD_NOT_LOAD_MORPH_FILE, e);
        }
    }

    private void init() {
        flattener.setReceiver(new DefaultStreamReceiver() {
            @Override
            public void literal(final String name, final String value) {
                dispatch(name, value, getElseSources(), false);
            }
        });
    }

    protected List<NamedValueReceiver> getElseSources() {
        return elseSources;
    }

    protected void setEntityMarker(final String entityMarker) {
        flattener.setEntityMarker(entityMarker);
    }

    /**
     * Sett the {@link MorphErrorHandler}.
     *
     * @param errorHandler the {@link MorphErrorHandler}
     */
    public void setErrorHandler(final MorphErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    protected void registerNamedValueReceiver(final String source, final NamedValueReceiver data) {
        if (ELSE_NESTED_KEYWORD.equals(source)) {
            elseNested = true;
        }

        if (ELSE_KEYWORD.equals(source) || ELSE_FLATTENED_KEYWORD.equals(source) || elseNested) {
            if (elseSources.isEmpty()) {
                elseSources.add(data);
            }
            else {
                LOG.warn("Only one of '_else', '_elseFlattened' and '_elseNested' is allowed. Ignoring the superflous ones.");
            }
        }
        else {
            dataRegistry.register(source, data);
        }
    }

    @Override
    public void startRecord(final String identifier) {
        flattener.startRecord(identifier);
        elseNestedEntities.clear();
        entityCountStack.clear();

        entityCount = 0;
        currentEntityCount = 0;
        entityCountStack.push(Integer.valueOf(entityCount));

        ++recordCount;
        recordCount %= Integer.MAX_VALUE;

        final String identifierFinal = identifier;

        outputStreamReceiver.startRecord(identifierFinal);
        dispatch(StandardEventNames.ID, identifierFinal, null, false);
    }

    @Override
    public void endRecord() {
        for (final FlushListener listener : recordEndListener) {
            listener.flush(recordCount, currentEntityCount);
        }

        outputStreamReceiver.endRecord();
        flattener.endRecord();

        entityCountStack.pop();

        if (!elseNestedEntities.isEmpty() || !entityCountStack.isEmpty()) {
            throw new IllegalStateException(ENTITIES_NOT_BALANCED);
        }
    }

    @Override
    public void startEntity(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Entity name must not be null.");
        }

        ++entityCount;
        currentEntityCount = entityCount;
        entityCountStack.push(Integer.valueOf(entityCount));

        flattener.startEntity(name);
        elseNestedEntities.push(new EntityEntry(flattener));
    }

    @Override
    public void endEntity() {
        dispatch(flattener.getCurrentPath(), "", getElseSources(), true);
        flattener.endEntity();

        elseNestedEntities.pop();
        currentEntityCount = entityCountStack.pop().intValue();
    }

    @Override
    public void literal(final String name, final String value) {
        currentLiteralName = name;
        flattener.literal(name, value);
    }

    @Override
    public void resetStream() {
        // TODO: Implement proper reset handling
        outputStreamReceiver.resetStream();
    }

    @Override
    public void closeStream() {
        for (final Closeable closeable : resources) {
            try {
                closeable.close();
            }
            catch (final IOException e) {
                errorHandler.error(e);
            }
        }
        outputStreamReceiver.closeStream();
    }

    private void dispatch(final String path, final String value, final List<NamedValueReceiver> fallbackReceiver, final boolean endEntity) {
        final List<NamedValueReceiver> matchingData = getData(path);

        if (matchingData != null) {
            send(path, value, matchingData);
        }
        else if (fallbackReceiver != null) {
            dispatchFallback(path, endEntity, k -> send(escapeFeedbackChar(k), value, fallbackReceiver));
        }
    }

    private void dispatchFallback(final String path, final boolean endEntity, final Consumer<String> consumer) {
        final EntityEntry entityEntry = elseNested ? elseNestedEntities.peek() : null;

        if (endEntity) {
            if (entityEntry != null && entityEntry.getStarted()) {
                outputStreamReceiver.endEntity();
            }
        }
        else if (entityEntry != null) {
            if (getData(entityEntry.getPath()) == null) {
                final Deque<String> entities = new LinkedList<>();

                for (final EntityEntry e : elseNestedEntities) {
                    if (e.getStarted()) {
                        break;
                    }

                    e.setStarted(true);
                    entities.push(e.getName());
                }

                entities.forEach(outputStreamReceiver::startEntity);
                consumer.accept(currentLiteralName);
            }
        }
        else {
            consumer.accept(path);
        }
    }

    private List<NamedValueReceiver> getData(final String path) {
        final List<NamedValueReceiver> matchingData = dataRegistry.get(path);
        return matchingData != null && !matchingData.isEmpty() ? matchingData : null;
    }

    private void send(final String path, final String value, final List<NamedValueReceiver> dataList) {
        for (final NamedValueReceiver data : dataList) {
            try {
                data.receive(path, value, null, recordCount, currentEntityCount);
            }
            catch (final RuntimeException e) { // checkstyle-disable-line IllegalCatch
                errorHandler.error(e);
            }
        }
    }

    private boolean startsWithFeedbackChar(final String name) {
        return name.length() != 0 && name.charAt(0) == FEEDBACK_CHAR;
    }

    private String escapeFeedbackChar(final String name) {
        return name == null ? null : (startsWithFeedbackChar(name) ? ESCAPE_CHAR : "") + name;
    }

    /**
     * @param streamReceiver
     *            the outputHandler to set
     */
    @Override
    public <R extends StreamReceiver> R setReceiver(final R streamReceiver) {
        if (streamReceiver == null) {
            throw new IllegalArgumentException("'streamReceiver' must not be null");
        }
        this.outputStreamReceiver = streamReceiver;
        return streamReceiver;
    }

    /**
     * Gets the {@link StreamReceiver}.
     *
     * @return the output {@link StreamReceiver}
     */
    public StreamReceiver getStreamReceiver() {
        return outputStreamReceiver;
    }

    @Override
    public void receive(final String name, final String value, final NamedValueSource source, final int unusedRecordCount, final int unusedEntityCount) {
        if (null == name) {
            throw new IllegalArgumentException("encountered literal with name='null'. This indicates a bug in a function or a collector.");
        }

        if (startsWithFeedbackChar(name)) {
            dispatch(name, value, null, false);
            return;
        }

        String unescapedName = name;
        if (name.length() > 1 && name.charAt(0) == ESCAPE_CHAR && (name.charAt(1) == FEEDBACK_CHAR || name.charAt(1) == ESCAPE_CHAR)) {
            unescapedName = name.substring(1);
        }
        outputStreamReceiver.literal(unescapedName, value);
    }

    @Override
    public Map<String, String> getMap(final String mapName) {
        return maps.getOrDefault(mapName, Collections.emptyMap());
    }

    @Override
    public String getValue(final String mapName, final String key) {
        final Map<String, String> map = getMap(mapName);
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return map.get(Maps.DEFAULT_MAP_KEY);
    }

    @Override
    public Map<String, String> putMap(final String mapName, final Map<String, String> map) {
        if (map instanceof Closeable) {
            final Closeable closable = (Closeable) map;
            resources.add(closable);
        }
        return maps.put(mapName, map);
    }

    @Override
    public String putValue(final String mapName, final String key, final String value) {
        return maps.computeIfAbsent(mapName, k -> new HashMap<>()).put(key, value);
    }

    @Override
    public Collection<String> getMapNames() {
        return Collections.unmodifiableSet(maps.keySet());
    }

    /**
     * Adds a {@link FlushListener} to the record end.
     *
     * @param flushListener the {@link FlushListener}
     */
    public void registerRecordEndFlush(final FlushListener flushListener) {
        recordEndListener.add(flushListener);
    }

    @Override
    public void addNamedValueSource(final NamedValueSource namedValueSource) {
        namedValueSource.setNamedValueReceiver(this);
    }

    @Override
    public void setNamedValueReceiver(final NamedValueReceiver receiver) {
        throw new UnsupportedOperationException("The Metamorph object cannot act as a NamedValueSender");
    }

    @Override
    public void setSourceLocation(final SourceLocation sourceLocation) {
        // Nothing to do
        // Metamorph does not have a source location (we could
        // in theory use the location of the module in a flux
        // script)
    }

    @Override
    public SourceLocation getSourceLocation() {
        // Metamorph does not have a source location
        return null;
    }

    private static class EntityEntry {

        private final String name;
        private final String path;

        private boolean started;

        EntityEntry(final StreamFlattener flattener) {
            name = flattener.getCurrentEntityName();
            path = flattener.getCurrentPath();
        }

        private String getName() {
            return name;
        }

        private String getPath() {
            return path;
        }

        private void setStarted(final boolean started) {
            this.started = started;
        }

        private boolean getStarted() {
            return started;
        }

    }

}
