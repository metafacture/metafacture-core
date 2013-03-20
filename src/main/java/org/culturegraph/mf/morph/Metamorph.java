/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.morph;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.pipe.StreamFlattener;
import org.culturegraph.mf.types.MultiMap;
import org.culturegraph.mf.util.StreamConstants;



/**
 * Transforms a data stream send via the {@link StreamReceiver} interface. Use
 * {@link MorphBuilder} to create an instance based on an xml description
 * 
 * @author Markus Michael Geipel
 */
@Description("applies a metamorph transformation to the event stream. Metamorph definition is given in brackets.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class Metamorph implements StreamPipe<StreamReceiver>, NamedValueReceiver, MultiMap {

	public static final String ELSE_KEYWORD = "_else";
	public static final char FEEDBACK_CHAR = '@';
	public static final String METADATA = "__meta";
	public static final String VAR_START = "$[";
	public static final String VAR_END = "]";

	private static final String ENTITIES_NOT_BALANCED = "Entity starts and ends are not balanced";

	private final Registry<NamedValueReceiver> dataRegistry = MorphCollectionFactory.createRegistry();
	private final List<NamedValueReceiver> elseSources = MorphCollectionFactory.createList();
	
//rivate final Registry<FlushListener> entityEndListenerRegistry = new WildcardRegistry<FlushListener>();

	private final MultiMap multiMap = MorphCollectionFactory.createMultiMap();
	private final List<Closeable> resources = MorphCollectionFactory.createList();

	private final StreamFlattener flattener = new StreamFlattener();

	private final Deque<Integer> entityCountStack = MorphCollectionFactory.createDeque();
	private int entityCount;
	private int currentEntityCount;

	private StreamReceiver outputStreamReceiver;
	private MorphErrorHandler errorHandler = new DefaultErrorHandler();
	private int recordCount;
	private final List<FlushListener> recordEndListener = MorphCollectionFactory.createList();

	protected Metamorph() {
		// package private
		init();
	}

	public Metamorph(final Reader morphDefReader) {
		final MorphBuilder builder = new MorphBuilder(this);
		builder.walk(morphDefReader);
		init();
	}
	
	public Metamorph(final Reader morphDefReader, final Map<String, String> vars) {
		final MorphBuilder builder = new MorphBuilder(this);
		builder.walk(morphDefReader, vars);
		init();
	}
	
	public Metamorph(final InputStream inputStream, final Map<String, String> vars) {
		final MorphBuilder builder = new MorphBuilder(this);
		builder.walk(inputStream, vars);
		init();
	}
	
	public Metamorph(final InputStream inputStream) {
		final MorphBuilder builder = new MorphBuilder(this);
		builder.walk(inputStream);
		init();
	}

	public Metamorph(final String morphDef) {
		final MorphBuilder builder = new MorphBuilder(this);
		builder.walk(morphDef);
		init();
	}
	
	public Metamorph(final String morphDef, final Map<String, String> vars) {
		final MorphBuilder builder = new MorphBuilder(this);
		builder.walk(morphDef, vars);
		init();
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

		//final String path = data.getSource();

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
		dispatch(StreamConstants.ID, identifierFinal, null);
	}

	@Override
	public void endRecord() {

		for(FlushListener listener: recordEndListener){
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
		for (Closeable closeable : resources) {
			try {
				closeable.close();
			} catch (IOException e) {
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

	/**
	 * @param key
	 * @param value
	 * @param dataList
	 *            destination
	 */
	private void send(final String key, final String value, final List<NamedValueReceiver> dataList) {
		for (NamedValueReceiver data : dataList) {
			try {
				data.receive(key, value, null, recordCount, currentEntityCount);
			} catch (RuntimeException e) {
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

	/**
	 * @return the outputStreamReceiver
	 */
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
		} else {
			outputStreamReceiver.literal(name, value);
		}

	}

	@Override
	public Map<String, String> getMap(final String mapName) {
		return multiMap.getMap(mapName);
	}

	@Override
	public String getValue(final String mapName, final String key) {
		return multiMap.getValue(mapName, key);
	}

	@Override
	public Map<String, String> putMap(final String mapName, final Map<String, String> map) {
		if (map instanceof Closeable) {
			final Closeable closable = (Closeable) map;
			resources.add(closable);
		}
		return multiMap.putMap(mapName, map);
	}

	@Override
	public String putValue(final String mapName, final String key, final String value) {
		return multiMap.putValue(mapName, key, value);
	}

	@Override
	public Collection<String> getMapNames() {
		return multiMap.getMapNames();
	}

	public void registerRecordEndFlush(final FlushListener flushListener) {
		recordEndListener.add(flushListener);
	}

}
