/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.framework.helpers;

import java.io.IOException;

import org.culturegraph.mf.framework.XmlPipe;
import org.culturegraph.mf.framework.XmlReceiver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Default implementation of {@link XmlReceiver} which
 * simply does nothing. Do not use this class as a base class
 * for modules which are implement {@link XmlPipe}; use
 * {@link DefaultXmlPipe} for that.
 *
 * @see DefaultStreamPipe

 * @author Christoph Böhme <c.boehme@dnb.de>
 *
 */
public class DefaultXMLReceiver extends DefaultLifeCycle implements XmlReceiver {

	@Override
	public void setDocumentLocator(final Locator locator) {
		// Default implementation does nothing
	}

	@Override
	public void startDocument() throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void endDocument() throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri)
			throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes atts) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void characters(final char[] chars, final int start, final int length)
			throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void ignorableWhitespace(final char[] chars, final int start,
			final int length) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void processingInstruction(final String target, final String data)
			throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void notationDecl(final String name, final String publicId, final String systemId)
			throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void unparsedEntityDecl(final String name, final String publicId,
			final String systemId, final String notationName) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public InputSource resolveEntity(final String publicId, final String systemId)
			throws SAXException, IOException {
		return null;
	}

	@Override
	public void warning(final SAXParseException exception) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void error(final SAXParseException exception) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void fatalError(final SAXParseException exception) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId)
			throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void endDTD() throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void endEntity(final String name) throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void startCDATA() throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void endCDATA() throws SAXException {
		// Default implementation does nothing
	}

	@Override
	public void comment(final char[] chars, final int start, final int length)
			throws SAXException {
		// Default implementation does nothing
	}

}
