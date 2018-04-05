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

import java.util.StringJoiner;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

@In(XmlReceiver.class)
@Out(XmlReceiver.class)
@Description("Logs a stream of SAX events.")
@FluxCommand("log-sax")
public class SaxEventLogger extends DefaultXmlPipe<XmlReceiver>
{
    private static final Logger LOG = LoggerFactory.getLogger(SaxEventLogger.class);

    private String prefix;

    public SaxEventLogger()
    {
        this("");
    }

    public SaxEventLogger(final String prefix)
    {
        super();
        this.prefix = prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public void startDocument()
    {
        LOG.debug("{} <!-- Document Start -->", prefix);
        if (null != getReceiver()) {
            try
            {
                getReceiver().startDocument();
            } catch (SAXException e)
            {
                throw new MetafactureException(e);
            }
        }
    }

    @Override
    public void endDocument()
    {
        LOG.debug("{} <!-- Document End -->", prefix);
        if (null != getReceiver()) {
            try
            {
                getReceiver().endDocument();
            } catch (SAXException e)
            {
                throw new MetafactureException(e);
            }
        }
    }

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes)
    {
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 0; i < attributes.getLength(); ++i) {
            final String name = attributes.getLocalName(i);
            final String value = attributes.getValue(i);
            joiner.add(name + "=" + "\"" + value + "\"");
        }

        LOG.debug("{} <{} {}>", prefix, localName, joiner.toString());

        if (null != getReceiver()) {
            try
            {
                getReceiver().startElement(uri, localName, qName, attributes);
            } catch (SAXException e)
            {
                throw new MetafactureException(e);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        LOG.debug("{} </{}>", prefix, localName);
        if (null != getReceiver()) {
            try
            {
                getReceiver().endElement(uri, localName, qName);
            } catch (SAXException e)
            {
                throw new MetafactureException(e);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
        LOG.debug("{} {}", prefix, new String(ch));
        if (null != getReceiver()) {
            try
            {
                getReceiver().characters(ch, start, length);
            } catch (SAXException e)
            {
                throw new MetafactureException(e);
            }
        }
    }
}
