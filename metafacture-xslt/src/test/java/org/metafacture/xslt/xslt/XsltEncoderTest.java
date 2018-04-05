/*
 * Copyright 2018 Deutsche Nationalbibliothek
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
package org.metafacture.xslt.xslt;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.xslt.XsltEncoder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.helpers.AttributesImpl;
import static org.mockito.Mockito.verify;


public class XsltEncoderTest
{
    private XsltEncoder xsltTransformer;

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void identityTransformation() throws Exception
    {
        String stylesheetId = "src/test/resources/identity.xsl";
        xsltTransformer = new XsltEncoder(stylesheetId);
        xsltTransformer.setReceiver(receiver);

        // Document start
        xsltTransformer.startDocument();
        // <record id="record-1">
        AttributesImpl recordAtts = new AttributesImpl();
        recordAtts.addAttribute("", "id", "id", "ID", "record-1");
        xsltTransformer.startElement("", "record", "record", recordAtts);
        // <entity name="entity-1"
        AttributesImpl entityAtts = new AttributesImpl();
        entityAtts.addAttribute("", "name", "name", "CDATA", "entity-1");
        xsltTransformer.startElement("", "entity", "entity", entityAtts);
        // <literal name="name-1">
        AttributesImpl literalAtts = new AttributesImpl();
        literalAtts.addAttribute("", "name", "name", "CDATA", "name-1");
        xsltTransformer.startElement("", "literal", "literal", literalAtts);
        // value-1
        xsltTransformer.characters("value-1".toCharArray(), 0, 7);
        // </literal>
        xsltTransformer.endElement("", "literal", "literal");
        // </entity>
        xsltTransformer.endElement("", "entity", "entity");
        // </record>
        xsltTransformer.endElement("", "record", "record");
        // Document end
        xsltTransformer.endDocument();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record id=\"record-1\"><entity name=\"entity-1\"><literal name=\"name-1\">value-1</literal></entity></record>";

        verify(receiver).process(expected);
    }

    @Test
    public void identityTransformationWithoutDeclaration() throws Exception
    {
        String stylesheetId = "src/test/resources/identityWithoutDeclaration.xsl";
        xsltTransformer = new XsltEncoder(stylesheetId);
        xsltTransformer.setReceiver(receiver);

        // Document start
        xsltTransformer.startDocument();
        // <record id="record-1">
        AttributesImpl recordAtts = new AttributesImpl();
        recordAtts.addAttribute("", "id", "id", "ID", "record-1");
        xsltTransformer.startElement("", "record", "record", recordAtts);
        // <entity name="entity-1"
        AttributesImpl entityAtts = new AttributesImpl();
        entityAtts.addAttribute("", "name", "name", "CDATA", "entity-1");
        xsltTransformer.startElement("", "entity", "entity", entityAtts);
        // <literal name="name-1">
        AttributesImpl literalAtts = new AttributesImpl();
        literalAtts.addAttribute("", "name", "name", "CDATA", "name-1");
        xsltTransformer.startElement("", "literal", "literal", literalAtts);
        // value-1
        xsltTransformer.characters("value-1".toCharArray(), 0, 7);
        // </literal>
        xsltTransformer.endElement("", "literal", "literal");
        // </entity>
        xsltTransformer.endElement("", "entity", "entity");
        // </record>
        xsltTransformer.endElement("", "record", "record");
        // Document end
        xsltTransformer.endDocument();

        String expected = "<record id=\"record-1\"><entity name=\"entity-1\"><literal name=\"name-1\">value-1</literal></entity></record>";

        verify(receiver).process(expected);
    }
}
