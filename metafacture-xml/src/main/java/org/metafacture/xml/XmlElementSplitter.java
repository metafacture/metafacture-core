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

package org.metafacture.xml;

import org.metafacture.commons.XmlUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashSet;
import java.util.Set;

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

    private String element;
    private StringBuilder builder = new StringBuilder();
    private Set<String> namespaces = new HashSet<>();
    private boolean inElement;
    private int recordCnt;
    private String root;
    private String rootStart = "";
    private String rootEnd = "";
    private String xmlDeclaration = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>";
    private int elementDepth;

    /**
     * Default constructor
     */
    public XmlElementSplitter() {
    }

    /**
     * Enriched constructor setting the top level element and the Element name
     *
     * @param topLevelElement the name of the top level XML tag
     * @param elementName the name of the tag defining a new Element to be split
     */
    public XmlElementSplitter(final String topLevelElement, final String elementName) {
        setTopLevelElement(topLevelElement);
        setElementName(elementName);
    }

    /**
     * Sets the name of the Element. All these elements in the XML stream will
     * be XML documents on their own.
     *
     * @param name Identifies the elements
     */
    public void setElementName(final String name) {
        this.element = name;
    }

    /**
     * Sets the top-level XML document element.
     *
     * @param newRoot the top level element. Leave at default to omit the
     *                top-level element.
     */
    public void setTopLevelElement(final String newRoot) {
        root = newRoot;
        rootStart = "<" + newRoot;
        rootEnd = "</" + newRoot + ">";
    }

    /**
     * Sets the XML declaration. The default is
     * {@code <?xml version = "1.0" encoding = "UTF-8"?>}. If an empty value is
     * given, the XML declaration is skipped.
     *
     * @param xmlDeclaration the XML declaration
     */
    public void setXmlDeclaration(final String xmlDeclaration) {
        this.xmlDeclaration = xmlDeclaration;
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        if (root != null & !prefix.isEmpty() && uri != null) {
            namespaces.add(" xmlns:" + prefix + "=\"" + uri + "\"");
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (!inElement) {
            if (element.equals(localName)) {
                builder = new StringBuilder();
                getReceiver().startRecord(String.valueOf(recordCnt));
                ++recordCnt;
                inElement = true;
                appendValuesToElement(qName, attributes);
                ++elementDepth;
            }
        }
        else {
            if (element.equals(localName)) {
                ++elementDepth;
            }
            appendValuesToElement(qName, attributes);
        }
    }

    private void appendValuesToElement(final String qName, final Attributes attributes) {
        this.builder.append("<" + qName);
        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); ++i) {
                builder.append(" " + attributes.getQName(i) + "=\"" + XmlUtil.escape(attributes.getValue(i)) + "\"");
            }
        }

        builder.append(">");
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (inElement) {
            builder.append("</" + qName + ">");
            if (element.equals(localName)) {
                if (elementDepth <= 1) {
                    final StringBuilder sb = new StringBuilder(xmlDeclaration + rootStart);
                    if (this.root != null && namespaces.size() > 0) {
                        for (final String ns : namespaces) {
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
                --elementDepth;
            }
        }
    }

    @Override
    public void characters(final char[] chars, final int start, final int length) throws SAXException {
        try {
            builder.append(XmlUtil.escape(new String(chars, start, length)));
        }
        catch (final Exception e) { // checkstyle-disable-line IllegalCatch
            reset();
        }
    }

    private void reset() {
        inElement = false;
        builder = new StringBuilder();
        elementDepth = 0;
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
    protected void onResetStream() {
        reset();
    }

}
