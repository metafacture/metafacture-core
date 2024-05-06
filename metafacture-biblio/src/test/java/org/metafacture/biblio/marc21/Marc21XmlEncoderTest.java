package org.metafacture.biblio.marc21;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.FormatException;
import org.metafacture.framework.MissingIdException;

public class Marc21XmlEncoderTest {
    MarcXmlEncoderTest marcXmlEncoderTest = new MarcXmlEncoderTest();

    @Before
    public void setUp() {
        marcXmlEncoderTest.encoder=new Marc21XmlEncoder();
        marcXmlEncoderTest.initializeEncoder();
    }

    @Test(expected = FormatException.class)
    public void createAnRecordWithLeader() {
        marcXmlEncoderTest.createAnRecordWithLeader();
    }

    @Test(expected = FormatException.class)
    public void issue336_createRecordWithTopLevelLeader() {
        marcXmlEncoderTest.issue336_createRecordWithTopLevelLeader();
    }

    @Test
    public void issue336_createRecordWithTopLevelLeader_Marc21Xml() {
        marcXmlEncoderTest.issue336_createRecordWithTopLevelLeader_Marc21Xml();
    }

    @Test(expected = MissingIdException.class)
    public void issue527ShouldEmitLeaderAlwaysAsWholeString() {
        marcXmlEncoderTest.issue527ShouldEmitLeaderAlwaysAsWholeString();
    }

}
