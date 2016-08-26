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
package org.culturegraph.mf.iso2709;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link DirectoryEntry}.
 *
 * @author Christoph Böhme
 */
public class DirectoryEntryTest {

	private static final String DUMMY_LABEL = StringUtil.repeatChars(' ', 24);

	private static final byte[] RECORD = asBytes(DUMMY_LABEL + "001234IMP" +
			"002567LEM" + Iso646Characters.IS2 + Iso646Characters.IS3);

	@Mock
	private Label label;

	private Iso646ByteBuffer buffer;
	private DirectoryEntry directoryEntry;

	@Before
	public void initMocksAndCreateSystemUnderTest() {
		MockitoAnnotations.initMocks(this);
		when(label.getFieldLengthLength()).thenReturn(1);
		when(label.getFieldStartLength()).thenReturn(2);
		when(label.getImplDefinedPartLength()).thenReturn(3);
		when(label.getBaseAddress()).thenReturn(RECORD.length - 1);

		buffer = new Iso646ByteBuffer(RECORD);
		directoryEntry = new DirectoryEntry(buffer, label);
	}

	@Test
	public void constructor_shouldCreateDirectoryEntryInstance() {
		final byte[] record = asBytes(DUMMY_LABEL + "001234IMP" +
				Iso646Characters.IS2 + Iso646Characters.IS3);
		when(label.getBaseAddress()).thenReturn(record.length - 1);

		buffer = new Iso646ByteBuffer(record);
		directoryEntry = new DirectoryEntry(buffer, label);

		assertNotNull(directoryEntry);
	}

	@Test(expected = FormatException.class)
	public void constructor_shouldThrowFormatExceptionIfBufferIsTooShort() {
		buffer = new Iso646ByteBuffer(asBytes("00005"));
		directoryEntry = new DirectoryEntry(buffer, label);  // Exception expected
	}

	@Test(expected = FormatException.class)
	public void constructor_shouldThrowFormatExceptionIfDirectoryDoesNotEndWithFieldSeparator() {
		final byte[] record = asBytes(DUMMY_LABEL + 'F' +
				Iso646Characters.IS3);
		when(label.getBaseAddress()).thenReturn(record.length - 1);

		buffer = new Iso646ByteBuffer(record);
		directoryEntry = new DirectoryEntry(buffer, label);  // Exception expected
	}

	@Test(expected = FormatException.class)
	public void constructor_shouldThrowFormatExceptionIfDirectoryIsNotMultipleOfEntryLength() {
		final byte[] record = asBytes(DUMMY_LABEL + "001234IM" +
				Iso646Characters.IS2 + Iso646Characters.IS3);
		when(label.getBaseAddress()).thenReturn(record.length - 1);

		buffer = new Iso646ByteBuffer(record);
		directoryEntry = new DirectoryEntry(buffer, label);  // Exception expected
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
		directoryEntry.reset();
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


	private static byte[] asBytes(final String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}

}
