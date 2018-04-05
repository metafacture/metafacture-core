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
import org.metafacture.framework.StreamReceiver;
import org.metafacture.xml.GenericXmlHandler;
import org.metafacture.xslt.ApplyXslt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.helpers.AttributesImpl;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ApplyXsltTest
{
    private String stylesheetId = "src/test/resources/identity.xsl";

    private ApplyXslt xsltTransformer;

    @Mock
    private StreamReceiver receiver;

    @Before
    public void setUp() throws Exception
    {
        System.setProperty("org.culturegraph.metamorph.xml.recordtag", "record");
        MockitoAnnotations.initMocks(this);
        xsltTransformer = new ApplyXslt(stylesheetId);
        xsltTransformer.setReceiver(new GenericXmlHandler()).setReceiver(receiver);
    }

    @Test
    public void identityTransformation() throws Exception
    {
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

        verify(receiver).startRecord("record-1");
        verify(receiver).startEntity("entity");
        verify(receiver).literal("name", "entity-1");
        verify(receiver).startEntity("literal");
        verify(receiver).literal("name", "name-1");
        verify(receiver).literal("value", "value-1");
        verify(receiver, times(2)).endEntity();
        verify(receiver).endRecord();
    }
}
