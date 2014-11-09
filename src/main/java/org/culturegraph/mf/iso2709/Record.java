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
package org.culturegraph.mf.iso2709;

import static org.culturegraph.mf.iso2709.Iso2709Format.FIELD_SEPARATOR;
import static org.culturegraph.mf.iso2709.Iso2709Format.IDENTIFIER_MARKER;
import static org.culturegraph.mf.iso2709.Iso2709Format.RECORD_SEPARATOR;

import java.nio.charset.Charset;

import org.culturegraph.mf.exceptions.FormatException;

/**
 * @author Christoph Böhme
 */
public class Record {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private static final byte[] DATA_SEPARATORS = {
			FIELD_SEPARATOR, IDENTIFIER_MARKER
	};

	private static final char[] EMPTY_FIELD_ID = new char[0];

	private final Iso646ByteBuffer buffer;
	private final Label label;
	private final DirectoryEntry dirEntry;

	private final boolean hasIdentifier;

	private Charset charset = DEFAULT_CHARSET;

	public Record(final byte[] recordData) {
		buffer = new Iso646ByteBuffer(recordData);
		label = new Label(buffer);
		dirEntry = new DirectoryEntry(buffer, label);
		hasIdentifier = findIdentifierTag();

		verifyRecordLength();
		verifyRecordsEndsWithRecordSeparator();
	}

	private boolean findIdentifierTag() {
		dirEntry.reset();
		while (!dirEntry.endOfDirectoryReached()) {
			if (isIdentifierFieldTag(dirEntry.getTag())) {
				verifyDirectoryPointingToFirstFieldInRecord();
				return true;
			}
			dirEntry.gotoNext();
		}
		return false;
	}

	private boolean isIdentifierFieldTag(final char[] tag) {
		return tag[0] == '0' && tag[1] == '0' && tag[2] == '1';
	}

	private void verifyDirectoryPointingToFirstFieldInRecord() {
		if (dirEntry.getFieldStart() != 0) {
			throw new FormatException("Start position of the record identifier" +
					"is invalid. It must be the first field in the record.");
		}
	}

	private void verifyRecordLength() {
		if (label.getRecordLength() != buffer.getLength()) {
			throw new FormatException("Record length in record label and actual " +
					"buffer length do not match: length in label is " +
					label.getRecordLength() + ", actual buffer length is " +
					buffer.getLength());
		}
	}

	private void verifyRecordsEndsWithRecordSeparator() {
		if (buffer.charAt(buffer.getLength() - 1) != RECORD_SEPARATOR) {
			throw new FormatException("Record must end with a record separator " +
					"character (ASCII code 28) but found: " +
					buffer.charAt(buffer.getLength() - 1));
		}
	}

	public Label getLabel() {
		return label;
	}

	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}

	/**
	 * Returns the contents of the record identifier field. The record identifier
	 * field has the tag <i>001</i>. It must be the first field in the record.
	 *
	 * <p> Defined in section 4.5.2 of the ISO 2709:2008 standard.
	 *
	 * @return a string which identifies the record or null if the record has
	 * no record identifier.
	 */
	public String getIdentifier() {
		if (!hasIdentifier) {
			return null;
		}
		final int dataStart = label.getBaseAddress();
		final int dataLength = buffer.distanceTo(DATA_SEPARATORS, dataStart);
		return buffer.stringAt(dataStart, dataLength, charset);
	}

	public void processFields(final FieldHandler fieldHandler) {
		boolean continuedField = false;
		dirEntry.reset();
		while (!dirEntry.endOfDirectoryReached()) {
			if (continuedField) {
				fieldHandler.additionalImplDefinedPart(dirEntry.getImplDefinedPart());
			} else {
				processField(fieldHandler);
			}
			continuedField = dirEntry.getFieldLength() == 0;
			dirEntry.gotoNext();
		}
	}

	private void processField(final FieldHandler fieldHandler) {
		final char[] tag = dirEntry.getTag();
		if (isReferenceField(tag)) {
			processReferenceField(fieldHandler);
		} else {
			processDataField(fieldHandler);
		}
	}

	private boolean isReferenceField(final char[] tag) {
		return tag[0] == '0' && tag[1] == '0';
	}

	private void processReferenceField(final FieldHandler fieldHandler) {
		final int fieldStart = label.getBaseAddress() + dirEntry.getFieldStart();
		final int fieldLength = buffer.distanceTo(FIELD_SEPARATOR, fieldStart);
		final String value = buffer.stringAt(fieldStart, fieldLength, charset);
		fieldHandler.referenceField(dirEntry.getTag(),
				dirEntry.getImplDefinedPart(), value);
	}

	private void processDataField(final FieldHandler fieldHandler) {
		final int fieldStart = label.getBaseAddress() + dirEntry.getFieldStart();
		final char[] indicators = buffer.charsAt(fieldStart,
				label.getIndicatorLength());
		fieldHandler.startDataField(dirEntry.getTag(),
				dirEntry.getImplDefinedPart(), indicators);
		processDataValues(fieldStart + label.getIndicatorLength(), fieldHandler);
		fieldHandler.endDataField();
	}

	private void processDataValues(final int fromIndex, final FieldHandler fieldHandler) {
		int start = fromIndex;
		while (buffer.charAt(start) != FIELD_SEPARATOR) {
			processDataValue(start, fieldHandler);
			start += label.getIdentifierLength();
			start += buffer.distanceTo(DATA_SEPARATORS, start);
		}
	}

	private void processDataValue(final int fromIndex, final FieldHandler fieldHandler) {
		final char[] fieldId;
		if (label.getIdentifierLength() > 1) {
			fieldId = buffer.charsAt(fromIndex + 1, label.getIdentifierLength() - 1);
		} else {
			fieldId = EMPTY_FIELD_ID;
		}
		final int dataStart = fromIndex + label.getIdentifierLength();
		final int dataLength = buffer.distanceTo(DATA_SEPARATORS, dataStart);
		final String data = buffer.stringAt(dataStart, dataLength, charset);
		fieldHandler.data(fieldId, data);
	}

}
