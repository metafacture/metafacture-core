/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.iso2709;

import static org.culturegraph.mf.iso2709.Util.calculateMaxValue;
import static org.culturegraph.mf.iso2709.Util.padWithZeros;

import org.culturegraph.mf.exceptions.FormatException;

/**
 * Builds a directory in ISO2709:2008 format. For fields whose length is greater
 * than the maximum value that can be stored in field length multiple directory
 * entries are created automatically.
 *
 * @author Christoph Böhme
 *
 */
final class DirectoryBuilder {

	private final StringBuilder directory = new StringBuilder();

	private final int fieldStartLength;
	private final int fieldLengthLength;

	private final int maxFieldStart;
	private final int maxFieldLength;

	private String tag;
	private String implDefinedPart;
	private int fieldStart;
	private int fieldEnd;

	public DirectoryBuilder(final RecordFormat format) {
		fieldStartLength = format.getFieldStartLength();
		fieldLengthLength = format.getFieldLengthLength();

		maxFieldStart = calculateMaxValue(fieldStartLength);
		maxFieldLength = calculateMaxValue(fieldLengthLength);

		reset();
	}

	public void setTag(final String tag) {
		this.tag = tag;
	}

	public void setImplDefinedPart(final String implDefinedPart) {
		this.implDefinedPart = implDefinedPart;
	}

	public void setFieldStart(final int fieldStart) {
		assert fieldStart >= 0;
		this.fieldStart = fieldStart;
	}

	public void setFieldEnd(final int fieldEnd) {
		assert fieldEnd >= 0;
		this.fieldEnd = fieldEnd;
	}

	public void write() {
		assert tag != null;
		assert implDefinedPart != null;
		assert fieldEnd >= fieldStart;

		checkAllPartsStartInAddressRange();

		int remainingLength = fieldEnd - fieldStart;
		int partStart = fieldStart;
		while (remainingLength > maxFieldLength) {
			writeDirectoryEntry(partStart, 0);
			remainingLength -= maxFieldLength;
			partStart += maxFieldLength;
		}
		writeDirectoryEntry(partStart, remainingLength);
	}

	private void checkAllPartsStartInAddressRange() {
		final int fieldLength = fieldEnd - fieldStart;
		final int lastPartLength = fieldLength % maxFieldLength;
		final int lastPartStart = fieldEnd - lastPartLength;
		if (lastPartStart > maxFieldStart) {
			throw new FormatException("the field is too long");
		}
	}

	private void writeDirectoryEntry(final int partStart, final int partLength) {
		directory.append(tag);
		directory.append(padWithZeros(partLength, fieldLengthLength));
		directory.append(padWithZeros(partStart, fieldStartLength));
		directory.append(implDefinedPart);
	}

	public void reset() {
		directory.setLength(0);
		tag = null;
		implDefinedPart = null;
		fieldStart = 0;
		fieldEnd = 0;
	}

	public int length() {
		return directory.length() + 1;
	}

	@Override
	public String toString() {
		return directory.toString() + Iso2709Format.FIELD_SEPARATOR;
	}

}
