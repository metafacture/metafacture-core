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
package org.culturegraph.mf.biblio.pica;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MissingIdException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Parses pica+ records. The parser only parses single records. A string
 * containing multiple records must be split into individual records before
 * passing it to {@code PicaDecoder}.
 * <p>
 * The parser is designed to accept any string as valid input and to parse pica
 * plain format as well as normalised pica. To achieve this, the parser behaves
 * as following:
 * <ul>
 *   <li>The parser assumes that the input starts with a field name.
 *
 *   <li>The field name and the first subfield are separated by a subfield
 *   marker (&#92;u001f).
 *
 *   <li>Fields are separated by record markers (&#92;u001d), field
 *   markers (&#92;u001e) or field end markers (&#92;u000a).
 *
 *   <li>Subfields are separated by subfield markers (&#92;u001f).
 *
 *   <li>The first character of a subfield is the name of the subfield
 *
 *   <li>The parser assumes that the end of the input marks the end of the
 *   current field and the end of the record.
 *
 *   <li>To handle input with multiple field and subfield separators following
 *   each other directly (for instance &#92;u000a and &#92;u001e), it is assumed
 *   that field names, subfields, subfield names or subfield values can be
 *   empty.
 * </ul>
 * Please note that the record marker is treated as a field delimiter and not
 * as a record delimiter. Records need to be separated prior to parsing them.
 * <p>
 * As the behaviour of the parser may result in unnamed fields or subfields or
 * fields with no subfields the {@code PicaDecoder} automatically filters empty
 * fields and subfields:
 * <ul>
 *   <li>Subfields without a name are ignored (such subfields cannot have any
 *   value because then the first character of the value would be the name of
 *   the subfield).
 *
 *   <li>Subfields which only have a name but no value are always parsed.
 *
 *   <li>Unnamed fields are only parsed if the contain not-ignored subfields.
 *
 *   <li>Named fields containing none or only ignored subfields are only parsed
 *   if {@link #setSkipEmptyFields(boolean)} is set to false otherwise they are
 *   ignored.
 *
 *   <li>Input containing only whitespace (spaces and tabs) is completely
 *   ignored.
 * </ul>
 * The {@code PicaDecoder} emits <i>start-entity</i> and <i>end-entity</i>
 * events for each parsed field and <i>literal</i> events for each parsed
 * subfield. Field names are trimmed by default (leading and trailing whitespace
 * is removed). This can be changed by setting
 * {@link #setTrimFieldNames(boolean)} to false.
 * <p>
 * The record id emitted with the <i>start-record</i> event is extracted from
 * one of the following pica fields:
 * <ul>
 *   <li><i>003&#64; $0</i>
 *   <li><i>107F $0</i>
 *   <li><i>203&#64; $0</i> (this field may have an optional occurrence marker)
 * </ul>
 * The value of the first matching field is used as the record id. The <i>$0</i>
 * subfield must be the first subfield in the field. If
 * {@link #setIgnoreMissingIdn(boolean)} is false and no matching field is not
 * found in the record a {@link MissingIdException} is thrown otherwise the
 * record identifier is an empty string.
 * <p>
 * For example, when run on the input
 * <pre>
 * 003&#64; &#92;u001f01234&#92;u001e
 * 028A &#92;u001faAndy&#92;u001fdWarhol&#92;u001e
 * </pre>
 *
 * the {@code PicaDecoder} will produce the following sequence of events:
 * <pre>{@literal
 * start-record "1234"
 * start-entity "003@"
 * literal "0": 1234
 * end-entity
 * start-entity "028A"
 * literal "a": Andy
 * literal "d": Warhol
 * end-entity
 * end-record
 * }</pre>
 *
 * The parser assumes that the input is utf-8 encoded. The parser does not
 * support other pica encodings.
 *
 * @author Christoph Böhme
 *
 */
@Description("Parses pica+ records. The parser only parses single records. " +
		"A string containing multiple records must be split into " +
		"individual records before passing it to PicaDecoder.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-pica")
public final class PicaDecoder
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final String START_MARKERS ="(?:^|" + PicaConstants.FIELD_MARKER +
			"|" + PicaConstants.FIELD_END_MARKER + "|" + PicaConstants.RECORD_MARKER + ")";
	private static final Pattern ID_FIELDS_PATTERN = Pattern.compile(
			START_MARKERS + "(?:003@|203@(?:/..+)?|107F) " + PicaConstants.SUBFIELD_MARKER + "0");

	private static final int BUFFER_SIZE = 1024 * 1024;

	private final Matcher idFieldMatcher = ID_FIELDS_PATTERN.matcher("");
	private final StringBuilder idBuilder = new StringBuilder();
	private final PicaParserContext parserContext = new PicaParserContext();

	private char[] buffer = new char[BUFFER_SIZE];
	private int recordLen;

	private boolean ignoreMissingIdn;

	/**
	 * Controls whether records having no record id are reported as faulty. By
	 * default such records are reported by the {@code PicaDecoder} by throwing
	 * a {@link MissingIdException}.
	 * <p>
	 * The setting can be changed at any time. It becomes effective with the next
	 * record that is being processed.
	 * <p>
	 * <strong>Default value: {@code false}</strong>
	 *
	 * @param ignoreMissingIdn if true, missing record ids do not trigger a
	 *                         {@link MissingIdException} but an empty string is
	 *                         used as record identifier instead.
	 */
	public void setIgnoreMissingIdn(final boolean ignoreMissingIdn) {
		this.ignoreMissingIdn = ignoreMissingIdn;
	}

	public boolean getIgnoreMissingIdn() {
		return ignoreMissingIdn;
	}

	/**
	 * Controls whether decomposed unicode characters in field values are
	 * normalised to their precomposed version. By default no normalisation is
	 * applied. The normalisation is only applied to values not to field or
	 * subfield names.
	 * <p>
	 * The setting can be changed at any time. It becomes effective with the next
	 * record that is being processed.
	 * <p>
	 * <strong>Default value: {@code false}</strong>
	 *
	 * @param normalizeUTF8 if true, decomposed unicode characters in values are
	 *                      normalised to their precomposed version.
	 */
	public void setNormalizeUTF8(final boolean normalizeUTF8) {
		parserContext.setNormalizeUTF8(normalizeUTF8);
	}

	public boolean getNormalizeUTF8() {
		return parserContext.getNormalizeUTF8();
	}

	/**
	 * Controls whether fields without subfields are skipped and no events are
	 * emitted for them. By default empty fields are skipped.
	 * <p>
	 * The setting can be changed at any time. It becomes effective with the next
	 * record that is being processed.
	 * <p>
	 * <strong>Default value: {@code true}</strong>
	 *
	 * @param skipEmptyFields if true, then empty fields are skipped.
	 */
	public void setSkipEmptyFields(final boolean skipEmptyFields) {
		parserContext.setSkipEmptyFields(skipEmptyFields);
	}

	public boolean getSkipEmptyFields() {
		return parserContext.getSkipEmptyFields();
	}

	/**
	 * Sets whether field names are trimmed (removal of leading and trailing
	 * whitespace). By default field names are trimmed.
	 * <p>
	 * The setting can be changed at any time. It becomes effective with the next
	 * record that is being processed.
	 * <p>
	 * <strong>Default value: {@code true}</strong>
	 *
	 * @param trimFieldNames if true, then field names are trimmed.
	 */
	public void setTrimFieldNames(final boolean trimFieldNames) {
		parserContext.setTrimFieldNames(trimFieldNames);
	}

	public boolean getTrimFieldNames() {
		return parserContext.getTrimFieldNames();
	}
	@Override
	public void process(final String record) {
		assert !isClosed();

		buffer = StringUtil.copyToBuffer(record, buffer);
		recordLen = record.length();

		if (isRecordEmpty()) {
			return;
		}

		String id = extractRecordId();
		if (id == null) {
			if (!ignoreMissingIdn) {
				throw new MissingIdException("Record has no id");
			}
			id = "";
		}
		getReceiver().startRecord(id);

		PicaParserState state = PicaParserState.FIELD_NAME;
		for (int i = 0; i < recordLen; ++i) {
			state = state.parseChar(buffer[i], parserContext);
		}
		state.endOfInput(parserContext);

		getReceiver().endRecord();
	}

	@Override
	protected void onSetReceiver() {
		parserContext.setReceiver(getReceiver());
	}

	@Override
	protected void onResetStream() {
		parserContext.reset();
	}

	private boolean isRecordEmpty() {
		for (int i = 0; i < recordLen; ++i) {
			if (buffer[i] != ' ' && buffer[i] != '\t') {
				return false;
			}
		}
		return true;
	}

	private String extractRecordId() {
		final int idFromIndex = findRecordId();
		if (idFromIndex == -1) {
			return null;
		}
		idBuilder.setLength(0);
		for (int i = idFromIndex; i < recordLen; ++i) {
			final char ch = buffer[i];
			if (isSubfieldDelimiter(ch)) {
				break;
			}
			idBuilder.append(ch);
		}
		return idBuilder.toString();
	}

	private int findRecordId() {
		idFieldMatcher.reset(new String(buffer, 0, recordLen));
		if (!idFieldMatcher.find()) {
			return -1;
		}
		return idFieldMatcher.end();
	}

	private static boolean isSubfieldDelimiter(final char ch) {
		return ch == PicaConstants.RECORD_MARKER
				|| ch == PicaConstants.FIELD_MARKER
				|| ch == PicaConstants.FIELD_END_MARKER
				|| ch == PicaConstants.SUBFIELD_MARKER;
	}

}
