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

import java.nio.charset.Charset;

import org.culturegraph.mf.framework.FormatException;

/**
 * Provides methods for reading and writing strings and integers in a byte
 * array.
 * <p>
 * All methods except for the {#stringAt(int, int, Charset)} method assume
 * that the bytes in the buffer represent characters encoded with the
 * encoding defined by ISO 646 (which is mostly equivalent with 7-bit ASCII).
 *
 * @author Christoph Böhme
 */
final class Iso646ByteBuffer {

	private static final int RADIX = 10;

	private final byte[] byteArray;

	private int writePosition;

	Iso646ByteBuffer(final int size) {
		this(new byte[size]);
	}

	Iso646ByteBuffer(final byte[] byteArray) {
		assert byteArray != null;
		this.byteArray = byteArray;
	}

	byte[] getByteArray() {
		return byteArray;
	}

	int getLength() {
		return byteArray.length;
	}

	int getFreeSpace() {
		return byteArray.length - writePosition;
	}

	void setWritePosition(final int writePosition) {
		assert 0 <= writePosition && writePosition <= byteArray.length;
		this.writePosition = writePosition;
	}

	int getWritePosition() {
		return writePosition;
	}

	/**
	 * Returns the distance from {@code fromIndex} to the next occurrence of
	 * {@code byteValue}. If the byte at {@code fromIndex} is equal to {@code
	 * byteValue} zero is returned. If there are no matching bytes between
	 * {@code fromIndex} and the end of the buffer then the distance to the end
	 * of the buffer is returned.
	 *
	 * @param byteValue byte to search for.
	 * @param fromIndex the position in the buffer from which to start searching.
	 * @return the distance in bytes to the next byte with the given value or if
	 * none is found to the end of the buffer.
	 */
	int distanceTo(final byte byteValue, final int fromIndex) {
		assert 0 <= fromIndex && fromIndex < byteArray.length;
		int index = fromIndex;
		for (; index < byteArray.length; ++index) {
			if (byteValue == byteArray[index]) {
				break;
			}
		}
		return index - fromIndex;
	}

	/**
	 * Returns the distance from {@code fromIndex} to the next occurrence of one
	 * of the bytes in {@code bytes}. If the byte at {@code fromIndex} is in
	 * {@code bytes} zero is returned. If there are no matching bytes between
	 * {@code fromIndex} and the end of the buffer then the distance to the
	 * end of the buffer is returned.
	 *
	 * @param bytes     bytes to search for.
	 * @param fromIndex the position in the buffer from which to start searching.
	 * @return the distance in bytes to the next byte with the given value or if
	 * none is found to the end of the buffer.
	 */
	int distanceTo(final byte[] bytes, final int fromIndex) {
		assert 0 <= fromIndex && fromIndex < byteArray.length;
		int index = fromIndex;
		for (; index < byteArray.length; ++index) {
			if (containsByte(bytes, byteArray[index])) {
				break;
			}
		}
		return index - fromIndex;
	}

	private boolean containsByte(final byte[] haystack, final byte needle) {
		for (final byte byteValue : haystack) {
			if (byteValue == needle) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a string containing the characters in the specified part of the
	 * record.
	 *
	 * @param fromIndex index of the first byte of the string.
	 * @param length number of bytes to include in the string. If zero an empty
	 *               string is returned.
	 * @param charset used for decoding the byte sequence into characters. It is
	 *                callers responsibility to make sure that the selected byte
	 *                range contains a valid byte sequence when working with
	 *                multi-byte encodings such as UTF-8.
	 * @return the string represented by the bytes in the given range
	 */
	String stringAt(final int fromIndex, final int length,
			final Charset charset) {
		return new String(byteArray, fromIndex, length, charset);
	}

	/**
	 * Returns the character value at {@code index}.
	 *
	 * @param index position of the byte in the buffer
	 * @return the character value of the byte at the given index
	 */
	char charAt(final int index) {
		return byteToChar(index);
	}

	char[] charsAt(final int fromIndex, final int length) {
		assert length >= 0;
		assert 0 <= fromIndex && (fromIndex + length) <= byteArray.length;
		final char[] chars = new char[length];
		for (int i = 0; i < length; ++i) {
			chars[i] = byteToChar(fromIndex + i);
		}
		return chars;
	}

	private char byteToChar(final int index) {
		final byte value = byteArray[index];
		if (value < 0) {
			throw new FormatException("Invalid character code found at index " +
					index);
		}
		return (char) value;
	}

	byte byteAt(final int index) {
		return byteArray[index];
	}

	/**
	 * Parses the character at {@code index} into an integer.
	 *
	 * @param index position of the byte to convert into an integer
	 * @return the integer value represented by the character at the given
	 * position.
	 * @throws NumberFormatException if a non-digit character is encountered.
	 */
	int parseIntAt(final int index) {
		return byteToDigit(index);
	}

	/**
	 * Parses characters in the specified part of the byte buffer into an integer
	 * This is a simplified version of {@link Integer#parseInt(String)}. It
	 * operates directly on the byte data and works only for positive numbers and
	 * a radix of 10.
	 *
	 * @param fromIndex position fo the byte range to convert into an integer
	 * @param length number of bytes to include in the range
	 * @return the integer value represented by the characters at the given
	 * range in the buffer.
	 * @throws NumberFormatException if a non-digit character was encountered or
	 *                               an overflow occurred.
	 */
	int parseIntAt(final int fromIndex, final int length) {
		assert length >= 0;
		assert 0 <= fromIndex && (fromIndex + length) <= byteArray.length;
		final int multiplyMax = Integer.MAX_VALUE / RADIX;
		int result = 0;
		for (int i = 0; i < length; ++i) {
			if (result > multiplyMax) {
				throwNumberIsToLargeException(fromIndex);
			}
			result *= RADIX;
			final int digit = byteToDigit(fromIndex + i);
			if (result > Integer.MAX_VALUE - digit) {
				throwNumberIsToLargeException(fromIndex);
			}
			result += digit;
		}
		return result;
	}

	private int byteToDigit(final int index) {
		final byte digit = byteArray[index];
		if (digit < Iso646Constants.ZERO || Iso646Constants.NINE < digit) {
			throw new NumberFormatException("digit expected at index " + index +
					" but got 0x" + Integer.toHexString(digit));
		}
		return digit - Iso646Constants.ZERO;
	}

	private void throwNumberIsToLargeException(final int index) {
		throw new NumberFormatException("number starting at index " + index +
				" is too large");
	}

	void writeChar(final char charValue) {
		assert charValue <= Iso646Constants.MAX_CHAR_CODE;
		byteArray[writePosition] = (byte) charValue;
		writePosition += 1;
	}

	void writeChars(final char[] chars) {
		assert (writePosition + chars.length) <= byteArray.length;
		for (final char charValue : chars) {
			writeChar(charValue);
		}
	}

	void writeByte(final byte value) {
		byteArray[writePosition] = value;
		writePosition += 1;
	}

	void writeBytes(final byte[] array) {
		System.arraycopy(array, 0, byteArray, writePosition, array.length);
		writePosition += array.length;
	}

	void writeInt(final int value) {
		assert 0 <= value && value < 10;
		byteArray[writePosition] = (byte) (Iso646Constants.ZERO + value);
		writePosition += 1;
	}

	void writeInt(final int value, final int digits) {
		assert value >= 0;
		assert digits >= 0;
		assert (writePosition + digits) <= byteArray.length;
		int head = value;
		for (int i = writePosition + digits - 1; i >= writePosition; i--) {
			byteArray[i] = (byte) (Iso646Constants.ZERO + head % RADIX);
			head /= RADIX;
		}
		assert head == 0;
		writePosition += digits;
	}

	@Override
	public String toString() {
		return stringAt(0, byteArray.length, Iso646Constants.CHARSET);
	}

}
