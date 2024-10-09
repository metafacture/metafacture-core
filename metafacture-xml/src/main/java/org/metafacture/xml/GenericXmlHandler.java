/*
 * Copyright 2013, 2014, 2021 Deutsche Nationalbibliothek et al
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;

import org.xml.sax.Attributes;

import java.util.regex.Pattern;

/**
 * A generic xml reader.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("A generic xml reader. Separates XML data in distrinct records with the defined record tag name (default: `recordtagname=\"record\"`)" +
        "If no matching record tag is found, the output will be empty." +
        "The handler breaks down XML elements with simple string values and optional attributes" +
        "into entities with a value subfield (name configurable) and additional subfields for each attribute." +
        "Record tag and value tag names can be configured, also attributes can get get an attributeMarker.")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-generic-xml")
public final class GenericXmlHandler extends DefaultXmlPipe<StreamReceiver> {

    public static final String RECORD_TAG_PROPERTY = "org.culturegraph.metamorph.xml.recordtag";

    public static final boolean EMIT_NAMESPACE = false;

    private static final Pattern TABS = Pattern.compile("\t+");

    private String attributeMarker = DEFAULT_ATTRIBUTE_MARKER;
    private String recordTagName = DEFAULT_RECORD_TAG;
    private String valueTagName = DEFAULT_VALUE_TAG;

    private boolean inRecord;
    private StringBuilder valueBuffer = new StringBuilder();

    private boolean emitNamespace = EMIT_NAMESPACE;

    /**
     * Constructs a GenericXmlHandler. Sets the record tag name to the value of
     * the system property {@value #RECORD_TAG_PROPERTY} if it's present.
     */
    public GenericXmlHandler() {
        final String recordTagNameProperty = System.getProperty(RECORD_TAG_PROPERTY);
        if (recordTagNameProperty != null) {
            recordTagName = recordTagNameProperty;
        }
    }

    /**
     * Creates a new {@code GenericXmlReader} with the given tag name as
     * marker for records.
     *
     * @deprecated Use default constructor and set the tag name later
     * with {@link #setRecordTagName(String)}.
     *
     * @param recordTagName tag name marking the start of a record.
     */
    @Deprecated
    public GenericXmlHandler(final String recordTagName) {
        this.recordTagName = recordTagName;
    }

    /**
     * Sets the tag name which marks the start of a record.
     * <p>
     * This value may only be changed between records. If it is changed
     * while processing a record the behaviour of this module is undefined.
     * <p>
     * <strong>Default value: {@value org.metafacture.framework.helpers.DefaultXmlPipe#DEFAULT_RECORD_TAG}</strong>
     *
     * @param recordTagName the tag name which marks the start of a record.
     */
    public void setRecordTagName(final String recordTagName) {
        this.recordTagName = recordTagName;
    }

    /**
     * Gets the record tag name.
     *
     * @return the record tag name.
     */
    public String getRecordTagName() {
        return recordTagName;
    }

    /**
     * Sets the value tag name.
     *
     * @param valueTagName the value tag name
     */
    public void setValueTagName(final String valueTagName) {
        this.valueTagName = valueTagName;
    }

    /**
     * Gets the value tag name.
     *
     * @return the value tag name
     */
    public String getValueTagName() {
        return valueTagName;
    }

    /**
     * Triggers namespace awareness. If set to "true" input data like "foo:bar"
     * will be passed through as "foo:bar". For backward compatibility the default
     * is set to "false", thus only "bar" is emitted.
     * <p>
     * <strong>Default value: {@value #EMIT_NAMESPACE}</strong>
     *
     * @param emitNamespace set to "true" if namespace should be emitted. Defaults
     *                      to "false".
     */
    public void setEmitNamespace(final boolean emitNamespace) {
        this.emitNamespace = emitNamespace;
    }

    /**
     * Checks whether the namespace should be emitted.
     *
     * @return true if the namespace should be emitted
     */
    public boolean getEmitNamespace() {
        return this.emitNamespace;
    }

    /**
     * Sets the attribute marker.
     *
     * @param attributeMarker the attribute marker
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
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        if (inRecord) {
            writeValue();
            if (emitNamespace) {
                getReceiver().startEntity(qName);
            }
            else {
                getReceiver().startEntity(localName);
            }
            writeAttributes(attributes);
        }
        else if (localName.equals(recordTagName)) {
            final String identifier = attributes.getValue("id");
            if (identifier == null) {
                getReceiver().startRecord("");
            }
            else {
                getReceiver().startRecord(identifier);
            }
            writeAttributes(attributes);
            inRecord = true;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        if (inRecord) {
            writeValue();
            if (localName.equals(recordTagName)) {
                inRecord = false;
                getReceiver().endRecord();
            }
            else {
                getReceiver().endEntity();
            }
        }
    }

    @Override
    public void characters(final char[] chars, final int start, final int length) {
        if (inRecord) {
            valueBuffer.append(TABS.matcher(new String(chars, start, length)).replaceAll(""));
        }
    }

    private void writeValue() {
        final String value = valueBuffer.toString();
        if (!value.trim().isEmpty()) {
            getReceiver().literal(valueTagName, value.replace('\n', ' '));
        }
        valueBuffer = new StringBuilder();
    }

    private void writeAttributes(final Attributes attributes) {
        final int length = attributes.getLength();

        for (int i = 0; i < length; ++i) {
            final String name = emitNamespace ? attributes.getQName(i) : attributes.getLocalName(i);
            final String value = attributes.getValue(i);
            getReceiver().literal(attributeMarker + name, value);
        }
    }

}
