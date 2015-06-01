package org.culturegraph.mf.stream.converter.xml;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author tgaengler
 */
public class XmlFilterEntityImpl extends XMLFilterImpl implements
		LexicalHandler {

	public static final String SAX_PROPERTIES_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
	private String currentEntity = null;

	public XmlFilterEntityImpl(final XMLReader reader)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		super(reader);
		setProperty(SAX_PROPERTIES_LEXICAL_HANDLER, this);
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		if (currentEntity == null) {
			super.characters(ch, start, length);
			return;
		}

		String entity = "&" + currentEntity + ";";
		super.characters(entity.toCharArray(), 0, entity.length());
		currentEntity = null;
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		currentEntity = name;
	}

	@Override
	public void endEntity(final String name) throws SAXException {
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId)
			throws SAXException {
	}

	@Override
	public void endDTD() throws SAXException {
	}

	@Override
	public void startCDATA() throws SAXException {
	}

	@Override
	public void endCDATA() throws SAXException {
	}

	@Override
	public void comment(final char[] ch, final int start, final int length) throws SAXException {
	}
}
