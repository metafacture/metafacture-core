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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.framework.FormatException;
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
	private RecordBuilder builder;

	@Before
	public void createSystemUnderTest() {
		format = RecordFormat.create()
				.withIndicatorLength(2)
				.withIdentifierLength(2)
				.withFieldStartLength(3)
				.withFieldLengthLength(2)
				.withImplDefinedPartLength(2)
				.build();
		builder = new RecordBuilder(format);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFormatIsNull() {
		new RecordBuilder(null);  // Exception expected
	}

	@Test
	public void shouldWriteRecordFormatToRecordLabel() {
		format = RecordFormat.create()
				.withIndicatorLength(2)
				.withIdentifierLength(3)
				.withFieldStartLength(4)
				.withFieldLengthLength(5)
				.withImplDefinedPartLength(6)
				.build();
		final RecordBuilder builder = new RecordBuilder(format);

		final byte[] record = builder.build();

		assertEquals("23", asString(record, 10, 12));
		assertEquals("546", asString(record, 20, 23));
	}

	@Test
	public void shouldWriteRecordStatusToRecordLabel() {
		builder.setRecordStatus('S');

		final byte[] record = builder.build();

		assertEquals(0x53, record[5]);
	}

	@Test
	public void shouldWriteSpaceIfRecordStatusNotSet() {
		final byte[] record = builder.build();

		assertEquals(0x20, record[5]);
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfRecordStatusIsNot7BitAscii() {
		builder.setRecordStatus('\u00df');  // Exception expected
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfRecordStatusIsInformationSeparator() {
		builder.setRecordStatus('\u001e');  // Exception expected
	}

	@Test
	public void shouldWriteImplCodesToRecordLabel() {
		builder.setImplCodes(asChars("IMPL"));

		final byte[] record = builder.build();

		assertEquals(0x49, record[6]);
		assertEquals(0x4d, record[7]);
		assertEquals(0x50, record[8]);
		assertEquals(0x4c, record[9]);
	}

	@Test
	public void shouldWriteSpacesIfImplCodesNotSet() {
		final byte[] record = builder.build();

		assertEquals(0x20, record[6]);
		assertEquals(0x20, record[7]);
		assertEquals(0x20, record[8]);
		assertEquals(0x20, record[9]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfImplCodesIsLessThanFour() {
		builder.setImplCodes(asChars("123"));  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfImplCodesIsGreaterThanFour() {
		builder.setImplCodes(asChars("12345"));  // Exception expected
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfImplCodesAreNot7BitAscii() {
		builder.setImplCodes(asChars("12\u00df4"));  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfImplCodesIsNull() {
		builder.setImplCodes(null);  // Exception expected
	}

	@Test
	public void shouldWriteSystemCharsToRecordLabel() {
		builder.setSystemChars(asChars("USC"));

		final byte[] record = builder.build();

		assertEquals(0x55, record[17]);
		assertEquals(0x53, record[18]);
		assertEquals(0x43, record[19]);
	}

	@Test
	public void shouldWriteSpacesIfSystemCharsNotSet() {
		final byte[] record = builder.build();

		assertEquals(0x20, record[17]);
		assertEquals(0x20, record[18]);
		assertEquals(0x20, record[19]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfSystemCharsIsLessThanThree() {
		builder.setSystemChars(asChars("12"));  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfLengthOfSystemCharsIsGreaterThanThree() {
		builder.setSystemChars(asChars("1234"));  // Exception expected
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfSystemCharsAreNot7BitAscii() {
		builder.setSystemChars(asChars("1\u00df3"));  // Exception expected
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfSystemCharIsInformationSeparator() {
		builder.setSystemChars(asChars("1\u001e3"));  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfSystemCharsIsNull() {
		builder.setSystemChars(null);  // Exception expected
	}

	@Test
	public void shouldWriteReserverdCharToRecordLabel() {
		builder.setReservedChar('R');

		final byte[] record = builder.build();

		assertEquals(0x52, record[23]);
	}

	@Test
	public void shouldWriteSpaceIfReservedCharNotSet() {
		final byte[] record = builder.build();

		assertEquals(0x20, record[23]);
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReservedCharIsNot7BitAscii() {
		builder.setReservedChar('\u00df');  // Exception expected
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReservedCharIsInformationSeparator() {
		builder.setReservedChar('\u001d');  // Exception expected
	}

	@Test
	public void shouldAppendReferenceFieldToRecord() {
		builder.appendReferenceField(asChars("002"), asChars("IM"), "Value");

		final byte[] record = builder.build();

		assertEquals("00206000IM", asString(record, 24, 34));
		assertEquals("Value\u001e", asString(record, 35, 41));
	}

	@Test
	public void shouldWriteTwoDirectoryEntriesForReferenceFieldsWithLongValue() {
		final String longValue = StringUtil.repeatChars('A', 110);
		builder.appendReferenceField(asChars("002"), asChars("IM"), longValue);

		final byte[] record = builder.build();

		assertEquals("00200000", asString(record, 24, 32));
		assertEquals("00212099", asString(record, 34, 42));
		assertEquals(longValue + '\u001e', asString(record, 45, 156));
	}

	@Test
	public void shouldFillImplDefinedPartOfReferenceFieldWithSpacesIfNotProvided() {
		builder.appendReferenceField(asChars("002"), "Value");

		final byte[] record = builder.build();

		assertEquals("00206000  ", asString(record, 24, 34));
	}

	@Test
	public void shouldAppendReferenceFieldWithoutImplDefinedPart() {
		format = RecordFormat.createFrom(format)
				.withImplDefinedPartLength(0)
				.build();
		final RecordBuilder builder = new RecordBuilder(format);
		builder.appendReferenceField(asChars("002"), "Value");

		final byte[] record = builder.build();

		assertEquals("00206000\u001e", asString(record, 24, 33));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfStartOfReferenceFieldIsNotInAddressRange() {
		final String longValue = StringUtil.repeatChars('A', 1000);
		builder.appendReferenceField(asChars("002"), asChars("IM"), longValue);

		builder.appendReferenceField(asChars("003"), asChars("IM"),
				"would not fit");  // Exception expected
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfStartOfLastPartOfReferenceFieldIsNotInAddressRange() {
		final String tooLongValue = StringUtil.repeatChars('A', 1100);

		builder.appendReferenceField(asChars("002"), asChars("IM"), tooLongValue);
		// Exception expected
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfReferenceFieldTagLengthIsNotThree() {
		builder.appendReferenceField(asChars("0020"), asChars("IM"), "Value");
		// Exception expected
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfTagDoesNotStartWithTwoZeros() {
		builder.appendReferenceField(asChars("012"), asChars("IM"), "Value");
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldTagIsNull() {
		builder.appendReferenceField(null, asChars("IM"), "Value");
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldImplDefinedPartLengthDoesNotMatchFormat() {
		builder.appendReferenceField(asChars("002"), asChars("IMP"), "Value");
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldImplDefinedPartIsNot7BitAscii() {
		builder.appendReferenceField(asChars("002"), asChars("I\u00df"), "Value");
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldImplDefinedPartIsInformationSeparator() {
		builder.appendReferenceField(asChars("002"), asChars("I\u001d"), "Value");
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldImplDefinedPartIsNull() {
		builder.appendReferenceField(asChars("002"), null, "value");
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfReferenceFieldValueIsNull() {
		builder.appendReferenceField(asChars("002"), asChars("IM"), null);
		// Exception expected
	}

	@Test
	public void shouldAppendDataFieldToRecord() {
		builder.startDataField(asChars("010"), asChars("IN"), asChars("IM"));
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("01003000IM", asString(record, 24, 34));
		assertEquals("IN\u001e", asString(record, 35, 38));
	}

	@Test
	public void shouldFillImplDefinedPartOfDataFieldWithSpacesIfNotProvided() {
		builder.startDataField(asChars("012"), asChars("IN"));
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("01203000  ", asString(record, 24, 34));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfImplDefinedPartIsNot7BitAscii() {
		builder.startDataField(asChars("012"), asChars("IN"), asChars("I\u00df"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfImplDefinedPartIsInformationSeparator() {
		builder.startDataField(asChars("012"), asChars("IN"), asChars("I\u001f"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorsAreNot7BitAscii() {
		builder.startDataField(asChars("012"), asChars("I\u00df"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorsIsInformationSeparator() {
		builder.startDataField(asChars("012"), asChars("I\u001e"));
		// Exception expected
	}

	@Test
	public void shouldFillIndicatorsOfDataFieldWithSpacesIfNotProvided() {
		builder.startDataField(asChars("012"));
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("  \u001e", asString(record, 35, 38));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldTagIsNull() {
		builder.startDataField(null, asChars("IN"), asChars("IM"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldIndicatorsIsNull() {
		builder.startDataField(asChars("020"), null, asChars("IM"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldImplDefinedPartIsNull() {
		builder.startDataField(asChars("020"), asChars("IN"), null);
		// Exception expected
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfDataFieldTagLengthIsNotThree() {
		builder.startDataField(asChars("01"), asChars("IN"), asChars("IM"));
		// Exception expected
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfDataFieldTagStartsWithTwoZeros() {
		builder.startDataField(asChars("002"), asChars("IN"), asChars("IM"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldImplDefinedPartLengthDoesNotMatchFormat() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IMP"));
		// Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorsLenghtDoesNotMatchformat() {
		builder.startDataField(asChars("020"), asChars("INS"), asChars("IM"));
		// Exception expected
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfAppendReferenceFieldIsCalledWhileAppendingDataField() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));

		builder.appendReferenceField(asChars("002"), asChars("IM"), "Value");
		// Exception expected
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowExceptionIfBuildIsCalledWhileAppendingDataField() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));

		builder.build();  // Exception expected
	}

	@Test(expected=IllegalStateException.class)
	public void shouldNotAllowAppendingReferenceFieldAfterFinishingDataField() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));
		builder.endDataField();

		builder.appendReferenceField(asChars("002"), asChars("IM"), "Value");
		// Exception expected
	}

	@Test
	public void shouldAppendSubfieldsToRecord() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("  "));
		builder.appendSubfield(asChars("A"), "val1");
		builder.appendSubfield(asChars("B"), "val2");
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("02015000  ", asString(record, 24, 34));
		assertEquals("\u001fAval1\u001fBval2\u001e", asString(record, 37, 50));
	}

	@Test
	public void shouldCountStringLengthInBytes() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("  "));
		// Letter ü requires two bytes when encoding in UTF-8:
		builder.appendSubfield(asChars("A"), "über");
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("02010000  ", asString(record, 24, 34));
		assertEquals("\u001fAüber\u001e", asString(record, 37, 45));
	}

	@Test
	public void shouldWriteTwoDirectoryEntriesForAFieldWithLongSubfields() {
		final String longValue1 = StringUtil.repeatChars('A', 60);
		final String longValue2 = StringUtil.repeatChars('B', 60);
		builder.startDataField(asChars("020"), asChars("IN"), asChars("  "));
		builder.appendSubfield(asChars("A"), longValue1);
		builder.appendSubfield(asChars("B"), longValue2);
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("02000000  ", asString(record, 24, 34));
		assertEquals("02028099  ", asString(record, 34, 44));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierIsNull() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));

		builder.appendSubfield(null, "Value");  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierIsNot7BitAscii() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));

		builder.appendSubfield(asChars("\u00df"), "Value");  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierIsInformationSeparator() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));

		builder.appendSubfield(asChars("\u001d"), "Value");  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfDataFieldValueIsNull() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));

		builder.appendSubfield(asChars("A"), null);  // Exception expected
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfLastPartOfDataFieldIsNotInAddressRange() {
		final String longValue = StringUtil.repeatChars('A', 1100);
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));
		builder.appendSubfield(asChars("A"), longValue);
		builder.appendSubfield(asChars("B"), "Value");

		builder.endDataField();  // Exception expected
	}

	@Test
	public void shouldLeaveRecordInACleanStateIfAppendingDataFieldFailed() {
		boolean exceptionThrown = false;
		final String longValue = StringUtil.repeatChars('A', 1100);
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));
		builder.appendSubfield(asChars("A"), longValue);
		builder.appendSubfield(asChars("B"), "Value");
		try {
			builder.endDataField();
		} catch (final FormatException e) {
			exceptionThrown = true;
		}

		final byte[] record = builder.build();

		assertTrue(exceptionThrown);
		assertEquals("\u001e\u001d", asString(record, 24, 26));
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfAppendSubfieldIsNotCalledWithinAppendFieldSequence() {
		builder.appendSubfield(asChars("A"), "Value");  // Exception expected
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfStartAppendFieldIsCalledTwice() {
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));
		builder.startDataField(asChars("020"), asChars("IN"), asChars("IM"));
		// Exception expected
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfEndAppendFieldIsNotMatchedByStartAppendField() {
		builder.endDataField();  // Exception expected
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierLengthDoesNotMatchFormat() {
		builder.startDataField(asChars("200"), asChars("IN"), asChars("IM"));

		builder.appendSubfield(asChars("12"), "Value");  // Exception expected
	}

	@Test
	public void shouldFillIdentifierWithSpacesIfNotProvided() {
		builder.startDataField(asChars("200"), asChars("IN"), asChars("IM"));
		builder.appendSubfield("Value");
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("\u001f Value", asString(record, 37, 44));
	}

	@Test
	public void shouldWriteOnlyIdentifierMarkerIfIdentifierLengthIsOne() {
		format = RecordFormat.createFrom(format)
				.withIdentifierLength(1)
				.build();
		final RecordBuilder builder = new RecordBuilder(format);
		builder.startDataField(asChars("200"), asChars("IN"), asChars("IM"));
		builder.appendSubfield("Value");
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("\u001fValue", asString(record, 37, 43));
	}

	@Test
	public void shouldWriteNoIdentifierMarkerIfIdentifierLengthIsZero() {
		format = RecordFormat.createFrom(format)
				.withIdentifierLength(0)
				.build();
		final RecordBuilder builder = new RecordBuilder(format);
		builder.startDataField(asChars("200"), asChars("IN"), asChars("IM"));
		builder.appendSubfield("Ada");
		builder.appendSubfield("Lovelace");
		builder.endDataField();

		final byte[] record = builder.build();

		assertEquals("AdaLovelace", asString(record, 37, 48));
	}

	@Test
	public void baseAddressShouldPointToEndOfDirectory() {
		final RecordBuilder builder = new RecordBuilder(format);

		builder.appendReferenceField(asChars("001"), asChars("  "), "value");

		final byte[] record = builder.build();
		assertEquals("00035", asString(record, 12, 17));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowExceptionIfBaseAddressIsNotInAddressRange() {
		format = RecordFormat.createFrom(format)
				.withFieldLengthLength(9)
				.withFieldStartLength(9)
				.withImplDefinedPartLength(9)
				.build();
		final RecordBuilder builder = new RecordBuilder(format);
		final int dirEntries = Iso2709Constants.MAX_PAYLOAD_LENGTH / (9 * 3 + 3) + 1;
		for (int i = 0; i < dirEntries; ++i) {
			builder.appendReferenceField(asChars("002"), asChars("123456789"), "");
		}

		builder.build();  // Exception expected
	}

	@Test
	public void recordLengthShouldMatchLengthOfRecordString() {
		builder.appendReferenceField(asChars("001"), asChars("  "), "value");

		final byte[] record = builder.build();

		assertEquals(String.format("%05d", record.length),
				asString(record, 0, 5));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowExceptionIfRecordLengthIsExceeded() {
		format = RecordFormat.createFrom(format)
				.withFieldLengthLength(9)
				.build();
		final RecordBuilder builder = new RecordBuilder(format);
		final String longValue = StringUtil.repeatChars('C', 100000);
		builder.appendReferenceField(asChars("002"), asChars("  "), longValue);

		builder.build();  // Exception expected
	}

	@Test
	public void shouldEndWithRecordSeparator() {
		final byte[] record = builder.build();

		assertEquals('\u001d', record[record.length - 1]);
	}

	@Test
	public void shouldResetBuilder() {
		builder.setRecordStatus('S');
		builder.setImplCodes(asChars("IMPL"));
		builder.setSystemChars(asChars("USC"));
		builder.appendReferenceField(asChars("002"), asChars("  "), "record1");

		builder.reset();

		final byte[] record = builder.build();
		assertEquals(26, record.length);
		assertEquals(0x20, record[5]);
		assertEquals(0x20, record[6]);
		assertEquals(0x20, record[7]);
		assertEquals(0x20, record[8]);
		assertEquals(0x20, record[9]);
		assertEquals(0x20, record[17]);
		assertEquals(0x20, record[18]);
		assertEquals(0x20, record[19]);
		assertEquals(0x20, record[23]);
	}

	private char[] asChars(final String value) {
		return value.toCharArray();
	}

	private String asString(final byte[] record, final int beginIndex,
			final int endIndex) {
		return new String(record, beginIndex, endIndex - beginIndex,
				StandardCharsets.UTF_8);
	}

}
