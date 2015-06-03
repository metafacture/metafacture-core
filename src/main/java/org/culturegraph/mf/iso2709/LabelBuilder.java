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
import static org.culturegraph.mf.iso2709.Util.toDigit;
import static org.culturegraph.mf.util.StringUtil.repeatChars;

import org.culturegraph.mf.exceptions.FormatException;

/**
 * Builds a record label in ISO 2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
final class LabelBuilder {

	private final StringBuilder label;

	private final String defaultLabel;

	private final int maxRecordLength;
	private final int maxBaseAddress;

	public LabelBuilder(final RecordFormat format) {
		label = new StringBuilder(repeatChars(' ',
				Iso2709Format.RECORD_LABEL_LENGTH));

		setIndicatorLength(format.getIndicatorLength());
		setIdentifierLength(format.getIdentifierLength());
		setFieldLengthLength(format.getFieldLengthLength());
		setFieldStartLength(format.getFieldStartLength());
		setImplDefinedPartLength(format.getImplDefinedPartLength());

		defaultLabel = label.toString();

		maxRecordLength = calculateMaxValue(Iso2709Format.RECORD_LENGTH_LENGTH);
		maxBaseAddress = calculateMaxValue(Iso2709Format.BASE_ADDRESS_LENGTH);
	}

	public void setRecordLength(final int recordLength) {
		checkRecordLengthInAddressRange(recordLength);
		label.replace(Iso2709Format.RECORD_LENGTH_START,
				Iso2709Format.RECORD_LENGTH_END,
				padWithZeros(recordLength, Iso2709Format.RECORD_LENGTH_LENGTH));
	}

	private void checkRecordLengthInAddressRange(final int recordLength) {
		if (recordLength > maxRecordLength) {
			throw new FormatException("record length is too large");
		}
	}

	public void setRecordStatus(final char recordStatus) {
		label.setCharAt(Iso2709Format.RECORD_STATUS_POS, recordStatus);
	}

	public void setImplCodes(final String implCodes) {
		label.replace(Iso2709Format.IMPL_CODES_START,
				Iso2709Format.IMPL_CODES_END, implCodes);
	}

	public void setIndicatorLength(final int indicatorLength) {
		label.setCharAt(Iso2709Format.INDICATOR_LENGTH_POS,
				toDigit(indicatorLength));
	}

	public void setIdentifierLength(final int identifierLength) {
		label.setCharAt(Iso2709Format.IDENTIFIER_LENGTH_POS,
				toDigit(identifierLength));
	}

	public void setBaseAddress(final int baseAddress) {
		checkBaseAddressInAddressRange(baseAddress);
		label.replace(Iso2709Format.BASE_ADDRESS_START,
				Iso2709Format.BASE_ADDRESS_END,
				padWithZeros(baseAddress, Iso2709Format.BASE_ADDRESS_LENGTH));
	}

	private void checkBaseAddressInAddressRange(final int baseAddress) {
		if (baseAddress > maxBaseAddress) {
			throw new FormatException("Base address is too large");
		}
	}

	public void setSystemChars(final String systemChars) {
		label.replace(Iso2709Format.SYSTEM_CHARS_START,
				Iso2709Format.SYSTEM_CHARS_END, systemChars);
	}

	public void setFieldLengthLength(final int fieldLengthLength) {
		label.setCharAt(Iso2709Format.FIELD_LENGTH_LENGTH_POS,
				toDigit(fieldLengthLength));
	}

	public void setFieldStartLength(final int fieldStartLength) {
		label.setCharAt(Iso2709Format.FIELD_START_LENGTH_POS,
				toDigit(fieldStartLength));
	}

	public void setImplDefinedPartLength(final int implDefinedPartLength) {
		label.setCharAt(Iso2709Format.IMPL_DEFINED_PART_LENGTH_POS,
				toDigit(implDefinedPartLength));
	}

	public void setReservedChar(final char reservedChar) {
		label.setCharAt(Iso2709Format.RESERVED_CHAR_POS, reservedChar);
	}

	public void reset() {
		label.replace(0, defaultLabel.length(), defaultLabel);
	}

	public int length() {
		return label.length();
	}

	@Override
	public String toString() {
		return label.toString();
	}

}
