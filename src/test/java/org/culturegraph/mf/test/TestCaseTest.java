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

	@Test
	public void shouldSupportFormetaAsInputAndResultFormat() {
		final Document doc = getXmlDocument();

		final Element inputElement = doc.createElement("input");
		inputElement.setAttribute("type", "text/x-formeta");
		inputElement.setTextContent("{l: v}");

		final Element resultElement = doc.createElement("result");
		resultElement.setAttribute("type", "text/x-formeta");
		resultElement.setTextContent("{l: v}");

		final Element config = doc.createElement("test-case");
		config.appendChild(inputElement);
		config.appendChild(resultElement);

		final TestCase testCase = new TestCase(config);
		testCase.run();

		// The test was successful if run does not throw
		// an exception.
	}

	@Test
	public void shouldSupportCGXmlAsInputAndResultFormat() {
		final Document doc = getXmlDocument();

		final Element inputRecord = doc.createElement("record");
		inputRecord.setAttribute("id", "1");

		final Element inputElement = doc.createElement("input");
		inputElement.setAttribute("type", "text/x-cg+xml");
		inputElement.appendChild(inputRecord);

		final Element resultRecord = doc.createElement("record");
		resultRecord.setAttribute("id", "1");

		final Element resultElement = doc.createElement("result");
		resultElement.setAttribute("type", "text/x-cg+xml");
		resultElement.appendChild(resultRecord);

		final Element config = doc.createElement("test-case");
		config.appendChild(inputElement);
		config.appendChild(resultElement);

		final TestCase testCase = new TestCase(config);
		testCase.run();

		// The test was successful if run does not throw
		// an exception.
	}

	private static Document getXmlDocument() {
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch(final ParserConfigurationException e) {
			throw new ShouldNeverHappenException(e);
		}

		return docBuilder.newDocument();
	}

}
