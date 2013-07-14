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

import org.culturegraph.mf.exceptions.FormatException;
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
 * would NOT produce an empty literal: 003@ \u001f\u001e
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

	private static final char[] ID_FIELD = {'0', '0', '3', '@', ' ', PicaConstants.SUBFIELD_DELIMITER, '0'};

	private static final int BUFFER_SIZE = 1024 * 1024;
	
	private final StringBuilder idBuilder = new StringBuilder();
	private final PicaParserContext parserContext = new PicaParserContext();
	
	private char[] buffer = new char[BUFFER_SIZE];
	private int recordLen;
	
	private boolean ignoreMissingIdn;
	private boolean fixUnexpectedEOR;

	public void setIgnoreMissingIdn(final boolean ignoreMissingIdn) {
		this.ignoreMissingIdn = ignoreMissingIdn;
	}
	
	public boolean getIgnoreMissingIdn() {
		return ignoreMissingIdn;
	}
	
	public void setFixUnexpectedEOR(final boolean fixUnexpectedEOR) {
		this.fixUnexpectedEOR = fixUnexpectedEOR;
	}
	
	public boolean getFixUnexpectedEOR() {
		return fixUnexpectedEOR;
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
		if (state != PicaParserState.FIELD_NAME || parserContext.hasUnprocessedText()) {
			if (fixUnexpectedEOR) {
				state = state.parseChar(PicaConstants.FIELD_DELIMITER, parserContext);
				assert state == PicaParserState.FIELD_NAME;
				assert !parserContext.hasUnprocessedText();
			} else {
				throw new FormatException("Unexpected end of record");
			}
		}
		
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
	
	private String extractRecordId() {
		idBuilder.setLength(0);
		
		int fieldPos = 0;
		boolean skip = false;
		for (int i = 0; i < recordLen; ++i) {
			if (buffer[i] == PicaConstants.FIELD_DELIMITER) {
				if (idBuilder.length() > 0) {
					return idBuilder.toString();
				}
				fieldPos = 0;
				skip = false;
				continue;
			}
			if (!skip) {
				if (fieldPos < ID_FIELD.length) {
					if (buffer[i] == ID_FIELD[fieldPos]) {
						fieldPos += 1;
					} else {
						skip = true;
					}
				} else {
					if (buffer[i] == PicaConstants.SUBFIELD_DELIMITER) {
						skip = true;
					} else {
						idBuilder.append(buffer[i]);
					}
				}
			}
		}
		
		return null;
	}
	
}
