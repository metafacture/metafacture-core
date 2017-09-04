package org.metafacture.xml;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.helpers.DefaultObjectReceiver;
import static org.junit.Assert.*;

public class MarcXmlEncoderTest
{

    private StringBuilder resultCollector;
    private MarcXmlEncoder encoder;

    @Before
    public void setUp() throws Exception
    {
        encoder = new MarcXmlEncoder();
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

    private void addOneRecord(MarcXmlEncoder encoder)
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
        assertTrue(actual.startsWith("<marc:collection"));
        assertTrue(actual.endsWith("</marc:collection>"));
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
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\"><marc:record></marc:record></marc:collection>";
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createARecord() throws Exception
    {
        addOneRecord(encoder);
        encoder.closeStream();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\"><marc:record><marc:controlfield tag=\"001\">92005291</marc:controlfield><marc:datafield tag=\"010\" ind1=\" \" ind2=\" \"><marc:subfield code=\"a\">92005291</marc:subfield></marc:datafield></marc:record></marc:collection>";
        String actual = resultCollector.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createTwoRecordsInOneCollection() throws Exception
    {
        addOneRecord(encoder);
        addOneRecord(encoder);
        encoder.closeStream();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\"><marc:record><marc:controlfield tag=\"001\">92005291</marc:controlfield><marc:datafield tag=\"010\" ind1=\" \" ind2=\" \"><marc:subfield code=\"a\">92005291</marc:subfield></marc:datafield></marc:record><marc:record><marc:controlfield tag=\"001\">92005291</marc:controlfield><marc:datafield tag=\"010\" ind1=\" \" ind2=\" \"><marc:subfield code=\"a\">92005291</marc:subfield></marc:datafield></marc:record></marc:collection>";
        String actual = resultCollector.toString();

        assertEquals(expected, actual);
    }
}