/*
 * Copyright 2014 Christoph Böhme
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases for class {@link MetamorphTestCase}.
 *
 * @author Christoph Böhme
 *
 */
public final class MetamorphTestCaseTest {

	private static final String TEST_CASE_TAG = "test-case";
	private static final String INPUT_TAG = "input";
	private static final String TRANSFORMATION_TAG = "transformation";
	private static final String RESULT_TAG = "result";

	private Document document;

	@Before
	public void createXmlDocument() {
		final DocumentBuilderFactory docBuilderFactory =
				DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch(final ParserConfigurationException e) {
			throw new AssertionError(
					"No error expected when creating a standard document builder", e);
		}

		document = docBuilder.newDocument();
	}

	@Test
	public void shouldSupportFormetaAsInputAndResultFormat()
			throws InitializationError {
		final Element inputElement = createFormetaRecord(INPUT_TAG);
		final Element resultElement = createFormetaRecord(RESULT_TAG);

		final Element testCaseElement = document.createElement(TEST_CASE_TAG);
		testCaseElement.appendChild(inputElement);
		testCaseElement.appendChild(resultElement);

		final MetamorphTestCase metamorphTestCase =
				new MetamorphTestCase(testCaseElement);
		metamorphTestCase.evaluate();

		// The test was successful if run does not throw
		// an exception.
	}

	@Test
	public void shouldSupportCGXmlAsInputAndResultFormat()
			throws InitializationError {
		final Element inputElement = createCGXmlRecord(INPUT_TAG);
		final Element resultElement = createCGXmlRecord(RESULT_TAG);

		final Element testCaseElement = document.createElement(TEST_CASE_TAG);
		testCaseElement.appendChild(inputElement);
		testCaseElement.appendChild(resultElement);

		final MetamorphTestCase metamorphTestCase =
				new MetamorphTestCase(testCaseElement);
		metamorphTestCase.evaluate();

		// The test was successful if run does not throw
		// an exception.
	}

	@Test
	public void issue229ShouldSupportMarcXmlAsInputAndResultFormat()
			throws InitializationError {
		final Element inputElement = createMarcXmlRecord(INPUT_TAG);
		final Element resultElement = createMarcXmlRecord(RESULT_TAG);

		final Element testCaseElement = document.createElement(TEST_CASE_TAG);
		testCaseElement.appendChild(inputElement);
		testCaseElement.appendChild(resultElement);

		final MetamorphTestCase metamorphTestCase =
				new MetamorphTestCase(testCaseElement);
		metamorphTestCase.evaluate();

		// The test was successful if run does not throw
		// an exception.
	}

	@Test
	public void issue219ShouldResolveXIncludesInMetamorphResources()
			throws InitializationError {
		final Element inputElement = createFormetaRecord(INPUT_TAG);
		final Element resultElement = createFormetaRecord(RESULT_TAG);

		final Element transformationElement =
				document.createElement(TRANSFORMATION_TAG);
		transformationElement.setAttribute("type", "text/x-metamorph+xml");
		transformationElement.setAttribute("src",
				"org/culturegraph/mf/test/issue219-should-resolve-xincludes-in-metamorph-resources1.xml");

		final Element testCaseElement = document.createElement(TEST_CASE_TAG);
		testCaseElement.appendChild(inputElement);
		testCaseElement.appendChild(transformationElement);
		testCaseElement.appendChild(resultElement);

		final MetamorphTestCase metamorphTestCase =
				new MetamorphTestCase(testCaseElement);
		metamorphTestCase.evaluate();

		// The test was successful if run does not throw
		// an exception.
	}

	private Element createFormetaRecord(final String elementName) {
		final Element element = document.createElement(elementName);
		element.setAttribute("type", "text/x-formeta");
		element.setTextContent("{l: v}");
		return element;
	}

	private Element createCGXmlRecord(final String elementName) {
		final Element recordElement = document.createElement("record");
		recordElement.setAttribute("id", "1");
		final Element element = document.createElement(elementName);
		element.setAttribute("type", "text/x-cg+xml");
		element.appendChild(recordElement);
		return element;
	}

	private Element createMarcXmlRecord(final String elementName) {
		final Element fieldElement = document.createElement("controlfield");
		fieldElement.setAttribute("tag", "001");
		fieldElement.setTextContent("123");
		final Element recordElement = document.createElement("record");
		recordElement.appendChild(fieldElement);
		final Element collectionElement = document.createElementNS(
				"http://www.loc.gov/MARC21/slim", "collection");
		collectionElement.appendChild(recordElement);
		final Element element = document.createElement(elementName);
		element.setAttribute("type", "application/marcxml+xml");
		element.appendChild(collectionElement);
		return element;
	}

}
