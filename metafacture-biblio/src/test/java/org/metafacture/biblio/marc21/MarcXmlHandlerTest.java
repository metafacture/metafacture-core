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

package org.metafacture.biblio.marc21;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link MarcXmlHandler}.
 *
 * @author Christoph Böhme
 *
 */
public final class MarcXmlHandlerTest {

    private static final String LEADER = "leader";
    private static final String CONTROLFIELD = "controlfield";
    private static final String NAMESPACE = "http://www.loc.gov/MARC21/slim";
    private static final String RECORD = "record";
    private static final String TYPE = "type";

    private MarcXmlHandler marcXmlHandler;

    @Mock
    private StreamReceiver receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        marcXmlHandler = new MarcXmlHandler();
        marcXmlHandler.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        marcXmlHandler.closeStream();
    }

    @Test
    public void shouldFindTagAttributeAtSecondPositionInControlFieldElement()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(NAMESPACE, "id", "id", "CDATA", "id-1");
        attributes.addAttribute(NAMESPACE, "tag", "tag", "CDATA", "001");

        final String fieldValue = "1234";

        marcXmlHandler.startElement(NAMESPACE, CONTROLFIELD, "", attributes);
        marcXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        marcXmlHandler.endElement(NAMESPACE, CONTROLFIELD, "");

        verify(receiver).literal("001", fieldValue);
    }

    @Test
    public void issue440_shouldNotRemoveWhitespaceFromControlFields() throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(NAMESPACE, "tag", "tag", "CDATA", "008");

        final String fieldValue = "      t20202020au |||||||||||| ||||ger d";

        marcXmlHandler.startElement(NAMESPACE, CONTROLFIELD, "", attributes);
        marcXmlHandler.characters(fieldValue.toCharArray(), 0, fieldValue.length());
        marcXmlHandler.endElement(NAMESPACE, CONTROLFIELD, "");

        verify(receiver).literal("008", fieldValue);
    }

    @Test
    public void issue233ShouldNotRemoveWhitespaceFromLeader()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        final String leaderValue = "  cdefghijklmnopqrstuv  ";

        marcXmlHandler.startElement(NAMESPACE, LEADER, "", attributes);
        marcXmlHandler.characters(leaderValue.toCharArray(), 0, leaderValue.length());
        marcXmlHandler.endElement(NAMESPACE, LEADER, "");

        verify(receiver).literal("leader", leaderValue);
    }

    @Test
    public void shouldRecognizeRecordsWithNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        marcXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        marcXmlHandler.endElement(NAMESPACE, RECORD, "");

        verify(receiver).startRecord("");
        verify(receiver).literal(TYPE, null);
        verify(receiver).endRecord();

        verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldNotRecognizeRecordsWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        marcXmlHandler.startElement(null, RECORD, "", attributes);
        marcXmlHandler.endElement(null, RECORD, "");

        verifyNoMoreInteractions(receiver);
    }

    @Test
    public void issue330ShouldOptionallyRecognizeRecordsWithoutNamespace()
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        marcXmlHandler.setNamespace(null);
        marcXmlHandler.startElement(null, RECORD, "", attributes);
        marcXmlHandler.endElement(null, RECORD, "");

        verify(receiver).startRecord("");
        verify(receiver).literal(TYPE, null);
        verify(receiver).endRecord();

        verifyNoMoreInteractions(receiver);
    }

    @Test
    public void shouldNotEncodeTypeAttributeAsMarkedLiteral() throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(NAMESPACE, "type", "type", "CDATA", "bibliographic");

        marcXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        marcXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).literal(TYPE, "bibliographic");
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
        verifyNoMoreInteractions(receiver);
    }

    @Test
    public void issue336_shouldEncodeTypeAttributeAsLiteralWithConfiguredMarker() throws SAXException {
        final String marker = "~";
        marcXmlHandler.setAttributeMarker(marker);

        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(NAMESPACE, "type", "type", "CDATA", "bibliographic");

        marcXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);
        marcXmlHandler.endElement(NAMESPACE, RECORD, "");

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("");
        ordered.verify(receiver).literal(marker + TYPE, "bibliographic");
        ordered.verify(receiver).endRecord();
        ordered.verifyNoMoreInteractions();
        verifyNoMoreInteractions(receiver);
    }

}
