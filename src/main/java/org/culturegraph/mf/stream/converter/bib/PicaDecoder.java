/*
 * Copyright 2013, 2014 Christoph Böhme
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
package org.culturegraph.mf.stream.converter.bib;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.util.StringUtil;

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
 * The content of subfield <i>003&#64; $0</i> is used as record id. If
 * {@link #setIgnoreMissingIdn(boolean)} is false and field
 * <i>003&#64; $0</i> is not found in the record a
 * {@link MissingIdException} is thrown otherwise the record identifier is an
 * empty string.
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

	private static final char[] ID_FIELD = {'0', '0', '3', '@', ' ', PicaConstants.SUBFIELD_MARKER, '0'};

	private static final int BUFFER_SIZE = 1024 * 1024;

	private final StringBuilder idBuilder = new StringBuilder();
	private final PicaParserContext parserContext = new PicaParserContext();

	private char[] buffer = new char[BUFFER_SIZE];
	private int recordLen;

	private boolean ignoreMissingIdn;

	/**
	 * Controls whether records having no pica subfield <i>003&#64; $0</i>
	 * (which contains the record identifier <i>IDN</i>) are reported as faulty.
	 * By default such records are reported by the {@code PicaDecoder} by throwing
	 * a {@link MissingIdException}.
	 * <p>
	 * The setting can be changed at any time. It becomes effective with the next
	 * record that is being processed.
	 * <p>
	 * <strong>Default value: {@code false}</strong>
	 *
	 * @param ignoreMissingIdn if true, missing IDNs do not trigger a
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

	/**
	 * Searches the record for the sequence specified in {@code ID_FIELD}
	 * and returns all characters following this sequence until the next
	 * control character (see {@link PicaConstants}) is found or the end of
	 * the record is reached. Only the first occurrence of the sequence is
	 * processed, later occurrences are ignored.
	 *
	 * If the sequence is not found in the string or if it is not followed
	 * by any characters then {@code null} is returned.
	 *
	 * @return value of subfield 003@$0 or null if the
	 *         field is not found or is empty.
	 */
	private String extractRecordId() {
		idBuilder.setLength(0);

		int fieldPos = 0;
		boolean skip = false;
		for (int i = 0; i < recordLen; ++i) {
			if (isFieldDelimiter(buffer[i])) {
				if (idBuilder.length() > 0) {
					break;
				}
				fieldPos = 0;
				skip = false;
			} else {
				if (!skip) {
					if (fieldPos < ID_FIELD.length) {
						if (buffer[i] == ID_FIELD[fieldPos]) {
							fieldPos += 1;
						} else {
							skip = true;
						}
					} else {
						if (buffer[i] == PicaConstants.SUBFIELD_MARKER) {
							break;
						}
						idBuilder.append(buffer[i]);
					}
				}
			}
		}

		if (idBuilder.length() > 0) {
			return idBuilder.toString();
		}
		return null;
	}

	private static boolean isFieldDelimiter(final char ch) {
		return ch == PicaConstants.RECORD_MARKER
				|| ch == PicaConstants.FIELD_MARKER
				|| ch == PicaConstants.FIELD_END_MARKER;
	}

}
