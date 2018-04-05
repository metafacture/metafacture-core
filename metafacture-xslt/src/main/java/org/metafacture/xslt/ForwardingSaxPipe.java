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

import java.io.IOException;


import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.xml.sax.*;

public class ForwardingSaxPipe extends DefaultXmlPipe<XmlReceiver>
{
    @Override
    public void setDocumentLocator(final Locator locator) {
        getReceiver().setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException
    {
        getReceiver().startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        getReceiver().endDocument();
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
        getReceiver().startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
        getReceiver().endPrefixMapping(prefix);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        getReceiver().startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        getReceiver().endElement(uri, localName, qName);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        getReceiver().characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        getReceiver().ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        getReceiver().processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
        getReceiver().skippedEntity(name);
    }

    @Override
    public void notationDecl(final String name, final String publicId, final String systemId)
            throws SAXException {
        getReceiver().notationDecl(name, publicId, systemId);
    }

    @Override
    public void unparsedEntityDecl(final String name, final String publicId,
                                   final String systemId, final String notationName) throws SAXException {
        getReceiver().unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException
    {
        getReceiver().resolveEntity(publicId, systemId);
        return null;
    }

    @Override
    public void warning(final SAXParseException exception) throws SAXException {
        getReceiver().warning(exception);
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        getReceiver().error(exception);
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        getReceiver().fatalError(exception);
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId)
            throws SAXException {
        getReceiver().startDTD(name, publicId, systemId);
    }

    @Override
    public void endDTD() throws SAXException {
        getReceiver().endDTD();
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        getReceiver().startEntity(name);
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        getReceiver().endEntity(name);
    }

    @Override
    public void startCDATA() throws SAXException {
        getReceiver().startCDATA();
    }

    @Override
    public void endCDATA() throws SAXException {
        getReceiver().endCDATA();
    }

    @Override
    public void comment(final char[] chars, final int start, final int length)
            throws SAXException {
        getReceiver().comment(chars, start, length);
    }
}
