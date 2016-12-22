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
package org.culturegraph.mf.xml;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link GenericXmlHandler}.
 *
 * @author Christoph Böhme
 *
 */
public final class GenericXMLHandlerTest {

	@Mock
	private StreamReceiver receiver;

	private GenericXmlHandler genericXmlHandler;

	private AttributesImpl attributes;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		genericXmlHandler = new GenericXmlHandler("record");
		genericXmlHandler.setReceiver(receiver);
	}

	@Before
	public void createHelperObjects() {
		attributes = new AttributesImpl();
	}

	@Test
	public void shouldIgnoreElementsOutsideRecordElement() {
		genericXmlHandler.startElement("", "ignore-me", "ignore-me", attributes);

		verifyZeroInteractions(receiver);
	}

	@Test
	public void shouldEmitRecordElementAsStartAndEndRecordEvent() {
		genericXmlHandler.startElement("", "record", "record", attributes);
		genericXmlHandler.endElement("", "record", "record");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldEmitEmptyStringIfRecordTagHasNoIdAttribute() {
		genericXmlHandler.startElement("", "record", "record", attributes);

		verify(receiver).startRecord("");
	}

	@Test
	public void shouldEmitValueOfIdAttribute() {
		attributes.addAttribute("", "id", "id", "CDATA", "theRecordID");
		genericXmlHandler.startElement("", "record", "record", attributes);

		verify(receiver).startRecord("theRecordID");
	}

	@Test
	public void shouldEmitAttributesOnRecordElementAsLiterals() {
		attributes.addAttribute("", "attr", "attr", "CDATA", "attr-value");
		genericXmlHandler.startElement("", "record", "record", attributes);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal("attr", "attr-value");
	}

	@Test
	public void shouldEmitElementsAsStartAndEndEntityEvents() {
		genericXmlHandler.startElement("", "record", "record", attributes);
		genericXmlHandler.startElement("", "entity", "entity", attributes);
		genericXmlHandler.endElement("", "entity", "entity");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).endEntity();
	}

	@Test
	public void shouldEmitAttributesOnEntityElementAsLiterals() {
		genericXmlHandler.startElement("", "record", "record", attributes);
		attributes.addAttribute("", "attr", "attr", "CDATA", "attr-value");
		genericXmlHandler.startElement("", "entity", "entity", attributes);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).literal("attr", "attr-value");
	}

	@Test
	public void shouldEmitPCDataAsALiteralNamedValue() {
		final char[] charData = "char-data".toCharArray();
		genericXmlHandler.startElement("", "record", "record", attributes);
		genericXmlHandler.startElement("", "entity", "entity", attributes);
		genericXmlHandler.characters(charData, 0, charData.length);
		genericXmlHandler.endElement("", "entity", "entity");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).literal("value", "char-data");
	}

}
