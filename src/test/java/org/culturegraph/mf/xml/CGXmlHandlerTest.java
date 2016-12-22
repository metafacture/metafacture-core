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
package org.culturegraph.mf.xml;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link CGXmlHandler}.
 *
 * @author Christoph Böhme
 *
 */
public final class CGXmlHandlerTest {

	private static final String CGXML_NS = "http://www.culturegraph.org/cgxml";

	@Mock
	private StreamReceiver receiver;

	private AttributesImpl attributes;

	private CGXmlHandler cgXmlHandler;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		cgXmlHandler = new CGXmlHandler();
		cgXmlHandler.setReceiver(receiver);
	}

	@Before
	public void createHelperObjects() {
		attributes = new AttributesImpl();
	}

	@Test
	public void shouldAcceptCgXmlVersion1() {
		attributes.addAttribute("", "version", "cgxml:version", "CDATA",
				"1.0");
		cgXmlHandler.startElement(CGXML_NS, "cgxml", "cgxml:cgxml", attributes);
		cgXmlHandler.endElement(CGXML_NS, "cgxml", "cgxml:cgxml");

		verifyZeroInteractions(receiver);
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfVersionIsNot1() {
		attributes.addAttribute("", "version", "cgxml:version", "CDATA",
				"2.0");
		cgXmlHandler.startElement(CGXML_NS, "cgxml", "cgxml:cgxml", attributes);

		// Exception expected
	}

	@Test
	public void shouldIgnoreRecordsElement() {
		cgXmlHandler.startElement(CGXML_NS, "records", "cgxml:records", attributes);
		cgXmlHandler.endElement(CGXML_NS, "records", "cgxml:records");

		verifyZeroInteractions(receiver);
	}

	@Test
	public void shouldEmitStartAndEndRecordEventsForRecordElements() {
		attributes.addAttribute("", "id", "cgxml:id", "CDATA", "1");
		cgXmlHandler.startElement(CGXML_NS, "record", "cgxml:record", attributes);
		cgXmlHandler.endElement(CGXML_NS, "record", "cgxml:record");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldEmitStartRecordWithEmptyIdIfIdAttributeIsMissing() {
		cgXmlHandler.startElement(CGXML_NS, "record", "cgxml:record", attributes);

		verify(receiver).startRecord("");
	}

	@Test
	public void shouldEmitStartAndEndEntityEventsForEntityElements() {
		attributes.addAttribute("", "name", "cgxml:name", "CDATA", "e-name");
		cgXmlHandler.startElement(CGXML_NS, "entity", "cgxml:entity", attributes);
		cgXmlHandler.endElement(CGXML_NS, "entity", "cgxml:entity");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("e-name");
		ordered.verify(receiver).endEntity();
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionEmptyNameAttributeIsMissing() {
		cgXmlHandler.startElement(CGXML_NS, "entity", "cgxml:entity", attributes);

		// Exception expected
	}

	@Test
	public void shouldEmitLiteralEventForLiteralElements() {
		attributes.addAttribute("", "name", "cgxml:name", "CDATA", "l-name");
		attributes.addAttribute("", "value", "cgxml:value", "CDATA", "l-val");
		cgXmlHandler.startElement(CGXML_NS, "literal", "cgxml:literal", attributes);
		cgXmlHandler.endElement(CGXML_NS, "literal", "cgxml:literal");

		verify(receiver).literal("l-name", "l-val");
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfLiteralNameAttributeIsMissing() {
		attributes.addAttribute("", "value", "cgxml:value", "CDATA", "l-val");
		cgXmlHandler.startElement(CGXML_NS, "literal", "cgxml:literal", attributes);

		// Exception expected
	}

}
