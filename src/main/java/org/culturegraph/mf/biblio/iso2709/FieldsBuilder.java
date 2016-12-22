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
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_SEPARATOR;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.culturegraph.mf.framework.FormatException;

/**
 * Builds a list of fields in ISO 2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
final class FieldsBuilder {

	private static final int NO_MARKER_SET = -1;
	private static final char[] NO_INDICATORS = new char[0];

	private final Iso646ByteBuffer buffer;
	private final int identifierLength;

	private Charset charset = StandardCharsets.UTF_8;

	private int undoMarker = NO_MARKER_SET;
	private boolean inField;


	FieldsBuilder(final RecordFormat format, final int maxSize) {
		buffer = new Iso646ByteBuffer(maxSize);
		identifierLength = format.getIdentifierLength();
	}

	void setCharset(final Charset charset) {
		assert charset != null;
		this.charset = charset;
	}

	Charset getCharset() {
		return charset;
	}

	int startField() {
		return startField(NO_INDICATORS);
	}

	int startField(final char[] indicators) {
		assert !inField;
		checkCapacity(indicators.length + Byte.BYTES);
		inField = true;
		undoMarker = buffer.getWritePosition();
		buffer.writeChars(indicators);
		return undoMarker;
	}

	int endField() {
		assert inField;
		checkCapacity(Byte.BYTES);
		inField = false;
		buffer.writeByte(FIELD_SEPARATOR);
		return buffer.getWritePosition();
	}

	void appendValue(final String value) {
		assert inField;
		final byte[] bytes = value.getBytes(charset);
		checkCapacity(bytes.length + Byte.BYTES);
		buffer.writeBytes(bytes);
	}

	void appendSubfield(final char[] identifier, final String value) {
		assert inField;
		final byte[] bytes = value.getBytes(charset);
		checkCapacity(bytes.length + identifierLength + Byte.BYTES);
		if (identifierLength > 0) {
			buffer.writeByte(IDENTIFIER_MARKER);
			buffer.writeChars(identifier);
		}
		buffer.writeBytes(bytes);
	}

	private void checkCapacity(final int dataLength) {
		if (dataLength > buffer.getFreeSpace()) {
			throw new FormatException("not enough space for field");
		}
	}

	void undoLastField() {
		assert undoMarker != NO_MARKER_SET;
		buffer.setWritePosition(undoMarker);
		undoMarker = NO_MARKER_SET;
	}

	void reset() {
		buffer.setWritePosition(0);
		undoMarker = NO_MARKER_SET;
		inField = false;
	}

	int length() {
		assert !inField;
		return buffer.getWritePosition() + Byte.BYTES;
	}

	void copyToBuffer(final byte[] destBuffer, final int fromIndex) {
		assert !inField;
		final int fieldLength = buffer.getWritePosition();
		System.arraycopy(buffer.getByteArray(), 0, destBuffer, fromIndex,
				fieldLength);
		final int fieldsEnd = fromIndex + fieldLength;
		destBuffer[fieldsEnd] = RECORD_SEPARATOR;
	}

	@Override
	public String toString() {
		return buffer.stringAt(0, buffer.getWritePosition(), charset);
	}

}
