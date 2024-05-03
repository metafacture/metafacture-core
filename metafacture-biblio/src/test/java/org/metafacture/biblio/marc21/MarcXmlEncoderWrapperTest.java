package org.metafacture.biblio.marc21;

import org.junit.Test;

public class MarcXmlEncoderWrapperTest {
    @Test
    public void testMarcXmlEncoder() {
        MarcXmlEncoderTest test = new MarcXmlEncoderTest(new MarcXmlEncoder());
        test.createAnRecordWithLeader();
        MarcXmlEncoderTest test1 = new MarcXmlEncoderTest(new Marc21XmlEncoder());
        test1.createAnRecordWithLeader();
    }
}
