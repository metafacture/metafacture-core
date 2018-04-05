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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.xslt.StreamToSax;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.metafacture.xslt.mockito.SingleAttributeMatcher.hasSingleAttribute;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class StreamToSaxTest
{
    private StreamToSax handler;

    @Mock
    private XmlReceiver receiver;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        handler = new StreamToSax();
        handler.setReceiver(receiver);
    }

    @After
    public void cleanUp()
    {
        handler.closeStream();
    }

    @Test
    public void convertLiteralToSax() throws Exception
    {
        handler.literal("name-1", "value-1");

        verify(receiver).startElement(eq(""), eq("literal"), eq("literal"),
                argThat(hasSingleAttribute("", "name", "name", "CDATA", "name-1")));

        String value = "value-1";
        verify(receiver).characters(value.toCharArray(), 0, value.length());

        verify(receiver).endElement("", "literal", "literal");
    }

    @Test
    public void convertEntityToSax() throws Exception
    {
        handler.startEntity("entity-1");
        handler.endEntity();

        verify(receiver).startElement(eq(""), eq("entity"), eq("entity"),
                argThat(hasSingleAttribute("", "name", "name", "CDATA", "entity-1")));
        verify(receiver).endElement("", "entity", "entity");
    }

    @Test
    public void convertRecordToSax() throws Exception
    {
        handler.startRecord("rec-1");
        handler.endRecord();

        verify(receiver).startDocument();
        verify(receiver).startElement(eq(""), eq("record"), eq("record"),
                argThat(hasSingleAttribute("", "id", "id", "ID", "rec-1")));
        verify(receiver).endElement("", "record", "record");
        verify(receiver).endDocument();
    }
}
