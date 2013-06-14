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
package org.culturegraph.mf.stream.sink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.MultiMap;
import org.culturegraph.mf.util.ResourceUtil;

/**
 *
 * writes a stream to XML
 *
 * @author Markus Michael Geipel
 *
 */
@Description("writes a stream to xml")
@In(StreamReceiver.class)
@Out(String.class)
public final class SimpleXmlWriter extends DefaultStreamPipe<ObjectReceiver<String>> {

	public static final String ATTRIBUTE_MARKER = "~";
	public static final String NAMESPACES = "namespaces";

	public static final String DEFAULT_ROOT_TAG = "records";
	public static final String DEFAULT_RECORD_TAG = "record";

	private static final String NEW_LINE = "\n";
	private static final String INDENT = "\t";

	private static final String BEGIN_ATTRIBUTE = "=\"";
	private static final String END_ATTRIBUTE = "\"";
	private static final String BEGIN_OPEN_ELEMENT = "<";
	private static final String END_OPEN_ELEMENT = ">";
	private static final String END_EMPTY_ELEMENT = " />";
	private static final String BEGIN_CLOSE_ELEMENT = "</";
	private static final String END_CLOSE_ELEMENT = ">";

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	private static final String XMLNS_MARKER = " xmlns:";

	private String rootTag = DEFAULT_ROOT_TAG;
	private String recordTag = DEFAULT_RECORD_TAG;
	private Map<String, String> namespaces = new HashMap<String, String>();
	private boolean writeXmlHeader = true;
	private boolean separateRoots;

	private Element element;
	private boolean start = true;

	public void setRootTag(final String rootTag) {
		this.rootTag = rootTag;
	}

	public void setRecordTag(final String tag) {
		recordTag = tag;
	}

	public void setNamespaceFile(final String file) {
		final Properties properties = ResourceUtil.loadProperties(file);
		for (final Entry<Object, Object> entry : properties.entrySet()) {
			namespaces.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	public void setWriteXmlHeader(final boolean writeXmlHeader) {
		this.writeXmlHeader = writeXmlHeader;
	}

	public void setSeparateRoots(final boolean separateRoots) {
		this.separateRoots = separateRoots;
	}

	public void configure(final MultiMap multimap) {
		this.namespaces = multimap.getMap(NAMESPACES);
	}

	@Override
	public void startRecord(final String identifier) {
		if (separateRoots || start) {
			writeHeader();
		}
		element = new Element(recordTag);
	}

	@Override
	public void endRecord() {
		if (recordTag.isEmpty()) {
			final StringBuilder builder = new StringBuilder();
			for (final Element child : element.getChildren()) {
				child.writeToStringBuilder(builder, 1);
			}
			getReceiver().process(builder.toString());
		} else {
			getReceiver().process(element.toString());
		}
		if (separateRoots) {
			writeFooter();
		}
	}

	@Override
	public void startEntity(final String name) {
		element = element.createChild(name);
	}

	@Override
	public void endEntity() {
		element = element.getParent();
	}

	@Override
	public void literal(final String name, final String value) {
		if (name.isEmpty()) {
			element.setText(value);
		} else if (name.startsWith(ATTRIBUTE_MARKER)) {
			element.addAttribute(name.substring(1), value);
		} else {
			final Element temp = element.createChild(name);
			temp.setText(value);
		}
	}

	@Override
	protected void onResetStream() {
		writeFooter();
		start = true;
	}

	@Override
	protected void onCloseStream() {
		if (!separateRoots) {
			writeFooter();
		}
	}

	private void writeHeader() {

		final StringBuilder builder = new StringBuilder();

		if (writeXmlHeader) {
			builder.append(XML_HEADER);
		}

		builder.append(BEGIN_OPEN_ELEMENT);
		builder.append(rootTag);
		for (final Entry<String, String> entry : namespaces.entrySet()) {
			builder.append(XMLNS_MARKER);
			builder.append(entry.getKey());
			builder.append(BEGIN_ATTRIBUTE);
			escape(builder, entry.getValue());
			builder.append(END_ATTRIBUTE);
		}
		builder.append(END_OPEN_ELEMENT);
		getReceiver().process(builder.toString());
		start = false;
	}

	private void writeFooter() {
		getReceiver().process(BEGIN_CLOSE_ELEMENT + rootTag + END_CLOSE_ELEMENT);
	}

	/**
	 *
	 */
	private static final class Element {
		private static final List<Element> NO_CHILDREN = Collections.emptyList();

		private final StringBuilder attributes = new StringBuilder();
		private String text = "";
		private List<Element> children = NO_CHILDREN;
		private final Element parent;
		private final String name;

		public Element(final String name) {
			this.name = name;
			this.parent = null;
		}

		private Element(final String name, final Element parent) {
			this.name = name;
			this.parent = parent;
		}

		public List<Element> getChildren() {
			return children;
		}

		public void addAttribute(final String name, final String value) {
			attributes.append(" ");
			attributes.append(name);
			attributes.append(BEGIN_ATTRIBUTE);
			escape(attributes, value);
			attributes.append(END_ATTRIBUTE);
		}

		public void setText(final String text) {
			this.text = text;
		}

		public Element createChild(final String name) {
			final Element child = new Element(name, this);
			if (children == NO_CHILDREN) {
				children = new ArrayList<SimpleXmlWriter.Element>();
			}
			children.add(child);
			return child;
		}

		public Element getParent() {
			return parent;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			writeToStringBuilder(builder, 1);
			return builder.toString();
		}

		public void writeToStringBuilder(final StringBuilder builder, final int indent) {
			builder.append(NEW_LINE);
			indent(builder, indent);
			builder.append(BEGIN_OPEN_ELEMENT);
			builder.append(name);
			builder.append(attributes);
			if (text.isEmpty() && children.isEmpty()) {
				builder.append(END_EMPTY_ELEMENT);
			} else {
				builder.append(END_OPEN_ELEMENT);
			}

			escape(builder, text);

			for (final Element element : children) {
				element.writeToStringBuilder(builder, indent + 1);
			}
			if (text.isEmpty() && !children.isEmpty()) {
				builder.append(NEW_LINE);
				indent(builder, indent);
			}

			if (!text.isEmpty() || !children.isEmpty()) {
				builder.append(BEGIN_CLOSE_ELEMENT);
				builder.append(name);
				builder.append(END_CLOSE_ELEMENT);
			}
		}

		private static void indent(final StringBuilder builder, final int indent) {
			for (int i = 0; i < indent; ++i) {
				builder.append(INDENT);
			}
		}
	}

	protected static void escape(final StringBuilder builder, final String str) {

		final int len = str.length();
		for (int i = 0; i < len; ++i) {
			final char c = str.charAt(i);
			final String entityName;
			switch (c) {
			case '&':
				entityName = "amp";
				break;
			case '<':
				entityName = "lt";
				break;
			case '>':
				entityName = "gt";
				break;
			case '\'':
				entityName = "apos";
				break;
			case '"':
				entityName = "quot";
				break;
			default:
				entityName = null;
				break;
			}

			if (entityName == null) {
				builder.append(c);
			} else {
				builder.append('&');
				builder.append(entityName);
				builder.append(';');
			}
		}
	}

}
