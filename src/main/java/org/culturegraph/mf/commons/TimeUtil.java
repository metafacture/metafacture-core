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

/**
 * Time related utility functions.
 *
 * @author Christoph Böhme
 */
public final class TimeUtil {

	public static final String[] UNIT_SYMBOLS = { "ns", "µs", "ms", "s", "min", "h" };
	public static final long[] UNIT_FACTORS = { 1L, 1000L, 1000L, 1000L, 60L, 60L };

	public static final int BASE_UNIT_INDEX = 3;

	public static final long HOURS = 60L * 60L * 1000L * 1000L * 1000L;
	public static final long MINUTES = 60L * 1000L * 1000L * 1000L;
	public static final long SECONDS = 1000L * 1000L * 1000L;
	public static final long MILLISECONDS = 1000L * 1000L;
	public static final long MICROSECONDS = 1000L;
	public static final long NANOSECONDS = 1L;

	private TimeUtil() {
		// No instances allowed
	}

	public static String formatDuration(final long duration) {
		long major = duration;
		long minor = 0;
		int i = -1;
		while (i < UNIT_FACTORS.length - 1 && major >= UNIT_FACTORS[i + 1]) {
			long carry = 0;
			if (i > 0 && minor >= UNIT_FACTORS[i] / 2) {
				carry = 1;
			}
			i += 1;
			minor = major % UNIT_FACTORS[i] + carry;
			major /= UNIT_FACTORS[i];
		}

		if (i == 0 || minor == 0) {
			if (i < 0) {
				i = BASE_UNIT_INDEX;  // Use seconds as default unit
			}
			return String.format("%d%s", Long.valueOf(major), UNIT_SYMBOLS[i]);
		}
		return String.format("%d%s %d%s", Long.valueOf(major), UNIT_SYMBOLS[i], Long.valueOf(minor), UNIT_SYMBOLS[i - 1]);
	}

}
