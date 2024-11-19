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

package org.metafacture.xml;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.commons.XmlUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.framework.helpers.DefaultXmlPipe;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 *
 * Encodes a stream as XML.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
@Description("Encodes a stream as XML. Defaults: `rootTag=\"records\"`, `recordTag=\"record\"`, no attributeMarker.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("stream-to-xml")
public final class SimpleXmlEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

    public static final String ATTRIBUTE_MARKER = "~";
    public static final String DEFAULT_VALUE_TAG = "";

    private static final String NEW_LINE = "\n";
    private static final String INDENT = "\t";

    private static final String BEGIN_ATTRIBUTE = "=\"";
    private static final String END_ATTRIBUTE = "\"";
    private static final String BEGIN_OPEN_ELEMENT = "<";
    private static final String END_OPEN_ELEMENT = ">";
    private static final String END_EMPTY_ELEMENT = " />";
    private static final String BEGIN_CLOSE_ELEMENT = "</";
    private static final String END_CLOSE_ELEMENT = ">";

    private static final String XML_HEADER = "<?xml version=\"%s\" encoding=\"%s\"?>\n";
    private static final String XMLNS_MARKER = " xmlns";
    private static final String DEFAULT = "__default";

    private final StringBuilder builder = new StringBuilder();

    private String attributeMarker = ATTRIBUTE_MARKER;
    private String rootTag = DefaultXmlPipe.DEFAULT_ROOT_TAG;
    private String recordTag = DefaultXmlPipe.DEFAULT_RECORD_TAG;
    private String valueTag = DEFAULT_VALUE_TAG;
    private Map<String, String> namespaces = new HashMap<String, String>();
    private boolean writeRootTag = true;
    private boolean writeXmlHeader = true;
    private String xmlHeaderEncoding = "UTF-8";
    private String xmlHeaderVersion = "1.0";

    private boolean separateRoots;

    private Element element;
    private boolean atStreamStart = true;

    /**
     * Creates an instance of {@link SimpleXmlEncoder}.
     */
    public SimpleXmlEncoder() {
    }

    /**
     * Sets the root tag.
     *
     * @param rootTag the root tag
     */
    public void setRootTag(final String rootTag) {
        this.rootTag = rootTag;
    }

    /**
     * Sets the record tag.
     *
     * @param tag the record tag
     */
    public void setRecordTag(final String tag) {
        recordTag = tag;
    }

    /**
     * Sets the value tag.
     *
     * @param valueTag the value tag
     */
    public void setValueTag(final String valueTag) {
        this.valueTag = valueTag;
    }

    /**
     * Gets the value tag.
     *
     * @return the value tag
     */
    public String getValueTag() {
        return valueTag;
    }

    /**
     * Loads namespaces from a file.
     *
     * @param file the name of the file to load the namespace properties from
     */
    public void setNamespaceFile(final String file) {
        final Properties properties;
        try {
            properties = ResourceUtil.loadProperties(file);
        }
        catch (final IOException e) {
            throw new MetafactureException("Failed to load namespaces list", e);
        }
        propertiesToMap(properties);
    }

    /**
     * Loads namespaces from a URL.
     *
     * @param url the URL to load the namespace properties from.
     */
    public void setNamespaceFile(final URL url) {
        final Properties properties;
        try {
            properties = ResourceUtil.loadProperties(url);
        }
        catch (final IOException e) {
            throw new MetafactureException("Failed to load namespaces list", e);
        }
        propertiesToMap(properties);
    }

    /**
     * Flags whether to write the XML header.
     *
     * @param writeXmlHeader true if the XML header should be written
     */
    public void setWriteXmlHeader(final boolean writeXmlHeader) {
        this.writeXmlHeader = writeXmlHeader;
    }

    /**
     * Sets the XML header encoding.
     *
     * @param xmlHeaderEncoding the XML header encoding
     */
    public void setXmlHeaderEncoding(final String xmlHeaderEncoding) {
        this.xmlHeaderEncoding = xmlHeaderEncoding;
    }

    /**
     * Sets the XML header version.
     *
     * @param xmlHeaderVersion the XML header version.
     */
    public void setXmlHeaderVersion(final String xmlHeaderVersion) {
        this.xmlHeaderVersion = xmlHeaderVersion;
    }

    /**
     * Flags whether to write the root tag.
     *
     * @param writeRootTag true if the root tag should be written
     */
    public void setWriteRootTag(final boolean writeRootTag) {
        this.writeRootTag  = writeRootTag;
    }

    /**
     * Flags whether to separate roots.
     *
     * @param separateRoots true if roots should be separated
     */
    public void setSeparateRoots(final boolean separateRoots) {
        this.separateRoots = separateRoots;
    }

    /**
     * Sets the namespaces.
     *
     * @param namespaces the namespaces
     */
    public void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Sets the namespace(s).
     *
     * @param namespacesString the namespaces as a String. It allows Java Properties
     *                         structure, i.e. a key-value structure where the key is separated from the value
     *                         by an equal sign '=', a colon ':' or a white space ' '. Multiple namespaces
     *                         are separated by a line feed '\n'
     */
    public void setNamespaces(final String namespacesString) {
        final Properties properties = new Properties();
        try (StringReader sr = new StringReader(namespacesString)) {
            properties.load(sr);
        }
        catch (final IOException e) {
            throw new MetafactureException("Failed to create namespace list");
        }
        propertiesToMap(properties);
    }

    /**
     * Sets the attribute marker.
     *
     * @param attributeMarker the attribute marker.
     */
    public void setAttributeMarker(final String attributeMarker) {
        this.attributeMarker = attributeMarker;
    }

    /**
     * Gets the attribute marker.
     *
     * @return the attribute marker
     */
    public String getAttributeMarker() {
        return attributeMarker;
    }

    @Override
    public void startRecord(final String identifier) {
        if (separateRoots) {
            writeHeader();
        }
        else if (atStreamStart) {
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
            final String name = XMLNS_MARKER + (isDefaultNamespace(key) ? "" : ":" + key);
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
        if (name.equals(valueTag)) {
            element.setText(value);
        }
        else if (name.startsWith(attributeMarker)) {
            element.addAttribute(name.substring(attributeMarker.length()), value);
        }
        else {
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
            builder.append(String.format(XML_HEADER, xmlHeaderVersion, xmlHeaderEncoding));
        }
        if (writeRootTag) {
            builder.append(BEGIN_OPEN_ELEMENT);
            builder.append(rootTag);
            for (final Entry<String, String> entry : namespaces.entrySet()) {
                builder.append(XMLNS_MARKER);
                if (!isDefaultNamespace(entry.getKey())) {
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
        builder.append(XmlUtil.escape(str, false));
    }

    private boolean isDefaultNamespace(final String ns) {
        return ns.isEmpty() || ns.equals(DEFAULT);
    }

    private void propertiesToMap(final Properties properties) {
        properties.forEach((k, v) -> namespaces.put(k.toString(), v.toString()));
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

        Element(final String name) {
            this.name = name;
            this.parent = null;
        }

        private Element(final String name, final Element parent) {
            this.name = name;
            this.parent = parent;
        }

        public void addAttribute(final String attributeName, final String value) {
            attributes.append(" ");
            attributes.append(attributeName);
            attributes.append(BEGIN_ATTRIBUTE);
            writeEscaped(attributes, value);
            attributes.append(END_ATTRIBUTE);
        }

        public void setText(final String text) {
            this.text = text;
        }

        public Element createChild(final String attributeName) {
            final Element child = new Element(attributeName, this);
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
