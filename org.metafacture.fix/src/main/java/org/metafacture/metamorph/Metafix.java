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

package org.metafacture.metamorph;

import org.metafacture.commons.tries.SimpleRegexTrie;
import org.metafacture.fix.FixStandaloneSetup;
import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.interpreter.FixInterpreter;
import org.metafacture.framework.StandardEventNames;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.metamorph.api.FlushListener;
import org.metafacture.metamorph.api.InterceptorFactory;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.MorphErrorHandler;
import org.metafacture.metamorph.api.NamedValuePipe;
import org.metafacture.metamorph.api.NamedValueReceiver;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.SourceLocation;

import com.google.common.io.CharStreams;
import com.google.inject.Injector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Transforms a data stream sent via the {@link StreamReceiver} interface. Use
 * {@link FixBuilder} to create an instance based on a Fix DSL description.
 *
 * @author Markus Michael Geipel (Metamorph)
 * @author Christoph Böhme (Metamorph)
 * @author Fabian Steeg (Metafix)
 */

public class Metafix implements StreamPipe<StreamReceiver>, NamedValuePipe, Maps { // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity

    public static final String ELSE_KEYWORD = "_else";
    public static final char FEEDBACK_CHAR = '@';
    public static final char ESCAPE_CHAR = '\\';
    public static final String METADATA = "__meta";
    public static final String VAR_START = "$[";
    public static final String VAR_END = "]";

    private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";
    private static final Object ELSE_NESTED_KEYWORD = "_elseNested";
    private static final Object ELSE_FLATTENED_KEYWORD = "_elseFlattened";

    private static final InterceptorFactory NULL_INTERCEPTOR_FACTORY = new NullInterceptorFactory();
    private static final Map<String, String> NO_VARS = Collections.emptyMap();

    // See https://www.w3.org/TR/json-ld11/#keywords
    private static final List<String> JSONLD_KEYWORDS = Arrays.asList(
            "@base", "@container", "@context", "@direction", "@graph", "@id", "@import", "@included", "@index", "@json",
            "@language", "@list", "@nest", "@none", "@prefix", "@propagate", "@protected", "@reverse", "@set", "@type",
            "@value", "@version", "@vocab");

    // warning: auxiliary class WildcardRegistry in WildcardDataRegistry.java should not be accessed from outside its own source file
    //private final Registry<NamedValueReceiver> dataRegistry = new WildcardRegistry<>();
    private final Registry<NamedValueReceiver> dataRegistry = new Registry<NamedValueReceiver>() {
        private final SimpleRegexTrie<NamedValueReceiver> trie = new SimpleRegexTrie<>();

        @Override
        public void register(final String path, final NamedValueReceiver value) {
            trie.put(path, value);
        }

        @Override
        public List<NamedValueReceiver> get(final String path) {
            return trie.get(path);
        }
    };

    private final List<Closeable> resources = new ArrayList<>();
    private final List<NamedValueReceiver> elseSources = new ArrayList<>();
    private final Map<String, Map<String, String>> maps = new HashMap<>();
    private final StreamFlattener flattener = new StreamFlattener();

    private final Deque<Integer> entityCountStack = new LinkedList<>();
    private int currentEntityCount;
    private int entityCount;

    private StreamReceiver outputStreamReceiver;
    private MorphErrorHandler errorHandler = new DefaultErrorHandler();
    private int recordCount;
    private final List<FlushListener> recordEndListener = new ArrayList<>();

    private final List<Expression> expressions = new ArrayList<>();
    private Map<String, String> vars = NO_VARS;

    private boolean elseNested;
    private boolean elseNestedEntityStarted;
    private String currentLiteralName;

    public Metafix() {
        init();
    }

    public Metafix(final String fixDef) throws FileNotFoundException {
        this(fixDef, NO_VARS);
    }

    public Metafix(final String fixDef, final Map<String, String> vars) throws FileNotFoundException {
        this(fixDef, vars, NULL_INTERCEPTOR_FACTORY);
    }

    public Metafix(final String fixDef, final InterceptorFactory interceptorFactory) throws FileNotFoundException {
        this(fixDef, NO_VARS, interceptorFactory);
    }

    public Metafix(final String fixDef, final Map<String, String> vars, final InterceptorFactory interceptorFactory) throws FileNotFoundException {
        this(fixDef.endsWith(".fix") ? new FileReader(fixDef) : new StringReader(fixDef), vars, interceptorFactory);
    }

    public Metafix(final Reader morphDef) {
        this(morphDef, NO_VARS);
    }

    public Metafix(final Reader fixDef, final Map<String, String> vars) {
        this(fixDef, vars, NULL_INTERCEPTOR_FACTORY);
    }

    public Metafix(final Reader fixDef, final InterceptorFactory interceptorFactory) {
        this(fixDef, NO_VARS, interceptorFactory);
    }

    public Metafix(final Reader fixDef, final Map<String, String> vars, final InterceptorFactory interceptorFactory) {
        buildPipeline(fixDef, vars, interceptorFactory);
        init();
    }

    public Metafix(final InputStream fixDef) {
        this(fixDef, NO_VARS);
    }

    public Metafix(final InputStream fixDef, final Map<String, String> vars) {
        this(fixDef, vars, NULL_INTERCEPTOR_FACTORY);
    }

    public Metafix(final InputStream fixDef, final InterceptorFactory interceptorFactory) {
        this(fixDef, NO_VARS, interceptorFactory);
    }

    public Metafix(final InputStream fixDef, final Map<String, String> vars, final InterceptorFactory interceptorFactory) {
        this(new InputStreamReader(fixDef), vars, interceptorFactory);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    private void init() {
        flattener.setReceiver(new DefaultStreamReceiver() {
            @Override
            public void literal(final String name, final String value) {
                dispatch(name, value, getElseSources(), false);
            }
        });
    }

    private void buildPipeline(final Reader fixDef, final Map<String, String> theVars, final InterceptorFactory interceptorFactory) {
        final Fix fix = parseFix(fixDef);
        this.vars = theVars;
        // TODO: unify FixInterpreter and FixBuilder
        new FixInterpreter().run(this, fix);
        new FixBuilder(this, interceptorFactory).walk(fix);
    }

    private Fix parseFix(final Reader fixDef) {
        // TODO: do this only once per application
        final Injector injector = new FixStandaloneSetup().createInjectorAndDoEMFRegistration();
        FixStandaloneSetup.doSetup();

        try {
            final URI uri = URI.createFileURI(absPathToTempFile(fixDef, ".fix"));
            final Resource resource = injector.getInstance(XtextResourceSet.class).getResource(uri, true);
            final IResourceValidator validator = ((XtextResource) resource).getResourceServiceProvider().getResourceValidator();

            for (final Issue issue : validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl)) {
                System.err.println(issue.getMessage());
            }

            return (Fix) resource.getContents().get(0);
        }
        catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String absPathToTempFile(final Reader fixDef, final String suffix) throws IOException {
        // TODO: avoid temp file creation
        final File file = File.createTempFile("metafix", suffix);
        file.deleteOnExit();

        try (FileWriter out = new FileWriter(file)) {
            CharStreams.copy(fixDef, out);
        }

        return file.getAbsolutePath();
    }

    protected List<NamedValueReceiver> getElseSources() {
        return elseSources;
    }

    protected void setEntityMarker(final String entityMarker) {
        flattener.setEntityMarker(entityMarker);
    }

    protected void setErrorHandler(final MorphErrorHandler errorHandler) {
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
                System.out.println("Only one of '_else', '_elseFlattened' and '_elseNested' is allowed. Ignoring the superflous ones.");
            }
        }
        else {
            dataRegistry.register(source, data);
        }
    }

    @Override
    public void startRecord(final String identifier) {
        flattener.startRecord(identifier);
        entityCountStack.clear();

        entityCount = 0;
        currentEntityCount = 0;
        entityCountStack.add(Integer.valueOf(entityCount));

        ++recordCount;
        recordCount %= Integer.MAX_VALUE;

        outputStreamReceiver.startRecord(identifier);
        dispatch(StandardEventNames.ID, identifier, null, false);
    }

    @Override
    public void endRecord() {
        for (final FlushListener listener : recordEndListener) {
            listener.flush(recordCount, currentEntityCount);
        }

        outputStreamReceiver.endRecord();
        entityCountStack.removeLast();

        if (!entityCountStack.isEmpty()) {
            throw new IllegalStateException(ENTITIES_NOT_BALANCED);
        }

        flattener.endRecord();
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
    }

    @Override
    public void endEntity() {
        dispatch(flattener.getCurrentPath(), "", getElseSources(), true);
        currentEntityCount = entityCountStack.pop().intValue();
        flattener.endEntity();
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

    private void dispatch(final String path, final String value, final List<NamedValueReceiver> fallback, final boolean endEntity) {
        final List<NamedValueReceiver> matchingData = findMatchingData(path, fallback);

        if (matchingData != null) {
            send(path, value, matchingData);
        }
        else if (fallback != null) {
            if (endEntity) {
                if (elseNestedEntityStarted) {
                    outputStreamReceiver.endEntity();
                    elseNestedEntityStarted = false;
                }
            }
            else {
                final String entityName = elseNested ? flattener.getCurrentEntityName() : null;

                if (entityName != null) {
                    if (findMatchingData(entityName, fallback) == null) {
                        if (!elseNestedEntityStarted) {
                            outputStreamReceiver.startEntity(entityName);
                            elseNestedEntityStarted = true;
                        }

                        send(currentLiteralName, value, fallback);
                    }
                }
                else {
                    send(path, value, fallback);
                }
            }
        }
    }

    private List<NamedValueReceiver> findMatchingData(final String path, final List<NamedValueReceiver> fallback) {
        final List<NamedValueReceiver> matchingData = dataRegistry.get(path);
        return matchingData != null && !matchingData.isEmpty() ? matchingData : null;
    }

    private void send(final String key, final String value, final List<NamedValueReceiver> dataList) {
        for (final NamedValueReceiver data : dataList) {
            data.receive(key, value, null, recordCount, currentEntityCount);
        }
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

    @Override
    public void receive(final String name, final String value, final NamedValueSource source, final int recordCountParam, final int entityCountParam) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "encountered literal with name='null'. This indicates a bug in a function or a collector.");
        }
        final int end = Math.min(name.indexOf(flattener.getEntityMarker()), name.indexOf(FixBuilder.ARRAY_MARKER));
        final String firstNameSegment = end == -1 ? name : name.substring(0, end);
        if (name.length() != 0 && name.charAt(0) == FEEDBACK_CHAR && !JSONLD_KEYWORDS.contains(firstNameSegment)) {
            dispatch(name, value, null, false);
            return;
        }

        final String unescapedName;

        if (name.length() > 1 && name.charAt(0) == ESCAPE_CHAR && (name.charAt(1) == FEEDBACK_CHAR || name.charAt(1) == ESCAPE_CHAR)) {
            unescapedName = name.substring(1);
        }
        else {
            unescapedName = name;
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
        return map.containsKey(key) ? map.get(key) : map.get(Maps.DEFAULT_MAP_KEY);
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

    public void registerRecordEndFlush(final FlushListener flushListener) {
        recordEndListener.add(flushListener);
    }

    @Override
    public void addNamedValueSource(final NamedValueSource namedValueSource) {
        namedValueSource.setNamedValueReceiver(this);
    }

    @Override
    public void setNamedValueReceiver(final NamedValueReceiver receiver) {
        throw new UnsupportedOperationException("The Metafix object cannot act as a NamedValueSender");
    }

    @Override
    public void setSourceLocation(final SourceLocation sourceLocation) {
        // Nothing to do
        // Metafix does not have a source location (we could
        // in theory use the location of the module in a flux
        // script)
    }

    @Override
    public SourceLocation getSourceLocation() {
        // Metafix does not have a source location
        return null;
    }

    public Map<String, String> getVars() {
        return vars;
    }

}
