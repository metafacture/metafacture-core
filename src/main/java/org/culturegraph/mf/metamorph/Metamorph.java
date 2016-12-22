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
package org.culturegraph.mf.metamorph;

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

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StandardEventNames;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;
import org.culturegraph.mf.mangling.StreamFlattener;
import org.culturegraph.mf.metamorph.api.FlushListener;
import org.culturegraph.mf.metamorph.api.InterceptorFactory;
import org.culturegraph.mf.metamorph.api.Maps;
import org.culturegraph.mf.metamorph.api.MorphBuildException;
import org.culturegraph.mf.metamorph.api.MorphErrorHandler;
import org.culturegraph.mf.metamorph.api.NamedValuePipe;
import org.culturegraph.mf.metamorph.api.NamedValueReceiver;
import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.SourceLocation;
import org.xml.sax.InputSource;

/**
 * Transforms a data stream send via the {@link StreamReceiver} interface. Use
 * {@link MorphBuilder} to create an instance based on an xml description
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 */
@Description("applies a metamorph transformation to the event stream. Metamorph "
		+ "definition is given in brackets.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("morph")
public final class Metamorph implements StreamPipe<StreamReceiver>, NamedValuePipe, Maps {

	public static final String ELSE_KEYWORD = "_else";
	public static final char FEEDBACK_CHAR = '@';
	public static final char ESCAPE_CHAR = '\\';
	public static final String METADATA = "__meta";
	public static final String VAR_START = "$[";
	public static final String VAR_END = "]";

	private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";
	private static final String COULD_NOT_LOAD_MORPH_FILE = "Could not load morph file";

	private static final InterceptorFactory NULL_INTERCEPTOR_FACTORY = new NullInterceptorFactory();
	private static final Map<String, String> NO_VARS = Collections.emptyMap();

	private final Registry<NamedValueReceiver> dataRegistry =
			new WildcardRegistry<>();
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

	protected Metamorph() {
		// package private
		init();
	}

	public Metamorph(final String morphDef) {
		this(morphDef, NO_VARS);
	}

	public Metamorph(final String morphDef, final Map<String, String> vars) {
		this(morphDef, vars, NULL_INTERCEPTOR_FACTORY);
	}

	public Metamorph(final String morphDef, final InterceptorFactory interceptorFactory) {
		this(morphDef, NO_VARS, interceptorFactory);
	}

	public Metamorph(final String morphDef, final Map<String, String> vars,
			final InterceptorFactory interceptorFactory) {

		this(getInputSource(morphDef), vars, interceptorFactory);
	}

	public Metamorph(final Reader morphDef) {
		this(morphDef, NO_VARS);
	}

	public Metamorph(final Reader morphDef, final Map<String, String> vars) {
		this(morphDef, vars, NULL_INTERCEPTOR_FACTORY);
	}

	public Metamorph(final Reader morphDef, final InterceptorFactory interceptorFactory) {
		this(morphDef, NO_VARS, interceptorFactory);
	}

	public Metamorph(final Reader morphDef, final Map<String, String> vars,
			final InterceptorFactory interceptorFactory) {

		this(new InputSource(morphDef), vars, interceptorFactory);
	}

	public Metamorph(final InputStream morphDef) {
		this(morphDef, NO_VARS);
	}

	public Metamorph(final InputStream morphDef, final Map<String, String> vars) {
		this(morphDef, vars, NULL_INTERCEPTOR_FACTORY);
	}

	public Metamorph(final InputStream morphDef, final InterceptorFactory interceptorFactory) {
		this(morphDef, NO_VARS, interceptorFactory);
	}

	public Metamorph(final InputStream morphDef, final Map<String, String> vars,
			final InterceptorFactory interceptorFactory) {

		this(new InputSource(morphDef), vars, interceptorFactory);
	}

	public Metamorph(final InputSource inputSource) {
		this(inputSource, NO_VARS);
	}

	public Metamorph(final InputSource inputSource, final Map<String, String> vars) {
		this(inputSource, vars, NULL_INTERCEPTOR_FACTORY);
	}

	public Metamorph(final InputSource inputSource, final InterceptorFactory interceptorFactory) {
		this(inputSource, NO_VARS, interceptorFactory);
	}

	public Metamorph(final InputSource inputSource, final Map<String, String> vars,
			final InterceptorFactory interceptorFactory) {
		buildPipeline(inputSource, vars, interceptorFactory);
		init();
	}

	private void buildPipeline(InputSource inputSource, Map<String, String> vars,
			InterceptorFactory interceptorFactory) {
		try {
			final MorphBuilder builder = new MorphBuilder(this, interceptorFactory);
			builder.walk(inputSource, vars);
		} catch (RuntimeException e) {
			throw new MetamorphException(
					"Error while building the Metamorph transformation pipeline: " +
							e.getMessage(), e);
		}
	}

	private static InputSource getInputSource(final String morphDef) {
		try {
			return new InputSource(
					ResourceUtil.getUrl(morphDef).toExternalForm());
		} catch (final MalformedURLException e) {
			throw new MorphBuildException(COULD_NOT_LOAD_MORPH_FILE, e);
		}
	}

	private void init() {
		flattener.setReceiver(new DefaultStreamReceiver() {
			@Override
			public void literal(final String name, final String value) {
				dispatch(name, value, getElseSources());
			}
		});
	}

	protected List<NamedValueReceiver> getElseSources() {
		return elseSources;
	}

	protected void setEntityMarker(final String entityMarker) {
		flattener.setEntityMarker(entityMarker);
	}

	public void setErrorHandler(final MorphErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	protected void registerNamedValueReceiver(final String source, final NamedValueReceiver data) {
		if (ELSE_KEYWORD.equals(source)) {
			elseSources.add(data);
		} else {
			dataRegistry.register(source, data);
		}
	}

	@Override
	public void startRecord(final String identifier) {
		flattener.startRecord(identifier);
		entityCountStack.clear();

		entityCount = 0;
		currentEntityCount = 0;

		++recordCount;
		recordCount %= Integer.MAX_VALUE;

		entityCountStack.add(Integer.valueOf(entityCount));

		final String identifierFinal = identifier;

		outputStreamReceiver.startRecord(identifierFinal);
		dispatch(StandardEventNames.ID, identifierFinal, null);
	}

	@Override
	public void endRecord() {

		for(final FlushListener listener: recordEndListener){
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
		dispatch(flattener.getCurrentPath(), "", null);
		currentEntityCount = entityCountStack.pop().intValue();
		flattener.endEntity();

	}


	@Override
	public void literal(final String name, final String value) {
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
			} catch (final IOException e) {
				errorHandler.error(e);
			}
		}
		outputStreamReceiver.closeStream();
	}

	protected void dispatch(final String path, final String value, final List<NamedValueReceiver> fallback) {
		final List<NamedValueReceiver> matchingData = findMatchingData(path, fallback);
		if (null != matchingData) {
			send(path, value, matchingData);
		}
	}

	private List<NamedValueReceiver> findMatchingData(final String path, final List<NamedValueReceiver> fallback) {
		final List<NamedValueReceiver> matchingData = dataRegistry.get(path);
		if (matchingData == null || matchingData.isEmpty()) {
			return fallback;
		}
		return matchingData;
	}

	private void send(final String key, final String value, final List<NamedValueReceiver> dataList) {
		for (final NamedValueReceiver data : dataList) {
			try {
				data.receive(key, value, null, recordCount, currentEntityCount);
			} catch (final RuntimeException e) {
				errorHandler.error(e);
			}
		}
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

	public StreamReceiver getStreamReceiver() {
		return outputStreamReceiver;
	}

	@Override
	public void receive(final String name, final String value, final NamedValueSource source, final int recordCount,
			final int entityCount) {
		if (null == name) {
			throw new IllegalArgumentException(
					"encountered literal with name='null'. This indicates a bug in a function or a collector.");
		}

		if (name.length() != 0 && name.charAt(0) == FEEDBACK_CHAR) {
			dispatch(name, value, null);
			return;
		}

		String unescapedName = name;
		if(name.length() > 1 && name.charAt(0) == ESCAPE_CHAR
				&& (name.charAt(1) == FEEDBACK_CHAR || name.charAt(1) == ESCAPE_CHAR)) {
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

}
