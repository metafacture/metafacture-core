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

import static org.culturegraph.mf.util.StringUtil.repeatChars;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.util.Require;

/**
 * Builds records in ISO2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordBuilder {

	private final Matcher referenceFieldTagMatcher = Pattern.compile(
			"^00[1-9a-zA-Z]$").matcher("");
	private final Matcher dataFieldTagMatcher = Pattern.compile(
			"^(0[1-9a-zA-Z][0-9a-zA-Z])|([1-9a-zA-Z][0-9a-zA-Z]{2})$").matcher(
			"");

	private final LabelBuilder label;
	private final DirectoryBuilder directory;
	private final FieldsBuilder fields;

	private final int indicatorLength;
	private final int identifierLength;
	private final int implDefinedPartLength;

	private final String defaultImplDefinedPart;
	private final String defaultIndicators;
	private final String defaultIdentifier;

	public RecordBuilder(final RecordFormat format) {
		Require.notNull(format);

		label = new LabelBuilder(format);
		directory = new DirectoryBuilder(format);
		fields = new FieldsBuilder(format);

		indicatorLength = format.getIndicatorLength();
		identifierLength = format.getIdentifierLength();
		implDefinedPartLength = format.getImplDefinedPartLength();

		defaultImplDefinedPart = repeatChars(' ', implDefinedPartLength);
		defaultIndicators = repeatChars(' ', indicatorLength);
		if (identifierLength > 0) {
			defaultIdentifier = repeatChars(' ', identifierLength - 1);
		} else {
			defaultIdentifier = "";
		}
	}

	public void setRecordStatus(final char recordStatus) {
		label.setRecordStatus(recordStatus);
	}

	public void setImplCodes(final String implCodes) {
		Require.notNull(implCodes);
		Require.that(implCodes.length() == Iso2709Format.IMPL_CODES_LENGTH);
		label.setImplCodes(implCodes);
	}

	public void setSystemChars(final String systemChars) {
		Require.notNull(systemChars);
		Require.that(systemChars.length() == Iso2709Format.SYSTEM_CHARS_LENGTH);
		label.setSystemChars(systemChars);
	}

	public void setReservedChar(final char reservedChar) {
		label.setReservedChar(reservedChar);
	}

	public void appendReferenceField(final String tag, final String value) {
		appendReferenceField(tag, defaultImplDefinedPart, value);
	}

	public void appendReferenceField(final String tag,
			final String implDefinedPart, final String value) {

		Require.notNull(tag);
		Require.notNull(implDefinedPart);
		Require.that(implDefinedPart.length() == implDefinedPartLength);
		Require.notNull(value);

		checkReferenceFieldTagFormat(tag);

		final int fieldStart = fields.startField("");
		fields.appendValue(value);
		final int fieldEnd = fields.endField();

		directory.setTag(tag);
		directory.setImplDefinedPart(implDefinedPart);
		directory.setFieldStart(fieldStart);
		directory.setFieldEnd(fieldEnd);

		try {
			directory.write();
		} catch (final FormatException e) {
			fields.undoLastField();
			throw e;
		}
	}

	private void checkReferenceFieldTagFormat(final String tag) {
		if (!referenceFieldTagMatcher.reset(tag).matches()) {
			throw new FormatException("Invalid tag format for reference field");
		}
	}

	public void startField(final String tag) {
		startField(tag, defaultIndicators);
	}

	public void startField(final String tag, final String indicators) {
		startField(tag, indicators, defaultImplDefinedPart);
	}

	public void startField(final String tag, final String indicators,
			final String implDefinedPart) {

		Require.notNull(tag);
		Require.notNull(indicators);
		Require.that(indicators.length() == indicatorLength);
		Require.notNull(implDefinedPart);
		Require.that(implDefinedPart.length() == implDefinedPartLength);

		checkDataFieldTagFormat(tag);

		final int fieldStart = fields.startField(indicators);

		directory.setTag(tag);
		directory.setImplDefinedPart(implDefinedPart);
		directory.setFieldStart(fieldStart);
	}

	private void checkDataFieldTagFormat(final String tag) {
		if (!dataFieldTagMatcher.reset(tag).matches()) {
			throw new FormatException("Invalid tag format for data field");
		}
	}

	public void endField() {
		final int fieldEnd = fields.endField();

		directory.setFieldEnd(fieldEnd);

		try {
			directory.write();
		} catch (final FormatException e) {
			fields.undoLastField();
			throw e;
		}
	}

	public void appendSubfield(final String value) {
		Require.notNull(value);
		fields.appendSubfield(defaultIdentifier, value);
	}

	public void appendSubfield(final String identifier, final String value) {
		Require.notNull(identifier);
		Require.notNull(value);
		Require.that(identifier.length() + 1 == identifierLength);

		fields.appendSubfield(identifier, value);
	}

	public void reset() {
		label.reset();
		directory.reset();
		fields.reset();
	}

	@Override
	public String toString() {
		final int baseAddress = label.length() + directory.length();
		final int recordLength = baseAddress + fields.length();

		label.setBaseAddress(baseAddress);
		label.setRecordLength(recordLength);

		return label.toString() + directory.toString() + fields.toString();
	}

}
