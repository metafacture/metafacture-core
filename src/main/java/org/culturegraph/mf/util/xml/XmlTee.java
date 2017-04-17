/* Copyright 2013 hbz, Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0 */
package org.culturegraph.mf.util.xml;

import java.io.IOException;

import org.culturegraph.mf.framework.DefaultTee;
import org.culturegraph.mf.framework.XmlPipe;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
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
public final class XmlTee extends DefaultTee<XmlReceiver>implements XmlPipe<XmlReceiver> {

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.characters(ch, start, length);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.endDocument();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.endElement(uri, localName, qName);
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.endPrefixMapping(prefix);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// unused
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		// unused
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// unused
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// unused
	}

	@Override
	public void startDocument() throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.startDocument();
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.startElement(uri, localName, qName, atts);
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.startPrefixMapping(prefix, uri);
		}
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		// unused
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
			throws SAXException {
		// unused
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		return null;
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		// unused
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		// unused
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		// unused
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		// unused
	}

	@Override
	public void endCDATA() throws SAXException {
		// unused
	}

	@Override
	public void endDTD() throws SAXException {
		// unused
	}

	@Override
	public void endEntity(String name) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.endEntity(name);
		}
	}

	@Override
	public void startCDATA() throws SAXException {
		// unused
	}

	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		// unused
	}

	@Override
	public void startEntity(String name) throws SAXException {
		for (XmlReceiver receiver : getReceivers()) {
			receiver.startEntity(name);
		}
	}

}
