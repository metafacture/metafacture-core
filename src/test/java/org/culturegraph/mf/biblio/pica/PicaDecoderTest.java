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
package org.culturegraph.mf.biblio.pica;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.MissingIdException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link PicaDecoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class PicaDecoderTest {

	private static final String RECORD_ID = "2809";
	private static final String ENTITY_028A = "028A";
	private static final String NAME_A = "a";
	private static final String NAME_D = "d";
	private static final String VALUE_A = "Eco";
	private static final String VALUE_D = "Umberto";
	private static final String COMPOSED_UTF8 = "Über";  // 'Ü' constructed from U and diacritics
	private static final String STANDARD_UTF8 = "Über";  // 'Ü' is a single character

	private static final String RECORD_MARKER = "\u001d";
	private static final String FIELD_MARKER = "\u001e";
	private static final String SUBFIELD_MARKER = "\u001f";
	private static final String FIELD_END_MARKER = "\n";

	private static final String FIELD_001AT_0_TEST = "001@ " + SUBFIELD_MARKER + "0test";
	private static final String FIELD_003AT_0_ID = "003@ " + SUBFIELD_MARKER + "0" + RECORD_ID;
	private static final String FIELD_107F_0_ID = "107F " + SUBFIELD_MARKER + "0" + RECORD_ID;
	private static final String FIELD_203AT_0_ID = "203@ " + SUBFIELD_MARKER + "0" + RECORD_ID;
	private static final String FIELD_203AT_01_0_ID = "203@/01 " + SUBFIELD_MARKER + "0" + RECORD_ID;
	private static final String FIELD_203AT_100_0_ID = "203@/100 " + SUBFIELD_MARKER + "0" + RECORD_ID;
	private static final String FIELD_021A_A_UEBER = "021A " + SUBFIELD_MARKER + "a" + COMPOSED_UTF8;
	private static final String FIELD_028A = ENTITY_028A + " ";

	private PicaDecoder picaDecoder;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		picaDecoder = new PicaDecoder();
		picaDecoder.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		picaDecoder.closeStream();
	}

	@Test
	public void shouldParseRecordStartingWithRecordMarker() {
		picaDecoder.process(
				RECORD_MARKER + FIELD_001AT_0_TEST +
				FIELD_MARKER + FIELD_003AT_0_ID);


		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At0Test(ordered);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordStartingWithFieldMarker() {
		picaDecoder.process(
				FIELD_MARKER + FIELD_001AT_0_TEST +
				FIELD_MARKER + FIELD_003AT_0_ID);


		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At0Test(ordered);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordStartingWithSubfieldMarker() {
		picaDecoder.process(
				SUBFIELD_MARKER + NAME_A + VALUE_A +
				FIELD_MARKER + FIELD_003AT_0_ID);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity("");
		ordered.verify(receiver).literal(NAME_A, VALUE_A);
		ordered.verify(receiver).endEntity();
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordStartingWithEmptySubfield() {
		picaDecoder.process(
				SUBFIELD_MARKER +
				FIELD_MARKER + FIELD_003AT_0_ID);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordStartingWithFieldEndMarker() {
		picaDecoder.process(
				FIELD_END_MARKER + FIELD_001AT_0_TEST +
				FIELD_MARKER + FIELD_003AT_0_ID);


		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At0Test(ordered);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordStartingWithFieldName() {
		picaDecoder.process(
				FIELD_001AT_0_TEST +
				FIELD_MARKER + FIELD_003AT_0_ID);


		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At0Test(ordered);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordEndingWithRecordMarker() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_001AT_0_TEST + RECORD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		verify001At0Test(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordEndingWithFieldMarker() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_001AT_0_TEST + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		verify001At0Test(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordEndingWithSubfieldMarker() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A +
				SUBFIELD_MARKER + NAME_A + VALUE_A +
				SUBFIELD_MARKER + NAME_D + VALUE_D +
				SUBFIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity(ENTITY_028A);
		ordered.verify(receiver).literal(NAME_A, VALUE_A);
		ordered.verify(receiver).literal(NAME_D, VALUE_D);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordEndingWithSubfieldName() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A +
				SUBFIELD_MARKER + NAME_A + VALUE_A +
				SUBFIELD_MARKER + NAME_D);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity(ENTITY_028A);
		ordered.verify(receiver).literal(NAME_A, VALUE_A);
		ordered.verify(receiver).literal(NAME_D, "");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseRecordEndingWithFieldName() {
		// Do not skip the last field because it has no
		// sub fields:
		picaDecoder.setSkipEmptyFields(false);

		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity(ENTITY_028A);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldParseMultiLineRecordFormat() {
		picaDecoder.process(
				RECORD_MARKER + FIELD_END_MARKER +
				FIELD_MARKER + FIELD_001AT_0_TEST + FIELD_END_MARKER +
				FIELD_MARKER + FIELD_003AT_0_ID + FIELD_END_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At0Test(ordered);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldExtractPicaProductionNumberAfterRecordMarkerAsRecordId() {
		picaDecoder.process(RECORD_MARKER + FIELD_003AT_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberAfterFieldMarkerAsRecordId() {
		picaDecoder.process(FIELD_MARKER + FIELD_003AT_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberAfterFieldEndMarkerAsRecordId() {
		picaDecoder.process(FIELD_END_MARKER + FIELD_003AT_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberFollowedByRecordMarkerAsRecordId() {
		picaDecoder.process(FIELD_003AT_0_ID + RECORD_MARKER);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberFollowedByFieldMarkerAsRecordId() {
		picaDecoder.process(FIELD_003AT_0_ID + FIELD_MARKER);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberFollowedBySubfieldMarkerAsRecordId() {
		picaDecoder.process(FIELD_003AT_0_ID + SUBFIELD_MARKER);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberFollowedByFieldEndMarkerAsRecordId() {
		picaDecoder.process(FIELD_003AT_0_ID + FIELD_END_MARKER);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractPicaProductionNumberAtRecordEndAsRecordId() {
		picaDecoder.process(FIELD_003AT_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractLocalProductionNumberAsRecordId() {
		picaDecoder.process(FIELD_107F_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractCopyControlNumberAsRecordId() {
		picaDecoder.process(FIELD_203AT_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractCopyControlNumberWithOccurrenceAsRecordId() {
		picaDecoder.process(FIELD_203AT_01_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test
	public void shouldExtractCopyControlNumberWithThreeDigitOccurrenceAsRecordId() {
		picaDecoder.process(FIELD_203AT_100_0_ID);

		verify(receiver).startRecord(RECORD_ID);
	}

	@Test(expected=MissingIdException.class)
	public void shouldThrowMissingIdExceptionIfNoRecordIdIsFound() {
		picaDecoder.process(FIELD_001AT_0_TEST);
		// Exception expected
	}

	@Test
	public void shouldIgnoreMatchWithinFieldData() {
		picaDecoder.setIgnoreMissingIdn(true);

		picaDecoder.process(FIELD_001AT_0_TEST + FIELD_003AT_0_ID);

		verify(receiver).startRecord("");
	}

	@Test
	public void shouldIgnoreIncompleteMatch() {
		picaDecoder.setIgnoreMissingIdn(true);

		picaDecoder.process("003@ " + FIELD_MARKER + FIELD_001AT_0_TEST);

		verify(receiver).startRecord("");
	}

	@Test
	public void shouldSkipUnnamedFieldsWithNoSubFields() {
		// Make sure that the field is skipped because
		// it is empty and not because it has no sub
		// fields:
		picaDecoder.setSkipEmptyFields(false);

		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldSkipUnnamedFieldsWithOnlyUnnamedSubFields() {
		// Make sure that the field is skipped because
		// it is empty and not because it only has empty
		// sub fields:
		picaDecoder.setSkipEmptyFields(false);

		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				SUBFIELD_MARKER + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldNotSkipUnnamedFieldsWithSubFields() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				SUBFIELD_MARKER + NAME_A + VALUE_A +
				FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity("");
		ordered.verify(receiver).literal(NAME_A, VALUE_A);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldSkipUnnamedSubfields() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A +
				SUBFIELD_MARKER +
				SUBFIELD_MARKER + NAME_A + VALUE_A +
				FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity(ENTITY_028A);
		ordered.verify(receiver).literal(NAME_A, VALUE_A);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldSkipEmptyFieldsByDefault() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldSkipFieldsWithOnlyUnnamedSubfieldsByDefault() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A +
				SUBFIELD_MARKER +
				FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldNotSkipEmptyFieldsIfConfigured() {
		picaDecoder.setSkipEmptyFields(false);

		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity(ENTITY_028A);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldNotSkipFieldsWithOnlyUnnamedSubfieldsIfConfigured() {
		picaDecoder.setSkipEmptyFields(false);

		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_028A +
				SUBFIELD_MARKER +
				FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		ordered.verify(receiver).startEntity(ENTITY_028A);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test(expected=MissingIdException.class)
	public void shouldFailIfIdIsMissingByDefault() {
		picaDecoder.process(
				FIELD_001AT_0_TEST + FIELD_MARKER);
	}

	@Test
	public void shouldIgnoreMissingIdIfConfigured() {
		picaDecoder.setIgnoreMissingIdn(true);

		picaDecoder.process(
				FIELD_001AT_0_TEST + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		verify001At0Test(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldNotNormalizeUTF8ByDefault() {
		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_021A_A_UEBER + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		verify021AAUeber(ordered, COMPOSED_UTF8);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldNormalizeUTF8IfConfigured() {
		picaDecoder.setNormalizeUTF8(true);

		picaDecoder.process(
				FIELD_003AT_0_ID + FIELD_MARKER +
				FIELD_021A_A_UEBER + FIELD_MARKER);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify003At0ID(ordered);
		verify021AAUeber(ordered, STANDARD_UTF8);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldTrimWhitespaceInFieldNamesByDefault() {
		picaDecoder.process(
				"  fieldname  " + SUBFIELD_MARKER + "0subfield" +
						FIELD_MARKER + FIELD_003AT_0_ID);

		verify(receiver).startEntity("fieldname");
	}

	@Test
	public void shouldNotTrimWhitespaceInFieldNamesIfConfigured() {
		picaDecoder.setTrimFieldNames(false);

		picaDecoder.process(
				"  fieldname  " + SUBFIELD_MARKER + "0subfield" +
				FIELD_MARKER + FIELD_003AT_0_ID);

		verify(receiver).startEntity("  fieldname  ");
	}

	private void verify003At0ID(final InOrder ordered) {
		ordered.verify(receiver).startEntity("003@");
		ordered.verify(receiver).literal("0", RECORD_ID);
		ordered.verify(receiver).endEntity();
	}

	private void verify001At0Test(final InOrder ordered) {
		ordered.verify(receiver).startEntity("001@");
		ordered.verify(receiver).literal("0", "test");
		ordered.verify(receiver).endEntity();
	}

	private void verify021AAUeber(final InOrder ordered, final String value) {
		ordered.verify(receiver).startEntity("021A");
		ordered.verify(receiver).literal("a", value);
		ordered.verify(receiver).endEntity();
	}

}
