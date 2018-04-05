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
package org.metafacture.xslt.adapter;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.XmlReceiver;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.helpers.AttributesImpl;
import static org.metafacture.xslt.mockito.SingleAttributeMatcher.hasSingleAttribute;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class XmlContentHandlerTest
{
    private XmlContentHandler adapter;

    @Mock
    private XmlReceiver receiver;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        adapter = new XmlContentHandler(receiver);
    }

    @Test
    public void emptyDocument() throws Exception
    {
        adapter.startDocument();
        adapter.endDocument();

        verify(receiver).startDocument();
        verify(receiver).endDocument();
    }

    @Test
    public void singleElement() throws Exception
    {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "id", "id", "ID", "1");
        adapter.startElement("", "elem", "elem", atts);
        adapter.characters("dummy".toCharArray(),0, 5);
        adapter.endElement("", "elem", "elem");

        verify(receiver).startElement(eq(""), eq("elem"), eq("elem"),
                argThat(hasSingleAttribute("", "id", "id", "ID", "1")));
        verify(receiver).characters("dummy".toCharArray(), 0, 5);
        verify(receiver).endElement("", "elem", "elem");
    }

    @Test
    public void singleDocumentWithElement() throws Exception
    {
        adapter.startDocument();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "id", "id", "ID", "1");
        adapter.startElement("", "elem", "elem", attrs);
        adapter.characters("dummy".toCharArray(),0, 5);
        adapter.endElement("", "elem", "elem");
        adapter.endDocument();

        verify(receiver).startDocument();
        verify(receiver).startElement(eq(""), eq("elem"), eq("elem"),
                argThat(hasSingleAttribute("", "id", "id", "ID", "1")));
        verify(receiver).characters("dummy".toCharArray(), 0, 5);
        verify(receiver).endElement("", "elem", "elem");
        verify(receiver).endDocument();
    }

}
