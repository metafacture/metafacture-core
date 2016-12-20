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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.culturegraph.mf.commons.reflection.ConfigurableClass;
import org.culturegraph.mf.commons.reflection.ReflectionUtil;
import org.culturegraph.mf.metamorph.api.Collect;
import org.culturegraph.mf.metamorph.api.ConditionAware;
import org.culturegraph.mf.metamorph.api.FlushListener;
import org.culturegraph.mf.metamorph.api.Function;
import org.culturegraph.mf.metamorph.api.InterceptorFactory;
import org.culturegraph.mf.metamorph.api.Maps;
import org.culturegraph.mf.metamorph.api.MorphBuildException;
import org.culturegraph.mf.metamorph.api.NamedValuePipe;
import org.culturegraph.mf.metamorph.xml.Location;
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
	private final InterceptorFactory interceptorFactory;
	private final Deque<StackFrame> stack = new LinkedList<StackFrame>();

	private static final class StackFrame {

		private final NamedValuePipe headPipe;

		private NamedValuePipe pipe;
		private boolean inEntityName;
		private boolean inCondition;

		public StackFrame(final NamedValuePipe headPipe) {
			this.headPipe = headPipe;
			this.pipe = headPipe;
		}

		public NamedValuePipe getHeadPipe() {
			return headPipe;
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

	protected MorphBuilder(final Metamorph metamorph,
			final InterceptorFactory interceptorFactory) {

		super();

		this.metamorph = metamorph;
		this.interceptorFactory = interceptorFactory;
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
		final String mapName = resolvedAttribute(mapNode, AttributeName.NAME);

		final String mapDefault = resolvedAttribute(mapNode, AttributeName.DEFAULT);

		for (Node entryNode = mapNode.getFirstChild(); entryNode != null; entryNode = entryNode.getNextSibling()) {
			final String entryName = resolvedAttribute(entryNode, AttributeName.NAME);
			final String entryValue = resolvedAttribute(entryNode, AttributeName.VALUE);
			metamorph.putValue(mapName, entryName, entryValue);
		}

		if (mapDefault != null) {
			metamorph.putValue(mapName, Maps.DEFAULT_MAP_KEY, mapDefault);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleMapClass(final Node mapNode) {
		final Map<String, String> attributes = resolvedAttributeMap(mapNode);
		final String mapName = resolveVars(attributes.remove(AttributeName.NAME.getString()));
		final Map<String, String> map;
		if (mapNode.getLocalName().equals(JAVAMAP)) {
			final String className = resolvedAttribute(mapNode, AttributeName.CLASS);
			attributes.remove(AttributeName.CLASS.getString());
			final ConfigurableClass<? extends Map> mapClass =
					ReflectionUtil.loadClass(className, Map.class);
			map = mapClass.newInstance(attributes);
		} else if (getMapFactory().containsKey(mapNode.getLocalName())) {
			map = getMapFactory().newInstance(mapNode.getLocalName(), attributes);
		} else {
			throw new MorphBuildException("Map " + mapNode.getLocalName() + NOT_FOUND);
		}

		metamorph.putMap(mapName, map);
	}

	@Override
	@SuppressWarnings("unchecked")
	// protected by 'if (Function.class.isAssignableFrom(clazz))'
	protected void handleFunctionDefinition(final Node functionDefNode) {
		final Class<?> clazz;
		final String className = resolvedAttribute(functionDefNode, AttributeName.CLASS);
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch (final ClassNotFoundException e) {
			throw new MorphBuildException("Function " + className + NOT_FOUND, e);
		}
		if (Function.class.isAssignableFrom(clazz)) {
			getFunctionFactory().registerClass(resolvedAttribute(functionDefNode, AttributeName.NAME),
					(Class<Function>) clazz);
		} else {
			throw new MorphBuildException(className + " does not implement interface 'Function'");
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
		data.setName(resolvedAttribute(dataNode, AttributeName.NAME));
		data.setSourceLocation(getSourceLocation(dataNode));

		final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
		final NamedValuePipe delegate;
		if (interceptor == null) {
			delegate = data;
		} else {
			delegate = interceptor;
			data.addNamedValueSource(delegate);
		}

		final String source = resolvedAttribute(dataNode, AttributeName.SOURCE);
		metamorph.registerNamedValueReceiver(source, delegate);

		stack.push(new StackFrame(data));
	}

	@Override
	protected void exitData(final Node node) {
		final NamedValuePipe dataPipe = stack.pop().getPipe();

		final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
		final NamedValuePipe delegate;
		if (interceptor == null) {
			delegate = dataPipe;
		} else {
			delegate = interceptor;
			delegate.addNamedValueSource(dataPipe);
		}

		final StackFrame parent = stack.peek();
		if (parent.isInEntityName()) {
			// Protected xsd schema and by assertion in enterName:
			((Entity) parent.getPipe()).setNameSource(delegate);
		} else if (parent.isInCondition()) {
			// Protected xsd schema and by assertion in enterIf:
			((ConditionAware) parent.getPipe()).setConditionSource(delegate);
		} else {
			parent.getPipe().addNamedValueSource(delegate);
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
		// flushWith should not be passed to the headPipe object via a
		// setter (see newInstance):
		attributes.remove(AttributeName.FLUSH_WITH.getString());

		if (!getCollectFactory().containsKey(node.getLocalName())) {
			throw new MorphBuildException("Collector " + node.getLocalName() +
					NOT_FOUND);
		}
		final Collect collect;
		if (ENTITY.equals(node.getLocalName())) {
			collect = getCollectFactory().newInstance(node.getLocalName(), attributes,
					metamorph);
		} else {
			collect = getCollectFactory().newInstance(node.getLocalName(), attributes);
		}
		collect.setSourceLocation(getSourceLocation(node));

		stack.push(new StackFrame(collect));
	}

	@Override
	protected void exitCollect(final Node node) {
		final StackFrame currentCollect = stack.pop();
		final Collect collector = (Collect) currentCollect.getHeadPipe();
		final NamedValuePipe tailPipe = currentCollect.getPipe();

		final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
		final NamedValuePipe delegate;
		if (interceptor == null || tailPipe instanceof Entity) {
			// The result of entity collectors cannot be intercepted
			// because they only use the receive/emit interface for
			// signalling while the actual data is transferred using
			// a custom mechanism. In order for this to work the Entity
			// class checks whether source and receiver are an
			// instances of Entity. If an interceptor is inserted between
			// entity elements this mechanism will break.
			delegate = tailPipe;
		} else {
			delegate = interceptor;
			delegate.addNamedValueSource(tailPipe);
		}

		final StackFrame parent = stack.peek();
		if (parent.isInEntityName()) {
			// Protected xsd schema and by assertion in enterName:
			((Entity) parent.getPipe()).setNameSource(delegate);
		} else if (parent.isInCondition()) {
			// Protected xsd schema and by assertion in enterIf:
			((ConditionAware) parent.getPipe()).setConditionSource(delegate);
		} else {
			parent.getPipe().addNamedValueSource(delegate);
		}

		// must be set after recursive calls to flush descendants before parent
		final String flushWith = resolvedAttribute(node, AttributeName.FLUSH_WITH);
		if (null != flushWith) {
			collector.setWaitForFlush(true);
			registerFlush(flushWith, collector);
		}
	}

	private void registerFlush(final String flushWith, final FlushListener flushListener) {
		final String[] keysSplit = OR_PATTERN.split(flushWith);
		for (final String key : keysSplit) {
			final FlushListener interceptor = interceptorFactory.createFlushInterceptor(flushListener);
			final FlushListener delegate;
			if (interceptor == null) {
				delegate = flushListener;
			} else {
				delegate = interceptor;
			}
			if (key.equals(RECORD)) {
				metamorph.registerRecordEndFlush(delegate);
			} else {
				metamorph.registerNamedValueReceiver(key, new Flush(delegate));
			}
		}
	}

	@Override
	protected void handleFunction(final Node functionNode) {
		final Function function;
		final Map<String, String> attributes = resolvedAttributeMap(functionNode);
		if (functionNode.getLocalName().equals(JAVA)) {
			final String className = resolvedAttribute(functionNode,
					AttributeName.CLASS);
			attributes.remove(AttributeName.CLASS.getString());
			final ConfigurableClass<? extends Function> functionClass =
					ReflectionUtil.loadClass(className, Function.class);
			function = functionClass.newInstance(attributes);
		} else if (getFunctionFactory().containsKey(functionNode.getLocalName())) {
			final String flushWith = attributes.remove(AttributeName.FLUSH_WITH.getString());
			function = getFunctionFactory().newInstance(functionNode.getLocalName(), attributes);
			if (null != flushWith) {
				registerFlush(flushWith, function);
			}
		} else {
			throw new MorphBuildException(functionNode.getLocalName() + NOT_FOUND);
		}

		function.setSourceLocation(getSourceLocation(functionNode));

		function.setMaps(metamorph);

		// add key value entries...
		for (Node mapEntryNode = functionNode.getFirstChild(); mapEntryNode != null; mapEntryNode = mapEntryNode
				.getNextSibling()) {
			final String entryName = resolvedAttribute(mapEntryNode, AttributeName.NAME);
			final String entryValue = resolvedAttribute(mapEntryNode, AttributeName.VALUE);
			function.putValue(entryName, entryValue);
		}

		final StackFrame head = stack.peek();

		final NamedValuePipe interceptor = interceptorFactory.createNamedValueInterceptor();
		final NamedValuePipe delegate;
		if (interceptor == null) {
			delegate = function;
		} else {
			delegate = interceptor;
			function.addNamedValueSource(delegate);
		}
		delegate.addNamedValueSource(head.getPipe());

		head.setPipe(function);
	}

	private XmlSourceLocation getSourceLocation(final Node node) {
		return new XmlSourceLocation((Location) node.getUserData(
				Location.USER_DATA_ID));
	}

}
