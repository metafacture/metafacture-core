/*
 *  Copyright 2013, 2014 hbz
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
package org.culturegraph.mf.biblio;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests for class {@link AlephMabXmlHandler}.
 *
 * @author Christoph Böhme (rewrite)
 * @author Pascal Christoph (dr0i)
 */
public final class AlephMabXmlHandlerTest {

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private StreamReceiver receiver;

	private AlephMabXmlHandler mabXmlHandler;

	@Before
	public void setup() {
		mabXmlHandler = new AlephMabXmlHandler();
		mabXmlHandler.setReceiver(receiver);
	}

	@Test
	public void shouldIgnoreLeader() throws SAXException {
		final String leader = "00000nM2.01200024------h";
		mabXmlHandler.startElement(null, "ListRecords", "", new AttributesImpl());
		mabXmlHandler.startElement(null, "leader", "", new AttributesImpl());
		mabXmlHandler.characters(leader.toCharArray(), 0, leader.length());
		mabXmlHandler.endElement(null, "leader", "");
		mabXmlHandler.endElement(null, "ListRecords", "");

		final InOrder ordered	= Mockito.inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldParseControlField() throws SAXException {
		final AttributesImpl attributes = new AttributesImpl();
		final String data = "MH";

		mabXmlHandler.startElement(null, "ListRecords", "", new AttributesImpl());
		attributes.addAttribute(null, "tag", "tag", "CDATA", "FMT");
		mabXmlHandler.startElement(null, "controlfield", "", attributes);
		mabXmlHandler.characters(data.toCharArray(), 0, data.length());
		mabXmlHandler.endElement(null, "controlfield", "");
		mabXmlHandler.endElement(null, "ListRecords", "");

		final InOrder ordered	= Mockito.inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("FMT");
		ordered.verify(receiver).literal("", data);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldParseDataField() throws SAXException {
		final AttributesImpl attributes = new AttributesImpl();
		final String data = "1234";

		mabXmlHandler.startElement(null, "ListRecords", "", new AttributesImpl());
		attributes.addAttribute(null, "tag", "tag", "CDATA", "001");
		attributes.addAttribute(null, "ind1", "ind1", "CDATA", "a");
		attributes.addAttribute(null, "ind2", "ind2", "CDATA", "b");
		mabXmlHandler.startElement(null, "datafield", "", attributes);
		attributes.clear();
		attributes.addAttribute(null, "code", "code", "CDATA", "a");
		mabXmlHandler.startElement(null, "subfield", "", attributes);
		mabXmlHandler.characters(data.toCharArray(), 0, data.length());
		mabXmlHandler.endElement(null, "subfield", "");
		mabXmlHandler.endElement(null, "datafield", "");
		mabXmlHandler.endElement(null, "ListRecords", "");

		final InOrder ordered	= Mockito.inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("001ab");
		ordered.verify(receiver).literal("a", data);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

}
