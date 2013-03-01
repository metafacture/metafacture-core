/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.morph;

import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.culturegraph.mf.exceptions.MorphDefException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Helper to load dom {@link Document}s in {@link MorphBuilder}.
 * 
 * @author Markus Michael Geipel
 *
 */
final class DomLoader {
	
	private static final ErrorHandler ERROR_HANDLER = new PrivateErrorHandler();
	
	private DomLoader() {
		// no instances
	}
	
	public static Document parse(final String schemaFile, final InputSource inputSource){
		try {
			final DocumentBuilder documentBuilder = getDocumentBuilder(schemaFile);
			final Document document = documentBuilder.parse(inputSource);
			
			//xerces issue
			removeEmptyTextNodes(document.getDocumentElement());
			
			return document;
		} catch (SAXException e) {
			throw new MorphDefException(e);
		} catch (IOException e) {
			throw new MorphDefException(e);
		}
	}
	
	private static void removeEmptyTextNodes(final Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.TEXT_NODE) {
				final Node old = child;
				child = child.getNextSibling();
				
				
				if(old.getNodeValue().trim().isEmpty()){
					node.removeChild(old);
				}
			}else{
				removeEmptyTextNodes(child);
				child = child.getNextSibling();
			}
		}
	}

	public static String getDocumentBuilderFactoryImplName(){
		return DocumentBuilderFactory.newInstance().getClass().getName();
	}
	
	private static DocumentBuilder getDocumentBuilder(final String schemaFile) {

		try {
			
			final URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(schemaFile);
			if (schemaUrl == null) {
				throw new MorphDefException("'" + schemaFile + "' not found!");
			}
			
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final Schema schema = schemaFactory.newSchema(schemaUrl);
			final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

			builderFactory.setSchema(schema);
			builderFactory.setIgnoringElementContentWhitespace(true);
			builderFactory.setIgnoringComments(true);
			builderFactory.setNamespaceAware(true);
			builderFactory.setCoalescing(true);
			builderFactory.setXIncludeAware(true);

			final DocumentBuilder builder = builderFactory.newDocumentBuilder();
		
			builder.setErrorHandler(ERROR_HANDLER);

			return builder;

		} catch (ParserConfigurationException e) {
			throw new MorphDefException(e);
		} catch (SAXException e) {
			throw new MorphDefException(e);
		}
	}
	
	/**
	 * Error handler
	 */
	private static class PrivateErrorHandler implements ErrorHandler {
		private static final String PARSE_ERROR = "Error parsing xml: ";
		
		PrivateErrorHandler() {
			// to avoid synthetic accessor methods
		}

		@Override
		public void warning(final SAXParseException exception) throws SAXException {
			handle(exception);
		}

		@Override
		public void fatalError(final SAXParseException exception) throws SAXException {
			handle(exception);
		}

		@Override
		public void error(final SAXParseException exception) throws SAXException {
			handle(exception);
		}
		
		private void handle(final SAXParseException exception) {
			throw new MorphDefException(PARSE_ERROR + exception.getMessage(), exception);
		}
	}
}
