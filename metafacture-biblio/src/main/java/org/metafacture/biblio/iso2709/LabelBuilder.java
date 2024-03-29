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

import java.util.Arrays;

/**
 * Builds a record label for an ISO 2709:2008 record. The record label consists
 * of the first 24 octets of the record.
 * <p>
 *
 * @author Christoph Böhme
 *
 */
class LabelBuilder {

    private static final char DEFAULT_RECORD_STATUS = ' ';
    private static final char[] DEFAULT_IMPL_CODES = {' ', ' ', ' ', ' '};
    private static final char[] DEFAULT_SYSTEM_CHARS = {' ', ' ', ' '};
    private static final char DEFAULT_RESERVED_CHAR = ' ';

    private final Iso646ByteBuffer buffer;
    private final byte[] defaultLabel;

    LabelBuilder(final RecordFormat recordFormat) {
        buffer = new Iso646ByteBuffer(Iso2709Constants.RECORD_LABEL_LENGTH);
        defaultLabel = buildDefaultLabel(recordFormat);
    }

    private byte[] buildDefaultLabel(final RecordFormat recordFormat) {
        writeRecordFormatToLabel(recordFormat);
        setRecordLength(Iso2709Constants.MIN_RECORD_LENGTH);
        setRecordStatus(DEFAULT_RECORD_STATUS);
        setImplCodes(DEFAULT_IMPL_CODES);
        setBaseAddress(Iso2709Constants.MIN_BASE_ADDRESS);
        setSystemChars(DEFAULT_SYSTEM_CHARS);
        setReservedChar(DEFAULT_RESERVED_CHAR);
        return Arrays.copyOf(buffer.getByteArray(), buffer.getLength());
    }

    private void writeRecordFormatToLabel(final RecordFormat recordFormat) {
        buffer.setWritePosition(Iso2709Constants.INDICATOR_LENGTH_POS);
        buffer.writeInt(recordFormat.getIndicatorLength());
        buffer.setWritePosition(Iso2709Constants.IDENTIFIER_LENGTH_POS);
        buffer.writeInt(recordFormat.getIdentifierLength());
        buffer.setWritePosition(Iso2709Constants.FIELD_LENGTH_LENGTH_POS);
        buffer.writeInt(recordFormat.getFieldLengthLength());
        buffer.setWritePosition(Iso2709Constants.FIELD_START_LENGTH_POS);
        buffer.writeInt(recordFormat.getFieldStartLength());
        buffer.setWritePosition(Iso2709Constants.IMPL_DEFINED_PART_LENGTH_POS);
        buffer.writeInt(recordFormat.getImplDefinedPartLength());
    }

    void setRecordLength(final int recordLength) {
        assert recordLength >= Iso2709Constants.MIN_RECORD_LENGTH;
        assert recordLength <= Iso2709Constants.MAX_RECORD_LENGTH;
        buffer.setWritePosition(Iso2709Constants.RECORD_LENGTH_START);
        buffer.writeInt(recordLength, Iso2709Constants.RECORD_LENGTH_LENGTH);
    }

    void setRecordStatus(final char recordStatus) {
        buffer.setWritePosition(Iso2709Constants.RECORD_STATUS_POS);
        buffer.writeChar(recordStatus);
    }

    void setImplCodes(final char[] implCodes) {
        assert implCodes.length == Iso2709Constants.IMPL_CODES_LENGTH;
        buffer.setWritePosition(Iso2709Constants.IMPL_CODES_START);
        buffer.writeChars(implCodes);
    }

    void setImplCode(final int index, final char value) {
        assert 0 <= index && index < Iso2709Constants.IMPL_CODES_LENGTH;
        buffer.setWritePosition(Iso2709Constants.IMPL_CODES_START + index);
        buffer.writeChar(value);
    }

    void setBaseAddress(final int baseAddress) {
        assert baseAddress >= Iso2709Constants.MIN_BASE_ADDRESS;
        assert baseAddress <= Iso2709Constants.MAX_BASE_ADDRESS;
        buffer.setWritePosition(Iso2709Constants.BASE_ADDRESS_START);
        buffer.writeInt(baseAddress, Iso2709Constants.BASE_ADDRESS_LENGTH);
    }

    void setSystemChars(final char[] systemChars) {
        assert systemChars.length == Iso2709Constants.SYSTEM_CHARS_LENGTH;
        buffer.setWritePosition(Iso2709Constants.SYSTEM_CHARS_START);
        buffer.writeChars(systemChars);
    }

    void setSystemChar(final int index, final char value) {
        assert 0 <= index && index < Iso2709Constants.SYSTEM_CHARS_LENGTH;
        buffer.setWritePosition(Iso2709Constants.SYSTEM_CHARS_START + index);
        buffer.writeChar(value);
    }

    void setReservedChar(final char reservedChar) {
        buffer.setWritePosition(Iso2709Constants.RESERVED_CHAR_POS);
        buffer.writeChar(reservedChar);
    }

    void reset() {
        buffer.setWritePosition(0);
        buffer.writeBytes(defaultLabel);
    }

    void copyToBuffer(final byte[] destBuffer) {
        System.arraycopy(buffer.getByteArray(), 0, destBuffer, 0,
                buffer.getLength());
    }

    @Override
    public String toString() {
        return buffer.stringAt(0, buffer.getLength(), Iso646Constants.CHARSET);
    }

}
