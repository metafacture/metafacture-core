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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.culturegraph.mf.commons.StringUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link DirectoryEntry}.
 *
 * @author Christoph Böhme
 */
public class DirectoryEntryTest {

	private static final String DUMMY_LABEL = StringUtil.repeatChars(' ', 24);

	private static final byte[] RECORD = asBytes(DUMMY_LABEL + "001234IMP" +
			"002567LEM" + "012090AKB" +  "012890AKB" +
			Iso646Constants.INFORMATION_SEPARATOR_2 +
			Iso646Constants.INFORMATION_SEPARATOR_3);

	private DirectoryEntry directoryEntry;

	@Before
	public void createSystemUnderTest() {
		final RecordFormat recordFormat = RecordFormat.create()
				.withFieldLengthLength(1)
				.withFieldStartLength(2)
				.withImplDefinedPartLength(3)
				.build();
		final Iso646ByteBuffer buffer = new Iso646ByteBuffer(RECORD);
		directoryEntry = new DirectoryEntry(buffer, recordFormat, RECORD.length - 1);
	}

	@Test
	public void constructor_shouldSetFirstEntryAsCurrentEntry() {
		assertArrayEquals("001".toCharArray(), directoryEntry.getTag());
	}

	@Test
	public void gotoNext_shouldSetNextEntryAsCurrentEntry() {
		directoryEntry.gotoNext();
		assertArrayEquals("002".toCharArray(), directoryEntry.getTag());
	}

	@Test
	public void reset_shouldSetFirstEntryAsCurrentEntry() {
		directoryEntry.gotoNext();
		directoryEntry.rewind();
		assertArrayEquals("001".toCharArray(), directoryEntry.getTag());
	}

	@Test
	public void endOfDirectoryReached_shouldReturnFalseIfNotAtEndOFDirectory() {
		assertFalse(directoryEntry.endOfDirectoryReached());
	}

	@Test
	public void endOfDirectoryReached_shouldReturnTrueIfAtEndOFDirectory() {
		directoryEntry.gotoNext();
		directoryEntry.gotoNext();
		directoryEntry.gotoNext();
		directoryEntry.gotoNext();
		assertTrue(directoryEntry.endOfDirectoryReached());
	}

	@Test
	public void getTag_shouldReturnTagFromCurrentEntry() {
		assertArrayEquals("001".toCharArray(), directoryEntry.getTag());
		directoryEntry.gotoNext();
		assertArrayEquals("002".toCharArray(), directoryEntry.getTag());
	}

	@Test
	public void getFieldLength_shouldReturnFieldLengthFromCurrentEntry() {
		assertEquals(2, directoryEntry.getFieldLength());
		directoryEntry.gotoNext();
		assertEquals(5, directoryEntry.getFieldLength());
	}

	@Test
	public void getFieldStart_shouldReturnFieldStartFromCurrentEntry() {
		assertEquals(34, directoryEntry.getFieldStart());
		directoryEntry.gotoNext();
		assertEquals(67, directoryEntry.getFieldStart());
	}

	@Test
	public void getImplDefinedPart_shouldReturnImplDefinedPartFromCurrentEntry() {
		assertArrayEquals("IMP".toCharArray(), directoryEntry.getImplDefinedPart());
		directoryEntry.gotoNext();
		assertArrayEquals("LEM".toCharArray(), directoryEntry.getImplDefinedPart());
	}

	@Test
	public void isRecordIdField_shouldReturnOnlyTrueIfTagIs001() {
		assertTrue(directoryEntry.isRecordIdField());
		directoryEntry.gotoNext();
		assertFalse(directoryEntry.isRecordIdField());
		directoryEntry.gotoNext();
		assertFalse(directoryEntry.isRecordIdField());
	}

	@Test
	public void isReferenceField_shouldReturnOnlyTrueIfTagStartsWith00() {
		assertTrue(directoryEntry.isReferenceField());
		directoryEntry.gotoNext();
		assertTrue(directoryEntry.isReferenceField());
		directoryEntry.gotoNext();
		assertFalse(directoryEntry.isReferenceField());
	}

	@Test
	public void isContinuedField_shouldReturnTrueIfFieldHasZeroLength() {
		assertFalse(directoryEntry.isContinuedField());
		directoryEntry.gotoNext();
		assertFalse(directoryEntry.isContinuedField());
		directoryEntry.gotoNext();
		assertTrue(directoryEntry.isContinuedField());
		directoryEntry.gotoNext();
		assertFalse(directoryEntry.isContinuedField());
	}

	private static byte[] asBytes(final String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}

}
