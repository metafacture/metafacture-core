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

package org.metafacture.biblio.iso2709;

import org.metafacture.commons.Require;

import java.util.Arrays;

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
    public static final int TAG_LENGTH = Iso2709Constants.TAG_LENGTH;

    /**
     * The number of characters in the implementation codes element of the
     * record leader.
     */
    public static final int IMPL_CODES_LENGTH = Iso2709Constants.IMPL_CODES_LENGTH;

    /**
     * The number of characters in the system characters element of the
     * record leader.
     */
    public static final int SYSTEM_CHARS_LENGTH = Iso2709Constants.SYSTEM_CHARS_LENGTH;

    private final int indicatorLength;
    private final int identifierLength;
    private final int fieldLengthLength;
    private final int fieldStartLength;
    private final int implDefinedPartLength;

    /**
     * Initializes the RecordFormat.
     *
     * @param indicatorLength       the length of the indicator
     * @param identifierLength      the length of the identifier
     * @param fieldLengthLength     the length of the field
     * @param fieldStartLength      the length of the indicator
     * @param implDefinedPartLength the length of the part
     */
    public RecordFormat(final int indicatorLength, final int identifierLength,
            final int fieldLengthLength, final int fieldStartLength,
            final int implDefinedPartLength) {
        this.indicatorLength = indicatorLength;
        this.identifierLength = identifierLength;
        this.fieldLengthLength = fieldLengthLength;
        this.fieldStartLength = fieldStartLength;
        this.implDefinedPartLength = implDefinedPartLength;
    }

    /**
     * Initializes a RecordFormat defined by a RecordFormat.
     *
     * @param source the RecordFormat
     */
    public RecordFormat(final RecordFormat source) {
        Require.notNull(source);

        indicatorLength = source.indicatorLength;
        identifierLength = source.identifierLength;
        fieldLengthLength = source.fieldLengthLength;
        fieldStartLength = source.fieldStartLength;
        implDefinedPartLength = source.implDefinedPartLength;
    }

    /**
     * Returns a new default Builder.
     *
     * @return Builder
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Returns a new Builder created from a RecordFormat.
     *
     * @param source the RecordFormat
     * @return Builder
     */
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
            return indicatorLength == other.indicatorLength &&
                    identifierLength == other.identifierLength &&
                    fieldLengthLength == other.fieldLengthLength &&
                    fieldStartLength == other.fieldStartLength &&
                    implDefinedPartLength == other.implDefinedPartLength;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int[] items = {indicatorLength, identifierLength, fieldLengthLength,
            fieldStartLength, implDefinedPartLength};
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

        private static final int RADIX = 10;

        private int indicatorLength;
        private int identifierLength;
        private int fieldLengthLength;
        private int fieldStartLength;
        private int implDefinedPartLength;

        Builder() {
        }

        /**
         * Returns a new Builder with defined indicator length.
         *
         * @param currentIndicatorLength the length of the indicator
         * @return Builder
         */
        public Builder withIndicatorLength(final int currentIndicatorLength) {
            Require.notNegative(currentIndicatorLength);
            Require.that(currentIndicatorLength < RADIX);
            indicatorLength = currentIndicatorLength;
            return this;
        }

        /**
         * Returns a new Builder with defined identifier length.
         *
         * @param currentIdentifierLength the length of the identifier
         * @return Builder
         */
        public Builder withIdentifierLength(final int currentIdentifierLength) {
            Require.notNegative(currentIdentifierLength);
            Require.that(currentIdentifierLength < RADIX);
            identifierLength = currentIdentifierLength;
            return this;
        }

        /**
         * Returns a new Builder with defined field length.
         *
         * @param currentFieldLengthLength the length of the field
         * @return Builder
         */
        public Builder withFieldLengthLength(final int currentFieldLengthLength) {
            Require.that(currentFieldLengthLength > 0);
            Require.that(currentFieldLengthLength < RADIX);
            fieldLengthLength = currentFieldLengthLength;
            return this;
        }

        /**
         * Returns a new Builder with defined field start length.
         *
         * @param currentFieldStartLength the length of the field start
         * @return Builder
         */
        public Builder withFieldStartLength(final int currentFieldStartLength) {
            Require.that(currentFieldStartLength > 0);
            Require.that(currentFieldStartLength < RADIX);
            fieldStartLength = currentFieldStartLength;
            return this;
        }

        /**
         * Returns a new Builder with defined part length.
         *
         * @param currentImplDefinedPartLength the length of the part
         * @return Builder
         */
        public Builder withImplDefinedPartLength(final int currentImplDefinedPartLength) {
            Require.notNegative(currentImplDefinedPartLength);
            Require.that(currentImplDefinedPartLength < RADIX);
            implDefinedPartLength = currentImplDefinedPartLength;
            return this;
        }

        /**
         * Returns a new RecordFormat.
         *
         * @return RecordFormat
         */
        public RecordFormat build() {
            return new RecordFormat(indicatorLength, identifierLength,
                    fieldLengthLength, fieldStartLength, implDefinedPartLength);
        }

    }

}
