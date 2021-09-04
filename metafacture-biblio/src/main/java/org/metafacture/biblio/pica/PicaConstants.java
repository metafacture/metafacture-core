/* Copyright 2016,2019 Christoph Böhme and others
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

package org.metafacture.biblio.pica;

/**
 * Useful constants for PICA+.
 * PICA+ comes with two possible serializations:
 * a normalized one and a non-normalized.
 *
 * @author Christoph Böhme (initial implementation)
 * @author Pascal Christoph (dr0i) (add support for non-normalized pica+)
 * @author Fabian Steeg (fsteeg) (switch to enum)
 *
 */
enum PicaConstants {
	// We use '\0' for null/empty
	RECORD_MARKER('\u001d', '\n'), //
	FIELD_MARKER('\u001e', '\0'), //
	SUBFIELD_MARKER('\u001f', '$'), //
	FIELD_END_MARKER('\n', '\n'), //
	NO_MARKER('\0', '\0');

	char normalized;
	char nonNormalized;

	PicaConstants(char normalized, char nonNormalized) {
		this.normalized = normalized;
		this.nonNormalized = nonNormalized;
	}

	public char get(boolean isNormalized) {
		return isNormalized ? normalized : nonNormalized;
	}

	public static PicaConstants from(boolean isNormalized, char ch) {
		for (PicaConstants value : values()) {
			if (ch == (isNormalized ? value.normalized : value.nonNormalized)) {
				return value;
			}
		}
		return NO_MARKER;
	}
}
