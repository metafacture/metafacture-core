/*
 * Copyright 2013 Pascal Christoph (hbz)
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
package org.culturegraph.mf.xml;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link XmlElementSplitter}.
 *
 * @author Christoph Böhme (rewrite)
 * @author Pascal Christoph (dr0i)
 *
 */
public class XmlElementSplitterTest {

	private static final String NAMESPACE =
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private StreamReceiver receiver;

	private XmlElementSplitter xmlElementSplitter;

	@Before
	public void setup() {
		xmlElementSplitter = new XmlElementSplitter();
		xmlElementSplitter.setReceiver(receiver);
	}

	@Test
	public void shouldSplitXmlAtDefinedElementName() throws SAXException {
		xmlElementSplitter.setElementName("Description");
		xmlElementSplitter.setTopLevelElement("rdf:RDF");

		xmlElementSplitter.startPrefixMapping("rdf", NAMESPACE);
		xmlElementSplitter.startElement(NAMESPACE, "RDF", "rdf:RDF",
				new AttributesImpl());
		startDescription("1");
		emitResourceContent("r1", "1");
		xmlElementSplitter.endElement(NAMESPACE, "Description", "rdf:Description");
		startDescription("2");
		emitResourceContent("r2", "2");
		xmlElementSplitter.endElement(NAMESPACE, "Description", "rdf:Description");
		xmlElementSplitter.endElement(NAMESPACE, "RDF", "rdf:RDF");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("0");
		ordered.verify(receiver).literal("Element",
				"<?xml version = \"1.0\" encoding = \"UTF-8\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description rdf:about=\"1\"><a rdf:resource=\"r1\">1</a></rdf:Description></rdf:RDF>");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("Element",
				"<?xml version = \"1.0\" encoding = \"UTF-8\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description rdf:about=\"2\"><a rdf:resource=\"r2\">2</a></rdf:Description></rdf:RDF>");
		ordered.verify(receiver).endRecord();
	}

	private void startDescription(final String id) throws SAXException {
		final AttributesImpl attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "about", "rdf:about", "CDATA", id);
		xmlElementSplitter.startElement(NAMESPACE, "Description", "rdf:Description",
				attributes);
	}

	private void emitResourceContent(final String resource, final String data)
			throws SAXException {
		final AttributesImpl attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "resource", "rdf:resource", "CDATA",
				resource);
		xmlElementSplitter.startElement(null, "a", "a", attributes);
		xmlElementSplitter.characters(data.toCharArray(), 0, data.length());
		xmlElementSplitter.endElement(null, "a", "a");
	}

}
