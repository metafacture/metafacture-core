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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import java.nio.charset.Charset;

import org.culturegraph.mf.framework.FormatException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link Record}.
 *
 * @author Christoph Böhme
 */
public class RecordTest {

	@Mock
	private FieldHandler fieldHandler;

	private Record record;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void constructor_shouldCreateRecordInstance() {
		final byte[] data = asBytes("00026SIMPL1200025SYS345R\u001e\u001d");

		record = new Record(data);

		assertNotNull(record);
	}

	@Test(expected = FormatException.class)
	public void constructor_shouldThrowFormatExceptionIfSizeOfRecordDataIsLessThanMinRecordLength() {
		final byte[] data = asBytes("00005");

		record = new Record(data);  // Exception expected
	}

	@Test
	public void getIdentifier_shouldReturnRecordIdentifier() {
		final byte[] data = asBytes("00034SIMPL0000030SYS110R" + "00120\u001e" +
				"ID\u001e\u001d");

		record = new Record(data);

		assertEquals("ID", record.getRecordId());
	}

	@Test
	public void getIdentifier_shouldReturnNullIfRecordHasNoIdentifier() {
		final byte[] data = asBytes("00034SIMPL0000030SYS110R" + "00220\u001e" +
				"XY\u001e\u001d");

		record = new Record(data);

		assertNull(record.getRecordId());
	}

	@Test
	public void processFields_shouldCallHandlerReferenceFieldForReferenceFields() {
		final byte[] data = asBytes("00042SIMPL0000035SYS110R" + "00120" +
				"00223\u001e" + "ID\u001e" + "XY\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).referenceField(asChars("001"), asChars(""), "ID");
		ordered.verify(fieldHandler).referenceField(asChars("002"), asChars(""), "XY");
	}

	@Test
	public void processFields_shouldHandleDataFieldsInRecordWithoutIndicatorsAndIdentifiers() {
		final byte[] data = asBytes("00044SIMPL0000037SYS111R" + "01120X" +
				"01223Y\u001e" + "F1\u001e" + "F2\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars("X"),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars(""), "F1");
		ordered.verify(fieldHandler).endDataField();
		ordered.verify(fieldHandler).startDataField(asChars("012"), asChars("Y"),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars(""), "F2");
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldHandleDataFieldsInRecordWithIndicatorsButWithoutIdentifiers() {
		final byte[] data = asBytes("00044SIMPL1000035SYS110R" + "01130" +
				"01234\u001e" + "XF1\u001e" + "YF2\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars(""),
				asChars("X"));
		ordered.verify(fieldHandler).data(asChars(""), "F1");
		ordered.verify(fieldHandler).endDataField();
		ordered.verify(fieldHandler).startDataField(asChars("012"), asChars(""),
				asChars("Y"));
		ordered.verify(fieldHandler).data(asChars(""), "F2");
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldHandleDataFieldsInRecordWithoutIndicatorsButWithTwoOctetIdentifiers() {
		final byte[] data = asBytes("00050SIMPL0200035SYS110R" + "01150" +
				"01295\u001e" + "\u001fXF1\u001e" + "\u001fYF2\u001fZF3\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars(""),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars("X"), "F1");
		ordered.verify(fieldHandler).endDataField();
		ordered.verify(fieldHandler).startDataField(asChars("012"), asChars(""),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars("Y"), "F2");
		ordered.verify(fieldHandler).data(asChars("Z"), "F3");
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldHandleDataFieldsInRecordWithoutIndicatorsButWithOneOctetIdentifiers() {
		final byte[] data = asBytes("00038SIMPL0100030SYS110R" + "01170\u001e" +
				"\u001fF1\u001fF2\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars(""),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars(""), "F1");
		ordered.verify(fieldHandler).data(asChars(""), "F2");
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldHandleEmptyDataFieldInRecordWithoutIndicatorsButWithIdentifiers() {
		final byte[] data = asBytes("00032SIMPL0200030SYS110R" + "01110\u001e" +
				"\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars(""),
				asChars(""));
		ordered.verify(fieldHandler, never()).data(any(char[].class), any(String.class));
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldHandleDataFieldsWithoutContentInRecordWithoutIndicatorsButWithIdentifiers() {
		final byte[] data = asBytes("00036SIMPL0200030SYS110R" + "01150\u001e" +
				"\u001fX\u001fY\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars(""),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars("X"), "");
		ordered.verify(fieldHandler).data(asChars("Y"), "");
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldHandleDataFieldsInRecordWithIndicatorsAndOctetIdentifiers() {
		final byte[] data = asBytes("00051SIMPL2200035SYS110R" + "01160" +
				"01296\u001e" + "AB\u001fX1\u001e" + "CD\u001fY2\u001fZ3\u001e" +
				"\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars(""),
				asChars("AB"));
		ordered.verify(fieldHandler).data(asChars("X"), "1");
		ordered.verify(fieldHandler).endDataField();
		ordered.verify(fieldHandler).startDataField(asChars("012"), asChars(""),
				asChars("CD"));
		ordered.verify(fieldHandler).data(asChars("Y"), "2");
		ordered.verify(fieldHandler).data(asChars("Z"), "3");
		ordered.verify(fieldHandler).endDataField();
	}

	@Test
	public void processFields_shouldConcatenateContinuedReferenceFieldsAndReportAllImplDefinedParts() {
		final byte[] data = asBytes("00062SIMPL0000046SYS121R" + "001000A" +
				"001309B" + "002312C\u001e" + "abcdefghijk\u001e" + "XY\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).referenceField(asChars("001"), asChars("A"),
				"abcdefghijk");
		ordered.verify(fieldHandler).additionalImplDefinedPart(asChars("B"));
		ordered.verify(fieldHandler).referenceField(asChars("002"), asChars("C"), "XY");
	}

	@Test
	public void processFields_shouldConcatenateContinuedFieldsAndReportAllImplDefinedParts() {
		final byte[] data = asBytes("00062SIMPL0000046SYS121R" + "011000A" +
				"011309B" + "012312C\u001e" + "abcdefghijk\u001e" + "XY\u001e\u001d");
		record = new Record(data);

		record.processFields(fieldHandler);

		final InOrder ordered = inOrder(fieldHandler);
		ordered.verify(fieldHandler).startDataField(asChars("011"), asChars("A"),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars(""), "abcdefghijk");
		ordered.verify(fieldHandler).endDataField();
		ordered.verify(fieldHandler).additionalImplDefinedPart(asChars("B"));
		ordered.verify(fieldHandler).startDataField(asChars("012"), asChars("C"),
				asChars(""));
		ordered.verify(fieldHandler).data(asChars(""), "XY");
		ordered.verify(fieldHandler).endDataField();
	}

	private static byte[] asBytes(final String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}

	private static char[] asChars(final String str) {
		return str.toCharArray();
	}

}
