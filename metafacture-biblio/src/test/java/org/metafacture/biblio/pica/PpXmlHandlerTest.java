/*
 * Copyright 2014 Deutsche Nationalbibliothek
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

package org.metafacture.biblio.pica;

import org.metafacture.framework.StreamReceiver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link PpXmlHandler}.
 *
 * @author Tobias Bülte
 *
 */
public final class PpXmlHandlerTest {

    private static final String NAMESPACE = "http://www.oclcpica.org/xmlns/ppxml-1.0";
    private static final String RECORD = "record";
    private static final String DATAFIELD = "tag";
    private static final String SUBFIELD = "subf";

    private PpXmlHandler ppXmlHandler;

    @Mock
    private StreamReceiver receiver;

    public PpXmlHandlerTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ppXmlHandler = new PpXmlHandler();
        ppXmlHandler.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        ppXmlHandler.closeStream();
    }

    @Test
    public void shouldLabelDataFieldWithoutOccurrenceAttribute()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        final String fieldValue = "1234";

        ppXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        attributes.addAttribute(null, "id", "id", "CDATA", "003@");
        attributes.addAttribute(null, "occ", "occ", "CDATA", "");
        ppXmlHandler.startElement(null, DATAFIELD, "", attributes);
        attributes.clear();
        attributes.addAttribute(null, "id", "id", "CDATA", "0");
        ppXmlHandler.startElement(null, SUBFIELD, "", attributes);
        ppXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        ppXmlHandler.endElement(null, SUBFIELD, "");
        ppXmlHandler.endElement(null, DATAFIELD, "");
        ppXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered    = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).startEntity("003@");
        ordered.verify(receiver).literal("0", fieldValue);
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    public void shouldLabelDataFieldWithOneDigitOccurrenceAttribute()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        final String fieldValue = "utf-8";

        ppXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        attributes.addAttribute(null, "id", "id", "CDATA", "201U");
        attributes.addAttribute(null, "occ", "occ", "CDATA", "1");
        ppXmlHandler.startElement(null, DATAFIELD, "", attributes);
        attributes.clear();
        attributes.addAttribute(null, "id", "id", "CDATA", "0");
        ppXmlHandler.startElement(null, SUBFIELD, "", attributes);
        ppXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        ppXmlHandler.endElement(null, SUBFIELD, "");
        ppXmlHandler.endElement(null, DATAFIELD, "");
        ppXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered    = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).startEntity("201U/01");
        ordered.verify(receiver).literal("0", fieldValue);
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    public void shouldLabelDataFieldWithTwoDigitOccurrenceAttribute()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        final String fieldValue = "utf-8";

        ppXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        attributes.addAttribute(null, "id", "id", "CDATA", "201U");
        attributes.addAttribute(null, "occ", "occ", "CDATA", "01");
        ppXmlHandler.startElement(null, DATAFIELD, "", attributes);
        attributes.clear();
        attributes.addAttribute(null, "id", "id", "CDATA", "0");
        ppXmlHandler.startElement(null, SUBFIELD, "", attributes);
        ppXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        ppXmlHandler.endElement(null, SUBFIELD, "");
        ppXmlHandler.endElement(null, DATAFIELD, "");
        ppXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered    = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).startEntity("201U/01");
        ordered.verify(receiver).literal("0", fieldValue);
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

}
