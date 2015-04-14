/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.culturegraph.mf.exceptions.ShouldNeverHappenException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases for class {@link TestCase}.
 *
 * @author Christoph Böhme
 *
 */
public final class TestCaseTest {

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
			throw new ShouldNeverHappenException(e);
		}

		document = docBuilder.newDocument();
	}

	@Test
	public void shouldSupportFormetaAsInputAndResultFormat() {
		final Element inputElement = createFormetaRecord(INPUT_TAG);
		final Element resultElement = createFormetaRecord(RESULT_TAG);

		final Element testCaseElement = document.createElement(TEST_CASE_TAG);
		testCaseElement.appendChild(inputElement);
		testCaseElement.appendChild(resultElement);

		final TestCase testCase = new TestCase(testCaseElement);
		testCase.run();

		// The test was successful if run does not throw
		// an exception.
	}

	@Test
	public void shouldSupportCGXmlAsInputAndResultFormat() {
		final Element inputElement = createCGXmlRecord(INPUT_TAG);
		final Element resultElement = createCGXmlRecord(RESULT_TAG);

		final Element testCaseElement = document.createElement(TEST_CASE_TAG);
		testCaseElement.appendChild(inputElement);
		testCaseElement.appendChild(resultElement);

		final TestCase testCase = new TestCase(testCaseElement);
		testCase.run();

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

}
