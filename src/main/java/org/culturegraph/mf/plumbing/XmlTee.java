/*
 * Copyright 2013 Pascal Christoph (hbz)
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
package org.culturegraph.mf.plumbing;

import java.io.IOException;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.XmlPipe;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultTee;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Sends one {@XmlReceiver} to two {@XmlReceiver}.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description("Sends an object to more than one receiver.")
@In(XmlReceiver.class)
@Out(XmlReceiver.class)
@FluxCommand("xml-tee")
public final class XmlTee extends DefaultTee<XmlReceiver>implements XmlPipe<XmlReceiver> {

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.characters(ch, start, length);
		}
	}

	@Override
	public void comment(final char[] ch, final int start, final int length) throws SAXException {
		// unused
	}

	@Override
	public void endCDATA() throws SAXException {
		// unused
	}

	@Override
	public void endDocument() throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.endDocument();
		}
	}

	@Override
	public void endDTD() throws SAXException {
		// unused
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.endElement(uri, localName, qName);
		}
	}

	@Override
	public void endEntity(final String name) throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.endEntity(name);
		}
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.endPrefixMapping(prefix);
		}
	}

	@Override
	public void error(final SAXParseException exception) throws SAXException {
		// unused
	}

	@Override
	public void fatalError(final SAXParseException exception) throws SAXException {
		// unused
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		// unused
	}

	@Override
	public void notationDecl(final String name, final String publicId, final String systemId)
			throws SAXException {
		// unused
	}

	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
		// unused
	}

	@Override
	public InputSource resolveEntity(final String publicId, final String systemId)
			throws SAXException, IOException {
		return null;
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		// unused
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		// unused
	}

	@Override
	public void startCDATA() throws SAXException {
		// unused
	}

	@Override
	public void startDocument() throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.startDocument();
		}
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId)
			throws SAXException {
		// unused
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes atts) throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.startElement(uri, localName, qName, atts);
		}
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.startEntity(name);
		}
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		for (final XmlReceiver receiver : getReceivers()) {
			receiver.startPrefixMapping(prefix, uri);
		}
	}

	@Override
	public void unparsedEntityDecl(final String name, final String publicId, final String systemId,
			final String notationName) throws SAXException {
		// unused
	}

	@Override
	public void warning(final SAXParseException exception) throws SAXException {
		// unused
	}

}
