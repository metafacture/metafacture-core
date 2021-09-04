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

package org.metafacture.biblio.marc21;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A marc xml reader.
 * @author Markus Michael Geipel
 *
 */
@Description("A marc xml reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-marcxml")
public final class MarcXmlHandler extends DefaultXmlPipe<StreamReceiver> {

    private static final String SUBFIELD = "subfield";
    private static final String DATAFIELD = "datafield";
    private static final String CONTROLFIELD = "controlfield";
    private static final String RECORD = "record";
    private static final String NAMESPACE = "http://www.loc.gov/MARC21/slim";
    private static final String LEADER = "leader";
    private static final String TYPE = "type";
    private String currentTag = "";
    private String namespace = NAMESPACE;
    private StringBuilder builder = new StringBuilder();

    public MarcXmlHandler() {
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    private boolean checkNamespace(final String uri) {
        return namespace == null || namespace.equals(uri);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (SUBFIELD.equals(localName)) {
            builder = new StringBuilder();
            currentTag = attributes.getValue("code");
        }
        else if (DATAFIELD.equals(localName)) {
            getReceiver().startEntity(attributes.getValue("tag") + attributes.getValue("ind1") + attributes.getValue("ind2"));
        }
        else if (CONTROLFIELD.equals(localName)) {
            builder = new StringBuilder();
            currentTag = attributes.getValue("tag");
        }
        else if (RECORD.equals(localName) && checkNamespace(uri)) {
            getReceiver().startRecord("");
            getReceiver().literal(TYPE, attributes.getValue(TYPE));
        }
        else if (LEADER.equals(localName)) {
            builder = new StringBuilder();
            currentTag = LEADER;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (SUBFIELD.equals(localName)) {
            getReceiver().literal(currentTag, builder.toString().trim());

        }
        else if (DATAFIELD.equals(localName)) {
            getReceiver().endEntity();
        }
        else if (CONTROLFIELD.equals(localName)) {
            getReceiver().literal(currentTag, builder.toString().trim());

        }
        else if (RECORD.equals(localName) && checkNamespace(uri)) {
            getReceiver().endRecord();

        }
        else if (LEADER.equals(localName)) {
            getReceiver().literal(currentTag, builder.toString());
        }
    }

    @Override
    public void characters(final char[] chars, final int start, final int length) throws SAXException {
        builder.append(chars, start, length);
    }

}
