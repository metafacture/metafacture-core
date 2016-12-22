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
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.MAX_PAYLOAD_LENGTH;
import static org.culturegraph.mf.biblio.iso2709.Iso2709Constants.TAG_LENGTH;

import org.culturegraph.mf.framework.FormatException;

/**
 * Builds a directory in ISO2709:2008 format. For fields whose length is greater
 * than the maximum value that can be stored in field length multiple directory
 * entries are created automatically.
 *
 * @author Christoph Böhme
 *
 */
final class DirectoryBuilder {

	private final Iso646ByteBuffer buffer;

	private final int fieldStartLength;
	private final int fieldLengthLength;
	private final int implDefinedPartLength;
	private final int entryLength;
	private final int maxFieldStart;
	private final int maxFieldLength;

	DirectoryBuilder(final RecordFormat format) {
		buffer = new Iso646ByteBuffer(MAX_PAYLOAD_LENGTH);
		fieldStartLength = format.getFieldStartLength();
		fieldLengthLength = format.getFieldLengthLength();
		implDefinedPartLength = format.getImplDefinedPartLength();
		entryLength = TAG_LENGTH + fieldStartLength + fieldLengthLength +
				implDefinedPartLength;
		maxFieldStart = calculateMaxValue(fieldStartLength);
		maxFieldLength = calculateMaxValue(fieldLengthLength);
	}

	private int calculateMaxValue(final int digits) {
		assert digits >= 0;
		int maxValue = 1;
		for (int i = 0; i < digits; i++) {
			maxValue *= 10;
		}
		return maxValue - 1;
	}

	void addEntries(final char[] tag, final char[] implDefinedPart,
			final int fieldStart, final int fieldEnd) {
		assert tag.length == TAG_LENGTH;
		assert implDefinedPart.length == implDefinedPartLength;
		assert fieldStart >= 0;
		assert fieldEnd >= fieldStart;
		checkDirectoryCapacity(fieldStart, fieldEnd);
		checkFieldFitsInAddressSpace(fieldStart, fieldEnd);
		writeEntries(tag, implDefinedPart, fieldStart, fieldEnd);
	}

	private void checkDirectoryCapacity(final int fieldStart,
			final int fieldEnd) {
		final int fieldLength = fieldEnd - fieldStart;
		final int numberOfEntries = fieldLength / maxFieldLength +
				(fieldLength % maxFieldLength == 0 ? 0 : 1);
		if (numberOfEntries * entryLength > buffer.getFreeSpace()) {
			throw new FormatException(
					"directory does not have enough free space for directory entry");
		}
	}

	private void checkFieldFitsInAddressSpace(final int fieldStart,
			final int fieldEnd) {
		final int fieldLength = fieldEnd - fieldStart;
		final int lastPartLength = fieldLength % maxFieldLength;
		final int lastPartStart = fieldEnd - lastPartLength;
		if (lastPartStart > maxFieldStart) {
			throw new FormatException("field is too long");
		}
	}

	private void writeEntries(final char[] tag, final char[] implDefinedPart,
			final int fieldStart, final int fieldEnd) {
		int remainingLength = fieldEnd - fieldStart;
		int partStart = fieldStart;
		while (remainingLength > maxFieldLength) {
			writeEntry(tag, implDefinedPart, partStart, 0);
			remainingLength -= maxFieldLength;
			partStart += maxFieldLength;
		}
		writeEntry(tag, implDefinedPart, partStart, remainingLength);
	}

	private void writeEntry(final char[] tag, final char[] implDefinedPart,
			final int partStart, final int partLength) {
		buffer.writeChars(tag);
		buffer.writeInt(partLength, fieldLengthLength);
		buffer.writeInt(partStart, fieldStartLength);
		buffer.writeChars(implDefinedPart);
	}

	void reset() {
		buffer.setWritePosition(0);
	}

	int length() {
		return buffer.getWritePosition() + Byte.BYTES;
	}

	void copyToBuffer(final byte[] destBuffer, final int fromIndex) {
		final int directoryLength = buffer.getWritePosition();
		System.arraycopy(buffer.getByteArray(), 0, destBuffer, fromIndex,
				directoryLength);
		final int directoryEnd = fromIndex + directoryLength;
		destBuffer[directoryEnd] = FIELD_SEPARATOR;
	}

	@Override
	public String toString() {
		return buffer.stringAt(0, buffer.getWritePosition(), Iso646Constants.CHARSET);
	}

}
