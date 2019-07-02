/*
 * Copyright 2019 Pascal Christoph (hbz)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.helpers.DefaultObjectReceiver;

/**
 * Tests for class {@link MarcXmlEncoder}.
 *
 * @author some Jan (Eberhardt) did almost all
 * @author Pascal Christoph (dr0i) dug it up again
 *
 */

public class MarcXmlEncoderTest {
    private static StringBuilder resultCollector ;
    private static MarcXmlEncoder encoder ;
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String XML_1_DECLARATION = "<?xml version=\"1.1\" encoding=\"UTF-8\"?>";
    private static final String XML_16_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
    private static final String XML_ROOT_OPEN = "<marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21"
            + "/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">";
    private static final String XML_RECORD = "<marc:record><marc:controlfield tag=\"001\">92005291</marc:controlfield>"
            + "<marc:datafield tag=\"010\" ind1=\" \" ind2=\" \"><marc:subfield code=\"a\">92005291</marc:subfield>"
            + "</marc:datafield></marc:record>";
    private static final String XML_MARC_COLLECTION_END_TAG = "</marc:collection>";
    private static final String RECORD_ID = "92005291";
    
    @Before
    public void setUp() throws Exception {
        encoder = new MarcXmlEncoder();
        encoder.setFormatted(false);
        encoder.setReceiver(new DefaultObjectReceiver<String>() {
            @Override
            public void process(final String obj) {
                resultCollector.append(obj);
            }
        });
        resultCollector= new StringBuilder();
    }

    @After
    public void tearDown() throws Exception {
    }

    private void addOneRecord(MarcXmlEncoder encoder) {
        encoder.startRecord(RECORD_ID);
        encoder.literal("001", RECORD_ID);
        encoder.startEntity("010  ");
        encoder.literal("a", RECORD_ID);
        encoder.endEntity();
        encoder.endRecord();
    }

    @Test
    public void doNotOmitXmlDeclaration() throws Exception {
        encoder.omitXmlDeclaration(false);
        addOneRecord(encoder);
        encoder.closeStream();

        String actual = resultCollector.toString();
        assertTrue(actual.startsWith(XML_DECLARATION));
    }

    @Test
    public void omitXmlDeclaration() throws Exception {
        encoder.omitXmlDeclaration(true);
        addOneRecord(encoder);
        encoder.closeStream();
        String actual = resultCollector.toString();
        assertTrue(actual.startsWith("<marc:collection"));
        assertTrue(actual.endsWith(XML_MARC_COLLECTION_END_TAG));
    }

    @Test
    public void setXmlVersion() throws Exception {
        encoder.omitXmlDeclaration(false);
        encoder.setXmlVersion("1.1");
        addOneRecord(encoder);
        encoder.closeStream();

        String actual = resultCollector.toString();
        assertTrue(actual.startsWith(XML_1_DECLARATION));
    }

    @Test
    public void setXmlEncoding() throws Exception {
        encoder.omitXmlDeclaration(false);
        encoder.setXmlEncoding("UTF-16");
        addOneRecord(encoder);
        encoder.closeStream();

        String actual = resultCollector.toString();
        assertTrue(actual.startsWith(XML_16_DECLARATION));
    }

    @Test
    public void createAnEmptyRecord() throws Exception {
        encoder.startRecord("1");
        encoder.endRecord();
        encoder.closeStream();
        String expected = XML_DECLARATION + XML_ROOT_OPEN + "<marc:record></marc:record>"
               + XML_MARC_COLLECTION_END_TAG;
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createARecord() throws Exception {
        addOneRecord(encoder);
        encoder.closeStream();
        String expected = XML_DECLARATION + XML_ROOT_OPEN + XML_RECORD + "</marc:collection>";
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createTwoRecordsInOneCollection() throws Exception {
        addOneRecord(encoder);
        addOneRecord(encoder);
        encoder.closeStream();
        String expected = XML_DECLARATION + XML_ROOT_OPEN + XML_RECORD + XML_RECORD + "</marc:collection>";
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createAnRecordWithLeader() throws Exception {
        encoder.startRecord("1");
        encoder.startEntity("leader");
        encoder.literal("leader", "dummy");
        encoder.endEntity();
        encoder.endRecord();
        encoder.closeStream();
        String expected = XML_DECLARATION + XML_ROOT_OPEN + "<marc:record><marc:leader>dummy</marc:leader></marc:record>"
               + XML_MARC_COLLECTION_END_TAG;
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }
}
