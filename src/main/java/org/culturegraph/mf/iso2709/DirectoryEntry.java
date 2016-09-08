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

import org.culturegraph.mf.exceptions.FormatException;

/**
 * @author Christoph Böhme
 */
class DirectoryEntry {

	private final Iso646ByteBuffer buffer;

	private final int fieldLengthLength;
	private final int fieldStartLength;
	private final int implDefinedPartLength;
	private final int baseAddress;
	private final int entryLength;

	private int currentPosition;

	DirectoryEntry(final Iso646ByteBuffer buffer, final Label label) {
		this.buffer = buffer;
		this.fieldLengthLength = label.getFieldLengthLength();
		this.fieldStartLength = label.getFieldStartLength();
		this.implDefinedPartLength = label.getImplDefinedPartLength();
		this.baseAddress = label.getBaseAddress();
		this.entryLength = Iso2709Format.TAG_LENGTH + fieldLengthLength +
				fieldStartLength + implDefinedPartLength;
		verifyDirectoryLength();
		reset();
	}

	private void verifyDirectoryLength() {
		if (buffer.getLength() < Iso2709Format.MIN_RECORD_LENGTH) {
			throw new FormatException("Record is too short");
		}
		if (buffer.charAt(baseAddress - 1) != Iso2709Format.FIELD_SEPARATOR) {
			throw new FormatException("Expecting field separator at index " +
					(baseAddress - 1));
		}
		final int dirLength = baseAddress - Iso2709Format.RECORD_LABEL_LENGTH - 1;
		if (dirLength % entryLength != 0) {
			throw new FormatException("Directory length must be a multiple of the " +
					"directory entry length");
		}
	}

	void reset() {
		currentPosition = Iso2709Format.RECORD_LABEL_LENGTH;
	}

	void gotoNext() {
		assert !endOfDirectoryReached();
		currentPosition += entryLength;
	}

	boolean endOfDirectoryReached() {
		return currentPosition >= baseAddress - 1;
	}

	char[] getTag() {
		assert !endOfDirectoryReached();
		return buffer.charsAt(currentPosition, Iso2709Format.TAG_LENGTH);
	}

	int getFieldLength() {
		assert !endOfDirectoryReached();
		final int fieldLengthStart = currentPosition + Iso2709Format.TAG_LENGTH;
		return buffer.parseIntAt(fieldLengthStart, fieldLengthLength);
	}

	int getFieldStart() {
		assert !endOfDirectoryReached();
		final int fieldStartStart = currentPosition + Iso2709Format.TAG_LENGTH +
				fieldLengthLength;
		return buffer.parseIntAt(fieldStartStart, fieldStartLength);
	}

	char[] getImplDefinedPart() {
		assert !endOfDirectoryReached();
		final int implDefinedPartStart = currentPosition +
				Iso2709Format.TAG_LENGTH + fieldLengthLength + fieldStartLength;
		return buffer.charsAt(implDefinedPartStart, implDefinedPartLength);
	}

	@Override
	public String toString() {
		if (endOfDirectoryReached()) {
			return "END-OF_DIRECTORY";
		}
		return String.valueOf(getTag()) +
				String.valueOf(getFieldLength()) +
				String.valueOf(getFieldStart()) +
				String.valueOf(getImplDefinedPart());
	}

}
