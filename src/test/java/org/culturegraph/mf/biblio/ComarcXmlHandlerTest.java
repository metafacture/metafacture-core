/*
 * Copyright 2017 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.biblio;

import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link ComarcXmlHandler}.
 *
 * @author Lars Svensson
 *
 */
public final class ComarcXmlHandlerTest {

	private static final String SUBFIELD = "subfield";
	private static final String RECORD = "record";
	private static final String DATAFIELD = "datafield";
	private static final String NAMESPACE = "http://www.loc.gov/MARC21/slim";

	private ComarcXmlHandler comarcXmlHandler;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.comarcXmlHandler = new ComarcXmlHandler();
		this.comarcXmlHandler.setReceiver(this.receiver);
	}

	@After
	public void cleanup() {
		this.comarcXmlHandler.closeStream();
	}

	@Test
	public void shouldSetIdFrom000SubfieldX() throws SAXException {
		final String fieldValue = "1234";
		AttributesImpl attributes = new AttributesImpl();

		this.comarcXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);

		attributes.addAttribute(NAMESPACE, "tag", "tag", "CDATA", "000");
		attributes.addAttribute(NAMESPACE, "ind1", "ind1", "CDATA", " ");
		attributes.addAttribute(NAMESPACE, "ind2", "ind2", "CDATA", " ");
		this.comarcXmlHandler
				.startElement(NAMESPACE, DATAFIELD, "", attributes);

		attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "code", "code", "CDATA", "x");
		this.comarcXmlHandler.startElement(NAMESPACE, SUBFIELD, "", attributes);
		this.comarcXmlHandler.characters(fieldValue.toCharArray(), 0,
				fieldValue.length());
		this.comarcXmlHandler.endElement(NAMESPACE, SUBFIELD, "");

		this.comarcXmlHandler.endElement(NAMESPACE, DATAFIELD, "");
		this.comarcXmlHandler.endElement(NAMESPACE, RECORD, "");

		verify(this.receiver).literal("x", fieldValue);
		verify(this.receiver).startRecord(fieldValue);
		verify(this.receiver).endRecord();
	}

	@Test
	public void shouldAcceptRepeatableSubfields() throws SAXException {
		final String fieldValue1 = "1234";
		final String fieldValue2 = "9876";
		AttributesImpl attributes = new AttributesImpl();

		this.comarcXmlHandler.startElement(NAMESPACE, RECORD, "", attributes);

		attributes.addAttribute(NAMESPACE, "tag", "tag", "CDATA", "000");
		attributes.addAttribute(NAMESPACE, "ind1", "ind1", "CDATA", " ");
		attributes.addAttribute(NAMESPACE, "ind2", "ind2", "CDATA", " ");
		this.comarcXmlHandler
				.startElement(NAMESPACE, DATAFIELD, "", attributes);

		attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "code", "code", "CDATA", "x");
		this.comarcXmlHandler.startElement(NAMESPACE, SUBFIELD, "", attributes);
		this.comarcXmlHandler.characters(fieldValue1.toCharArray(), 0,
				fieldValue1.length());
		this.comarcXmlHandler.endElement(NAMESPACE, SUBFIELD, "");

		this.comarcXmlHandler.startElement(NAMESPACE, SUBFIELD, "", attributes);
		this.comarcXmlHandler.characters(fieldValue2.toCharArray(), 0,
				fieldValue2.length());
		this.comarcXmlHandler.endElement(NAMESPACE, SUBFIELD, "");

		this.comarcXmlHandler.endElement(NAMESPACE, DATAFIELD, "");
		this.comarcXmlHandler.endElement(NAMESPACE, RECORD, "");

		verify(this.receiver).literal("x", fieldValue1);
		verify(this.receiver).literal("x", fieldValue2);
		verify(this.receiver).startRecord(fieldValue1);
		verify(this.receiver).endRecord();
	}

	@Test
	public void shouldReadTwoFields() throws SAXException {
		final String fieldValue1 = "1234";
		final String fieldValue2 = "9876";
		AttributesImpl attributes = new AttributesImpl();

		// initialise the record
		this.comarcXmlHandler.startElement(NAMESPACE, RECORD, RECORD,
				attributes);

		// create datafield "000  "
		attributes.addAttribute(NAMESPACE, "tag", "tag", "CDATA", "000");
		attributes.addAttribute(NAMESPACE, "ind1", "ind1", "CDATA", " ");
		attributes.addAttribute(NAMESPACE, "ind2", "ind2", "CDATA", " ");
		this.comarcXmlHandler
				.startElement(NAMESPACE, DATAFIELD, "", attributes);

		attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "code", "code", "CDATA", "x");
		this.comarcXmlHandler.startElement(NAMESPACE, SUBFIELD, "", attributes);
		this.comarcXmlHandler.characters(fieldValue1.toCharArray(), 0,
				fieldValue1.length());
		this.comarcXmlHandler.endElement(NAMESPACE, SUBFIELD, "");

		this.comarcXmlHandler.startElement(NAMESPACE, SUBFIELD, "", attributes);
		this.comarcXmlHandler.characters(fieldValue2.toCharArray(), 0,
				fieldValue2.length());
		this.comarcXmlHandler.endElement(NAMESPACE, SUBFIELD, "");

		this.comarcXmlHandler.endElement(NAMESPACE, DATAFIELD, "");

		// create datafield "001 2"
		attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "tag", "tag", "CDATA", "001");
		attributes.addAttribute(NAMESPACE, "ind1", "ind1", "CDATA", " ");
		attributes.addAttribute(NAMESPACE, "ind2", "ind2", "CDATA", "2");
		this.comarcXmlHandler
				.startElement(NAMESPACE, DATAFIELD, "", attributes);

		attributes = new AttributesImpl();
		attributes.addAttribute(NAMESPACE, "code", "code", "CDATA", "a");
		this.comarcXmlHandler.startElement(NAMESPACE, SUBFIELD, "", attributes);
		this.comarcXmlHandler.characters(fieldValue1.toCharArray(), 0,
				fieldValue1.length());
		this.comarcXmlHandler.endElement(NAMESPACE, SUBFIELD, "");

		this.comarcXmlHandler.endElement(NAMESPACE, DATAFIELD, "");

		this.comarcXmlHandler.endElement(NAMESPACE, RECORD, "");

		verify(this.receiver).literal("x", fieldValue1);
		verify(this.receiver).literal("x", fieldValue2);
		verify(this.receiver).startRecord(fieldValue1);
		verify(this.receiver).endRecord();
		verify(this.receiver).startEntity("000  ");
		verify(this.receiver).startEntity("001 2");
		verify(this.receiver).literal("a", fieldValue1);
	}

}
