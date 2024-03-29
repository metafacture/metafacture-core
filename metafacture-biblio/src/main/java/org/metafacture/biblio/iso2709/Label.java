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

/**
 * Provides read access to the record label of an ISO 2709:2008 formatted
 * record. The record label consists of the first 24 octets of the record.
 * <p>
 * Use {@link LabelBuilder} if write access to the label is required.
 *
 * @author Christoph Böhme
 */
class Label {

    private final Iso646ByteBuffer buffer;

    Label(final Iso646ByteBuffer buffer) {
        final int bufferLength = buffer.getLength();
        assert bufferLength >= Iso2709Constants.RECORD_LABEL_LENGTH;
        this.buffer = buffer;
    }

    RecordFormat getRecordFormat() {
        return RecordFormat.create()
                .withIndicatorLength(getIndicatorLength())
                .withIdentifierLength(getIdentifierLength())
                .withFieldLengthLength(getFieldLengthLength())
                .withFieldStartLength(getFieldStartLength())
                .withImplDefinedPartLength(getImplDefinedPartLength())
                .build();
    }

    int getRecordLength() {
        return buffer.parseIntAt(Iso2709Constants.RECORD_LENGTH_START, Iso2709Constants.RECORD_LENGTH_LENGTH);
    }

    char getRecordStatus() {
        return buffer.charAt(Iso2709Constants.RECORD_STATUS_POS);
    }

    char[] getImplCodes() {
        return buffer.charsAt(Iso2709Constants.IMPL_CODES_START, Iso2709Constants.IMPL_CODES_LENGTH);
    }

    int getIndicatorLength() {
        return buffer.parseIntAt(Iso2709Constants.INDICATOR_LENGTH_POS);
    }

    int getIdentifierLength() {
        return buffer.parseIntAt(Iso2709Constants.IDENTIFIER_LENGTH_POS);
    }

    int getBaseAddress() {
        return buffer.parseIntAt(Iso2709Constants.BASE_ADDRESS_START, Iso2709Constants.BASE_ADDRESS_LENGTH);
    }

    char[] getSystemChars() {
        return buffer.charsAt(Iso2709Constants.SYSTEM_CHARS_START, Iso2709Constants.SYSTEM_CHARS_LENGTH);
    }

    int getFieldLengthLength() {
        return buffer.parseIntAt(Iso2709Constants.FIELD_LENGTH_LENGTH_POS);
    }

    int getFieldStartLength() {
        return buffer.parseIntAt(Iso2709Constants.FIELD_START_LENGTH_POS);
    }

    int getImplDefinedPartLength() {
        return buffer.parseIntAt(Iso2709Constants.IMPL_DEFINED_PART_LENGTH_POS);
    }

    char getReservedChar() {
        return buffer.charAt(Iso2709Constants.RESERVED_CHAR_POS);
    }

    @Override
    public String toString() {
        return buffer.stringAt(0, Iso2709Constants.RECORD_LABEL_LENGTH, Iso646Constants.CHARSET);
    }

}
