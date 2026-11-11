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

import org.metafacture.framework.FormatException;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Tests for class {@link Iso646ByteBuffer}.
 *
 * @author Christoph Böhme
 */
public final class Iso646ByteBufferTest {

    private static final char ASCII_UNMAPPABLE_CHAR = '\ufffd';

    private Iso646ByteBuffer byteBuffer;

    public Iso646ByteBufferTest() {
    }

    @Test
    public void getLengthShouldReturnRecordLength() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(3, byteBuffer.getLength());
    }

    @Test
    public void getFreeSpaceShouldReturnBufferLengthIfNothingWasWritten() {
        byteBuffer = new Iso646ByteBuffer(5);
        Assert.assertEquals(5, byteBuffer.getFreeSpace());
    }

    @Test
    public void getFreeSpaceShouldReturnSpaceBetweenWritePositionAndBufferEnd() {
        byteBuffer = new Iso646ByteBuffer(5);
        byteBuffer.setWritePosition(2);
        Assert.assertEquals(3, byteBuffer.getFreeSpace());
    }

    @Test
    public void getFreeSpaceShouldReturnZeroIfBufferIsFull() {
        byteBuffer = new Iso646ByteBuffer(5);
        byteBuffer.setWritePosition(5);
        Assert.assertEquals(0, byteBuffer.getFreeSpace());
    }

    @Test
    public void distanceToByteShouldReturnDistanceToFirstMatchingByte() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(2, byteBuffer.distanceTo((byte) 'x', 0));
    }

    @Test
    public void distanceToByteShouldReturnDistanceToEndOfBufferIfNoMatchFound() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(3, byteBuffer.distanceTo((byte) 'X', 0));
    }

    @Test
    public void distanceToByteShouldReturnZeroIfSearchStartsAtMatchingByte() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(0, byteBuffer.distanceTo((byte) 'T', 0));
    }

    @Test
    public void distanceToByteArrayShouldReturnDistanceToFirstMatchingByte() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(2, byteBuffer.distanceTo(asBytes("x"), 0));
    }

    @Test
    public void distanceToByteArrayShouldReturnDistanceToEndOfBufferIfNoMatchFound() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(3, byteBuffer.distanceTo(asBytes("X"), 0));
    }

    @Test
    public void distanceToByteArrayShouldReturnZeroIfSearchStartsAtMatchingByte() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(0, byteBuffer.distanceTo(asBytes("T"), 0));
    }

    @Test
    public void distanceToByteArrayShouldReturnDistanceToFirstMatchingByteOfTheBytesArray() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals(1, byteBuffer.distanceTo(asBytes("xu"), 0));
    }

    @Test
    public void stringAtShouldReturnStringDecodedAsUtf8() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tüx Tox"));
        Assert.assertEquals("Tüx Tox", byteBuffer.stringAt(0, byteBuffer.getLength(),
                StandardCharsets.UTF_8));
    }

    @Test
    public void stringAtShouldReturnEmptyStringIfLengthIsZero() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals("", byteBuffer.stringAt(0, 0, StandardCharsets.UTF_8));
    }

    @Test
    public void charAtShouldReturnCharacterAtIndexDecodedAsIso646() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertEquals('T', byteBuffer.charAt(0));
    }

    @Test(expected = FormatException.class)
    public void charAtShouldThrowFormatExceptionIfByteValueIsNotInIso646() {
        byteBuffer = new Iso646ByteBuffer(asBytes("ü"));
        byteBuffer.charAt(0); // Exception expected
    }

    @Test
    public void charsAtShouldReturnBytesAsCharacterArrayDecodedAsIso646() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux Tox"));
        Assert.assertArrayEquals("Tux".toCharArray(), byteBuffer.charsAt(0, 3));
    }

    @Test(expected = FormatException.class)
    public void charsAtShouldThrowFormatExceptionIfByteValueIsNotInIso646() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tüx Tox"));
        byteBuffer.charsAt(0, 4); // Exception expected
    }

    @Test
    public void charsAtShouldReturnEmptyCharacterArrayIfLengthIsZero() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        Assert.assertArrayEquals(new char[0], byteBuffer.charsAt(0, 0));
    }

    @Test
    public void byteAtShouldReturnByteAtIndex() {
        byteBuffer = new Iso646ByteBuffer(new byte[]{0x01, 0x02});
        Assert.assertEquals(0x02, byteBuffer.byteAt(1));
    }

    @Test
    public void parseIntAtShouldReturnIntValueAtIndex() {
        byteBuffer = new Iso646ByteBuffer(asBytes("299"));
        Assert.assertEquals(2, byteBuffer.parseIntAt(0));
    }

    @Test(expected = NumberFormatException.class)
    public void parseIntAtShouldThrowFormatExceptionIfNotADigit() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        byteBuffer.parseIntAt(0); // Exception expected
    }

    @Test
    public void parseIntAtShouldReturnIntValueForRange() {
        byteBuffer = new Iso646ByteBuffer(asBytes("299"));
        Assert.assertEquals(299, byteBuffer.parseIntAt(0, 3));
    }

    @Test
    public void parseIntAtShouldReturnZeroIfLengthIsZero() {
        byteBuffer = new Iso646ByteBuffer(asBytes("123"));
        Assert.assertEquals(0, byteBuffer.parseIntAt(0, 0));
    }

    @Test(expected = NumberFormatException.class)
    public void parseIntAtShouldThrowFormatExceptionIfNotANumber() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux"));
        byteBuffer.parseIntAt(0, 3); // Exception expected
    }

    @Test(expected = NumberFormatException.class)
    public void parseIntAtShouldThrowFormatExceptionIfNumberIsTooLarge() {
        byteBuffer = new Iso646ByteBuffer(asBytes("123456789123456789"));
        byteBuffer.parseIntAt(0, 18); // Exception expected
    }

    @Test
    public void writeCharShouldWriteCharAtWritePosition() {
        byteBuffer = new Iso646ByteBuffer(3);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeChar('c');

        Assert.assertArrayEquals(new byte[]{0x00, 0x63, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(2, byteBuffer.getWritePosition());
    }

    @Test
    public void writeCharsShouldWriteCharArrayAtWritePosition() {
        byteBuffer = new Iso646ByteBuffer(4);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeChars(new char[]{'c', 'b'});

        Assert.assertArrayEquals(new byte[]{0x00, 0x63, 0x62, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(3, byteBuffer.getWritePosition());
    }

    @Test
    public void writeByteShouldWriteByteAtWritePosition() {
        byteBuffer = new Iso646ByteBuffer(3);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeByte((byte) 0x61);

        Assert.assertArrayEquals(new byte[]{0x00, 0x61, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(2, byteBuffer.getWritePosition());
    }

    @Test
    public void writeBytesShouldWriteByteArrayAtWritePosition() {
        byteBuffer = new Iso646ByteBuffer(4);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeBytes(asBytes("cb"));

        Assert.assertArrayEquals(new byte[]{0x00, 0x63, 0x62, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(3, byteBuffer.getWritePosition());
    }

    @Test
    public void writeIntShouldWriteAsciiCodeOfSingleDigitAtWritePosition() {
        byteBuffer = new Iso646ByteBuffer(3);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeInt(3);

        Assert.assertArrayEquals(new byte[]{0x00, 0x33, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(2, byteBuffer.getWritePosition());
    }

    @Test
    public void writeIntShouldWriteAsciiCodesOfDigitsAtWritePosition() {
        byteBuffer = new Iso646ByteBuffer(5);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeInt(123, 3);

        Assert.assertArrayEquals(new byte[]{0x00, 0x31, 0x32, 0x33, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(4, byteBuffer.getWritePosition());
    }

    @Test
    public void writeIntShouldAddLeadingZerosIfNumberIsShorterThanDigits() {
        byteBuffer = new Iso646ByteBuffer(5);
        byteBuffer.setWritePosition(1);
        byteBuffer.writeInt(3, 3);

        Assert.assertArrayEquals(new byte[]{0x00, 0x30, 0x30, 0x33, 0x00},
                byteBuffer.getByteArray());
        Assert.assertEquals(4, byteBuffer.getWritePosition());
    }

    @Test
    public void toStringShouldReturnBufferContentDecodedAsISO646() {
        byteBuffer = new Iso646ByteBuffer(asBytes("Tux tüt"));
        Assert.assertEquals("Tux t" + ASCII_UNMAPPABLE_CHAR + ASCII_UNMAPPABLE_CHAR + "t",
                byteBuffer.toString());
    }

    private byte[] asBytes(final String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

}
