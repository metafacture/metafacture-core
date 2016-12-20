/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.MetafactureException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * Helper to load DOM {@link Document}s.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public final class DomLoader {

	private static final ErrorHandler SAX_ERROR_HANDLER =
			new SaxErrorHandler();

	private static final ErrorListener TRANSFORMER_ERROR_HANDLER =
			new TransformerErrorHandler();

	private DomLoader() {
		throw new AssertionError("No instances allowed");
	}

	public static Document parse(String schemaFile, InputSource input) {
		final Document document = createEmptyDocument();
		final XMLReader pipeline = createXmlFilterPipeline(schemaFile, document);
		process(new SAXSource(pipeline, input), new DOMResult(document));
		// Xerces does not use the XSD schema for deciding whether
		// whitespace is ignorable (it requires a DTD for this).
		// Since we do not use a DTD we have to use a different
		// method to remove ignorable whitespace.
		//
		// Note that this method does not only remove ignorable
		// whitespace but all text nodes containing only whitespace.
		removeEmptyTextNodes(document);
		return document;
	}

	private static Document createEmptyDocument() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			throw new MetafactureException(e);
		}
	}

	private static XMLReader createXmlFilterPipeline(String schemaFile,
			Document document) {
		XMLReader pipelineHead = createSaxReader(loadSchema(schemaFile));
		pipelineHead = new LocationAnnotator(pipelineHead, document);
		pipelineHead = new IgnorableWhitespaceFilter(pipelineHead);
		pipelineHead = new CommentsFilter(pipelineHead);
		pipelineHead = new CDataFilter(pipelineHead);
		pipelineHead.setErrorHandler(SAX_ERROR_HANDLER);
		return pipelineHead;
	}

	private static Schema loadSchema(String schemaFile) {
		try {
			return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					.newSchema(getSchemaUrl(schemaFile));
		} catch (SAXException e) {
			throw new MetafactureException(e);
		}
	}

	private static URL getSchemaUrl(String schemaFile) {
		try {
			return ResourceUtil.getUrl(schemaFile);
		} catch (final MalformedURLException e) {
			throw new MetafactureException("'" + schemaFile + "' not found:", e);
		}
	}

	private static XMLReader createSaxReader(Schema schema) {
		final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setSchema(schema);
		parserFactory.setNamespaceAware(true);
		parserFactory.setXIncludeAware(true);
		try {
			return parserFactory.newSAXParser().getXMLReader();
		} catch (ParserConfigurationException | SAXException e) {
			throw new MetafactureException(e);
		}
	}

	private static void process(Source source, Result result) {
		final Transformer transformer = createTransformer();
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new MetafactureException(e);
		}
	}

	private static Transformer createTransformer() {
		try {
			final Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setErrorListener(TRANSFORMER_ERROR_HANDLER);
			return transformer;
		} catch (TransformerConfigurationException e) {
			throw new MetafactureException(e);
		}
	}

	private static void removeEmptyTextNodes(final Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.TEXT_NODE) {
				final Node old = child;
				child = child.getNextSibling();

				if(old.getNodeValue().trim().isEmpty()) {
					node.removeChild(old);
				}
			} else {
				removeEmptyTextNodes(child);
				child = child.getNextSibling();
			}
		}
	}

	/**
	 * Error handler for the SAX parser.
	 *
	 */
	private static class SaxErrorHandler implements ErrorHandler {

		SaxErrorHandler() {
			// Avoid synthetic accessor methods
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
			throw new MetafactureException("Error parsing xml: " +
					exception.getMessage(), exception);
		}

	}

	/**
	 * Error handler for the transformer.
	 *
	 */
	private static class TransformerErrorHandler implements ErrorListener {

		TransformerErrorHandler() {
			// Avoid synthetic accessor methods
		}

		@Override
		public void warning(final TransformerException exception)
				throws TransformerException {
			handle(exception);
		}

		@Override
		public void error(final TransformerException exception)
				throws TransformerException {
			handle(exception);
		}

		@Override
		public void fatalError(final TransformerException exception)
				throws TransformerException {
			handle(exception);
		}

		private void handle(final TransformerException exception) {
			throw new MetafactureException("Error during DOM creation: " +
					exception.getMessage(), exception);
		}

	}

}
