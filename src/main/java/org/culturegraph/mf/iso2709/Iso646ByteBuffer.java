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

import java.nio.charset.Charset;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.util.Require;

/**
 * Provides methods for reading strings and integers from a byte array.
 * <p>
 * All methods expect for the {#stringAt(int, int, Charset)} method assume
 * that the bytes in the buffer represent characters encoded with the
 * encoding defined in ISO 646 (which is mostly equivalent with 7-bit ASCII).
 *
 * @author Christoph Böhme
 */
final class Iso646ByteBuffer {

	private static final byte ISO646_DIGIT_ZERO = 0x30;  // == '0'
	private static final byte ISO646_DIGIT_NINE = 0x39;  // == '9'

	private static final int RADIX = 10;

	private final byte[] buffer;

	Iso646ByteBuffer(final byte[] buffer) {
		Require.notNull(buffer);
		this.buffer = buffer;
	}

	int getLength() {
		return buffer.length;
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
	 */
	int distanceTo(final byte byteValue, final int fromIndex) {
		Require.validArrayIndex(fromIndex, buffer.length);

		int index = fromIndex;
		for (; index < buffer.length; ++index) {
			if (byteValue == buffer[index]) {
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
	 */
	int distanceTo(final byte[] bytes, final int fromIndex) {
		Require.notNull(bytes);
		Require.validArrayIndex(fromIndex, buffer.length);

		int index = fromIndex;
		for (; index < buffer.length; ++index) {
			if (containsByte(bytes, buffer[index])) {
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
	 * record. If {@code length} is zero then an empty string is returned.
	 */
	String stringAt(final int fromIndex, final int length,
			final Charset charset) {
		Require.validArraySlice(fromIndex, length, buffer.length);
		return new String(buffer, fromIndex, length, charset);
	}

	/**
	 * Returns the character value at {@code index}.
	 */
	char charAt(final int index) {
		Require.validArrayIndex(index, buffer.length);
		return byteToChar(index);
	}

	char[] charsAt(final int fromIndex, final int length) {
		Require.validArraySlice(fromIndex, length, buffer.length);
		final char[] chars = new char[length];
		for (int i = 0; i < length; ++i) {
			chars[i] = byteToChar(fromIndex + i);
		}
		return chars;
	}

	/**
	 * Parses the character at {@code index} into an integer.
	 *
	 * @throws NumberFormatException if a non-digit character is encountered.
	 */
	int parseIntAt(final int index) {
		Require.validArrayIndex(index, buffer.length);
		return byteToDigit(index);
	}

	/**
	 * Parses characters in the specified part of the byte buffer into an integer
	 * This is a simplified version of {@link Integer#parseInt(String)}. It
	 * operates directly on the byte data and works only for positive numbers and
	 * a radix of 10.
	 *
	 * @throws NumberFormatException if a non-digit character was encountered or
	 *                               an overflow occurred.
	 */
	int parseIntAt(final int fromIndex, final int length) {
		Require.validArraySlice(fromIndex, length, buffer.length);
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

	@Override
	public String toString() {
		return stringAt(0, buffer.length, Charset.forName("ASCII"));
	}

	private char byteToChar(final int index) {
		final byte value = buffer[index];
		if (value < 0) {
			throw new FormatException("Invalid character code found at index " +
					index);
		}
		return (char) value;
	}

	private int byteToDigit(final int index) {
		final byte digit = buffer[index];
		if (digit < ISO646_DIGIT_ZERO || ISO646_DIGIT_NINE < digit) {
			throw new NumberFormatException("digit expected at index " + index +
					" but got 0x" + Integer.toHexString(digit));
		}
		return digit - ISO646_DIGIT_ZERO;
	}

	private void throwNumberIsToLargeException(final int index) {
		throw new NumberFormatException("number starting at index " + index +
				" is too large");
	}

}
