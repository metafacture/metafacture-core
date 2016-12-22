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
package org.culturegraph.mf.jdom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Converts a stream into a {@link Document}.
 *
 * @author Markus Geipel
 */
@Description("Converts a stream into a JDom document.")
@In(StreamReceiver.class)
@Out(Document.class)
@FluxCommand("to-jdom-document")
public final class StreamToJDomDocument
		extends DefaultStreamPipe<ObjectReceiver<Document>> {

	private static final String ATTRIBUTE_MARKER = "~";
	private static final Pattern NAMESPACE_DELIMITER = Pattern.compile(":", Pattern.LITERAL);
	private static final String XML = "xml";

	private Document document;
	private Element currentElement;
	private final String rootTagName;
	private final Map<String, Namespace> namespaces = new HashMap<String,Namespace>();

	public StreamToJDomDocument(final String rootTagName, final String namespaceProperties) {
		this.rootTagName = rootTagName;
		namespaces.put(XML, Namespace.getNamespace(XML, "http://www.w3.org/XML/1998/namespace"));
		final Properties properties;
		try {
			properties = ResourceUtil.loadProperties(namespaceProperties);
		} catch (IOException e) {
			throw new MetafactureException("Cannot load namespaces list", e);
		}
		for (Entry<Object, Object> entry : properties.entrySet()) {
			namespaces.put(entry.getKey().toString(), Namespace.getNamespace(entry.getKey().toString(), entry.getValue().toString()));
		}
	}


	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		currentElement = createElement(rootTagName);
		for (Namespace namespace : namespaces.values()) {
			currentElement.addNamespaceDeclaration(namespace);
		}
		document = new Document(currentElement);
	}


	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		final Element parent = currentElement;
		currentElement = createElement(name);
		parent.addContent(currentElement);
	}

	private Element createElement(final String name) {
		final String[] parts = NAMESPACE_DELIMITER.split(name);
		if (parts.length == 2) {
			return new Element(parts[1], getNamespace(parts[0]));
		}
		return new Element(name);
	}

	private Namespace getNamespace(final String name){
		final Namespace namespace = namespaces.get(name);
		if(namespace==null){
			throw new IllegalArgumentException("Namespace " + name + " not registered");
		}
		return namespace;
	}


	@Override
	public void endEntity() {
		assert !isClosed();
		currentElement = currentElement.getParentElement();
	}


	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		if (name.isEmpty()) {
			currentElement.addContent(value);
		} else if (name.startsWith(ATTRIBUTE_MARKER)) {
			final String[] parts = NAMESPACE_DELIMITER.split(name);
			if (parts.length == 2) {
				currentElement.setAttribute(parts[1], value, getNamespace(parts[0].substring(1)));
			} else{
				currentElement.setAttribute(name.substring(1), value);
			}

		} else {
			final Element temp = createElement(name);
			currentElement.addContent(temp);
			temp.setText(value);
		}
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		getReceiver().process(document);
	}

}
