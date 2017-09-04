package org.metafacture.xml;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.helpers.DefaultObjectReceiver;
import static org.junit.Assert.*;


public class XmlEncoderTest
{

    private XmlEncoder encoder;

    private StringBuilder resultCollector;

    @Before
    public void setUp() throws Exception
    {
        encoder = new XmlEncoder();
        encoder.setPrettyPrinting(false);
        encoder.setReceiver(
                new DefaultObjectReceiver<String>() {
                    @Override
                    public void process(final String obj) {
                        resultCollector.append(obj);
                    }
                });
        resultCollector = new StringBuilder();
    }

    @After
    public void tearDown() throws Exception
    {
    }

    private void addOneRecord(XmlEncoder encoder)
    {
        encoder.startRecord("92005291");
        encoder.literal("001", "92005291");
        encoder.startEntity("010  ");
        encoder.literal("a", "92005291");
        encoder.endEntity();
        encoder.endRecord();
    }

    @Test
    public void doNotOmitXmlDeclaration() throws Exception
    {
        encoder.omitXmlDeclaration(false);
        addOneRecord(encoder);
        encoder.closeStream();

        String actual = resultCollector.toString();
        assertTrue(actual.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }

    @Test
    public void omitXmlDeclaration() throws Exception
    {
        encoder.omitXmlDeclaration(true);
        addOneRecord(encoder);
        encoder.closeStream();
        String actual = resultCollector.toString();
        assertTrue(actual.startsWith("<records"));
        assertTrue(actual.endsWith("</records>"));
    }

    @Test
    public void omitRootTag() throws Exception
    {
        encoder.omitXmlDeclaration(true);
        encoder.omitRootTag(true);
        addOneRecord(encoder);
        encoder.closeStream();
        String actual = resultCollector.toString();
        assertTrue(actual.startsWith("<record"));
        assertTrue(actual.endsWith("</record>"));
    }

    @Test
    public void setXmlVersion() throws Exception
    {
        encoder.omitXmlDeclaration(false);
        encoder.setXmlVersion("1.1");
        addOneRecord(encoder);
        encoder.closeStream();

        String actual = resultCollector.toString();
        assertTrue(actual.startsWith("<?xml version=\"1.1\" encoding=\"UTF-8\"?>"));
    }

    @Test
    public void setXmlEncoding() throws Exception
    {
        encoder.omitXmlDeclaration(false);
        encoder.setXmlEncoding("UTF-16");
        addOneRecord(encoder);
        encoder.closeStream();

        String actual = resultCollector.toString();
        assertTrue(actual.startsWith("<?xml version=\"1.0\" encoding=\"UTF-16\"?>"));
    }

    @Test
    public void createAnEmptyRecord() throws Exception
    {
        encoder.startRecord("1");
        encoder.endRecord();
        encoder.closeStream();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><records><record id=\"1\"></record></records>";
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createARecord() throws Exception
    {
        addOneRecord(encoder);
        encoder.closeStream();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><records><record id=\"92005291\"><literal name=\"001\">92005291</literal><entity name=\"010  \"><literal name=\"a\">92005291</literal></entity></record></records>";
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createTwoRecordsInOneCollection() throws Exception
    {
        addOneRecord(encoder);
        addOneRecord(encoder);
        encoder.closeStream();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><records><record id=\"92005291\"><literal name=\"001\">92005291</literal><entity name=\"010  \"><literal name=\"a\">92005291</literal></entity></record><record id=\"92005291\"><literal name=\"001\">92005291</literal><entity name=\"010  \"><literal name=\"a\">92005291</literal></entity></record></records>";
        String actual = resultCollector.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void oneRecordPerCollection() throws Exception
    {
        encoder.emitOneRecordPerCollection(true);
        encoder.omitXmlDeclaration(true);
        addOneRecord(encoder);
        addOneRecord(encoder);
        encoder.closeStream();

        String expected = "<records><record id=\"92005291\"><literal name=\"001\">92005291</literal><entity name=\"010  \"><literal name=\"a\">92005291</literal></entity></record></records><records><record id=\"92005291\"><literal name=\"001\">92005291</literal><entity name=\"010  \"><literal name=\"a\">92005291</literal></entity></record></records>";
        String actual = resultCollector.toString();

        assertEquals(expected, actual);
    }
}