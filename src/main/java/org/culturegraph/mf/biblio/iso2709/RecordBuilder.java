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

import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.IMPL_CODES_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.MAX_PAYLOAD_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_LABEL_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.SYSTEM_CHARS_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.TAG_LENGTH;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.culturegraph.mf.commons.Require;
import org.culturegraph.mf.framework.FormatException;

/**
 * Builds records in ISO2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordBuilder {

	private static final char[] EMPTY_IDENTIFIER = new char[0];
	private static final char[] ID_FIELD_TAG = { '0', '0', '1' };
	private static final Pattern REFERENCE_FIELD_TAG_PATTERN = Pattern.compile(
			"^00[1-9a-zA-Z]$");
	private static final Pattern DATA_FIELD_TAG_PATTERN = Pattern.compile(
			"^(0[1-9a-zA-Z][0-9a-zA-Z])|([1-9a-zA-Z][0-9a-zA-Z]{2})$");

	private final LabelBuilder label;
	private final DirectoryBuilder directory;
	private final FieldsBuilder fields;

	private final int indicatorLength;
	private final int identifierLength;
	private final int implDefinedPartLength;

	private final char[] defaultImplDefinedPart;
	private final char[] defaultIndicators;
	private final char[] defaultIdentifier;

	private final char[] tag = new char[TAG_LENGTH];
	private final char[] implDefinedPart;

	private AppendState appendState;
	private int fieldStart;

	public RecordBuilder(final RecordFormat recordFormat) {
		Require.notNull(recordFormat);
		label = new LabelBuilder(recordFormat);
		directory = new DirectoryBuilder(recordFormat);
		fields = new FieldsBuilder(recordFormat, MAX_PAYLOAD_LENGTH);
		indicatorLength = recordFormat.getIndicatorLength();
		identifierLength = recordFormat.getIdentifierLength();
		implDefinedPartLength = recordFormat.getImplDefinedPartLength();
		defaultImplDefinedPart = arrayOfNSpaceChars(implDefinedPartLength);
		defaultIndicators = arrayOfNSpaceChars(indicatorLength);
		if (identifierLength > 1) {
			defaultIdentifier = arrayOfNSpaceChars(identifierLength - 1);
		} else {
			defaultIdentifier = EMPTY_IDENTIFIER;
		}
		implDefinedPart = new char[implDefinedPartLength];
		appendState = AppendState.ID_FIELD;
	}

	private char[] arrayOfNSpaceChars(final int count) {
		final char[] chars = new char[count];
		Arrays.fill(chars, ' ');
		return chars;
	}

	public void setCharset(final Charset charset) {
		fields.setCharset(Require.notNull(charset));
	}

	public Charset getCharset() {
		return fields.getCharset();
	}

	public void setRecordStatus(final char recordStatus) {
		require7BitAscii(recordStatus);
		label.setRecordStatus(recordStatus);
	}

	public void setImplCodes(final char[] implCodes) {
		Require.notNull(implCodes);
		Require.that(implCodes.length == IMPL_CODES_LENGTH);
		for (final char implCode : implCodes) {
			Require.that(implCode < Iso646Constants.MAX_CHAR_CODE);
		}
		label.setImplCodes(implCodes);
	}

	public void setImplCode(final int index, final char value) {
		Require.that(0 <= index && index < IMPL_CODES_LENGTH);
		Require.that(value < Iso646Constants.MAX_CHAR_CODE);
		label.setImplCode(index, value);
	}

	public void setSystemChars(final char[] systemChars) {
		Require.notNull(systemChars);
		Require.that(systemChars.length == SYSTEM_CHARS_LENGTH);
		require7BitAscii(systemChars);
		label.setSystemChars(systemChars);
	}

	public void setSystemChar(final int index, final char value) {
		Require.that(0 <= index && index < IMPL_CODES_LENGTH);
		Require.that(value < Iso646Constants.MAX_CHAR_CODE);
		label.setSystemChar(index, value);
	}

	public void setReservedChar(final char reservedChar) {
		require7BitAscii(reservedChar);
		label.setReservedChar(reservedChar);
	}

	public void appendIdentifierField(final String value) {
		appendIdentifierField(defaultImplDefinedPart,value);
	}

	public void appendIdentifierField(final char[] implDefinedPart,
			final String value) {
		requireNotInDataField();
		requireNotAppendingReferenceFields();
		requireNotAppendingDataFields();
		if (appendState != AppendState.ID_FIELD) {
			throw new IllegalStateException("no id field allowed");
		}
		appendReferenceField(ID_FIELD_TAG, implDefinedPart, value);
	}

	public void appendReferenceField(final char[] tag, final String value) {
		appendReferenceField(tag, defaultImplDefinedPart, value);
	}

	public void appendReferenceField(final char[] tag,
			final char[] implDefinedPart, final String value) {
		requireNotInDataField();
		requireNotAppendingDataFields();
		Require.notNull(tag);
		Require.notNull(implDefinedPart);
		Require.that(implDefinedPart.length == implDefinedPartLength);
		require7BitAscii(implDefinedPart);
		Require.notNull(value);

		checkValidReferenceFieldTag(tag);
		final int fieldStart = fields.startField();
		fields.appendValue(value);
		final int fieldEnd = fields.endField();
		try {
			directory.addEntries(tag, implDefinedPart, fieldStart, fieldEnd);
		} catch (final FormatException e) {
			fields.undoLastField();
			throw e;
		}
		appendState = AppendState.REFERENCE_FIELD;
	}

	private void checkValidReferenceFieldTag(final char[] tag) {
		if (!REFERENCE_FIELD_TAG_PATTERN.matcher(String.valueOf(tag)).matches()) {
			throw new FormatException("invalid tag format for reference field");
		}
	}

	public void startDataField(final char[] tag) {
		startDataField(tag, defaultIndicators);
	}

	public void startDataField(final char[] tag, final char[] indicators) {
		startDataField(tag, indicators, defaultImplDefinedPart);
	}

	public void startDataField(final char[] tag, final char[] indicators,
			final char[] implDefinedPart) {
		requireNotInDataField();
		Require.notNull(tag);
		Require.notNull(indicators);
		Require.that(indicators.length == indicatorLength);
		require7BitAscii(indicators);
		Require.notNull(implDefinedPart);
		Require.that(implDefinedPart.length == implDefinedPartLength);
		require7BitAscii(implDefinedPart);

		checkValidDataFieldTag(tag);
		copyArray(tag, this.tag);
		copyArray(implDefinedPart, this.implDefinedPart);
		fieldStart = fields.startField(indicators);
		appendState = AppendState.IN_DATA_FIELD;
	}

	private void checkValidDataFieldTag(final char[] tag) {
		if (!DATA_FIELD_TAG_PATTERN.matcher(String.valueOf(tag)).matches()) {
			throw new FormatException("invalid tag format for data field");
		}
	}

	private void copyArray(final char[] source, final char[] destination) {
		System.arraycopy(source, 0, destination, 0, destination.length);
	}

	public void endDataField() {
		requireInDataField();
		final int fieldEnd = fields.endField();
		appendState = AppendState.DATA_FIELD;
		try {
			directory.addEntries(tag, implDefinedPart, fieldStart, fieldEnd);
		} catch (final FormatException e) {
			fields.undoLastField();
			throw e;
		}
	}

	public void appendSubfield(final String value) {
		requireInDataField();
		Require.notNull(value);
		fields.appendSubfield(defaultIdentifier, value);
	}

	public void appendSubfield(final char[] identifier, final String value) {
		requireInDataField();
		Require.notNull(identifier);
		require7BitAscii(identifier);
		Require.that(identifier.length + 1 == identifierLength);
		Require.notNull(value);
		fields.appendSubfield(identifier, value);
	}

	public byte[] build() {
		requireNotInDataField();
		final int baseAddress = RECORD_LABEL_LENGTH + directory.length();
		final int recordLength = baseAddress + fields.length();
		label.setBaseAddress(baseAddress);
		label.setRecordLength(recordLength);
		final byte[] recordBuffer = new byte[recordLength];
		label.copyToBuffer(recordBuffer);
		directory.copyToBuffer(recordBuffer, RECORD_LABEL_LENGTH);
		fields.copyToBuffer(recordBuffer, baseAddress);
		return recordBuffer;
	}

	private void requireNotAppendingReferenceFields() {
		if (appendState == AppendState.REFERENCE_FIELD) {
			throw new IllegalStateException("must not be appending reference fields");
		}
	}

	private void requireNotAppendingDataFields() {
		if (appendState == AppendState.DATA_FIELD) {
			throw new IllegalStateException("must not be appending data fields");
		}
	}

	private void requireInDataField() {
		if (appendState != AppendState.IN_DATA_FIELD) {
			throw new IllegalStateException();
		}
	}

	private void requireNotInDataField() {
		if (appendState == AppendState.IN_DATA_FIELD) {
			throw new IllegalStateException("must not be in field");
		}
	}

	private void require7BitAscii(final char[] charArray) {
		for (final char charCode : charArray) {
			require7BitAscii(charCode);
		}
	}

	private void require7BitAscii(final char charCode) {
		Require.that(charCode <= Iso646Constants.MAX_CHAR_CODE);
		Require.that(charCode != Iso646Constants.INFORMATION_SEPARATOR_1);
		Require.that(charCode != Iso646Constants.INFORMATION_SEPARATOR_2);
		Require.that(charCode != Iso646Constants.INFORMATION_SEPARATOR_3);
	}

	public void reset() {
		label.reset();
		directory.reset();
		fields.reset();
		appendState = AppendState.ID_FIELD;
	}

	@Override
	public String toString() {
		return "label: " + label.toString() + "\n"
				+ "directory: " + directory.toString() + "\n"
				+ "fields: " + fields.toString();
	}

	private enum AppendState {
		ID_FIELD,
		REFERENCE_FIELD,
		DATA_FIELD,
		IN_DATA_FIELD
	}

}
