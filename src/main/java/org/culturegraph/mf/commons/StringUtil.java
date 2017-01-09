/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.commons;

import java.nio.CharBuffer;
import java.util.Map;

/**
 * Some handy string utility methods
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
public final class StringUtil {

	private static final String DEFAULT_VARSTART = "${";
	private static final String DEFAULT_VAREND = "}";

	private StringUtil() {
		// no instances allowed
	}

	public static <O> O fallback(final O value, final O fallbackValue) {
		if (value == null) {
			return fallbackValue;
		}
		return value;
	}

	public static String format(final String format, final Map<String, String> variables) {
		return format(format, DEFAULT_VARSTART, DEFAULT_VAREND, true, variables);
	}

	public static String format(final String format, final String varStartIndicator, final String varEndIndicator,
			final Map<String, String> variables) {
		return format(format, varStartIndicator, varEndIndicator, true, variables);
	}

	public static String format(final String format, final boolean ignoreMissingVars,
			final Map<String, String> variables) {
		return format(format, DEFAULT_VARSTART, DEFAULT_VAREND, ignoreMissingVars, variables);
	}

	public static String format(final String format, final String varStartIndicator, final String varEndIndicator,
			final boolean ignoreMissingVars, final Map<String, String> variables) {
		if (format.indexOf(varStartIndicator) < 0) { // shortcut if there is
														// nothing to replace
			return format;
		}

		int varStart = 0;
		int varEnd = 0;
		int oldEnd = 0;
		String varName;
		String varValue;

		final StringBuilder builder = new StringBuilder();
		final char[] formatChars = format.toCharArray();

		while (true) {
			varStart = format.indexOf(varStartIndicator, oldEnd);
			varEnd = format.indexOf(varEndIndicator, varStart);
			if (varStart < 0 || varEnd < 0) {
				builder.append(formatChars, oldEnd, formatChars.length - oldEnd);
				break;
			}

			builder.append(formatChars, oldEnd, varStart - oldEnd);

			varName = format.substring(varStart + varStartIndicator.length(), varEnd);
			varValue = variables.get(varName);
			if (varValue == null) {
				if (ignoreMissingVars) {
					varValue = "";
				} else {
					throw new IllegalArgumentException("Variable '" + varName
							+ "' was not assigned!\nAssigned variables:\n" + variables);
				}
			}
			builder.append(varValue);

			oldEnd = varEnd + varEndIndicator.length();
		}
		return builder.toString();
	}

	/**
	 * Copies the contents of {@code str} into the {@code currentBuffer}. If the size of
	 * the buffer is not sufficient to store the string then a new buffer is allocated.
	 * {@code copyToBuffer} is intended to be used as shown in the example:
	 * <pre>{@code
	 *   final int INITIAL_SIZE = 10;
	 *   char[] myBuffer = new char[INITIAL_SIZE];
	 *
	 *   List<String> strings = getAListOfStringsFromSomewhere();
	 *   for (String str : strings) {
	 *     myBuffer = StringUtil.copyToBuffer(str, myBuffer);
	 *     dataLen = str.length();
	 *     // Process data in myBuffer in the range from 0 to dataLen
	 *   }
	 *   myBuffer = null;
	 * }</pre>
	 *
	 * This allows the buffer to be reused but at the same time frees the user from
	 * having to manage the size of the buffer.
	 *
	 * @param str string to copy. Must not be null.
	 * @param currentBuffer array to store the string in. If it is too small a new buffer
	 *                      is allocated.  currentBuffer must not be null.
	 *                      The length of the current buffer must not be 0.
	 * @return either currentBuffer or a new buffer if one was allocated. The returned
	 *                      buffer may be larger than the string. Users should call
	 *                      {@code str.length()} to retrieve the actual length of the
	 *                      data in the returned buffer.
	 */
	public static char[] copyToBuffer(final String str, final char[] currentBuffer) {
		assert str != null;
		assert currentBuffer != null;
		assert currentBuffer.length > 0;

		final int strLen = str.length();
		char[] buffer = currentBuffer;
		int bufferLen = buffer.length;

		while(strLen > bufferLen) {
			bufferLen *= 2;
		}
		if (bufferLen > buffer.length) {
			buffer = new char[bufferLen];
		}
		str.getChars(0, strLen, buffer, 0);
		return buffer;
	}

	/**
	 * Creates a string which contains a sequence of repeated characters.
	 *
	 * @param ch to repeat
	 * @param count number of repetitions
	 * @return a string with {@code count} consisting only of {@code ch}
	 */
	public static String repeatChars(final char ch, final int count) {
		return CharBuffer.allocate(count).toString().replace('\0', ch);
	}

}
