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

import java.util.Arrays;

import org.culturegraph.mf.commons.Require;

/**
 * Holds the configuration of an instance of the ISO2709:2008 format.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordFormat {

	/**
	 * The number of characters in the field tags.
	 */
	public final int TAG_LENGTH = Iso2709Constants.TAG_LENGTH;

	/**
	 * The number of characters in the implementation codes element of the
	 * record leader.
	 */
	public final int IMPL_CODES_LENGTH = Iso2709Constants.IMPL_CODES_LENGTH;

	/**
	 * The number of characters in the system characters element of the
	 * record leader.
	 */
	public final int SYSTEM_CHARS_LENGTH = Iso2709Constants.SYSTEM_CHARS_LENGTH;

	private final int indicatorLength;
	private final int identifierLength;
	private final int fieldLengthLength;
	private final int fieldStartLength;
	private final int implDefinedPartLength;

	public RecordFormat(final int indicatorLength, final int identifierLength,
			final int fieldLengthLength, final int fieldStartLength,
			final int implDefinedPartLength) {
		this.indicatorLength = indicatorLength;
		this.identifierLength = identifierLength;
		this.fieldLengthLength = fieldLengthLength;
		this.fieldStartLength = fieldStartLength;
		this.implDefinedPartLength = implDefinedPartLength;
	}

	public RecordFormat(final RecordFormat source) {
		Require.notNull(source);

		indicatorLength = source.indicatorLength;
		identifierLength = source.identifierLength;
		fieldLengthLength = source.fieldLengthLength;
		fieldStartLength = source.fieldStartLength;
		implDefinedPartLength = source.implDefinedPartLength;
	}

	public static Builder create() {
		return new Builder();
	}

	public static Builder createFrom(final RecordFormat source) {
		return create()
				.withIndicatorLength(source.indicatorLength)
				.withIdentifierLength(source.identifierLength)
				.withFieldLengthLength(source.fieldLengthLength)
				.withFieldStartLength(source.fieldStartLength)
				.withImplDefinedPartLength(source.implDefinedPartLength);
	}

	public int getIndicatorLength() {
		return indicatorLength;
	}

	public int getIdentifierLength() {
		return identifierLength;
	}

	public int getFieldLengthLength() {
		return fieldLengthLength;
	}

	public int getFieldStartLength() {
		return fieldStartLength;
	}

	public int getImplDefinedPartLength() {
		return implDefinedPartLength;
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

	public static class Builder {

		private int indicatorLength;
		private int identifierLength;
		private int fieldLengthLength;
		private int fieldStartLength;
		private int implDefinedPartLength;

		public Builder withIndicatorLength(final int indicatorLength) {
			Require.notNegative(indicatorLength);
			Require.that(indicatorLength <= 9);
			this.indicatorLength = indicatorLength;
			return this;
		}

		public Builder withIdentifierLength(final int identifierLength) {
			Require.notNegative(identifierLength);
			Require.that(identifierLength <= 9);
			this.identifierLength = identifierLength;
			return this;
		}

		public Builder withFieldLengthLength(final int fieldLengthLength) {
			Require.that(fieldLengthLength > 0);
			Require.that(fieldLengthLength <= 9);
			this.fieldLengthLength = fieldLengthLength;
			return this;
		}

		public Builder withFieldStartLength(final int fieldStartLength) {
			Require.that(fieldStartLength > 0);
			Require.that(fieldStartLength <= 9);
			this.fieldStartLength = fieldStartLength;
			return this;
		}

		public Builder withImplDefinedPartLength(final int implDefinedPartLength) {
			Require.notNegative(implDefinedPartLength);
			Require.that(implDefinedPartLength <= 9);
			this.implDefinedPartLength = implDefinedPartLength;
			return this;
		}

		public RecordFormat build() {
			return new RecordFormat(indicatorLength, identifierLength,
					fieldLengthLength, fieldStartLength, implDefinedPartLength);
		}

	}

}
