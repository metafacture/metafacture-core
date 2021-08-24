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

import java.util.regex.Pattern;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;


/**
 * A generic xml reader.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("A generic xml reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-generic-xml")
public final class GenericXmlHandler extends DefaultXmlPipe<StreamReceiver> {

    public static final String DEFAULT_RECORD_TAG = "record";

    private static final Pattern TABS = Pattern.compile("\t+");

    private String recordTagName = DEFAULT_RECORD_TAG;

    private boolean inRecord;
    private StringBuilder valueBuffer = new StringBuilder();

    private boolean emitNamespace = false;

    public GenericXmlHandler() {
        super();
        final String recordTagNameProperty = System.getProperty(
                "org.culturegraph.metamorph.xml.recordtag");
        if (recordTagNameProperty != null) {
           recordTagName = recordTagNameProperty;
        }
    }

    /**
     * Creates a new {@code GenericXmlReader} with the given tag name as
     * marker for records.
     *
     * @deprecated Use default constructor and set the tag name latter
     * with {@link #setRecordTagName(String)}.
     *
     * @param recordTagName tag name marking the start of a record.
     */
    @Deprecated
    public GenericXmlHandler(final String recordTagName) {
        super();
        this.recordTagName = recordTagName;
    }

    /**
     * Sets the tag name which marks the start of a record.
     * <p>
     * This value may only be changed between records. If it is changed
     * while processing a record the behaviour of this module is undefined.
     * <p>
     * <strong>Default value: {@value DEFAULT_RECORD_TAG}</strong>
     *
     * @param recordTagName the tag name which marks the start of a record.
     */
    public void setRecordTagName(String recordTagName) {
        this.recordTagName = recordTagName;
    }

    public String getRecordTagName() {
        return recordTagName;
    }

    /**
     * On entity level namespaces can be emitted, e.g.: "foo:bar". The default is to
     * ignore the namespace so that only "bar" is emitted.
     *
     * @param emitNamespace set to "true" if namespace should be emitted. Defaults
     *                      to "false".
     */
    public void setEmitNamespace(boolean emitNamespace) {
        this.emitNamespace = emitNamespace;
    }

    public boolean getEmitNamespace() {
        return this.emitNamespace;
    }

    @Override
    public void startElement(final String uri, final String localName,
            final String qName, final Attributes attributes) {

        if (inRecord) {
            writeValue();
            if (emitNamespace) {
                getReceiver().startEntity(qName);
            } else {
                getReceiver().startEntity(localName);
            }
            writeAttributes(attributes);
        } else if (localName.equals(recordTagName)) {
            final String identifier = attributes.getValue("id");
            if (identifier == null) {
                getReceiver().startRecord("");
            } else {
                getReceiver().startRecord(identifier);
            }
            writeAttributes(attributes);
            inRecord = true;
        }
    }

    @Override
    public void endElement(final String uri, final String localName,
            final String qName) {
        if (inRecord) {
            writeValue();
            if (localName.equals(recordTagName)) {
                inRecord = false;
                getReceiver().endRecord();
            } else {
                getReceiver().endEntity();
            }
        }
    }

    @Override
    public void characters(final char[] chars, final int start, final int length) {
        if (inRecord) {
            valueBuffer.append(TABS.matcher(new String(chars, start, length))
                    .replaceAll(""));
        }
    }

    private void writeValue() {
        final String value = valueBuffer.toString();
        if (!value.trim().isEmpty()) {
            getReceiver().literal("value", value.replace('\n', ' '));
        }
        valueBuffer = new StringBuilder();
    }

    private void writeAttributes(final Attributes attributes) {
        final int length = attributes.getLength();

        for (int i = 0; i < length; ++i) {
            String name;
            if (emitNamespace) {
                name = attributes.getQName(i);
            } else
                name = attributes.getLocalName(i);
            final String value = attributes.getValue(i);
            getReceiver().literal(name, value);
        }
    }

}
