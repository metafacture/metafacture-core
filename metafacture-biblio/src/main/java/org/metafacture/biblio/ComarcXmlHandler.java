/*
 * Copyright 2017 Deutsche Nationalbibliothek
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

package org.metafacture.biblio;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * A class handling ComarcXML. Comarc is the MARC version used by the National
 * Library of Slovenia. It does not have a leader, instead most (all?) the
 * meta-information is placed in field 000, particularly the record ID which is
 * in 000 $x.
 * <p>
 * This implementation works with the assumption that field 000 is
 * <em>always</em> the first field in the record and that this field has no
 * indicators. The consequence is that when the reader reads a record it does
 * not call receiver.startRecord until it has read the complete field 000 so
 * that it can initialise the record with the ID found in 000 $x.
 *
 * @author Lars Svensson
 */
@Description("A comarc xml reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-comarcxml")
public class ComarcXmlHandler extends DefaultXmlPipe<StreamReceiver> {

    private static final String SUBFIELD = "subfield";
    private static final String DATAFIELD = "datafield";
    private static final String RECORD = "record";
    private static final String NAMESPACE = "http://www.loc.gov/MARC21/slim";
    private static final int RECORD_NOT_INITIALISED = 0;
    private static final int RECORD_INITIALISED = 1;

    private final List<Entry> subfieldValues = new ArrayList<Entry>();

    private int state = RECORD_NOT_INITIALISED;
    private String currentTag = "";
    private StringBuilder builder = new StringBuilder();

    /**
     * Creates an instance of {@link ComarcXmlHandler}.
     */
    public ComarcXmlHandler() {
    }

    @Override
    public void startElement(final String uri, final String localName,
            final String qName, final Attributes attributes)
            throws SAXException {
        if (SUBFIELD.equals(localName)) {
            this.builder = new StringBuilder();
            this.currentTag = attributes.getValue("code");
        }
        else if (DATAFIELD.equals(localName) && this.state == RECORD_INITIALISED) {
            getReceiver().startEntity(
                    attributes.getValue("tag") +
                    attributes.getValue("ind1") +
                    attributes.getValue("ind2"));
        }
        else if (RECORD.equals(localName) && NAMESPACE.equals(uri)) {
            this.state = RECORD_NOT_INITIALISED;
        }
    }

    @Override
    public void endElement(final String uri, final String localName,
            final String qName) throws SAXException {
        if (SUBFIELD.equals(localName)) {
            this.subfieldValues.add(new Entry(this.currentTag, this.builder
                    .toString().trim()));
        }
        else if (DATAFIELD.equals(localName)) {
            switch (this.state) {
                case RECORD_NOT_INITIALISED:
                    super.getReceiver().startRecord(getFirstSubfield("x"));
                    super.getReceiver().startEntity("000  ");
                    this.state = RECORD_INITIALISED;
                    // fall through so that the entity 000 is properly ended
                case RECORD_INITIALISED:
                    for (final Entry entry : this.subfieldValues) {
                        super.getReceiver().literal(entry.getKey(), entry.getValue());
                    }
                    super.getReceiver().endEntity();
                    this.subfieldValues.clear();
                    break;
                default:
                    throw new SAXException(
                            "State was not one of initialised or not initialised");
            }
        }
        else if (RECORD.equals(localName) && NAMESPACE.equals(uri)) {
            getReceiver().endRecord();
        }
    }

    @Override
    public void characters(final char[] chars, final int start, final int length)
            throws SAXException {
        this.builder.append(chars, start, length);
    }

    private String getFirstSubfield(final String subfieldCode) {
        String ret = null;
        for (final Entry entry : this.subfieldValues) {
            if (subfieldCode.equals(entry.getKey())) {
                ret = entry.getValue();
                break;
            }
        }
        return ret;
    }

    private class Entry {
        private final String key;
        private final String value;

        Entry(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }
    }

}
