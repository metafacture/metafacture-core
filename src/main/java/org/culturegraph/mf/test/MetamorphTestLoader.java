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
package org.culturegraph.mf.test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.runners.model.InitializationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Utility methods for loading Metamorph-Test resources.
 *
 * @author Christoph Böhme
 *
 */
final class MetamorphTestLoader {

	private static final String SCHEMA_FILE = "schemata/metamorph-test.xsd";
	private static final String TEST_CASE_TAG = "test-case";

	private MetamorphTestLoader() {
		// No instances allowed
	}

	static List<MetamorphTestCase> load(final URL testDef)
			throws InitializationError {
		final InputSource inputSource = new InputSource(testDef.toExternalForm());
		return load(inputSource);
	}

	private static List<MetamorphTestCase> load(final InputSource inputSource)
			throws InitializationError {

		try {
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final URL schemaUrl = Thread.currentThread().getContextClassLoader()
					.getResource(SCHEMA_FILE);
			if (schemaUrl == null) {
				throw new InitializationError("XML Schema not found: " + SCHEMA_FILE);
			}
			final Schema schema = schemaFactory.newSchema(schemaUrl);

			final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setIgnoringElementContentWhitespace(true);
			builderFactory.setIgnoringComments(true);
			builderFactory.setNamespaceAware(true);
			builderFactory.setCoalescing(true);
			builderFactory.setSchema(schema);

			final DocumentBuilder builder = builderFactory.newDocumentBuilder();
			final Document doc = builder.parse(inputSource);

			final List<MetamorphTestCase> metamorphTestCases = new ArrayList<>();
			final NodeList testCaseNodes = doc.getElementsByTagName(TEST_CASE_TAG);
			for(int i=0; i < testCaseNodes.getLength(); ++i) {
				final Element testCaseElement = (Element) testCaseNodes.item(i);
				metamorphTestCases.add(new MetamorphTestCase(testCaseElement));
			}

			return metamorphTestCases;

		} catch (final ParserConfigurationException|SAXException|IOException e) {
			throw new InitializationError(e);
		}
	}

}

