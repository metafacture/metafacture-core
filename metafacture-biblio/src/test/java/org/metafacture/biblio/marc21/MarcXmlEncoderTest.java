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

import org.metafacture.framework.FormatException;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.MissingIdException;
import org.metafacture.framework.helpers.DefaultObjectReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link MarcXmlEncoder}.
 *
 * @author some Jan (Eberhardt) did almost all
 * @author Pascal Christoph (dr0i) dug it up again
 *
 */
public class MarcXmlEncoderTest {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String XML_1_DECLARATION = "<?xml version=\"1.1\" encoding=\"UTF-8\"?>";
    private static final String XML_16_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
    private static final String XML_ROOT_OPEN = "<marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21" +
            "/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">";
    private static final String XML_RECORD = "<marc:record><marc:controlfield tag=\"001\">92005291</marc:controlfield>" +
            "<marc:datafield tag=\"010\" ind1=\" \" ind2=\" \"><marc:subfield code=\"a\">92005291</marc:subfield>" +
            "</marc:datafield></marc:record>";
    private static final String XML_MARC_COLLECTION_END_TAG = "</marc:collection>";
    private static final String RECORD_ID = "92005291";

    private static StringBuilder resultCollector;
    private static int resultCollectorsResetStreamCount;
    private static MarcXmlEncoder encoder;

    public MarcXmlEncoderTest() {
    }

    @Before
    public void setUp() {
        encoder = new MarcXmlEncoder();
        encoder.setFormatted(false);
        encoder.setReceiver(new DefaultObjectReceiver<String>() {
            @Override
            public void process(final String obj) {
                resultCollector.append(obj);
            }

            @Override
            public void resetStream() {
                ++resultCollectorsResetStreamCount;
            }
        });
        resultCollector = new StringBuilder();
    }

    private void addOneRecord() {
        encoder.startRecord(RECORD_ID);
        encoder.literal("001", RECORD_ID);
        encoder.startEntity("010  ");
        encoder.literal("a", RECORD_ID);
        encoder.endEntity();
        encoder.endRecord();
    }

    @Test
    public void doNotOmitXmlDeclaration() {
        encoder.omitXmlDeclaration(false);
        addOneRecord();
        encoder.closeStream();

        final String actual = resultCollector.toString();
        Assert.assertTrue(actual.startsWith(XML_DECLARATION));
    }

    @Test
    public void omitXmlDeclaration() {
        encoder.omitXmlDeclaration(true);
        addOneRecord();
        encoder.closeStream();
        final String actual = resultCollector.toString();
        Assert.assertTrue(actual.startsWith("<marc:collection"));
        Assert.assertTrue(actual.endsWith(XML_MARC_COLLECTION_END_TAG));
    }

    @Test
    public void setXmlVersion() {
        encoder.omitXmlDeclaration(false);
        encoder.setXmlVersion("1.1");
        addOneRecord();
        encoder.closeStream();

        final String actual = resultCollector.toString();
        Assert.assertTrue(actual.startsWith(XML_1_DECLARATION));
    }

    @Test
    public void setXmlEncoding() {
        encoder.omitXmlDeclaration(false);
        encoder.setXmlEncoding("UTF-16");
        addOneRecord();
        encoder.closeStream();

        final String actual = resultCollector.toString();
        Assert.assertTrue(actual.startsWith(XML_16_DECLARATION));
    }

    @Test
    public void createAnEmptyRecord() {
        encoder.startRecord(RECORD_ID);
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN + "<marc:record></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createARecordPrettyPrint() {
        encoder.setFormatted(true);
        encoder.startRecord(RECORD_ID);
        encoder.startEntity(Marc21EventNames.LEADER_ENTITY);
        encoder.literal(Marc21EventNames.LEADER_ENTITY, "dummy");
        encoder.endEntity();
        encoder.literal("001", RECORD_ID);
        encoder.startEntity("010  ");
        encoder.literal("a", RECORD_ID);
        encoder.endEntity();
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + "\n" + XML_ROOT_OPEN + "\n" +
                "\t<marc:record>\n" +
                "\t\t<marc:leader>dummy</marc:leader>\n" +
                "\t\t<marc:controlfield tag=\"001\">92005291</marc:controlfield>\n" +
                "\t\t<marc:datafield tag=\"010\" ind1=\" \" ind2=\" \">\n" +
                "\t\t\t<marc:subfield code=\"a\">92005291</marc:subfield>\n" +
                "\t\t</marc:datafield>\n" +
                "\t</marc:record>\n" +
                XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createARecordWithEscapedSequences() {
        encoder.startRecord(RECORD_ID);
        encoder.literal("001", "&<>\"");
        encoder.endRecord();
        encoder.onResetStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN + "<marc:record>" +
                "<marc:controlfield tag=\"001\">&amp;&lt;&gt;&quot;</marc:controlfield>" + "</marc:record>" +
                XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createTwoRecordsInOneCollection() {
        addOneRecord();
        addOneRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN + XML_RECORD + XML_RECORD + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void issue403_shouldNotEmitNamespaceIfDisabled() {
        encoder.setEmitNamespace(false);
        addOneRecord();
        addOneRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + "<collection xmlns=\"http://www.loc.gov/MARC21/slim\">" +
            XML_RECORD + XML_RECORD + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected.replace("marc:", ""), actual);
    }

    @Test(expected = MetafactureException.class)
    public void emitExceptionWhenEntityLengthNot5() {
        encoder.startRecord(RECORD_ID);
        encoder.startEntity("123456");
    }

    private void createRecordWithLeader(final String id, final String... leader) {
        encoder.startRecord(id);
        encoder.startEntity(Marc21EventNames.LEADER_ENTITY);
        encoder.literal(Marc21EventNames.RECORD_STATUS_LITERAL, leader[0]);
        encoder.literal(Marc21EventNames.RECORD_TYPE_LITERAL, leader[1]);
        encoder.literal(Marc21EventNames.BIBLIOGRAPHIC_LEVEL_LITERAL, leader[2]);
        encoder.literal(Marc21EventNames.TYPE_OF_CONTROL_LITERAL, leader[3]);
        encoder.literal(Marc21EventNames.CHARACTER_CODING_LITERAL, leader[4]);
        encoder.literal(Marc21EventNames.ENCODING_LEVEL_LITERAL, leader[5]);
        encoder.literal(Marc21EventNames.CATALOGING_FORM_LITERAL, leader[6]);
        encoder.literal(Marc21EventNames.MULTIPART_LEVEL_LITERAL, leader[7]);
        encoder.endEntity();
        encoder.endRecord();
    }

    @Test
    public void createRecordWithLeader() {
        encoder.startRecord("1");
        encoder.startEntity(Marc21EventNames.LEADER_ENTITY);
        encoder.literal(Marc21EventNames.LEADER_ENTITY, "dummy");
        encoder.endEntity();
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record><marc:leader>dummy</marc:leader></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = FormatException.class)
    public void createRecordWithLeaderEnsureCorrectMarc21Xml() {
        encoder.setEnsureCorrectMarc21Xml(true);
        createRecordWithLeader();
    }

    @Test
    public void issue336_createRecordWithTopLevelLeaderDummy() {
        encoder.startRecord("1");
        encoder.literal(Marc21EventNames.LEADER_ENTITY, "dummy");
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record><marc:leader>dummy</marc:leader></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void issue548_createRecordWithTypeAttributeInRecordTagAndLeader() {
        encoder.startRecord(RECORD_ID);
        encoder.literal("type", "Bibliographic");
        encoder.startEntity(Marc21EventNames.LEADER_ENTITY);
        encoder.literal(Marc21EventNames.LEADER_ENTITY, "dummy");
        encoder.endEntity();
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +  "<marc:record type=\"Bibliographic\">" +
            "<marc:leader>dummy</marc:leader></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void issue336_createRecordWithTopLevelLeaderDefaultMarc21Xml() {
        issue336_createRecordWithTopLevelLeader("00000naa a2200000uc 4500");
    }

    @Test
    public void issue336_createRecordWithTopLevelLeaderEnsureCorrectMarc21Xml() {
        encoder.setEnsureCorrectMarc21Xml(true);
        issue336_createRecordWithTopLevelLeader("00048naa a2200037uc 4500");
    }

    private void issue336_createRecordWithTopLevelLeader(final String expectedLeader) {
        encoder.startRecord("1");
        encoder.literal("001", "8u3287432");
        encoder.literal(Marc21EventNames.LEADER_ENTITY, "00000naa a2200000uc 4500");
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
            "<marc:record><marc:leader>" + expectedLeader + "</marc:leader>" +
            "<marc:controlfield tag=\"001\">8u3287432</marc:controlfield></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void issue548_failWhenLeaderIsNotFirst() {
        encoder.startRecord("1");
        encoder.literal("001", "8u3287432");
        encoder.literal(Marc21EventNames.LEADER_ENTITY, "00000naa a2200000uc 4500");
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
            "<marc:record><marc:controlfield tag=\"001\">8u3287432</marc:controlfield>" +
            "<marc:leader>00000naa a2200000uc 4500</marc:leader></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void issue527_shouldEmitLeaderAlwaysAsWholeString() {
        createRecordWithLeader("1", "a", "o", "a", " ", "a", "z", "u", " ");
        createRecordWithLeader("2", "d", "u", "m", " ", "m", "y", "#", " ");
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record><marc:leader>aoa azu </marc:leader></marc:record>" +
                "<marc:record><marc:leader>dum my# </marc:leader></marc:record>" + XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = MissingIdException.class)
    public void issue527_shouldEmitLeaderAlwaysAsWholeStringEnsureCorrectMarc21Xml() {
        encoder.setEnsureCorrectMarc21Xml(true);
        issue527_shouldEmitLeaderAlwaysAsWholeString();
    }

    @Test
    public void sendDataAndClearWhenRecordStartedAndStreamResets() {
        encoder.startRecord("1");
        encoder.onResetStream();
        encoder.endRecord();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN + "<marc:record>" + XML_MARC_COLLECTION_END_TAG +
                "</marc:record>";
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void sendAndClearDataWhenOnResetStream() {
        encoder.onResetStream();
        final String expected = "";
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldIgnoreNullValueOfLiteral() {
        encoder.startRecord(RECORD_ID);
        encoder.literal("data", null);
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record><marc:controlfield tag=\"data\"></marc:controlfield></marc:record>" +
                XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldIgnoreNullValueOfTypeLiteral() {
        encoder.startRecord(RECORD_ID);
        encoder.literal("type", null);
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record></marc:record>" +
                XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void issue402_shouldEncodeTypeLiteralAsAttribute() {
        encoder.startRecord(RECORD_ID);
        encoder.literal("type", "value");
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record type=\"value\"></marc:record>" +
                XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotEncodeNestedTypeLiteralAsAttribute() {
        encoder.startRecord(RECORD_ID);
        encoder.startEntity("tag12");
        encoder.literal("type", "value");
        encoder.endEntity();
        encoder.endRecord();
        encoder.closeStream();
        final String expected = XML_DECLARATION + XML_ROOT_OPEN +
                "<marc:record>" +
                "<marc:datafield tag=\"tag\" ind1=\"1\" ind2=\"2\">" +
                "<marc:subfield code=\"type\">value</marc:subfield>" +
                "</marc:datafield>" +
                "</marc:record>" +
                XML_MARC_COLLECTION_END_TAG;
        final String actual = resultCollector.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void issue543_shouldNotWriteFooterWhenRecordIsEmpty() {
        encoder.closeStream();
        final String actual = resultCollector.toString();
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void issue543_shouldOnlyResetStreamOnce() {
        resultCollectorsResetStreamCount = 0;
        encoder.resetStream();
        Assert.assertEquals(resultCollectorsResetStreamCount, 1);
    }

    @Test
    public void issue543_shouldOnlyResetStreamOnceUsingWrapper() {
        resultCollectorsResetStreamCount = 0;
        encoder.setEnsureCorrectMarc21Xml(true);
        encoder.resetStream();
        Assert.assertEquals(resultCollectorsResetStreamCount, 1);
    }

}
