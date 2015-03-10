/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.iso2709;


/**
 * @author Christoph Böhme
 *
 */
final class Util {

	private static final int RADIX = 10;

	private Util() {
		// No instances allowed
	}

	public static char toDigit(final int value) {
		assert 0 <= value && value < RADIX;
		return Character.forDigit(value, RADIX);
	}

	public static String padWithZeros(final int value, final int length) {
		assert length >= 0;

		final String format = "%0" + length + "d";
		return String.format(format, value);
	}

	public static int calculateMaxValue(final int digits) {
		int maxValue = 1;
		for (int i = 0; i < digits; ++i) {
			maxValue *= RADIX;
		}
		return maxValue - 1;
	}

}
