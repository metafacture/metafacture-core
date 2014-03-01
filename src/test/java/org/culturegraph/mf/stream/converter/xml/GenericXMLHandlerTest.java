/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.converter.xml;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.DataFilePath;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 */
public final class GenericXMLHandlerTest {

	private ResourceOpener opener;
	private XmlDecoder xmlDecoder;
	private GenericXmlHandler genericXmlHandler;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		opener = new ResourceOpener();
		xmlDecoder = new XmlDecoder();
		genericXmlHandler = new GenericXmlHandler("record");
		opener.setReceiver(xmlDecoder)
				.setReceiver(genericXmlHandler)
				.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		opener.closeStream();
	}

	@Test
	public void testShouldIgnoreCharDataNotInARecord() {

		opener.process(DataFilePath.GENERIC_XML);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("id", "1");
		ordered.verify(receiver).literal("del", "no");
		ordered.verify(receiver).startEntity("name");
		ordered.verify(receiver).literal("value", "Record 1");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("description");
		ordered.verify(receiver).literal("lang", "de");
		ordered.verify(receiver).literal("value", "Erster Datensatz");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("2");
		ordered.verify(receiver).literal("id", "2");
		ordered.verify(receiver).literal("del", "yes");
		ordered.verify(receiver).startEntity("name");
		ordered.verify(receiver).literal("value", "Record 2");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("description");
		ordered.verify(receiver).literal("lang", "de");
		ordered.verify(receiver).literal("value", "Zweiter Datensatz");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldEmitEmptyStringIfRecordTagHasNoIdAttribute() {

		opener.process(DataFilePath.DATA_PREFIX + "shouldEmitEmptyStringIfRecordTagHasNoIdAttribute.xml");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldEmitValueOfIdAttribute() {

		opener.process(DataFilePath.DATA_PREFIX + "shouldEmitValueOfIdAttribute.xml");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("theRecordID");
		ordered.verify(receiver).endRecord();
	}

}
