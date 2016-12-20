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
package org.culturegraph.mf.metamorph.xml;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Extends {@link XMLFilterImpl} to also intercept events defined
 * in {@link LexicalHandler}.
 *
 * @author Christoph Böhme
 *
 */
class LexicalHandlerXmlFilter extends XMLFilterImpl implements LexicalHandler {

	private static final String LEXICAL_HANDLER_PROPERTY =
			"http://xml.org/sax/properties/lexical-handler";

	private LexicalHandler lexicalHandler;

	LexicalHandlerXmlFilter(final XMLReader parent) {
		super(parent);
	}

	@Override
	public void parse(final InputSource input) throws SAXException, IOException {
		if (lexicalHandler != null) {
			super.setProperty(LEXICAL_HANDLER_PROPERTY, this);
		}
		super.parse(input);
	}

	@Override
	public void parse(final String systemId) throws SAXException, IOException {
		if (lexicalHandler != null) {
			super.setProperty(LEXICAL_HANDLER_PROPERTY, this);
		}
		super.parse(systemId);
	}

	@Override
	public void setProperty(final String name, final Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {

		if (LEXICAL_HANDLER_PROPERTY.equals(name)) {
			lexicalHandler = (LexicalHandler) value;
		} else {
			super.setProperty(name, value);
		}
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId)
			throws SAXException {

		if (lexicalHandler != null) {
			lexicalHandler.startDTD(name, publicId, systemId);
		}
	}

	@Override
	public void endDTD() throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.endDTD();
		}
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.startEntity(name);
		}
	}

	@Override
	public void endEntity(final String name) throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.endEntity(name);
		}
	}

	@Override
	public void startCDATA() throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.startCDATA();
		}
	}

	@Override
	public void endCDATA() throws SAXException {
		if (lexicalHandler != null) {
			lexicalHandler.endCDATA();
		}
	}

	@Override
	public void comment(final char[] ch, final int start, final int length)
			throws SAXException {

		if (lexicalHandler != null) {
			lexicalHandler.comment(ch, start, length);
		}
	}

}
