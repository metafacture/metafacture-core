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
 * Tests for class {@link PicaXmlHandler}.
 *
 * @author Tobias Bülte
 *
 */
public final class PicaXmlHandlerTest {

    private static final String NAMESPACE = "info:srw/schema/5/picaXML-v1.0";
    private static final String RECORD = "record";
    private static final String DATAFIELD = "datafield";
    private static final String SUBFIELD = "subfield";

    private PicaXmlHandler picaXmlHandler;

    @Mock
    private StreamReceiver receiver;

    public PicaXmlHandlerTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        picaXmlHandler = new PicaXmlHandler();
        picaXmlHandler.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        picaXmlHandler.closeStream();
    }

    @Test
    public void shouldLabelDataFieldWithoutOccurrenceAttribute()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        final String fieldValue = "1234";

        picaXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        attributes.addAttribute(null, "tag", "tag", "CDATA", "003@");
        picaXmlHandler.startElement(null, DATAFIELD, "", attributes);
        attributes.clear();
        attributes.addAttribute(null, "code", "code", "CDATA", "0");
        picaXmlHandler.startElement(null, SUBFIELD, "", attributes);
        picaXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        picaXmlHandler.endElement(null, SUBFIELD, "");
        picaXmlHandler.endElement(null, DATAFIELD, "");
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered    = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).startEntity("003@");
        ordered.verify(receiver).literal("0", fieldValue);
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    public void shouldLabelDataFieldWithOccurrenceAttribute()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        final String fieldValue = "utf-8";

        picaXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        attributes.addAttribute(null, "tag", "tag", "CDATA", "201U");
        attributes.addAttribute(null, "occurrence", "occurrence", "CDATA", "01");
        picaXmlHandler.startElement(null, DATAFIELD, "", attributes);
        attributes.clear();
        attributes.addAttribute(null, "code", "code", "CDATA", "0");
        picaXmlHandler.startElement(null, SUBFIELD, "", attributes);
        picaXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        picaXmlHandler.endElement(null, SUBFIELD, "");
        picaXmlHandler.endElement(null, DATAFIELD, "");
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered    = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).startEntity("201U/01");
        ordered.verify(receiver).literal("0", fieldValue);
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRecognizeRecordsWithNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        picaXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        Mockito.verify(receiver).startRecord("");
        Mockito.verify(receiver).endRecord();

        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldNotRecognizeRecordsWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        picaXmlHandler.startElement(null, RECORD, "", attributes);
        picaXmlHandler.endElement(null, RECORD, "");

        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldRecognizeRecordsWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        picaXmlHandler.setNamespace("");
        picaXmlHandler.startElement("", RECORD, "", attributes);
        picaXmlHandler.endElement("", RECORD, "");

        Mockito.verify(receiver).startRecord("");
        Mockito.verify(receiver).endRecord();

        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldNotRecognizeRecordsWithNamespaceWhenOptionallyWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        picaXmlHandler.setNamespace("");
        picaXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void issue569ShouldRecognizeRecordsWithAndWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        picaXmlHandler.setIgnoreNamespace(true);
        picaXmlHandler.startElement(null, RECORD, "", attributes);
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        Mockito.verify(receiver).startRecord("");
        Mockito.verify(receiver).endRecord();

        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void issue569ShouldRecognizeRecordsWithAndWithoutNamespaceOrderIndependently()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        final String fieldValue1 = "1234";

        picaXmlHandler.setIgnoreNamespace(true);
        picaXmlHandler.setNamespace("");
        picaXmlHandler.startElement(null, RECORD, "", attributes);
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        Mockito.verify(receiver).startRecord("");
        Mockito.verify(receiver).endRecord();

        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void issue569ShouldNotRecognizeRecordsWithAndWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        picaXmlHandler.setIgnoreNamespace(false);
        picaXmlHandler.startElement(null, RECORD, "", attributes);
        picaXmlHandler.endElement(NAMESPACE, RECORD, "");

        Mockito.verify(receiver).endRecord();

        Mockito.verifyNoMoreInteractions(receiver);
    }

}
