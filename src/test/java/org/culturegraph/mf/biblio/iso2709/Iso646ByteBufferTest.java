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

import java.nio.charset.StandardCharsets;

import org.culturegraph.mf.framework.FormatException;
import org.junit.Test;

/**
 * Tests for class {@link Iso646ByteBuffer}.
 *
 * @author Christoph Böhme
 */
public final class Iso646ByteBufferTest {

	private static final char ASCII_UNMAPPABLE_CHAR = '\ufffd';

	private Iso646ByteBuffer byteBuffer;

	@Test
	public void getLength_shouldReturnRecordLength() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals(3, byteBuffer.getLength());
	}

	@Test
	public void getFreeSpace_shouldReturnBufferLengthIfNothingWasWritten() {
		byteBuffer = new Iso646ByteBuffer(5);
		assertEquals(5, byteBuffer.getFreeSpace());
	}

	@Test
	public void getFreeSpace_shouldReturnSpaceBetweenWritePositionAndBufferEnd() {
		byteBuffer = new Iso646ByteBuffer(5);
		byteBuffer.setWritePosition(2);
		assertEquals(3, byteBuffer.getFreeSpace());
	}

	@Test
	public void getFreeSpace_shouldReturnZeroIfBufferIsFull() {
		byteBuffer = new Iso646ByteBuffer(5);
		byteBuffer.setWritePosition(5);
		assertEquals(0, byteBuffer.getFreeSpace());
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

	@Test
	public void stringAt_shouldReturnStringDecodedAsUtf8() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tüx Tox"));
		assertEquals("Tüx Tox", byteBuffer.stringAt(0, byteBuffer.getLength(),
				StandardCharsets.UTF_8));
	}

	@Test
	public void stringAt_shouldReturnEmptyStringIfLengthIsZero() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
		assertEquals("", byteBuffer.stringAt(0, 0, StandardCharsets.UTF_8));
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

	@Test
	public void byteAt_shouldReturnByteAtIndex() {
		byteBuffer = new Iso646ByteBuffer(new byte[] { 0x01, 0x02 });
		assertEquals(0x02, byteBuffer.byteAt(1));
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

	@Test(expected = NumberFormatException.class)
	public void parseIntAt_shouldThrowFormatExceptionIfNumberIsTooLarge() {
		byteBuffer = new Iso646ByteBuffer(asBytes("123456789123456789"));
		byteBuffer.parseIntAt(0, 18);  // Exception expected
	}

	@Test
	public void writeChar_shouldWriteCharAtWritePosition() {
		byteBuffer = new Iso646ByteBuffer(3);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeChar('c');

		assertArrayEquals(new byte[]{ 0x00, 0x63, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(2, byteBuffer.getWritePosition());
	}

	@Test
	public void writeChars_shouldWriteCharArrayAtWritePosition() {
		byteBuffer = new Iso646ByteBuffer(4);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeChars(new char[]{ 'c', 'b' });

		assertArrayEquals(new byte[]{ 0x00, 0x63, 0x62, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(3, byteBuffer.getWritePosition());
	}

	@Test
	public void writeByte_shouldWriteByteAtWritePosition() {
		byteBuffer = new Iso646ByteBuffer(3);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeByte((byte) 0x61);

		assertArrayEquals(new byte[]{ 0x00, 0x61, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(2, byteBuffer.getWritePosition());
	}

	@Test
	public void writeBytes_shouldWriteByteArrayAtWritePosition() {
		byteBuffer = new Iso646ByteBuffer(4);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeBytes(asBytes("cb"));

		assertArrayEquals(new byte[]{ 0x00, 0x63, 0x62, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(3, byteBuffer.getWritePosition());
	}

	@Test
	public void writeInt_shouldWriteAsciiCodeOfSingleDigitAtWritePosition() {
		byteBuffer = new Iso646ByteBuffer(3);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeInt(3);

		assertArrayEquals(new byte[]{ 0x00, 0x33, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(2, byteBuffer.getWritePosition());
	}

	@Test
	public void writeInt_shouldWriteAsciiCodesOfDigitsAtWritePosition() {
		byteBuffer = new Iso646ByteBuffer(5);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeInt(123, 3);

		assertArrayEquals(new byte[]{ 0x00, 0x31, 0x32, 0x33, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(4, byteBuffer.getWritePosition());
	}

	@Test
	public void writeInt_shouldAddLeadingZerosIfNumberIsShorterThanDigits() {
		byteBuffer = new Iso646ByteBuffer(5);
		byteBuffer.setWritePosition(1);
		byteBuffer.writeInt(3, 3);

		assertArrayEquals(new byte[]{ 0x00, 0x30, 0x30, 0x33, 0x00 },
				byteBuffer.getByteArray());
		assertEquals(4, byteBuffer.getWritePosition());
	}

	@Test
	public void toString_shouldReturnBufferContentDecodedAsISO646() {
		byteBuffer = new Iso646ByteBuffer(asBytes("Tux tüt"));
		assertEquals("Tux t" + ASCII_UNMAPPABLE_CHAR + ASCII_UNMAPPABLE_CHAR + "t",
				byteBuffer.toString());
	}

	private byte[] asBytes(final String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}

}
