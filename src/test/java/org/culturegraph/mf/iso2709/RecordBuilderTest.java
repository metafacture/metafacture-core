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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link RecordBuilder}.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordBuilderTest {

	private RecordFormat format;

	@Before
	public void setup() {
		format = new RecordFormat();
		format.setIndicatorLength(2);
		format.setIdentifierLength(2);
		format.setFieldStartLength(3);
		format.setFieldLengthLength(2);
		format.setImplDefinedPartLength(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFormatIsNull() {
		new RecordBuilder(null);
	}

	@Test
	public void shouldWriteRecordFormatToRecordLabel() {
		final RecordFormat format = new RecordFormat();
		format.setIndicatorLength(2);
		format.setIdentifierLength(3);
		format.setFieldStartLength(4);
		format.setFieldLengthLength(5);
		format.setImplDefinedPartLength(6);

		final RecordBuilder builder = new RecordBuilder(format);
		final String record = builder.toString();

		assertEquals("23", record.substring(10, 12));
		assertEquals("546", record.substring(20, 23));
	}

	@Test
	public void shouldWriteRecordStatusToRecordLabel() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setRecordStatus('S');

		final String record = builder.toString();
		assertEquals('S', record.charAt(5));
	}

	@Test
	public void shouldWriteSpaceIfRecordStatusNotSet() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		final String record = builder.toString();
		assertEquals(' ', record.charAt(5));
	}

	@Test
	public void shouldWriteImplCodesToRecordLabel() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setImplCodes("IMPL");

		final String record = builder.toString();
		assertEquals("IMPL", record.substring(6, 10));
	}

	@Test
	public void shouldWriteSpacesIfImplCodesNotSet() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		final String record = builder.toString();
		assertEquals("    ", record.substring(6, 10));

	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfImplCodesIsLessThanFour() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setImplCodes("123");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfImplCodesIsGreaterThanFour() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setImplCodes("12345");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfImplCodesIsNull() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setImplCodes(null);
	}

	@Test
	public void shouldWriteSystemCharsToRecordLabel() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setSystemChars("USC");

		final String record = builder.toString();
		assertEquals("USC", record.substring(17, 20));
	}

	@Test
	public void shouldWriteSpacesIfSystemCharsNotSet() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		final String record = builder.toString();
		assertEquals("   ", record.substring(17, 20));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfSystemCharsIsLessThanThree() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setSystemChars("12");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfSystemCharsIsGreaterThanThree() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setSystemChars("1234");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfSystemCharsIsNull() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setSystemChars(null);
	}

	@Test
	public void shouldWriteReservedCharToRecordLabel() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		builder.setReservedChar('R');

		final String record = builder.toString();
		assertEquals('R', record.charAt(23));
	}

	@Test
	public void shouldWriteSpaceIfReservedCharNotSet() {
		final RecordBuilder builder = new RecordBuilder(new RecordFormat());

		final String record = builder.toString();
		assertEquals(' ', record.charAt(23));
	}

	@Test
	public void shouldAppendReferenceFieldToRecord() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("002", "IM", "Value");

		final String record = builder.toString();
		assertEquals("00206000IM", record.substring(24, 34));
		assertEquals("Value\u001e", record.substring(35, 41));
	}

	@Test
	public void shouldWriteTwoDirectoryEntriesForReferenceFieldsWithLongValue() {
		final RecordBuilder builder = new RecordBuilder(format);

		final String longValue = StringUtil.repeatChars('A', 110);
		builder.appendReferenceField("002", "IM", longValue);

		final String record = builder.toString();
		assertEquals("00200000", record.substring(24, 32));
		assertEquals("00212099", record.substring(34, 42));
		assertEquals(longValue + '\u001e', record.substring(45, 156));
	}

	@Test
	public void shouldFillImplDefinedPartOfReferenceFieldWithSpacesIfNotProvided() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("002", "Value");

		final String record = builder.toString();
		assertEquals("00206000  ", record.substring(24, 34));
	}

	@Test
	public void shouldAppendReferenceFieldWithoutImplDefinedPart() {
		format.setImplDefinedPartLength(0);
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("002", "Value");

		final String record = builder.toString();
		assertEquals("00206000\u001e", record.substring(24, 33));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfStartOfReferenceFieldIsNotInAddressRange() {
		final RecordBuilder builder = new RecordBuilder(format);

		final String longValue = StringUtil.repeatChars('A', 1000);
		builder.appendReferenceField("002", "IM", longValue);

		builder.appendReferenceField("003", "IM", "would not fit");
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfStartOfLastPartOfReferenceFieldIsNotInAddressRange() {
		final RecordBuilder builder = new RecordBuilder(format);

		final String tooLongValue = StringUtil.repeatChars('A', 1100);
		builder.appendReferenceField("002", "IM", tooLongValue);
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfReferenceFieldTagLengthIsNotThree() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("0020", "IM", "Value");
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfTagDoesNotStartWithTwoZeros() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("012", "IM", "Value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldTagIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField(null, "IM", "Value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldImplDefinedPartLengthDoesNotMatchFormat() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("002", "IMP", "Value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldImplDefinedPartIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("002", null, "value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldValueIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("002", "IM", null);
	}

	@Test
	public void shouldAppendDataFieldToRecord() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("010", "IN", "IM");
		builder.endField();

		final String record = builder.toString();
		assertEquals("01003000IM", record.substring(24, 34));
		assertEquals("IN\u001e", record.substring(35, 38));
	}

	@Test
	public void shouldFillImplDefinedPartOfDataFieldWithSpacesIfNotProvided() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("012", "IN");
		builder.endField();

		final String record = builder.toString();
		assertEquals("01203000  ", record.substring(24, 34));
	}

	@Test
	public void shouldFillIndicatorsOfDataFieldWithSpacesIfNotProvided() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("012");
		builder.endField();

		final String record = builder.toString();
		assertEquals("  \u001e", record.substring(35, 38));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldTagIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField(null, "IN", "IM");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldIndictaorsIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", null, "IM");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldImplDefinedPartIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", null);
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfDataFieldTagLengthIsNotThree() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("01", "IN", "IM");
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfDataFieldTagStartsWithTwoZeros() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("002", "IN", "IM");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldImplDefinedPartLengthDoesNotMatchFormat() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IMP");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorsLenghtDoesNotMatchformat() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "INS", "IM");
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfAppendReferenceFieldIsCalledWhileAppendingDataField() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IM");
		builder.appendReferenceField("002", "IM", "Value");
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfToStringIsCalledWhileAppendingDataField() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IM");
		builder.toString();
	}

	@Test
	public void shouldAllowAppendingReferenceFieldAfterFinishingDataField() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IM");
		builder.endField();
		builder.appendReferenceField("002", "IM", "Value");

		// No assertions need. If no exception was thrown
		// the test was successful
	}

	@Test
	public void shouldAppendSubfieldsToRecord() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "  ");
		builder.appendSubfield("A", "val1");
		builder.appendSubfield("B", "val2");
		builder.endField();

		final String record = builder.toString();
		assertEquals("02015000  ", record.substring(24, 34));
		assertEquals("\u001fAval1\u001fBval2\u001e", record.substring(37, 50));
	}

	@Test
	public void shouldWriteTwoDirectoryEntriesForAFieldWithLongSubfields() {
		final RecordBuilder builder = new RecordBuilder(format);

		final String longValue1 = StringUtil.repeatChars('A', 60);
		final String longValue2 = StringUtil.repeatChars('B', 60);
		builder.startField("020", "IN", "  ");
		builder.appendSubfield("A", longValue1);
		builder.appendSubfield("B", longValue2);
		builder.endField();

		final String record = builder.toString();
		assertEquals("02000000  ", record.substring(24, 34));
		assertEquals("02028099  ", record.substring(34, 44));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IM");
		builder.appendSubfield(null, "Value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldValueIsNull() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IM");
		builder.appendSubfield("A", null);
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfLastPartOfDataFieldIsNotInAddressRange() {
		final RecordBuilder builder = new RecordBuilder(format);

		final String longValue = StringUtil.repeatChars('A', 1100);
		builder.startField("020", "IN", "IM");
		builder.appendSubfield("A", longValue);
		builder.appendSubfield("B", "Value");
		builder.endField();
	}

	@Test
	public void shouldLeaveRecordInACleanStateIfAppendingDataFieldFailed() {
		final RecordBuilder builder = new RecordBuilder(format);

		boolean exceptionThrown = false;
		final String longValue = StringUtil.repeatChars('A', 1100);
		builder.startField("020", "IN", "IM");
		builder.appendSubfield("A", longValue);
		builder.appendSubfield("B", "Value");
		try {
			builder.endField();
		} catch (final FormatException e) {
			exceptionThrown = true;
		}

		final String record = builder.toString();
		assertTrue(exceptionThrown);
		assertEquals("\u001e\u001d", record.substring(24, 26));
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfAppendSubfieldIsNotCalledWithinAppendFieldSequence() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendSubfield("A", "Value");
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfStartAppendFieldIsCalledTwice() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("020", "IN", "IM");
		builder.startField("020", "IN", "IM");
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfEndAppendFieldIsNotMatchedByStartAppendField() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.endField();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierLengthDoesNotMatchFormat() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("200", "IN", "IM");
		builder.appendSubfield("12", "Value");
	}

	@Test
	public void shouldFillIdentifierWithSpacesIfNotProvided() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("200", "IN", "IM");
		builder.appendSubfield("Value");
		builder.endField();

		final String record = builder.toString();
		assertEquals("\u001f Value", record.substring(37, 44));
	}

	@Test
	public void shouldWriteOnlyIdentifierMarkerIfIdentifierLengthIsOne() {
		format.setIdentifierLength(1);
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("200", "IN", "IM");
		builder.appendSubfield("Value");
		builder.endField();

		final String record = builder.toString();
		assertEquals("\u001fValue", record.substring(37, 43));
	}

	@Test
	public void shouldWriteNoIdentifierMarkerIfIdentifierLengthIsZero() {
		format.setIdentifierLength(0);
		final RecordBuilder builder = new RecordBuilder(format);

		builder.startField("200", "IN", "IM");
		builder.appendSubfield("Ada");
		builder.appendSubfield("Lovelace");
		builder.endField();

		final String record = builder.toString();
		assertEquals("AdaLovelace", record.substring(37, 48));
	}

	@Test
	public void baseAddressShouldPointToEndOfDirectory() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("001", "  ", "value");

		final String record = builder.toString();
		assertEquals("00035", record.substring(12, 17));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfBaseAddressIsNotInAddressRange() {
		format.setFieldStartLength(9);
		format.setFieldLengthLength(9);
		format.setImplDefinedPartLength(9);
		final RecordBuilder builder = new RecordBuilder(format);

		final int dirEntries = (100000 - 24 - 1) / (9 * 3 + 3) + 1;
		for (int i = 0; i < dirEntries; ++i) {
			builder.appendReferenceField("002", "123456789", "");
		}
		builder.toString();
	}

	@Test
	public void recordLengthShouldMatchLengthOfRecordString() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField("001", "  ", "value");

		final String record = builder.toString();
		assertEquals(String.format("%05d", record.length()),
				record.substring(0, 5));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfRecordLengthisTooLarge() {
		format.setFieldLengthLength(9);
		final RecordBuilder builder = new RecordBuilder(format);

		final String longValue = StringUtil.repeatChars('C', 100000);
		builder.appendReferenceField("002", "  ", longValue);
		builder.toString();
	}

	@Test
	public void shouldEndWithRecordSeparator() {
		final RecordBuilder builder = new RecordBuilder(format);
		final String record = builder.toString();

		assertEquals('\u001d', record.charAt(record.length() - 1));
	}

	@Test
	public void shouldResetBuilder() {
		final RecordBuilder builder = new RecordBuilder(format);
		builder.setRecordStatus('S');
		builder.setImplCodes("IMPL");
		builder.setSystemChars("USC");
		builder.setReservedChar('R');
		builder.appendReferenceField("002", "  ", "record1");
		builder.toString();

		builder.reset();

		final String record = builder.toString();
		assertEquals(26, record.length());
		assertEquals(' ', record.charAt(5));
		assertEquals("    ", record.substring(6, 10));
		assertEquals("   ", record.substring(17, 20));
		assertEquals(' ', record.charAt(23));
	}

}
