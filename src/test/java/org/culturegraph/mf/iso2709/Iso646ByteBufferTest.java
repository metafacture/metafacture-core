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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;

import org.culturegraph.mf.exceptions.FormatException;
import org.junit.Test;

/**
 * Tests for class {@link Iso646ByteBuffer}.
 *
 * @author Christoph Böhme
 */
public final class Iso646ByteBufferTest {

	private static final char ASCII_UNMAPPABLE_CHAR = '\ufffd';

	private Iso646ByteBuffer byteBuffer;

	@Test(expected = IllegalArgumentException.class)
	public void constructor_shouldThrowIllegalArgumentExceptionIfBufferIsNull() {
		byteBuffer = new Iso646ByteBuffer(null);  // Exception expected
	}

	@Test
	public void getLength_shouldReturnRecordLength() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(3, byteBuffer.getLength());
	}

	@Test
	public void distanceTo_byteArray_shouldReturnDistanceToFirstMatchingByte() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(2, byteBuffer.distanceTo(asBytes("x"), 0));
	}

	@Test
	public void distanceTo_byteArray_shouldReturnDistanceToEndOfBufferIfNoMatchFound() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(3, byteBuffer.distanceTo(asBytes("X"), 0));
	}

	@Test
	public void distanceTo_byteArray_shouldReturnZeroIfSearchStartsAtMatchingByte() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(0, byteBuffer.distanceTo(asBytes("T"), 0));
	}

	@Test
	public void distanceTo_byteArray_shouldReturnDistanceToFirstMatchingByteOfTheBytesArray() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(1, byteBuffer.distanceTo(asBytes("xu"), 0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void distanceTo_byteArray_shouldThrowIllegalArgumentExceptionIfBytesIsNull() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.distanceTo(null, 0);  // Exception expected
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void distanceTo_byteArray_shouldThrowIndexOutOfBoundsExceptionIfFromIndexIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.distanceTo(asBytes("T"), -1);  // Exception expected
	}

	@Test
	public void distanceTo_byte_shouldReturnDistanceToFirstMatchingByte() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(2, byteBuffer.distanceTo((byte) 'x', 0));
	}

	@Test
	public void distanceTo_byte_shouldReturnDistanceToEndOfBufferIfNoMatchFound() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(3, byteBuffer.distanceTo((byte) 'X', 0));
	}

	@Test
	public void distanceTo_byte_shouldReturnZeroIfSearchStartsAtMatchingByte() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(0, byteBuffer.distanceTo((byte) 'T', 0));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void distanceTo_byte_shouldThrowIndexOutOfBoundsExceptionIfFromIndexIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.distanceTo((byte) 'T', -1);  // Exception expected
	}

	@Test
	public void charAt_shouldReturnCharacterAtIndexDecodedAsIso646() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals('T', byteBuffer.charAt(0));
	}

	@Test(expected = FormatException.class)
	public void charAt_shouldThrowFormatExceptionIfByteValueIsNotInIso646() {
		byteBuffer = new Iso646ByteBuffer(asBytes("ü"));
		byteBuffer.charAt(0);  // Exception expected
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void charAt_shouldThrowIndexOutOfBoundsExceptionIfIndexIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.charAt(-1);  // Exception expected
	}

	@Test
	public void charsAt_shouldReturnBytesAsCharacterArrayDecodedAsIso646() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux Tox"));
		assertArrayEquals("Tux".toCharArray(), byteBuffer.charsAt(0, 3));
	}

	@Test(expected = FormatException.class)
	public void charsAt_shouldThrowFormatExceptionIfByteValueIsNotInIso646() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tüx Tox"));
		byteBuffer.charsAt(0, 4);  // Exception expected
	}

	@Test
	public void charsAt_shouldReturnEmptyCharacterArrayIfLengthIsZero() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertArrayEquals(new char[0], byteBuffer.charsAt(0, 0));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void charsAt_shouldThrowIndexOutOfBoundsExceptionIfRangeIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.charsAt(0, -1);  // Exception expected
	}

	@Test
	public void stringAt_shouldReturnStringDecodedAsUtf8() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tüx Tox"));
		assertEquals("Tüx Tox", byteBuffer.stringAt(0, byteBuffer.getLength(),
				Charset.forName("UTF-8")));
	}

	@Test
	public void stringAt_shouldReturnEmptyStringIfLengthIsZero() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals("", byteBuffer.stringAt(0, 0, Charset.forName("UTF-8")));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void stringAt_shouldThrowIndexOutOfBoundsExceptionIfRangeIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.stringAt(0, -1, Charset.forName("UTF-8"));  // Exception expected
	}

	@Test
	public void parseIntAt_shouldReturnIntValueAtIndex() {
		byteBuffer = new Iso646ByteBuffer(asBytes("299"));
		assertEquals(2, byteBuffer.parseIntAt(0));
	}

	@Test(expected = NumberFormatException.class)
	public void parseIntAt_shouldThrowFormatExceptionIfNotADigit() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.parseIntAt(0);  // Exception expected
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void parseIntAt_shouldThrowIndexOutOfBoundsExceptionIfIndexIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("299"));
		byteBuffer.parseIntAt(-1);  // Exception expected
	}

	@Test
	public void parseIntAt_shouldReturnIntValueForRange() {
		byteBuffer = new Iso646ByteBuffer(asBytes("299"));
		assertEquals(299, byteBuffer.parseIntAt(0, 3));
	}

	@Test
	public void parseIntAt_shouldReturnZeroIfLengthIsZero() {
		byteBuffer = new Iso646ByteBuffer(asBytes("123"));
		assertEquals(0, byteBuffer.parseIntAt(0, 0));
	}

	@Test(expected = NumberFormatException.class)
	public void parseIntAt_shouldThrowFormatExceptionIfNotANumber() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		byteBuffer.parseIntAt(0, 3);  // Exception expected
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void parseIntAt_shouldThrowIndexOutOfBoundsExceptionIfRangeIsInvalid() {
		byteBuffer = new Iso646ByteBuffer(asBytes("123"));
		byteBuffer.parseIntAt(0, -1);  // Exception expected
	}

	@Test
	public void toString_shouldReturnBufferContentDecodedAsISO646() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux tüt"));
		assertEquals("Tux t" + ASCII_UNMAPPABLE_CHAR + ASCII_UNMAPPABLE_CHAR + "t",
				byteBuffer.toString());
	}

	private byte[] asBytes(final String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}

}
