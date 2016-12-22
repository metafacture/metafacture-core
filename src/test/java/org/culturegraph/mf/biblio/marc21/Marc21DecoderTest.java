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
package org.culturegraph.mf.biblio.marc21;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link Marc21Decoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class Marc21DecoderTest {

	private static final char SUBFIELD_MARKER = '\u001f';
	private static final char FIELD_SEPARATOR = '\u001e';
	private static final char RECORD_SEPARATOR = '\u001d';

	private static final String RECORD_ID = "identifier";
	private static final String CONTROLFIELD_VALUE = "controlfield";

	private static final String FIELD1 = "AB" + SUBFIELD_MARKER + "1"
			+ "value1";
	private static final String FIELD2 = "CD" + SUBFIELD_MARKER + "2"
			+ "value2" + SUBFIELD_MARKER + "3" + "value3";

	private static final String RECORD_LABEL = "00128noa a2200073zu 4500";
	private static final String DIRECTORY = "001001100000" + "002001300011"
			+ "100001100024" + "200003100035";
	private static final String DATA = RECORD_ID + FIELD_SEPARATOR
			+ CONTROLFIELD_VALUE + FIELD_SEPARATOR + FIELD1 + FIELD_SEPARATOR
			+ FIELD2 + FIELD_SEPARATOR;
	private static final String RECORD = RECORD_LABEL + DIRECTORY
			+ FIELD_SEPARATOR + DATA + RECORD_SEPARATOR;

	private Marc21Decoder marc21Decoder;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		marc21Decoder = new Marc21Decoder();
		marc21Decoder.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		marc21Decoder.closeStream();
	}

	@Test
	public void shouldProcessMarc21Record() {
		marc21Decoder.process(RECORD);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity("leader");
		ordered.verify(receiver).literal("status", "n");
		ordered.verify(receiver).literal("type", "o");
		ordered.verify(receiver).literal("bibliographicLevel", "a");
		ordered.verify(receiver).literal("typeOfControl", " ");
		ordered.verify(receiver).literal("characterCodingScheme", "a");
		ordered.verify(receiver).literal("encodingLevel", "z");
		ordered.verify(receiver).literal("catalogingForm", "u");
		ordered.verify(receiver).literal("multipartLevel", " ");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("001", RECORD_ID);
		ordered.verify(receiver).literal("002", CONTROLFIELD_VALUE);
		ordered.verify(receiver).startEntity("100AB");
		ordered.verify(receiver).literal("1", "value1");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("200CD");
		ordered.verify(receiver).literal("2", "value2");
		ordered.verify(receiver).literal("3", "value3");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldIgnoreEmptyRecords() {
		marc21Decoder.process("");
		verifyZeroInteractions(receiver);
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfRecordIsNotMarc21() {
		marc21Decoder.process("00026RIMPL1100024SYS3330" + FIELD_SEPARATOR
				+ RECORD_SEPARATOR);
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfRecordIsTooShort() {
		marc21Decoder.process("00005");
	}

}
