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
 * Constants defining the positions and lengths of the elements of an ISO
 * 2709:2008 record.
 *
 * @author Christoph Böhme
 *
 */
public final class Iso2709Format {

	public static final int RECORD_LABEL_LENGTH = 24;

	public static final int RECORD_LENGTH_START = 0;
	public static final int RECORD_LENGTH_LENGTH = 5;
	public static final int RECORD_LENGTH_END = RECORD_LENGTH_START
			+ RECORD_LENGTH_LENGTH;

	public static final int RECORD_STATUS_POS = 5;

	public static final int IMPL_CODES_START = 6;
	public static final int IMPL_CODES_LENGTH = 4;
	public static final int IMPL_CODES_END = IMPL_CODES_START
			+ IMPL_CODES_LENGTH;

	public static final int INDICATOR_LENGTH_POS = 10;
	public static final int IDENTIFIER_LENGTH_POS = 11;

	public static final int BASE_ADDRESS_START = 12;
	public static final int BASE_ADDRESS_LENGTH = 5;
	public static final int BASE_ADDRESS_END = BASE_ADDRESS_START
			+ BASE_ADDRESS_LENGTH;

	public static final int SYSTEM_CHARS_START = 17;
	public static final int SYSTEM_CHARS_LENGTH = 3;
	public static final int SYSTEM_CHARS_END = SYSTEM_CHARS_START
			+ SYSTEM_CHARS_LENGTH;

	public static final int FIELD_LENGTH_LENGTH_POS = 20;
	public static final int FIELD_START_LENGTH_POS = 21;
	public static final int IMPL_DEFINED_PART_LENGTH_POS = 22;
	public static final int RESERVED_CHAR_POS = 23;

	public static final int TAG_LENGTH = 3;

	public static final char IDENTIFIER_MARKER = Iso646Characters.IS1;
	public static final char FIELD_SEPARATOR = Iso646Characters.IS2;
	public static final char RECORD_SEPARATOR = Iso646Characters.IS3;

	private Iso2709Format() {
		// No instance allowed
	}

}
