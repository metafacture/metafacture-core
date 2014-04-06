/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.morph.collectors.Collect;
import org.culturegraph.mf.morph.collectors.Entity;
import org.culturegraph.mf.morph.functions.Function;
import org.culturegraph.mf.types.MultiMap;
import org.culturegraph.mf.util.reflection.ObjectFactory;
import org.culturegraph.mf.util.xml.Location;
import org.w3c.dom.Node;

/**
 * Builds a {@link Metamorph} from an xml description
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public final class MorphBuilder extends AbstractMetamorphDomWalker {

	private static final String NOT_FOUND = " not found.";
	private static final String JAVA = "java";
	private static final String JAVAMAP = "javamap";
	private static final String RECORD = "record";
	private static final String OR_STRING = "|";
	private static final Pattern OR_PATTERN = Pattern.compile(OR_STRING, Pattern.LITERAL);

	private final Metamorph metamorph;
	private final Deque<StackFrame> stack = new LinkedList<StackFrame>();

	private static final class StackFrame {

		private NamedValuePipe pipe;
		private boolean inEntityName;
		private boolean inCondition;

		public StackFrame(final NamedValuePipe pipe) {
			this.pipe = pipe;
		}

		public void setPipe(final NamedValuePipe pipe) {
			this.pipe = pipe;
		}

		public NamedValuePipe getPipe() {
			return pipe;
		}

		public void setInEntityName(final boolean inEntityName) {
			this.inEntityName = inEntityName;
		}

		public boolean isInEntityName() {
			return inEntityName;
		}

		public void setInCondition(final boolean inCondition) {
			this.inCondition = inCondition;
		}

		public boolean isInCondition() {
			return inCondition;
		}

	}

	protected MorphBuilder(final Metamorph metamorph) {
		super();

		this.metamorph = metamorph;
		stack.push(new StackFrame(metamorph));
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
		} else if (getMapFactory().containsKey(mapNode.getLocalName())) {
			map = getMapFactory().newInstance(mapNode.getLocalName(), attributes);
		} else {
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
		} catch (final ClassNotFoundException e) {
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
		final Data data = new Data();
		data.setName(resolvedAttribute(dataNode, ATTRITBUTE.NAME));
		data.setSourceLocation((Location) dataNode.getUserData(Location.USER_DATA_ID));

		final String source = resolvedAttribute(dataNode, ATTRITBUTE.SOURCE);
		metamorph.registerNamedValueReceiver(source, data);

		stack.push(new StackFrame(data));
	}

	@Override
	protected void exitData(final Node node) {
		final NamedValuePipe dataPipe = stack.pop().getPipe();

		final StackFrame parent = stack.peek();
		if (parent.isInEntityName()) {
			// Protected xsd schema and by assertion in enterName:
			((Entity) parent.getPipe()).setNameSource(dataPipe);
		} else if (parent.isInCondition()) {
			// Protected xsd schema and by assertion in enterIf:
			((ConditionAware) parent.getPipe()).setConditionSource(dataPipe);
		} else {
			parent.getPipe().addNamedValueSource(dataPipe);
		}
	}

	@Override
	protected void enterName(final Node nameNode) {
		assert stack.peek().getPipe() instanceof Entity :
				"statement `name` is only allowed in `entity` statements";

		stack.peek().setInEntityName(true);
	}

	@Override
	protected void exitName(final Node nameNode) {
		stack.peek().setInEntityName(false);
	}

	@Override
	protected void enterIf(final Node nameNode) {
		assert stack.peek().getPipe() instanceof ConditionAware :
				"statement `if` is not allowed in the current element";

		stack.peek().setInCondition(true);
	}

	@Override
	protected void exitIf(final Node nameNode) {
		stack.peek().setInCondition(false);
	}

	@Override
	protected void enterCollect(final Node node) {
		final Map<String, String> attributes = resolvedAttributeMap(node);
		// must be set after recursive calls to flush descendants before parent
		attributes.remove(ATTRITBUTE.FLUSH_WITH.getString());

		if (!getCollectFactory().containsKey(node.getLocalName())) {
			throw new IllegalArgumentException("Collector " + node.getLocalName() + NOT_FOUND);
		}
		final Collect collect = getCollectFactory().newInstance(node.getLocalName(), attributes, metamorph);
		collect.setSourceLocation((Location) node.getUserData(Location.USER_DATA_ID));

		stack.push(new StackFrame(collect));
	}

	@Override
	protected void exitCollect(final Node node) {
		final NamedValuePipe collectPipe = stack.pop().getPipe();

		final StackFrame parent = stack.peek();

		if (parent.isInEntityName()) {
			// Protected xsd schema and by assertion in enterName:
			((Entity) parent.getPipe()).setNameSource(collectPipe);
		} else if (parent.isInCondition()) {
			// Protected xsd schema and by assertion in enterIf:
			((ConditionAware) parent.getPipe()).setConditionSource(collectPipe);
		} else {
			parent.getPipe().addNamedValueSource(collectPipe);
		}

		// must be set after recursive calls to flush descendants before parent
		final String flushWith = resolvedAttribute(node, ATTRITBUTE.FLUSH_WITH);
		if (null != flushWith) {
			assert collectPipe instanceof Collect :
					"Invokations of enterXXX and exitXXX are not properly balanced";

			((Collect) collectPipe).setWaitForFlush(true);
			registerFlush(flushWith, ((Collect) collectPipe));
		}
	}

	private void registerFlush(final String flushWith, final FlushListener flushListener) {
		final String[] keysSplit = OR_PATTERN.split(flushWith);
		for (final String key : keysSplit) {
			if (key.equals(RECORD)) {
				metamorph.registerRecordEndFlush(flushListener);
			} else {
				metamorph.registerNamedValueReceiver(key, new Flush(flushListener));
			}
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

		function.setSourceLocation((Location) functionNode.getUserData(Location.USER_DATA_ID));

		function.setMultiMap(metamorph);

		// add key value entries...
		for (Node mapEntryNode = functionNode.getFirstChild(); mapEntryNode != null; mapEntryNode = mapEntryNode
				.getNextSibling()) {
			final String entryName = resolvedAttribute(mapEntryNode, ATTRITBUTE.NAME);
			final String entryValue = resolvedAttribute(mapEntryNode, ATTRITBUTE.VALUE);
			function.putValue(entryName, entryValue);
		}

		final StackFrame head = stack.peek();
		function.addNamedValueSource(head.getPipe());
		head.setPipe(function);
	}

}
