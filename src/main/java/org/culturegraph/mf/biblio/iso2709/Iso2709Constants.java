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

/**
 * Constants defining the positions and lengths of the elements of an ISO
 * 2709:2008 record.
 *
 * @author Christoph Böhme
 *
 */
final class Iso2709Constants {

	static final int RECORD_LABEL_LENGTH = 24;

	static final int MIN_RECORD_LENGTH = RECORD_LABEL_LENGTH + 2;
	static final int MAX_RECORD_LENGTH = 99_999;

	static final int MIN_BASE_ADDRESS = RECORD_LABEL_LENGTH + 1;
	static final int MAX_BASE_ADDRESS = MAX_RECORD_LENGTH - 1;

	static final int MAX_PAYLOAD_LENGTH = MAX_RECORD_LENGTH - MIN_BASE_ADDRESS;

	static final int RECORD_LENGTH_START = 0;
	static final int RECORD_LENGTH_LENGTH = 5;

	static final int RECORD_STATUS_POS = 5;

	static final int IMPL_CODES_START = 6;
	static final int IMPL_CODES_LENGTH = 4;

	static final int INDICATOR_LENGTH_POS = 10;
	static final int IDENTIFIER_LENGTH_POS = 11;

	static final int BASE_ADDRESS_START = 12;
	static final int BASE_ADDRESS_LENGTH = 5;

	static final int SYSTEM_CHARS_START = 17;
	static final int SYSTEM_CHARS_LENGTH = 3;

	static final int FIELD_LENGTH_LENGTH_POS = 20;
	static final int FIELD_START_LENGTH_POS = 21;
	static final int IMPL_DEFINED_PART_LENGTH_POS = 22;
	static final int RESERVED_CHAR_POS = 23;

	static final int TAG_LENGTH = 3;

	static final byte IDENTIFIER_MARKER = Iso646Constants.INFORMATION_SEPARATOR_1;
	static final byte FIELD_SEPARATOR = Iso646Constants.INFORMATION_SEPARATOR_2;
	static final byte RECORD_SEPARATOR = Iso646Constants.INFORMATION_SEPARATOR_3;

	private Iso2709Constants() {
		throw new AssertionError("class should not be instantiated");
	}

}
