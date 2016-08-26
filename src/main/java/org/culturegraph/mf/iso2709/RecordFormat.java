/*
 * Copyright 2014 Christoph Böhme
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

import java.util.Arrays;

import org.culturegraph.mf.util.Require;

/**
 * Holds the configuration of an instance of the ISO2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordFormat {

	public static final int MAX_LENGTH = 9;

	private int indicatorLength;
	private int identifierLength;
	private int fieldLengthLength;
	private int fieldStartLength;
	private int implDefinedPartLength;

	public RecordFormat() {
		indicatorLength = 0;
		identifierLength = 0;
		fieldLengthLength = 0;
		fieldStartLength = 0;
		implDefinedPartLength = 0;
	}

	public RecordFormat(final RecordFormat source) {
		Require.notNull(source);

		indicatorLength = source.indicatorLength;
		identifierLength = source.identifierLength;
		fieldLengthLength = source.fieldLengthLength;
		fieldStartLength = source.fieldStartLength;
		implDefinedPartLength = source.implDefinedPartLength;
	}

	public int getIndicatorLength() {
		return indicatorLength;
	}

	public void setIndicatorLength(final int indicatorLength) {
		Require.notNegative(indicatorLength);
		Require.that(indicatorLength <= MAX_LENGTH);

		this.indicatorLength = indicatorLength;
	}

	public int getIdentifierLength() {
		return identifierLength;
	}

	public void setIdentifierLength(final int identifierLength) {
		Require.notNegative(identifierLength);
		Require.that(identifierLength <= MAX_LENGTH);

		this.identifierLength = identifierLength;
	}

	public int getFieldLengthLength() {
		return fieldLengthLength;
	}

	public void setFieldLengthLength(final int fieldLengthLength) {
		Require.that(fieldLengthLength > 0);
		Require.that(fieldLengthLength <= MAX_LENGTH);

		this.fieldLengthLength = fieldLengthLength;
	}

	public int getFieldStartLength() {
		return fieldStartLength;
	}

	public void setFieldStartLength(final int fieldStartLength) {
		Require.that(fieldStartLength > 0);
		Require.that(fieldStartLength <= MAX_LENGTH);

		this.fieldStartLength = fieldStartLength;
	}

	public int getImplDefinedPartLength() {
		return implDefinedPartLength;
	}

	public void setImplDefinedPartLength(final int implDefinedPartLength) {
		Require.notNegative(implDefinedPartLength);
		Require.that(implDefinedPartLength <= MAX_LENGTH);

		this.implDefinedPartLength = implDefinedPartLength;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof RecordFormat) {
			final RecordFormat other = (RecordFormat) obj;
			return indicatorLength == other.indicatorLength
					&& identifierLength == other.identifierLength
					&& fieldLengthLength == other.fieldLengthLength
					&& fieldStartLength == other.fieldStartLength
					&& implDefinedPartLength == other.implDefinedPartLength;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int[] items = { indicatorLength, identifierLength, fieldLengthLength,
				fieldStartLength, implDefinedPartLength };
		return Arrays.hashCode(items);
	}

	@Override
	public String toString() {
		return "(indicatorLength=" + indicatorLength + ", " +
				"identifierLength=" + identifierLength + ", " +
				"fieldLengthLength=" + fieldLengthLength + ", " +
				"fieldStartLength= " + fieldStartLength + ", " +
				"implDefinedPartLength=" + implDefinedPartLength + ")";
	}

}
