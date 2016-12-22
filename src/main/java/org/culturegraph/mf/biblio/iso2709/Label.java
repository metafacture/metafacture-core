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

import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.BASE_ADDRESS_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.BASE_ADDRESS_START;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.FIELD_LENGTH_LENGTH_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.FIELD_START_LENGTH_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.IDENTIFIER_LENGTH_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.IMPL_CODES_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.IMPL_CODES_START;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.IMPL_DEFINED_PART_LENGTH_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.INDICATOR_LENGTH_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_LABEL_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_LENGTH_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_LENGTH_START;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_STATUS_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RESERVED_CHAR_POS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.SYSTEM_CHARS_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.SYSTEM_CHARS_START;

/**
 * Provides read access to the record label of a ISO 2709:2008 formatted
 * record. The record label consists of the first 24 octets of the record.
 * <p>
 * Use {@link LabelBuilder} if write access to the label is required.
 *
 * @author Christoph Böhme
 */
class Label {

	private final Iso646ByteBuffer buffer;

	Label(final Iso646ByteBuffer buffer) {
		final int bufferLength = buffer.getLength();
		assert bufferLength >= RECORD_LABEL_LENGTH;
		this.buffer = buffer;
	}

	RecordFormat getRecordFormat() {
		return RecordFormat.create()
				.withIndicatorLength(getIndicatorLength())
				.withIdentifierLength(getIdentifierLength())
				.withFieldLengthLength(getFieldLengthLength())
				.withFieldStartLength(getFieldStartLength())
				.withImplDefinedPartLength(getImplDefinedPartLength())
				.build();
	}

	int getRecordLength() {
		return buffer.parseIntAt(RECORD_LENGTH_START, RECORD_LENGTH_LENGTH);
	}

	char getRecordStatus() {
		return buffer.charAt(RECORD_STATUS_POS);
	}

	char[] getImplCodes() {
		return buffer.charsAt(IMPL_CODES_START, IMPL_CODES_LENGTH);
	}

	int getIndicatorLength() {
		return buffer.parseIntAt(INDICATOR_LENGTH_POS);
	}

	int getIdentifierLength() {
		return buffer.parseIntAt(IDENTIFIER_LENGTH_POS);
	}

	int getBaseAddress() {
		return buffer.parseIntAt(BASE_ADDRESS_START, BASE_ADDRESS_LENGTH);
	}

	char[] getSystemChars() {
		return buffer.charsAt(SYSTEM_CHARS_START, SYSTEM_CHARS_LENGTH);
	}

	int getFieldLengthLength() {
		return buffer.parseIntAt(FIELD_LENGTH_LENGTH_POS);
	}

	int getFieldStartLength() {
		return buffer.parseIntAt(FIELD_START_LENGTH_POS);
	}

	int getImplDefinedPartLength() {
		return buffer.parseIntAt(IMPL_DEFINED_PART_LENGTH_POS);
	}

	char getReservedChar() {
		return buffer.charAt(RESERVED_CHAR_POS);
	}

	@Override
	public String toString() {
		return buffer.stringAt(0, RECORD_LABEL_LENGTH, Iso646Constants.CHARSET);
	}

}
