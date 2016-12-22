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
import java.nio.charset.StandardCharsets;

/**
 * Constant definitions for the ISO646:1991 standard.
 *
 * @author Christoph Böhme
 *
 */
final class Iso646Constants {

	static final Charset CHARSET = StandardCharsets.US_ASCII;

	static final char INFORMATION_SEPARATOR_3 = 0x1d;
	static final char INFORMATION_SEPARATOR_2 = 0x1e;
	static final char INFORMATION_SEPARATOR_1 = 0x1f;

	static final char ZERO = 0x30;
	static final char NINE = 0x39;

	static final char MAX_CHAR_CODE = 0x7f;

	private Iso646Constants() {
		throw new AssertionError("class should not be instantiated");
	}

}
