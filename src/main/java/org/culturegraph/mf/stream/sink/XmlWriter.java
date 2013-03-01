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

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.MultiMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * 
 * writes a stream to XML
 * 
 * @author Markus Michael Geipel
 * @deprecated use {@link SimpleXmlWriter} instead
 */
@Description("writes a stream to xml")
@In(StreamReceiver.class)
@Out(String.class)
@Deprecated
public final class XmlWriter extends DefaultStreamPipe<ObjectReceiver<String>> {
	public static final String ATTRIBUTE_MARKER = "~";
	public static final String TEXT_CONTENT_MARKER = "_text";
	public static final String NAMESPACES = "namespaces";
	
	private final DocumentBuilder documentBuilder;
	private final Transformer transformer;
	private Document document;
	private Element element;
	private Map<String, String> namespaces;
	private String recordTag = "record";

	public XmlWriter() throws ParserConfigurationException, TransformerException {
		super();
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		// builderFactory.setNamespaceAware(false);
		final TransformerFactory transformaterFactory = TransformerFactory.newInstance();
		transformer = transformaterFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		documentBuilder = builderFactory.newDocumentBuilder();

	}

	@Override
	public void startRecord(final String identifier) {
		document = documentBuilder.newDocument();
	
		element = document.createElement(recordTag);

		for (Entry<String, String> entry : namespaces.entrySet()) {
			element.setAttribute("xmlns:" + entry.getKey(), entry.getValue());
		}
		document.appendChild(element);
	}

	@Override
	public void endRecord() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		try {
			transformer.transform(new DOMSource(document), new StreamResult(baos));
		} catch (TransformerException e) {
			throw new MetafactureException("XML Error", e);
		}
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		getReceiver().process(baos.toString());
	}

	@Override
	public void startEntity(final String name) {
		element = (Element) element.appendChild(document.createElement(name));
	}

	@Override
	public void endEntity() {
		element = (Element) element.getParentNode();
	}

	@Override
	public void literal(final String name, final String value) {
		if (TEXT_CONTENT_MARKER.equals(name)) {
			element.setTextContent(value);
		} else if (name.startsWith(ATTRIBUTE_MARKER)) {
			element.setAttribute(name.substring(1), value);
		} else {
			final Element temp = (Element) element.appendChild(document.createElement(name));
			temp.setTextContent(value);
		}
	}

	public void configure(final MultiMap multimap) {
		this.namespaces = multimap.getMap(NAMESPACES);
	}

	public void setRecordTag(final String tag) {
		recordTag = tag;
		
	}


}
