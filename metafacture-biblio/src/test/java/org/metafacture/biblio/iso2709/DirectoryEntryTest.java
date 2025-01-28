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

import org.metafacture.commons.StringUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * Tests for class {@link DirectoryEntry}.
 *
 * @author Christoph Böhme
 */
public class DirectoryEntryTest {

    private static final String DUMMY_LABEL = StringUtil.repeatChars(' ', 24);

    private static final byte[] RECORD = asBytes(DUMMY_LABEL + "001234IMP" +
            "002567LEM" + "012090AKB" +  "012890AKB" +
            Iso646Constants.INFORMATION_SEPARATOR_2 +
            Iso646Constants.INFORMATION_SEPARATOR_3);

    private DirectoryEntry directoryEntry;

    public DirectoryEntryTest() {
    }

    @Before
    public void createSystemUnderTest() {
        final RecordFormat recordFormat = RecordFormat.create()
                .withFieldLengthLength(1)
                .withFieldStartLength(2)
                .withImplDefinedPartLength(3)
                .build();
        final Iso646ByteBuffer buffer = new Iso646ByteBuffer(RECORD);
        directoryEntry = new DirectoryEntry(buffer, recordFormat, RECORD.length - 1);
    }

    @Test
    public void constructorShouldSetFirstEntryAsCurrentEntry() {
        Assert.assertArrayEquals("001".toCharArray(), directoryEntry.getTag());
    }

    @Test
    public void gotoNextShouldSetNextEntryAsCurrentEntry() {
        directoryEntry.gotoNext();
        Assert.assertArrayEquals("002".toCharArray(), directoryEntry.getTag());
    }

    @Test
    public void resetShouldSetFirstEntryAsCurrentEntry() {
        directoryEntry.gotoNext();
        directoryEntry.rewind();
        Assert.assertArrayEquals("001".toCharArray(), directoryEntry.getTag());
    }

    @Test
    public void endOfDirectoryReachedShouldReturnFalseIfNotAtEndOFDirectory() {
        Assert.assertFalse(directoryEntry.endOfDirectoryReached());
    }

    @Test
    public void endOfDirectoryReachedShouldReturnTrueIfAtEndOFDirectory() {
        directoryEntry.gotoNext();
        directoryEntry.gotoNext();
        directoryEntry.gotoNext();
        directoryEntry.gotoNext();
        Assert.assertTrue(directoryEntry.endOfDirectoryReached());
    }

    @Test
    public void getTagShouldReturnTagFromCurrentEntry() {
        Assert.assertArrayEquals("001".toCharArray(), directoryEntry.getTag());
        directoryEntry.gotoNext();
        Assert.assertArrayEquals("002".toCharArray(), directoryEntry.getTag());
    }

    @Test
    public void getFieldLengthShouldReturnFieldLengthFromCurrentEntry() {
        Assert.assertEquals(2, directoryEntry.getFieldLength());
        directoryEntry.gotoNext();
        Assert.assertEquals(5, directoryEntry.getFieldLength());
    }

    @Test
    public void getFieldStartShouldReturnFieldStartFromCurrentEntry() {
        Assert.assertEquals(34, directoryEntry.getFieldStart());
        directoryEntry.gotoNext();
        Assert.assertEquals(67, directoryEntry.getFieldStart());
    }

    @Test
    public void getImplDefinedPartShouldReturnImplDefinedPartFromCurrentEntry() {
        Assert.assertArrayEquals("IMP".toCharArray(), directoryEntry.getImplDefinedPart());
        directoryEntry.gotoNext();
        Assert.assertArrayEquals("LEM".toCharArray(), directoryEntry.getImplDefinedPart());
    }

    @Test
    public void isRecordIdFieldShouldReturnOnlyTrueIfTagIs001() {
        Assert.assertTrue(directoryEntry.isRecordIdField());
        directoryEntry.gotoNext();
        Assert.assertFalse(directoryEntry.isRecordIdField());
        directoryEntry.gotoNext();
        Assert.assertFalse(directoryEntry.isRecordIdField());
    }

    @Test
    public void isReferenceFieldShouldReturnOnlyTrueIfTagStartsWith00() {
        Assert.assertTrue(directoryEntry.isReferenceField());
        directoryEntry.gotoNext();
        Assert.assertTrue(directoryEntry.isReferenceField());
        directoryEntry.gotoNext();
        Assert.assertFalse(directoryEntry.isReferenceField());
    }

    @Test
    public void isContinuedFieldShouldReturnTrueIfFieldHasZeroLength() {
        Assert.assertFalse(directoryEntry.isContinuedField());
        directoryEntry.gotoNext();
        Assert.assertFalse(directoryEntry.isContinuedField());
        directoryEntry.gotoNext();
        Assert.assertTrue(directoryEntry.isContinuedField());
        directoryEntry.gotoNext();
        Assert.assertFalse(directoryEntry.isContinuedField());
    }

    private static byte[] asBytes(final String str) {
        return str.getBytes(Charset.forName("UTF-8"));
    }

}
