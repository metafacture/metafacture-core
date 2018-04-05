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


import org.metafacture.framework.XmlReceiver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * Passes SAX events to a xml receiver class.
 */
public class XmlContentHandler extends XMLFilterImpl
{

    private XmlReceiver receiver;

    public XmlContentHandler(XmlReceiver receiver)
    {
        this.receiver = receiver;
    }

    @Override
    public void startDocument() throws SAXException
    {
        receiver.startDocument();
    }

    @Override
    public void endDocument() throws SAXException
    {
        receiver.endDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException
    {
        receiver.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        receiver.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        receiver.characters(ch, start, length);
    }
}
