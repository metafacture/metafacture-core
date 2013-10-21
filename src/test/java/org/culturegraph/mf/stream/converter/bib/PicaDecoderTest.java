/*
 *  Copyright 2013 Christoph Böhme
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
package org.culturegraph.mf.stream.converter.bib;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link PicaDecoder}.
 * 
 * @author Christoph Böhme
 *
 */
public final class PicaDecoderTest {
	
	private static final String RECORD_ID = "2809";
	private static final String COMPOSED_UTF8 = "Über";  // 'Ü' constructed from U and diacritics
	private static final String STANDARD_UTF8 = "Über";  // 'Ü' is a single character
	private static final String SUBFIELD = "\u001f";
	private static final String FIELD_END = "\u001e";
	
	private static final String FIELD_001AT = "001@ " + SUBFIELD + "0test" + FIELD_END;
	private static final String FIELD_003AT = "003@ " + SUBFIELD + "0" + RECORD_ID + FIELD_END;
	private static final String FIELD_021A = "021A " + SUBFIELD + "a" + COMPOSED_UTF8 + FIELD_END;
	private static final String FIELD_028A_START = "028A ";
	private static final String SUBFIELD_A = SUBFIELD + "aEco";
	private static final String SUBFIELD_D = SUBFIELD + "dUmberto";
	private static final String EMPTY_SUBFIELD = SUBFIELD;
	private static final String FIELD_028A_END = FIELD_END;
	private static final String EMPTY_FIELD_START = "";
	private static final String SUBFIELD_X = SUBFIELD + "Xyz";
	private static final String EMPTY_FIELD_END = FIELD_END;
	
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
	public void testShouldParseValidPica() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				SUBFIELD_A +
				SUBFIELD_D +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify028AStart(ordered);
		verifySubfieldA(ordered);
		verifySubfieldD(ordered);
		verify028AEnd(ordered);
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldSkipUnnamedFieldsWithNoSubFields() {
		picaDecoder.setSkipEmptyFields(false);
		
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				EMPTY_FIELD_START +
				EMPTY_FIELD_END);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldSkipUnnamedFieldsWithEmptySubFieldsOnly() {
		picaDecoder.setSkipEmptyFields(false);

		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				EMPTY_FIELD_START +
				EMPTY_SUBFIELD +
				EMPTY_FIELD_END);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldNotSkipUnnamedFieldsWithSubFields() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				EMPTY_FIELD_START +
				SUBFIELD_X +
				EMPTY_FIELD_END);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verifySubfieldX(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldSkipEmptySubfields() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				EMPTY_SUBFIELD +
				SUBFIELD_D +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify028AStart(ordered);
		verifySubfieldD(ordered);
		verify028AEnd(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldSkipEmptyFieldsByDefault() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldNotSkipEmptyFieldsIfConfigured() {
		picaDecoder.setSkipEmptyFields(false);
		
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify028AStart(ordered);
		verify028AEnd(ordered);
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldSkipEmptyFieldsWithOnlyEmptySubfieldsByDefault() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				EMPTY_SUBFIELD +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldNotSkipEmptyFieldsWithOnlyEmptySubfieldsIfConfigured() {
		picaDecoder.setSkipEmptyFields(false);
		
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				EMPTY_SUBFIELD +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify028AStart(ordered);
		verify028AEnd(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test(expected=MissingIdException.class)
	public void testShouldFailIfIdIsMissingByDefault() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_028A_START +
				SUBFIELD_A +
				SUBFIELD_D +
				FIELD_028A_END);
	}
	
	@Test
	public void testShouldIgnoreMissingIdIfConfigured() {
		picaDecoder.setIgnoreMissingIdn(true);

		picaDecoder.process(
				FIELD_001AT +
				FIELD_028A_START +
				SUBFIELD_A +
				SUBFIELD_D +
				FIELD_028A_END);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		verify001At(ordered);
		verify028AStart(ordered);
		verifySubfieldA(ordered);
		verifySubfieldD(ordered);
		verify028AEnd(ordered);
		ordered.verify(receiver).endRecord();
	}
	
	@Test(expected=FormatException.class)
	public void testShouldFailIfRecordIsIncompleteByDefault() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				SUBFIELD_A +
				SUBFIELD_D);
	}
	
	@Test
	public void testShouldFixIncompleteRecordIfConfigured() {
		picaDecoder.setFixUnexpectedEOR(true);
		
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_028A_START +
				SUBFIELD_A +
				SUBFIELD_D);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify028AStart(ordered);
		verifySubfieldA(ordered);
		verifySubfieldD(ordered);
		verify028AEnd(ordered);
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldNotNormalizeUTF8ByDefault() {
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_021A);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify021A(ordered, COMPOSED_UTF8);
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldNormalizeUTF8ByDefault() {
		picaDecoder.setNormalizeUTF8(true);
		
		picaDecoder.process(
				FIELD_001AT +
				FIELD_003AT +
				FIELD_021A);
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verify001At(ordered);
		verify003At(ordered);
		verify021A(ordered, STANDARD_UTF8);
		ordered.verify(receiver).endRecord();
	}
	
	private void verify003At(final InOrder ordered) {
		ordered.verify(receiver).startEntity("003@");
		ordered.verify(receiver).literal("0", RECORD_ID);
		ordered.verify(receiver).endEntity();
	}
	
	private void verify001At(final InOrder ordered) {
		ordered.verify(receiver).startEntity("001@");
		ordered.verify(receiver).literal("0", "test");
		ordered.verify(receiver).endEntity();
	}
	
	private void verify028AStart(final InOrder ordered) {
		ordered.verify(receiver).startEntity("028A");
	}

	private void verifySubfieldA(final InOrder ordered) {
		ordered.verify(receiver).literal("a", "Eco");
	}
	
	private void verifySubfieldD(final InOrder ordered) {
		ordered.verify(receiver).literal("d", "Umberto");
	}

	private void verify028AEnd(final InOrder ordered) {
		ordered.verify(receiver).endEntity();
	}
	
	private void verifySubfieldX(final InOrder ordered) {
		ordered.verify(receiver).startEntity("");
		ordered.verify(receiver).literal("X", "yz");
		ordered.verify(receiver).endEntity();
	}
	
	private void verify021A(final InOrder ordered, final String value) {
		ordered.verify(receiver).startEntity("021A");
		ordered.verify(receiver).literal("a", value);
		ordered.verify(receiver).endEntity();
	}
	
}
