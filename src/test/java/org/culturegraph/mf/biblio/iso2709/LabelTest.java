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
package org.culturegraph.mf.biblio.iso2709;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link Label}.
 *
 * @author Christoph Böhme
 *
 */
public final class LabelTest {

	private static final int RECORD_LENGTH = 26;
	private static final char RECORD_STATUS = 'S';
	private static final char[] IMPL_CODES = { 'I', 'M', 'P', 'L' };
	private static final int INDICATOR_LENGTH = 1;
	private static final int IDENTIFIER_LENGTH = 2;
	private static final char[] SYSTEM_CHARS = { 'S', 'Y', 'S' };
	private static final int FIELD_LENGTH_LENGTH = 3;
	private static final int FIELD_START_LENGTH = 4;
	private static final int IMPL_DEFINED_PART_LENGTH = 5;
	private static final char RESERVED_CHAR = 'R';
	private static final int BASE_ADDRESS = 25;

	private static final String RECORD_LABEL =
			String.format("%05d", RECORD_LENGTH) +
			RECORD_STATUS +
			String.valueOf(IMPL_CODES) +
			INDICATOR_LENGTH +
			IDENTIFIER_LENGTH +
			String.format("%05d", BASE_ADDRESS) +
			String.valueOf(SYSTEM_CHARS) +
			FIELD_LENGTH_LENGTH +
			FIELD_START_LENGTH +
			IMPL_DEFINED_PART_LENGTH + RESERVED_CHAR;

	private Label label;

	@Before
	public void createSystemUnderTest() {
		final byte[] recordLabelBytes = RECORD_LABEL.getBytes(
				Iso646Constants.CHARSET);
		final Iso646ByteBuffer buffer = new Iso646ByteBuffer(recordLabelBytes);
		label = new Label(buffer);
	}

	@Test
	public void getRecordFormat_shouldReturnRecordFormatObject() {
		final RecordFormat recordFormat = label.getRecordFormat();

		assertNotNull(recordFormat);
		final RecordFormat expectedFormat = RecordFormat.create()
				.withIndicatorLength(INDICATOR_LENGTH)
				.withIdentifierLength(IDENTIFIER_LENGTH)
				.withFieldStartLength(FIELD_START_LENGTH)
				.withFieldLengthLength(FIELD_LENGTH_LENGTH)
				.withImplDefinedPartLength(IMPL_DEFINED_PART_LENGTH)
				.build();
		assertEquals(expectedFormat, recordFormat);
	}

	@Test
	public void getRecordLength_shouldReturnRecordLength() {
		assertEquals(RECORD_LENGTH, label.getRecordLength());
	}

	@Test
	public void getRecordStatus_shouldReturnRecordStatus() {
		assertEquals(RECORD_STATUS, label.getRecordStatus());
	}

	@Test
	public void getImplCodes_shouldReturnImplCodes() {
		assertArrayEquals(IMPL_CODES, label.getImplCodes());
	}

	@Test
	public void getIndicatorLength_shouldReturnIndicatorLength() {
		assertEquals(INDICATOR_LENGTH, label.getIndicatorLength());
	}

	@Test
	public void getIdentifierLength_shouldReturnIdentifierLength() {
		assertEquals(IDENTIFIER_LENGTH, label.getIdentifierLength());
	}

	@Test
	public void getBaseAddress_shouldReturnBaseAddress() {
		assertEquals(BASE_ADDRESS, label.getBaseAddress());
	}

	@Test
	public void getSystemChars_shouldReturnUserSystemChars() {
		assertArrayEquals(SYSTEM_CHARS, label.getSystemChars());
	}

	@Test
	public void getFieldLengthLength_shouldReturnFieldLengthLength() {
		assertEquals(FIELD_LENGTH_LENGTH, label.getFieldLengthLength());
	}

	@Test
	public void getFieldStartLength_shouldReturnFieldStartLength() {
		assertEquals(FIELD_START_LENGTH, label.getFieldStartLength());
	}

	@Test
	public void getImplDefinedPartLength_shouldReturnImplDefinedPartLength() {
		assertEquals(IMPL_DEFINED_PART_LENGTH, label.getImplDefinedPartLength());
	}

	@Test
	public void getReservedChar_shouldReturnReservedChar() {
		assertEquals(RESERVED_CHAR, label.getReservedChar());
	}

	@Test
	public void toString_shouldReturnRecordLabel() {
		assertEquals(RECORD_LABEL, label.toString());
	}

}
