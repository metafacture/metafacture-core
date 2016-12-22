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

import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.FIELD_SEPARATOR;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.IDENTIFIER_MARKER;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.MIN_BASE_ADDRESS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.MIN_RECORD_LENGTH;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.culturegraph.mf.commons.Require;
import org.culturegraph.mf.framework.FormatException;

/**
 * Reads a record in ISO 2709:2008 format from a byte array.
 *
 * @author Christoph Böhme
 */
public final class Record {

	private static final int RECORD_ID_MISSING = -1;
	private static final char[] EMPTY_IDENTIFIER = new char[0];
	private static final byte[] DATA_SEPARATORS = {
			FIELD_SEPARATOR, IDENTIFIER_MARKER
	};

	private final Iso646ByteBuffer buffer;
	private final Label label;
	private final DirectoryEntry directoryEntry;

	private final int baseAddress;
	private final int indicatorLength;
	private final int identifierLength;
	private final int recordIdFieldStart;

	private Charset charset = StandardCharsets.UTF_8;

	private FieldHandler fieldHandler;

	/**
	 * Creates an instance of {@code Record} which provides access to the record
	 * stored in the array passed as argument.
	 *
	 * @param recordData a byte array containing a record in ISO 2709:2008 format.
	 */
	public Record(final byte[] recordData) {
		Require.notNull(recordData);
		checkRecordDataLength(recordData);
		buffer = new Iso646ByteBuffer(recordData);
		label = new Label(buffer);
		baseAddress = label.getBaseAddress();
		checkBaseAddress();
		directoryEntry = new DirectoryEntry(buffer, label.getRecordFormat(),
				baseAddress);
		indicatorLength = label.getIndicatorLength();
		identifierLength = label.getIdentifierLength();
		recordIdFieldStart = findRecordIdFieldStart();
	}

	private void checkRecordDataLength(final byte[] recordData) {
		if (recordData.length < MIN_RECORD_LENGTH) {
			throw new FormatException("record is too short");
		}
	}

	private void checkBaseAddress() {
		if (baseAddress < MIN_BASE_ADDRESS || baseAddress > buffer.getLength() - 1) {
			throw new FormatException("base address is out of range");
		}
	}

	private int findRecordIdFieldStart() {
		directoryEntry.rewind();
		while (!directoryEntry.endOfDirectoryReached()) {
			if (directoryEntry.isRecordIdField()) {
				return directoryEntry.getFieldStart();
			}
			directoryEntry.gotoNext();
		}
		return RECORD_ID_MISSING;
	}

	public RecordFormat getRecordFormat() {
		return label.getRecordFormat();
	}

	public char getRecordStatus() {
		return label.getRecordStatus();
	}

	public char[] getImplCodes() {
		return label.getImplCodes();
	}

	public char[] getSystemChars() {
		return label.getSystemChars();
	}

	public char getReservedChar() {
		return label.getReservedChar();
	}

	/**
	 * Sets the character encoding used for reading the data values. The encoding
	 * should be set before calling {@link #getRecordId()} or
	 * {@link #processFields(FieldHandler)}. If it is called while fields are
	 * being processed, the new encoding becomes effective on the next invocation
	 * of {@link FieldHandler#data(char[], String)}.
	 *
	 * @param charset the character encoding of the data values
	 */
	public void setCharset(final Charset charset) {
		this.charset = Require.notNull(charset);
	}

	/**
	 * Returns the current character encoding used for reading the data values.
	 * The default encoding is UTF-8.
	 *
	 * @return the current character encoding.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Returns the contents of the record identifier field. The record identifier
	 * field has the tag <i>001</i>. It must be the first field in the record.
	 * <p>
	 * Defined in section 4.5.2 of the ISO 2709:2008 standard.
	 *
	 * @return a string which identifies the record or null if the record has
	 * no record identifier.
	 */
	public String getRecordId() {
		if (recordIdFieldStart == RECORD_ID_MISSING) {
			return null;
		}
		final int dataStart = baseAddress + recordIdFieldStart;
		final int dataLength = buffer.distanceTo(DATA_SEPARATORS, dataStart);
		return buffer.stringAt(dataStart, dataLength, charset);
	}

	/**
	 * Iterates through all fields in the record and calls the appropriate method
	 * on the supplied {@link FieldHandler} instance.
	 *
	 * @param fieldHandler instance of field handler. Must not be null.
	 */
	public void processFields(final FieldHandler fieldHandler) {
		this.fieldHandler = Require.notNull(fieldHandler);
		boolean continuedField = false;
		directoryEntry.rewind();
		while (!directoryEntry.endOfDirectoryReached()) {
			if (continuedField) {
				fieldHandler.additionalImplDefinedPart(
						directoryEntry.getImplDefinedPart());
			} else {
				processField();
			}
			continuedField = directoryEntry.isContinuedField();
			directoryEntry.gotoNext();
		}
		this.fieldHandler = null;
	}

	private void processField() {
		if (directoryEntry.isReferenceField()) {
			processReferenceField();
		} else {
			processDataField();
		}
	}

	private void processReferenceField() {
		final int fieldStart = baseAddress + directoryEntry.getFieldStart();
		final int fieldLength = buffer.distanceTo(FIELD_SEPARATOR, fieldStart);
		final String value = buffer.stringAt(fieldStart, fieldLength, charset);
		fieldHandler.referenceField(directoryEntry.getTag(),
				directoryEntry.getImplDefinedPart(), value);
	}

	private void processDataField() {
		final int fieldStart = baseAddress + directoryEntry.getFieldStart();
		final char[] indicators = buffer.charsAt(fieldStart, indicatorLength);
		fieldHandler.startDataField(directoryEntry.getTag(),
				directoryEntry.getImplDefinedPart(), indicators);
		processDataValues(fieldStart + indicatorLength);
		fieldHandler.endDataField();
	}

	private void processDataValues(final int fromIndex) {
		int start = fromIndex;
		while (buffer.byteAt(start) != FIELD_SEPARATOR) {
			start = processDataValue(start);
		}
	}

	/**
	 * Reads the field value starting at {@code fromIndex} and calls
	 * {@link FieldHandler#data(char[], String)}.
	 *
	 * @param fromIndex index at which the identifier of the field value starts.
	 * @return the index of the end of field marker. This is the position write
	 * after the data field in the buffer. It can be used as the next starting
	 * position when processing multiple subfields.
	 */
	private int processDataValue(final int fromIndex) {
		final char[] identifier = getIdentifier(fromIndex);
		final int dataStart = fromIndex + identifierLength;
		final int dataLength = buffer.distanceTo(DATA_SEPARATORS, dataStart);
		final String data = buffer.stringAt(dataStart, dataLength, charset);
		fieldHandler.data(identifier, data);
		return dataStart + dataLength;
	}

	private char[] getIdentifier(final int fromIndex) {
		if (identifierLength > 1) {
			return buffer.charsAt(fromIndex + 1, identifierLength - 1);
		}
		return EMPTY_IDENTIFIER;
	}

}
