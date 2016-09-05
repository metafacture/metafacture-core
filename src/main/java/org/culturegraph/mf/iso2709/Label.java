/*
 *  Copyright 2016 Christoph Böhme
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

import org.culturegraph.mf.exceptions.FormatException;

/**
 * Provides access to the record label of a ISO 2709:2008 formatted record. The
 * record label consists of first 24 octets of the record.
 *
 * @throws FormatException
 *             will be thrown by all methods except for the {@link #toString()}
 *             method if the size of the record buffer is less than 24
 *             octets.
 *
 * @author Christoph Böhme
 *
 */
public class Label {

	private final Iso646ByteBuffer buffer;

	public Label(final Iso646ByteBuffer buffer) {
		this.buffer = buffer;
		verifyBufferLength();
	}

	private void verifyBufferLength() {
		if (buffer.getLength() < Iso2709Format.RECORD_LABEL_LENGTH) {
			throw new FormatException("Record is too short");
		}
	}

	/**
	 * Returns a {@link RecordFormat} object for the record.
	 */
	public RecordFormat getRecordFormat() {
		final RecordFormat recordFormat = new RecordFormat();
		recordFormat.setIndicatorLength(getIndicatorLength());
		recordFormat.setIdentifierLength(getIdentifierLength());
		recordFormat.setFieldLengthLength(getFieldLengthLength());
		recordFormat.setFieldStartLength(getFieldStartLength());
		recordFormat.setImplDefinedPartLength(getImplDefinedPartLength());
		return recordFormat;
	}

	/**
	 * Returns the integer value presented by the characters at positions 0 to 4
	 * in the record (record length).
	 *
	 * Defined in section 4.3.2 of the ISO 2709:2008 standard.
	 */
	public int getRecordLength() {
		return buffer.parseIntAt(Iso2709Format.RECORD_LENGTH_START,
				Iso2709Format.RECORD_LENGTH_LENGTH);
	}

	/**
	 * Returns the character at position 5 in the record (record status).
	 *
	 * Defined in section 4.3.3 of the ISO 2709:2008 standard.
	 */
	public char getRecordStatus() {
		return buffer.charAt(Iso2709Format.RECORD_STATUS_POS);
	}

	/**
	 * Returns the characters at positions 6 to 9 in the record (implementation
	 * codes).
	 *
	 * Defined in section 4.3.4 of the ISO 2709:2008 standard.
	 */
	public char[] getImplCodes() {
		return buffer.charsAt(Iso2709Format.IMPL_CODES_START,
				Iso2709Format.IMPL_CODES_LENGTH);
	}

	/**
	 * Returns the integer value presented by the character at position 10 in
	 * the record (indicator length).
	 *
	 * Defined in section 4.3.5 of the ISO 2709:2008 standard.
	 */
	public int getIndicatorLength() {
		return buffer.parseIntAt(Iso2709Format.INDICATOR_LENGTH_POS);
	}

	/**
	 * Returns the integer value presented by the character at position 11 in
	 * the record (identifier length).
	 *
	 * Defined in section 4.3.6 of the ISO 2709:2008 standard.
	 */
	public int getIdentifierLength() {
		return buffer.parseIntAt(Iso2709Format.IDENTIFIER_LENGTH_POS);
	}

	/**
	 * Returns the integer value presented by the characters at positions 12 to
	 * 16 in the record (base address of data).
	 *
	 * Defined in section 4.3.7 of the ISO 2709:2008 standard.
	 */
	public int getBaseAddress() {
		return buffer.parseIntAt(Iso2709Format.BASE_ADDRESS_START,
				Iso2709Format.BASE_ADDRESS_LENGTH);
	}

	/**
	 * Returns the characters at positions 17 to 19 in the record (for user
	 * systems).
	 *
	 * Defined in section 4.3.8 of the ISO 2709:2008 standard.
	 */
	public char[] getSystemChars() {
		return buffer.charsAt(Iso2709Format.SYSTEM_CHARS_START,
				Iso2709Format.SYSTEM_CHARS_LENGTH);
	}

	/**
	 * Returns the integer value represented by the character at position 20
	 * (length of field length in directory entries).
	 *
	 * Defined in section 4.3.9(a) of the ISO 2709:2008 standard.
	 */
	public int getFieldLengthLength() {
		return buffer.parseIntAt(Iso2709Format.FIELD_LENGTH_LENGTH_POS);
	}

	/**
	 * Returns the integer value represented by the character at position 21
	 * (length of starting character position in directory entries).
	 *
	 * Defined in section 4.3.9(b) of the ISO 2709:2008 standard.
	 */
	public int getFieldStartLength() {
		return buffer.parseIntAt(Iso2709Format.FIELD_START_LENGTH_POS);
	}

	/**
	 * Returns the integer value represented by the character at position 22
	 * (length of implementation defined part in directory entries).
	 *
	 * Defined in section 4.3.9(c) of the ISO 2709:2008 standard.
	 */
	public int getImplDefinedPartLength() {
		return buffer.parseIntAt(Iso2709Format.IMPL_DEFINED_PART_LENGTH_POS);
	}

	/**
	 * Returns the character at position 23 (reserved for future use).
	 *
	 * Defined in section 4.3.9(d) of the ISO 2709:2008 standard.
	 */
	public char getReservedChar() {
		return buffer.charAt(Iso2709Format.RESERVED_CHAR_POS);
	}

	/**
	 * Returns the 24 characters of the record label. If the record buffer does
	 * not contain a valid record an empty string is returned.
	 */
	@Override
	public String toString() {
		if (buffer.getLength() < Iso2709Format.RECORD_LABEL_LENGTH) {
			return "";
		}
		return String.valueOf(buffer.charsAt(0, Iso2709Format.RECORD_LABEL_LENGTH));
	}

}
