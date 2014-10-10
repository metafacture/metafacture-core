/*
 *  Copyright 2013, 2014 Christoph Böhme
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
package org.culturegraph.mf.stream.converter.bib;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.util.StringUtil;


/**
 * <p>Parses pica+ records. The parser only parses single records.
 * A string containing multiple records must be split into
 * individual records before passing it to {@code PicaDecoder}.</p>
 *
 * <p>The parser is designed to accept any string as valid input and
 * to parse pica plain format as well as normalised pica. To
 * achieve this, the parser behaves as following:</p>
 *
 * <ul>
 * <li>Fields are separated by record markers (0x1d), field
 * markers (0x1e) or field end markers (0x0a).</li>
 * <li>The field name and the first subfield are separated by
 * a subfield marker (0x01f).</li>
 * <li>The parser assumes that the input starts with a field
 * name.</li>
 * <li>The parser assumes that the end of the input marks
 * the end of the current field and the end of the record.
 * </li>
 * <li>Subfields are separated by subfield markers (0x1f).</li>
 * <li>The first character of a subfield is the name of the
 * subfield</li>
 * <li>To handle input with multiple field and subfield separators
 * following each  other directly (for instance 0x0a and 0x1e), it
 * is assumed that field names, subfields, subfield names or
 * subfield values can be empty.</li>
 * </ul>
 *
 * <p>Please note that the record markers is treated as a field
 * delimiter and not as a record delimiter. Records need to be
 * separated prior to parsing them.</p>
 *
 * <p>As the behaviour of the parser may result in unnamed fields or
 * subfields or fields with no subfields the {@code PicaDecoder}
 * automatically filters empty fields and subfields:</p>
 *
 * <ul>
 * <li>Subfields without a name are ignored (such fields cannot
 * have any value because then the first character of the value
 * would be the field name).</li>
 * <li>Subfields which only have a name but no value are always
 * parsed.</li>
 * <li>Unnamed Fields are only parsed if the contain not-ignored
 * subfields.</li>
 * <li>Named fields containing none or only ignored subfields are
 * only parsed if {@code skipEmptyFields} is set to {@code false}
 * otherwise they are ignored.</li>
 * <li>Input containing only whitespace (spaces and tabs) is
 * completely ignored</li>
 * </ul>
 *
 * <p>The {@code PicaDecoder} calls {@code receiver.startEntity} and
 * {@code receiver.endEntity} for each parsed field and
 * {@code receiver.literal} for each parsed subfield. Spaces in the
 * field name are not included in the entity name. The input
 * "028A \x1faAndy\x1fdWarhol\x1e" would produce the following
 * sequence of calls:</p>
 *
 * <ol>
 * <li>receiver.startEntity("028A")</li>
 * <li>receiver.literal("a", "Andy")</li>
 * <li>receiver.literal("d", "Warhol")</li>
 * <li>receiver.endEntity()</li>
 * </ol>
 *
 * <p>The content of subfield 003@$0 is used for the record id. If
 * {@code ignoreMissingIdn} is false and field 003@$0 is not found
 * in the record a {@link MissingIdException} is thrown.</p>
 *
 * <p>The parser assumes that the input is utf-8 encoded. The parser
 * does not support other pica encodings.</p>
 *
 * @author Christoph Böhme
 *
 */
@Description("Parses pica+ records. The parser only parses single records. " +
		"A string containing multiple records must be split into " +
		"individual records before passing it to PicaDecoder.")
@In(String.class)
@Out(StreamReceiver.class)
public final class PicaDecoder
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final char[] ID_FIELD = {'0', '0', '3', '@', ' ', PicaConstants.SUBFIELD_MARKER, '0'};

	private static final int BUFFER_SIZE = 1024 * 1024;

	private final StringBuilder idBuilder = new StringBuilder();
	private final PicaParserContext parserContext = new PicaParserContext();

	private char[] buffer = new char[BUFFER_SIZE];
	private int recordLen;

	private boolean ignoreMissingIdn;

	public void setIgnoreMissingIdn(final boolean ignoreMissingIdn) {
		this.ignoreMissingIdn = ignoreMissingIdn;
	}

	public boolean getIgnoreMissingIdn() {
		return ignoreMissingIdn;
	}

	public void setNormalizeUTF8(final boolean normalizeUTF8) {
		parserContext.setNormalizeUTF8(normalizeUTF8);
	}

	public boolean getNormalizeUTF8() {
		return parserContext.getNormalizeUTF8();
	}

	public void setSkipEmptyFields(final boolean skipEmptyFields) {
		parserContext.setSkipEmptyFields(skipEmptyFields);
	}

	public boolean getSkipEmptyFields() {
		return parserContext.getSkipEmptyFields();
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
