/*
 * Copyright 2014 Deutsche Nationalbibliothek
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
import static org.mockito.Mockito.times;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link PicaMultiscriptRemodeler}.
 *
 * @author Christoph Böhme
 *
 */
public class PicaMultiscriptRemodelerTest {

	private static final String RECORD_ID = "1234";
	private static final String FIELD_003AT = "003@";
	private static final String FIELD_033A = "033A";
	private static final String FIELD_021A = "021A";
	private static final String FIELD_021C = "021C";
	private static final String SCRIPT_LATIN = "Latn";
	private static final String SCRIPT_GREEK = "Grek";
	private static final String SCRIPT_ARABIC = "Arab";
	private static final String SCRIPT_HEBREW = "Hebr";

	private static final String VALUE_1 = "Subfield 1";
	private static final String VALUE_2 = "Subfield 2";
	private static final String VALUE_3 = "Subfield 3";
	private static final String VALUE_1_GREEK = "ĸµ 1";
	private static final String VALUE_2_GREEK = "ĸµ 2";
	private static final String VALUE_1_ARABIC = "Subfield/Arabic 1";
	private static final String VALUE_1_HEBREW = "Subfield/Hebrew 1";

	private PicaMultiscriptRemodeler remodeler;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		remodeler = new PicaMultiscriptRemodeler();
		remodeler.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		remodeler.closeStream();
	}

	@Test
	public void shouldSimplyPassThroughNonMultiscriptFields() {
		remodeler.startRecord(RECORD_ID);
		remodeler.startEntity(FIELD_003AT);
		remodeler.literal("0", RECORD_ID);
		remodeler.endEntity();
		remodeler.startEntity(FIELD_033A);
		remodeler.literal("p", VALUE_1);
		remodeler.literal("p", VALUE_2);
		remodeler.literal("n", VALUE_3);
		remodeler.endEntity();
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(FIELD_003AT);
		ordered.verify(receiver).literal("0", RECORD_ID);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity(FIELD_033A);
		ordered.verify(receiver).literal("p", VALUE_1);
		ordered.verify(receiver).literal("p", VALUE_2);
		ordered.verify(receiver).literal("n", VALUE_3);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldRemodelMultscriptField() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_GREEK, VALUE_1_GREEK);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_LATIN,
				VALUE_1, SCRIPT_GREEK, VALUE_1_GREEK);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldRemodelInterleafedMultscriptFields() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021C, "01", SCRIPT_LATIN, VALUE_1);
		emitMultscriptField(FIELD_021C, "02", SCRIPT_LATIN, VALUE_2);
		emitMultscriptField(FIELD_021C, "01", SCRIPT_GREEK, VALUE_1_GREEK);
		emitMultscriptField(FIELD_021C, "02", SCRIPT_GREEK, VALUE_2_GREEK);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyMultiscriptField(ordered, FIELD_021C, "01", SCRIPT_LATIN,
				VALUE_1, SCRIPT_GREEK, VALUE_1_GREEK);
		verifyMultiscriptField(ordered, FIELD_021C, "02", SCRIPT_LATIN,
				VALUE_2, SCRIPT_GREEK, VALUE_2_GREEK);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldPassThroughSingleMultiscriptField() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySingleMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_LATIN,
				VALUE_1);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldPassThroughSingleMultiscriptFieldFollowedByCompleteMultiscriptFieldWithTheSameName() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021C, "01", SCRIPT_LATIN, VALUE_1);
		emitMultscriptField(FIELD_021C, "02", SCRIPT_LATIN, VALUE_2);
		emitMultscriptField(FIELD_021C, "02", SCRIPT_GREEK, VALUE_2_GREEK);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySingleMultiscriptField(ordered, FIELD_021C, "01", SCRIPT_LATIN,
				VALUE_1);
		verifyMultiscriptField(ordered, FIELD_021C, "02", SCRIPT_LATIN,
				VALUE_2, SCRIPT_GREEK, VALUE_2_GREEK);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldPassThroughSingleMultiscriptFieldFollowedByCompleteMultiscriptFieldWithDifferentName() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		emitMultscriptField(FIELD_021C, "01", SCRIPT_LATIN, VALUE_2);
		emitMultscriptField(FIELD_021C, "01", SCRIPT_GREEK, VALUE_2_GREEK);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySingleMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_LATIN,
				VALUE_1);
		verifyMultiscriptField(ordered, FIELD_021C, "01", SCRIPT_LATIN,
				VALUE_2, SCRIPT_GREEK, VALUE_2_GREEK);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldPassThroughSingleMultiscriptFieldFollowedByNonMultiscriptField() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		remodeler.startEntity(FIELD_033A);
		remodeler.literal("n", VALUE_2);
		remodeler.endEntity();
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySingleMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_LATIN,
				VALUE_1);
		ordered.verify(receiver).startEntity(FIELD_033A);
		ordered.verify(receiver).literal("n", VALUE_2);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldPassThroughIncompleteMultiscriptFields() {
		remodeler.startRecord(RECORD_ID);
		remodeler.startEntity(FIELD_021C);
		remodeler.literal("T", "01");
		remodeler.literal("a", VALUE_1);
		remodeler.endEntity();
		remodeler.startEntity(FIELD_021C);
		remodeler.literal("U", SCRIPT_GREEK);
		remodeler.literal("a", VALUE_2_GREEK);
		remodeler.endEntity();
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(FIELD_021C);
		ordered.verify(receiver).literal("T", "01");
		ordered.verify(receiver).literal("a", VALUE_1);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity(FIELD_021C);
		ordered.verify(receiver).literal("U", SCRIPT_GREEK);
		ordered.verify(receiver).literal("a", VALUE_2_GREEK);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldLabelArabicAsNonLatinRightToLeftScript() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_ARABIC, VALUE_1_ARABIC);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_LATIN,
				VALUE_1, SCRIPT_ARABIC, VALUE_1_ARABIC);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldLabelHebrewAsNonLatinRightToLeftScript() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_HEBREW, VALUE_1_HEBREW);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_LATIN,
				VALUE_1, SCRIPT_HEBREW, VALUE_1_HEBREW);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldClearStateOnResetStream() {
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_LATIN, VALUE_1);
		remodeler.resetStream();
		remodeler.startRecord(RECORD_ID);
		emitMultscriptField(FIELD_021A, "01", SCRIPT_GREEK, VALUE_1_GREEK);
		remodeler.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).resetStream();
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySingleMultiscriptField(ordered, FIELD_021A, "01", SCRIPT_GREEK, VALUE_1_GREEK);
		ordered.verify(receiver).endRecord();
	}

	private void emitMultscriptField(final String field,
			final String groupNumber, final String script, final String value) {

		remodeler.startEntity(field);
		remodeler.literal("T", groupNumber);
		remodeler.literal("U", script);
		remodeler.literal("a", value);
		remodeler.endEntity();
	}

	private void verifyMultiscriptField(final InOrder ordered,
			final String field, final String groupNumber, final String script1,
			final String value1, final String script2, final String value2) {

		ordered.verify(receiver).startEntity(field);
		ordered.verify(receiver).startEntity(mapScriptToEntityName(script1));
		ordered.verify(receiver).literal("T", groupNumber);
		ordered.verify(receiver).literal("U", script1);
		ordered.verify(receiver).literal("a", value1);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity(mapScriptToEntityName(script2));
		ordered.verify(receiver).literal("T", groupNumber);
		ordered.verify(receiver).literal("U", script2);
		ordered.verify(receiver).literal("a", value2);
		ordered.verify(receiver, times(2)).endEntity();
	}

	private void verifySingleMultiscriptField(final InOrder ordered,
			final String field, final String groupNumber, final String script,
			final String value) {

		ordered.verify(receiver).startEntity(field);
		ordered.verify(receiver).literal("T", groupNumber);
		ordered.verify(receiver).literal("U", script);
		ordered.verify(receiver).literal("a", value);
		ordered.verify(receiver).endEntity();
	}

	private String mapScriptToEntityName(final String script) {
		if (SCRIPT_LATIN.equals(script)) {
			return PicaMultiscriptRemodeler.ENTITY_NAME_FOR_LATIN;
		} else if (SCRIPT_ARABIC.equals(script)
				|| SCRIPT_HEBREW.equals(script)) {
			return PicaMultiscriptRemodeler.ENTITY_NAME_FOR_NON_LATIN_RL;
		}
		return PicaMultiscriptRemodeler.ENTITY_NAME_FOR_NON_LATIN_LR;
	}

}
