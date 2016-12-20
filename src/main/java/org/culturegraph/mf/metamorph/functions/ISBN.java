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
package org.culturegraph.mf.metamorph.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.metamorph.api.helpers.AbstractSimpleStatelessFunction;

/**
 * Offers ISBN conversions.
 *
 * @author Markus Michael Geipel
 */
public final class ISBN extends AbstractSimpleStatelessFunction {

	private static final String CHECK = "0123456789X0";

	private static final String ISBN10 = "isbn10";
	private static final String ISBN13 = "isbn13";
	private static final Pattern ISBN_PATTERN = Pattern.compile("[\\dX]+");
	private static final Pattern DIRT_PATTERN = Pattern.compile("[\\.\\-]");
	private static final int ISBN10_SIZE = 10;
	private static final int ISBN10_MAGIC = 10;
	private static final int ISBN13_SIZE = 13;
	private static final int ISBN13_MAGIC = 3;
	private static final int ISBN10_MOD = 11;
	private static final int ISBN13_MOD = 10;

	private boolean to10;
	private boolean to13;
	private boolean verifyCheckDigit;
	private String errorString;

	public void setErrorString(final String errorString) {
		this.errorString = errorString;
	}

	@Override
	public String process(final String value) {
		String result = cleanse(value);
		final int size = result.length();

		if (verifyCheckDigit && !isValid(result)) {
			result = errorString;
		} else if (!(size == ISBN10_SIZE || size == ISBN13_SIZE)) {
			result = errorString;
		} else {
			if (to10 && ISBN13_SIZE == size) {
				result = isbn13to10(result);
			} else if (to13 && ISBN10_SIZE == size) {
				result = isbn10to13(result);
			}
		}
		return result;
	}

	public static String cleanse(final String isbn) {
		String normValue = isbn.replace('x', 'X');
		normValue = DIRT_PATTERN.matcher(normValue).replaceAll("");
		final Matcher matcher = ISBN_PATTERN.matcher(normValue);
		if (matcher.find()) {
			return matcher.group();
		}
		return "";
	}

	public void setTo(final String toString) {
		to13 = toString.equalsIgnoreCase(ISBN13);
		to10 = toString.equalsIgnoreCase(ISBN10);
	}

	private static char check10(final String isbn10Data) {
		assert isbn10Data.length() == ISBN10_SIZE - 1;

		int check = 0;
		for (int i = 0; i < ISBN10_SIZE - 1; ++i) {
			final int digit = charToInt(isbn10Data.charAt(i));
			check = check + (ISBN10_MAGIC - i) * digit;
		}
		check = ISBN10_MOD - (check % ISBN10_MOD);
		return CHECK.charAt(check);
	}

	private static char check13(final String isbn13Data) {
		assert isbn13Data.length() == ISBN13_SIZE - 1;

		int accumulator = 0;
		for (int i = 0; i < ISBN13_SIZE - 1; ++i) {
			final int digit = charToInt(isbn13Data.charAt(i));
			if ((i % 2) == 0) {
				accumulator = accumulator + digit;
			} else {
				accumulator = accumulator + ISBN13_MAGIC * digit;
			}
		}

		accumulator = accumulator % ISBN13_MOD;
		if (accumulator != 0) {
			accumulator = ISBN13_MOD - accumulator;
		}
		return CHECK.charAt(accumulator);
	}

	private static int charToInt(final char cha) {
		return (byte) cha - (byte) '0';
	}

	public static String isbn13to10(final String isbn) {
		if (isbn.length() != ISBN13_SIZE) {
			throw new IllegalArgumentException(
					"isbn must be 13 characters long");
		}
		final String isbn10Data = isbn.substring(3, 12);

		return isbn10Data + check10(isbn10Data);
	}

	public static String isbn10to13(final String isbn) {
		if (isbn.length() != ISBN10_SIZE) {
			throw new IllegalArgumentException(
					"isbn must be 10 characters long");
		}

		final String isbn13Data = "978" + isbn.substring(0, ISBN10_SIZE - 1);

		return isbn13Data + check13(isbn13Data);
	}

	public static boolean isValid(final String isbn) {
		boolean result = false;

		if (isbn.length() == ISBN10_SIZE) {
			result = check10(isbn.substring(0, ISBN10_SIZE - 1)) == isbn
					.charAt(ISBN10_SIZE - 1);
		} else if (isbn.length() == ISBN13_SIZE) {
			result = check13(isbn.substring(0, ISBN13_SIZE - 1)) == isbn
					.charAt(ISBN13_SIZE - 1);
		}
		return result;
	}

	public void setVerifyCheckDigit(final String verifyCheckDigit) {
		this.verifyCheckDigit = "true".equals(verifyCheckDigit);
	}

}
