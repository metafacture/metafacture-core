/*
 *  Copyright 2013 Christoph Böhme
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


/**
 * Parses a PICA+ record with UTF8 encoding assumed.
 * 
 * For each field in the stream the module calls:
 * <ol>
 * <li>receiver.startEntity</li>
 * <li>receiver.literal for each subfield of the field</li>
 * <li>receiver.endEntity</li>
 * </ol>
 * 
 * Spaces in the field name are not included in the entity name.
 * 
 * Empty subfields are skipped. For instance, processing the following input
 * would NOT produce an empty literal: 003@ \u001f\u001e. The parser also
 * skips unnamed fields without any subfields.
 * 
 * If {@code ignoreMissingIdn} is false and field 003@$0 is not found in the
 * record a {@link MissingIdException} is thrown.
 * 
 * @author Christoph Böhme
 * 
 */
@Description("Parses a PICA+ record with UTF8 encoding assumed.")
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
		
		copyToBuffer(record);
		
		if (recordIsEmpty()) {
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
	
	private void copyToBuffer(final String record) {
		recordLen = record.length();
		if(recordLen > buffer.length) {
			buffer = new char[buffer.length * 2];
		}
		record.getChars(0, recordLen, buffer, 0);
	}
	
	private boolean recordIsEmpty() {
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
	 * control character (see {@link PicaConstants} is found or the end of
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
