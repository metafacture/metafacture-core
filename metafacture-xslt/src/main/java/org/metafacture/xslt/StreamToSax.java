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
package org.metafacture.xslt;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Encodes a stream into XML events.
 */

@In(StreamReceiver.class)
@Out(XmlReceiver.class)
@Description("Encodes a stream as generic xml (sax event stream).")
@FluxCommand("stream-to-sax")
public class StreamToSax extends DefaultStreamPipe<XmlReceiver>
{
    @Override
    public void startRecord(final String identifier)
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "id", "id", "ID", identifier);

        try
        {
            getReceiver().startDocument();
            getReceiver().startElement("", "record", "record", attributes);
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void endRecord()
    {
        try
        {
            getReceiver().endElement("", "record", "record");
            getReceiver().endDocument();
        } catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void startEntity(final String name)
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "name", "name", "CDATA", name);
        try
        {
            getReceiver().startElement("", "entity", "entity", attributes);
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    public @Override void endEntity()
    {
        try
        {
            getReceiver().endElement("", "entity", "entity");
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void literal(final String name, final String value)
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "name", "name", "CDATA", name);

        try
        {
            getReceiver().startElement("", "literal", "literal", attributes);
            getReceiver().characters(value.toCharArray(),0, value.length());
            getReceiver().endElement("", "literal", "literal");
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }
}
