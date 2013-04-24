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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.morph.collectors.Collect;
import org.culturegraph.mf.morph.collectors.Entity;
import org.culturegraph.mf.morph.functions.Function;
import org.culturegraph.mf.types.MultiMap;
import org.culturegraph.mf.util.reflection.ObjectFactory;
import org.w3c.dom.Node;

/**
 * Builds a {@link Metamorph} from an xml description
 * 
 * @author Markus Michael Geipel
 */
public final class MorphBuilder extends AbstractMetamorphDomWalker {

	private static final String NOT_FOUND = " not found.";
	private static final String JAVA = "java";
	private static final String JAVAMAP = "javamap";
	private static final String RECORD = "record";
	// private final String morphDef;
	private final Metamorph metamorph;
	private final Deque<Collect> collectStack;
	private Data data;
	private Entity entity;

	protected MorphBuilder(final Metamorph metamorph) {
		super();
		this.collectStack = new LinkedList<Collect>();
		this.metamorph = metamorph;
	}

	@Override
	protected void setEntityMarker(final String entityMarker) {
		if (null != entityMarker) {
			metamorph.setEntityMarker(entityMarker);
		}
	}

	@Override
	protected void handleInternalMap(final Node mapNode) {
		final String mapName = resolvedAttribute(mapNode, ATTRITBUTE.NAME);

		final String mapDefault = resolvedAttribute(mapNode, ATTRITBUTE.DEFAULT);

		for (Node entryNode = mapNode.getFirstChild(); entryNode != null; entryNode = entryNode.getNextSibling()) {
			final String entryName = resolvedAttribute(entryNode, ATTRITBUTE.NAME);
			final String entryValue = resolvedAttribute(entryNode, ATTRITBUTE.VALUE);
			metamorph.putValue(mapName, entryName, entryValue);
		}

		if (mapDefault != null) {
			metamorph.putValue(mapName, MultiMap.DEFAULT_MAP_KEY, mapDefault);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleMapClass(final Node mapNode) {
		final Map<String, String> attributes = resolvedAttributeMap(mapNode);
		final String mapName = resolveVars(attributes.remove(ATTRITBUTE.NAME.getString()));
		final Map<String, String> map;
		
		if (mapNode.getLocalName().equals(JAVAMAP)) {
			final String className = resolvedAttribute(mapNode, ATTRITBUTE.CLASS);
			map = ObjectFactory.newInstance(ObjectFactory.loadClass(className, Map.class));
			attributes.remove(ATTRITBUTE.CLASS.getString());
			ObjectFactory.applySetters(map, attributes);
		}else if (getMapFactory().containsKey(mapNode.getLocalName())){
			map = getMapFactory().newInstance(mapNode.getLocalName(), attributes);
		}else{
			throw new IllegalArgumentException("Map " + mapNode.getLocalName() + NOT_FOUND);
		}
		
		metamorph.putMap(mapName, map);
	}

	@Override
	@SuppressWarnings("unchecked")
	// protected by 'if (Function.class.isAssignableFrom(clazz))'
	protected void handleFunctionDefinition(final Node functionDefNode) {
		final Class<?> clazz;
		final String className = resolvedAttribute(functionDefNode, ATTRITBUTE.CLASS);
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new MorphDefException("Function " + className + NOT_FOUND, e);
		}
		if (Function.class.isAssignableFrom(clazz)) {
			getFunctionFactory().registerClass(resolvedAttribute(functionDefNode, ATTRITBUTE.NAME),
					(Class<Function>) clazz);
		} else {
			throw new MorphDefException(className + " does not implement interface 'Function'");
		}
	}

	@Override
	protected void handleMetaEntry(final String name, final String value) {
		metamorph.putValue(Metamorph.METADATA, name, value);
	}

	@Override
	protected void init() {
		// nothing to do
	}

	@Override
	protected void finish() {
		// nothing to do
	}

	@Override
	protected void enterData(final Node dataNode) {
		final String source = resolvedAttribute(dataNode, ATTRITBUTE.SOURCE);
		data = new Data();
		data.setName(resolvedAttribute(dataNode, ATTRITBUTE.NAME));
		metamorph.registerNamedValueReceiver(source, data);
		
		if (entity != null) {
			entity.setNameSource(data);
			entity = null;
		}
	}

	@Override
	protected void exitData(final Node node) {
		if (collectStack.isEmpty()) {
			data.endPipe(metamorph);
		} else {
			final Collect parent = collectStack.peek();
			data.endPipe(parent);
			parent.addNamedValueSource(data);			
		}
		data = null;		
	}
	
	@Override
	protected void enterName(final Node nameNode) {
		entity = (Entity) collectStack.peek();
	}
	
	@Override
	protected void exitName(final Node nameNode) {
		entity = null;
	}

	@Override
	protected void enterCollect(final Node node) {
		final Map<String, String> attributes = resolvedAttributeMap(node);
		// must be set after recursive calls to flush decendents before parent
		attributes.remove(ATTRITBUTE.FLUSH_WITH.getString());

		if (!getCollectFactory().containsKey(node.getLocalName())) {
			throw new IllegalArgumentException("Collector " + node.getLocalName() + NOT_FOUND);
		}
		final Collect collect = getCollectFactory().newInstance(node.getLocalName(), attributes, metamorph);

		collectStack.push(collect);

		if (entity != null) {
			entity.setNameSource(collect);
			entity = null;
		}
}

	@Override
	protected void exitCollect(final Node node) {
		final Collect collect = collectStack.pop();
		if (collectStack.isEmpty()) {
			collect.endPipe(metamorph);
		} else {
			final Collect parent = collectStack.peek();
			parent.addNamedValueSource(collect);
			collect.endPipe(parent);
		}
		// must be set after recursive calls to flush descendants before parent
		final String flushWith = resolvedAttribute(node, ATTRITBUTE.FLUSH_WITH);
		if (null != flushWith) {
			collect.setWaitForFlush(true);
			registerFlush(flushWith, collect);
		}
	}

	private void registerFlush(final String flushWith, final FlushListener flushListener) {

		if (flushWith.equals(RECORD)) {
			metamorph.registerRecordEndFlush(flushListener);
		} else {
			metamorph.registerNamedValueReceiver(flushWith, new Flush(flushListener));
		}

	}

	@Override
	protected void handleFunction(final Node functionNode) {
		final Function function;
		final Map<String, String> attributes = resolvedAttributeMap(functionNode);
		if (functionNode.getLocalName().equals(JAVA)) {
			final String className = resolvedAttribute(functionNode, ATTRITBUTE.CLASS);
			function = ObjectFactory.newInstance(ObjectFactory.loadClass(className, Function.class));

			attributes.remove(ATTRITBUTE.CLASS.getString());
			ObjectFactory.applySetters(function, attributes);
		} else if (getFunctionFactory().containsKey(functionNode.getLocalName())) {
			final String flushWith = attributes.remove(ATTRITBUTE.FLUSH_WITH.getString());
			function = getFunctionFactory().newInstance(functionNode.getLocalName(), attributes);
			if (null != flushWith) {
				registerFlush(flushWith, function);
			}
		} else {
			throw new IllegalArgumentException(functionNode.getLocalName() + NOT_FOUND);
		}

		function.setMultiMap(metamorph);
		// nction.setEntityEndIndicator(metamorph);

		// add key value entries...
		for (Node mapEntryNode = functionNode.getFirstChild(); mapEntryNode != null; mapEntryNode = mapEntryNode
				.getNextSibling()) {
			final String entryName = resolvedAttribute(mapEntryNode, ATTRITBUTE.NAME);
			final String entryValue = resolvedAttribute(mapEntryNode, ATTRITBUTE.VALUE);
			function.putValue(entryName, entryValue);
		}
		if (data == null) {
			collectStack.peek().appendPipe(function);
		} else {
			data.appendPipe(function);
		}
	}

}
