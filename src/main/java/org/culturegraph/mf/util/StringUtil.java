/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.util;

import java.util.Map;

/**
 * Basic utils
 * 
 * @author Markus Michael Geipel
 * 
 */
public final class StringUtil {

	private static final String DEFAULT_VARSTART = "${";
	private static final String DEFAULT_VAREND = "}";

	private StringUtil() {
		// no instances
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

}
