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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.MetafactureException;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Tests for class {@link DomLoader}.
 *
 * @author Christoph Böhme
 *
 */
public final class DomLoaderTest {

	private static final String BASE_PATH =
			"org/culturegraph/mf/metamorph/xml/dom-loader/";

	private static final String SCHEMA_FILE = BASE_PATH + "test-schema.xsd";

	@Test
	public void shouldCreateDOM() throws FileNotFoundException, MalformedURLException {
		final String inputFile = "should-create-dom.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		// We assume DOM creation worked if the root node of the
		// document is found in the generated DOM:
		final Node rootNode = document.getDocumentElement();
		assertEquals(Node.ELEMENT_NODE, rootNode.getNodeType());
		assertEquals("test-schema", rootNode.getNodeName());
	}

	@Test(expected=MetafactureException.class)
	public void shouldValidateInputAgainstSchema()
			throws FileNotFoundException, MalformedURLException {

		final String inputFile = "should-validate-input-against-schema.xml";

		DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		// The input document contains an element not allowed by
		// the test-schema. If validation works an exception
		// should be thrown by the parser when attempting to
		// parse the document.
	}

	@Test
	public void domShouldNotContainWhitespaceOnlyTextNodes()
			throws FileNotFoundException, MalformedURLException {

		final String inputFile = "dom-should-not-contain-whitespace-only-text-nodes.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		// The root element should not contain any text nodes representing
		// whitespace but only an element node for the <string-element>
		// child element:
		final NodeList nodes1 = document.getDocumentElement().getChildNodes();
		assertEquals(1, nodes1.getLength());
		assertEquals(Node.ELEMENT_NODE, nodes1.item(0).getNodeType());

		// The element node for the <string-element> should not contain
		// any nodes after whitespace has been removed:
		final NodeList nodes2 = nodes1.item(0).getChildNodes();
		assertEquals(0, nodes2.getLength());
	}

	@Test
	public void domShouldNotContainComments()
			throws FileNotFoundException, MalformedURLException {

		final String inputFile = "dom-should-not-contain-comments.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		// The root element only contains a comment. As this comment
		// should not be included in the DOM, the root node should
		// have no children:
		final NodeList nodes = document.getDocumentElement().getChildNodes();
		assertEquals(0, nodes.getLength());
	}

	@Test
	public void shouldConvertAndAttachCDataNodesToTextNodes()
			throws FileNotFoundException, MalformedURLException {

		final String inputFile = "should-convert-and-attach-cdata-nodes-to-text-nodes.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		// The CDATA section in the input file and the surrounding
		// PCDATA text should be coalesced in a single text
		// node in the DOM:
		final NodeList nodes = document.getDocumentElement().getFirstChild().getChildNodes();
		assertEquals(1, nodes.getLength());
		assertEquals(Node.TEXT_NODE, nodes.item(0).getNodeType());
		assertEquals("pcdata-cdata-pcdata", nodes.item(0).getNodeValue());
	}

	@Test
	public void shouldBeXIncludeAware()
			throws FileNotFoundException, MalformedURLException {

		final String inputFile = "should-be-xinclude-aware1.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		// The input file only contains an xinclude statement. The
		// included file contains an element of type string-element.
		// If inclusion worked a node representing the <string-element>
		// should be found in the DOM:
		final Node stringElement = document.getDocumentElement().getFirstChild();
		assertEquals(Node.ELEMENT_NODE, stringElement.getNodeType());
		assertEquals("string-element", stringElement.getNodeName());
	}

	@Test
	public void shouldAnnotateDomWithLocationInformation()
			throws FileNotFoundException, MalformedURLException {

		final String inputFile = "should-annotate-dom-with-location-information.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		final Node rootNode = document.getDocumentElement();
		final Location location1 = (Location) rootNode.getUserData(Location.USER_DATA_ID);
		assertTrue(location1.getSystemId().endsWith(inputFile));
		assertEquals(3, location1.getElementStart().getLineNumber());
		assertEquals(57, location1.getElementStart().getColumnNumber());
		assertEquals(5, location1.getElementEnd().getLineNumber());
		assertEquals(15, location1.getElementEnd().getColumnNumber());

		final Node stringElement = document.getDocumentElement().getFirstChild();
		final Location location2 = (Location) stringElement.getUserData(Location.USER_DATA_ID);
		assertTrue(location2.getSystemId().endsWith(inputFile));
		assertEquals(4, location2.getElementStart().getLineNumber());
		assertEquals(18, location2.getElementStart().getColumnNumber());
		assertEquals(4, location2.getElementEnd().getLineNumber());
		assertEquals(39, location2.getElementEnd().getColumnNumber());
	}

	// This test case does not currently succeed on openJDK (version 1.7.0.60) due
	// to a bug in the Xerces implementation used by the JDK. This bug was fixed
	// in release 2.9.1 of Xerces. A bug report for updating openJDK exists:
	// https://bugs.openjdk.java.net/browse/JDK-8038043
	@Ignore
	@Test
	public void shouldAnnotateIncludedFilesCorrectly()
			throws FileNotFoundException, MalformedURLException {

		final String baseName = "should-annotate-included-files-correctly";
		final String inputFile = baseName + "1.xml";

		final Document document = DomLoader.parse(SCHEMA_FILE, openStream(inputFile));

		final Node rootNode = document.getDocumentElement();
		final Location location1 = (Location) rootNode.getUserData(Location.USER_DATA_ID);
		assertTrue(location1.getSystemId().endsWith(inputFile));
		assertEquals(4, location1.getElementStart().getLineNumber());
		assertEquals(46, location1.getElementStart().getColumnNumber());
		assertEquals(7, location1.getElementEnd().getLineNumber());
		assertEquals(15, location1.getElementEnd().getColumnNumber());

		final Node stringElement = document.getDocumentElement().getFirstChild();
		final Location location2 = (Location) stringElement.getUserData(Location.USER_DATA_ID);
		assertTrue(location2.getSystemId().endsWith(baseName + "2.xml"));
		assertEquals(3, location2.getElementStart().getLineNumber());
		assertEquals(62, location2.getElementStart().getColumnNumber());
		assertEquals(3, location2.getElementEnd().getLineNumber());
		assertEquals(62, location2.getElementEnd().getColumnNumber());
	}

	private static InputSource openStream(final String resource)
			throws FileNotFoundException, MalformedURLException {

		final URL resourceUrl = ResourceUtil.getUrl(BASE_PATH + resource);

		return new InputSource(resourceUrl.toExternalForm());
	}

}
