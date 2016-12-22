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

import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.MAX_BASE_ADDRESS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.MIN_BASE_ADDRESS;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.RECORD_LABEL_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.TAG_LENGTH;

/**
 * Provides access to a directory entry. A {@code DirectoryEntry} works like
 * an iterator or cursor. Use {@link #gotoNext()} to advance to the next
 * directory entry. Use {@link #rewind()} to go back to the first directory
 * entry.
 *
 * @author Christoph Böhme
 */
class DirectoryEntry {

	private final Iso646ByteBuffer buffer;

	private final int directoryEnd;
	private final int fieldLengthLength;
	private final int fieldStartLength;
	private final int implDefinedPartLength;
	private final int entryLength;

	private int currentPosition;

	DirectoryEntry(final Iso646ByteBuffer buffer, final RecordFormat recordFormat,
			final int baseAddress) {
		assert buffer != null;
		assert baseAddress >= MIN_BASE_ADDRESS;
		assert baseAddress <= MAX_BASE_ADDRESS;

		this.buffer = buffer;
		directoryEnd = baseAddress - Byte.BYTES;
		fieldLengthLength = recordFormat.getFieldLengthLength();
		fieldStartLength = recordFormat.getFieldStartLength();
		implDefinedPartLength = recordFormat.getImplDefinedPartLength();
		entryLength = TAG_LENGTH + fieldLengthLength + fieldStartLength +
				implDefinedPartLength;
		rewind();
	}

	void rewind() {
		currentPosition = RECORD_LABEL_LENGTH;
	}

	void gotoNext() {
		assert currentPosition < directoryEnd;
		currentPosition += entryLength;
	}

	boolean endOfDirectoryReached() {
		return currentPosition >= directoryEnd;
	}

	char[] getTag() {
		assert currentPosition < directoryEnd;
		return buffer.charsAt(currentPosition, TAG_LENGTH);
	}

	int getFieldLength() {
		assert currentPosition < directoryEnd;
		final int fieldLengthStart = currentPosition + TAG_LENGTH;
		return buffer.parseIntAt(fieldLengthStart, fieldLengthLength);
	}

	int getFieldStart() {
		assert currentPosition < directoryEnd;
		final int fieldStartStart = currentPosition + TAG_LENGTH +
				fieldLengthLength;
		return buffer.parseIntAt(fieldStartStart, fieldStartLength);
	}

	char[] getImplDefinedPart() {
		assert currentPosition < directoryEnd;
		final int implDefinedPartStart = currentPosition + TAG_LENGTH +
				fieldLengthLength + fieldStartLength;
		return buffer.charsAt(implDefinedPartStart, implDefinedPartLength);
	}

	boolean isRecordIdField() {
		final char[] tag = getTag();
		return tag[0] == '0' && tag[1] == '0' && tag[2] == '1';
	}

	boolean isReferenceField() {
		final char[] tag = getTag();
		return tag[0] == '0' && tag[1] == '0';
	}

	boolean isContinuedField() {
		return getFieldLength() == 0;
	}

	@Override
	public String toString() {
		if (endOfDirectoryReached()) {
			return "@END-OF-DIRECTORY";
		}
		return String.valueOf(getTag()) + String.valueOf(getFieldLength()) +
				String.valueOf(getFieldStart()) + String.valueOf(getImplDefinedPart());
	}

}
