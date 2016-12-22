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
package org.culturegraph.mf.xml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 *
 * Encodes a stream as XML
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
@Description("Encodes a stream as xml")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("stream-to-xml")
public final class SimpleXmlEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	public static final String ATTRIBUTE_MARKER = "~";

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
	private static final String XMLNS_MARKER = " xmlns";

	private final StringBuilder builder = new StringBuilder();

	private String rootTag = DEFAULT_ROOT_TAG;
	private String recordTag = DEFAULT_RECORD_TAG;
	private Map<String, String> namespaces = new HashMap<String, String>();
	private boolean writeRootTag = true;
	private boolean writeXmlHeader = true;
	private boolean separateRoots;

	private Element element;
	private boolean atStreamStart = true;

	public void setRootTag(final String rootTag) {
		this.rootTag = rootTag;
	}

	public void setRecordTag(final String tag) {
		recordTag = tag;
	}

	public void setNamespaceFile(final String file) {
		final Properties properties;
		try {
			properties = ResourceUtil.loadProperties(file);
		} catch (IOException e) {
			throw new MetafactureException("Failed to load namespaces list", e);
		}
		for (final Entry<Object, Object> entry : properties.entrySet()) {
			namespaces.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	public void setNamespaceFile(final URL url) {
		final Properties properties;
		try {
			properties = ResourceUtil.loadProperties(url);
		} catch (IOException e) {
			throw new MetafactureException("Failed to load namespaces list", e);
		}
		for (final Entry<Object, Object> entry : properties.entrySet()) {
			namespaces.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	public void setWriteXmlHeader(final boolean writeXmlHeader) {
		this.writeXmlHeader = writeXmlHeader;
	}

	public void setWriteRootTag(final boolean writeRootTag) {
		this.writeRootTag  = writeRootTag;
	}

	public void setSeparateRoots(final boolean separateRoots) {
		this.separateRoots = separateRoots;
	}

	public void setNamespaces(final Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	@Override
	public void startRecord(final String identifier) {
		if (separateRoots) {
			writeHeader();
		} else if (atStreamStart) {
			writeHeader();
			sendAndClearData();
		}
		atStreamStart = false;

		element = new Element(recordTag);
		if (!writeRootTag) {
			addNamespacesToElement();
		}
	}

	private void addNamespacesToElement() {
		for (final Entry<String, String> namespace : namespaces.entrySet()) {
			final String key = namespace.getKey();
			final String name = XMLNS_MARKER + (key.isEmpty() ? "" : ":") + key;
			element.addAttribute(name, namespace.getValue());
		}
	}

	@Override
	public void endRecord() {
		element.writeElement(builder, 1);
		if (separateRoots) {
			writeFooter();
		}
		sendAndClearData();
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
			element.createChild(name).setText(value);
		}
	}

	@Override
	protected void onResetStream() {
		if (!atStreamStart) {
			writeFooter();
		}
		sendAndClearData();
		atStreamStart = true;
	}

	@Override
	protected void onCloseStream() {
		if (!separateRoots) {
			if (!atStreamStart) {
				writeFooter();
			}
			sendAndClearData();
		}
	}

	private void sendAndClearData() {
		getReceiver().process(builder.toString());
		builder.delete(0, builder.length());
	}

	private void writeHeader() {
		if (writeXmlHeader) {
			builder.append(XML_HEADER);
		}
		if (writeRootTag) {
			builder.append(BEGIN_OPEN_ELEMENT);
			builder.append(rootTag);
			for (final Entry<String, String> entry : namespaces.entrySet()) {
				builder.append(XMLNS_MARKER);
				if (!entry.getKey().isEmpty()) {
					builder.append(':');
					builder.append(entry.getKey());
				}
				builder.append(BEGIN_ATTRIBUTE);
				writeEscaped(builder, entry.getValue());
				builder.append(END_ATTRIBUTE);
			}
			builder.append(END_OPEN_ELEMENT);
		}
	}

	private void writeFooter() {
		if (writeRootTag) {
			builder.append(NEW_LINE);
			builder.append(BEGIN_CLOSE_ELEMENT);
			builder.append(rootTag);
			builder.append(END_CLOSE_ELEMENT);
		}
	}

	protected static void writeEscaped(final StringBuilder builder, final String str) {

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

	/**
	 * An XML element.
	 *
	 */
	private static final class Element {

		private static final List<Element> NO_CHILDREN = Collections.emptyList();

		private final StringBuilder attributes = new StringBuilder();
		private final Element parent;
		private final String name;

		private String text = "";
		private List<Element> children = NO_CHILDREN;

		public Element(final String name) {
			this.name = name;
			this.parent = null;
		}

		private Element(final String name, final Element parent) {
			this.name = name;
			this.parent = parent;
		}

		public void addAttribute(final String name, final String value) {
			attributes.append(" ");
			attributes.append(name);
			attributes.append(BEGIN_ATTRIBUTE);
			writeEscaped(attributes, value);
			attributes.append(END_ATTRIBUTE);
		}

		public void setText(final String text) {
			this.text = text;
		}

		public Element createChild(final String name) {
			final Element child = new Element(name, this);
			if (children == NO_CHILDREN) {
				children = new ArrayList<SimpleXmlEncoder.Element>();
			}
			children.add(child);
			return child;
		}

		public Element getParent() {
			return parent;
		}

		public void writeElement(final StringBuilder builder, final int indent) {
			if (!name.isEmpty()) {
				builder.append(NEW_LINE);
				writeIndent(builder, indent);
				builder.append(BEGIN_OPEN_ELEMENT);
				builder.append(name);
				builder.append(attributes);
				if (text.isEmpty() && children.isEmpty()) {
					builder.append(END_EMPTY_ELEMENT);
					return;
				}
				builder.append(END_OPEN_ELEMENT);
			}

			writeEscaped(builder, text);

			for (final Element element : children) {
				element.writeElement(builder, indent + 1);
			}

			if (text.isEmpty() && !children.isEmpty()) {
				builder.append(NEW_LINE);
				writeIndent(builder, indent);
			}

			if (!name.isEmpty()) {
				builder.append(BEGIN_CLOSE_ELEMENT);
				builder.append(name);
				builder.append(END_CLOSE_ELEMENT);
			}
		}

		private static void writeIndent(final StringBuilder builder, final int indent) {
			for (int i = 0; i < indent; ++i) {
				builder.append(INDENT);
			}
		}

	}

}
