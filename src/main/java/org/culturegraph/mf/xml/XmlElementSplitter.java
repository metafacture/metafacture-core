/*
 * Copyright 2013, 2014 Pascal Christoph (hbz)
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

import java.util.HashSet;

import org.culturegraph.mf.commons.XmlUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An XML Element splitter.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("Splits elements (e.g. defining single records) residing in one XML document into multiple single XML documents.")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("split-xml-elements")
public final class XmlElementSplitter extends DefaultXmlPipe<StreamReceiver> {

	private String Element;
	private StringBuilder builder = new StringBuilder();
	private HashSet<String> namespaces = new HashSet<>();
	private boolean inElement = false;
	private int recordCnt = 0;
	private String root;
	private String rootStart = "";
	private String rootEnd = "";
	private String xmlDeclaration = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>";
	private int ElementDepth = 0;

	/**
	 * Default constructor
	 */
	public XmlElementSplitter() {
	}

	/**
	 * Enriched constructor setting the top level element and the Element name
	 *
	 * @param aTopLevelElement the name of the top level XML tag
	 * @param aElementName the name of the tag defining a new Element to be split
	 */
	public XmlElementSplitter(String aTopLevelElement, String aElementName) {
		setTopLevelElement(aTopLevelElement);
		setElementName(aElementName);
	}

	/**
	 * Sets the name of the Element. All these elements in the XML stream will
	 * be XML documents on their own.
	 *
	 * @param name Identifies the elements
	 */
	public void setElementName(final String name) {
		this.Element = name;
	}

	/**
	 * Sets the top-level XML document element.
	 *
	 * @param root the top level element. Don't set it to omit setting top level
	 *             element.
	 */
	public void setTopLevelElement(final String root) {
		this.root = root;
		this.rootStart = "<" + root;
		this.rootEnd = "</" + root + ">";
	}

	/**
	 * Sets the XML declaration.
	 *
	 * @param xmlDeclaration the xml declaration. Default is
	 *                       '{@code<?xml version = "1.0" encoding = "UTF-8"?>}'.
	 *                       If empty value is given, the xml declaration is
	 *                       skipped.
	 */
	public void setXmlDeclaration(final String xmlDeclaration) {
		this.xmlDeclaration = xmlDeclaration;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		super.startPrefixMapping(prefix, uri);
		if (root != null & !prefix.isEmpty() && uri != null) {
			namespaces.add(" xmlns:" + prefix + "=\"" + uri + "\"");
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		if (!inElement) {
			if (Element.equals(localName)) {
				builder = new StringBuilder();
				getReceiver().startRecord(String.valueOf(this.recordCnt++));
				inElement = true;
				appendValuesToElement(qName, attributes);
				ElementDepth++;
			}
		} else {
			if (Element.equals(localName)) {
				ElementDepth++;
			}
			appendValuesToElement(qName, attributes);
		}
	}

	private void appendValuesToElement(final String qName, final Attributes attributes) {
		this.builder.append("<" + qName);
		if (attributes.getLength() > 0) {
			for (int i = 0; i < attributes.getLength(); i++) {
				builder.append(" " + attributes.getQName(i) + "=\""
						+ XmlUtil.escape(attributes.getValue(i)) + "\"");
			}
		}

		builder.append(">");
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (inElement) {
			builder.append("</" + qName + ">");
			if (Element.equals(localName)) {
				if (ElementDepth <= 1) {
					StringBuilder sb = new StringBuilder(xmlDeclaration + rootStart);
					if (this.root != null && namespaces.size() > 0) {
						for (String ns : namespaces) {
							sb.append(ns);
						}
						sb.append(">");
					}
					builder.insert(0, sb.toString()).append(rootEnd);
					getReceiver().literal("Element", builder.toString());
					getReceiver().endRecord();
					reset();
					return;
				}
				ElementDepth--;
			}
		}
	}

	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		try {
			builder.append(XmlUtil.escape(new String(chars, start, length)));
		} catch (Exception e) {
			reset();
		}
	}

	private void reset() {
		inElement = false;
		builder = new StringBuilder();
		ElementDepth = 0;
	}

	/**
	 * Get the XML declaration.
	 *
	 * @return the XML decalration
	 */
	public String getXmlDeclaration() {
		return xmlDeclaration;
	}

	@Override
	public void onResetStream() {
		reset();
	}

}
