/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.formeta.parser;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.formeta.FormetaDecoder;
import org.culturegraph.mf.framework.FormatException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for {@link FormetaDecoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class FormetaParserTest {

	private static final String CONCISE_RECORD =
			"1{lit1:value 1,' ent1'{lit2:value \\{x\\},lit\\\\3:'value 2 '}lit4:value \\'3\\'}";

	private static final String VERBOSE_RECORD =
			"1{ lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' }";

	private static final String MULTILINE_RECORD =
			"1{\n" +
			"  lit1: 'value 1',\n" +
			"  ' ent1'{\n" +
			"    lit2: 'value {x}',\n" +
			"    'lit\\\\3': 'value 2 '\n" +
			"  },\n" +
			"  lit4: 'value \\'3\\''\n" +
			"}";

	private static final String BROKEN_RECORD =
			"1 { lit1: 'value 1',";

	private static final String INNER_RECORD =
			"inner{ lit1: value 1, ent1{ lit2: 'hello worlds\\'s end!' } }";

	private static final String OUTER_RECORD =
			"outer{" +
			"nested:inner\\{ lit1\\: value 1\\, ent1\\{ lit2\\: \\'hello worlds\\\\\\'s end!\\' \\} \\}," +
			"note:I can has nezted records" +
			"}";

	private static final String PARTIAL_RECORD =
			 "lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' ";

	private static final String BROKEN_PARTIAL_RECORD =
			 "lit1: 'value 1', ' ent1'{ lit2: 'value {x}'";

	private FormetaParser parser;

	@Mock
	private Emitter emitter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		parser = new FormetaParser();
		parser.setEmitter(emitter);
	}

	@Test
	public void testShouldParseConciselyFormattedRecords() {
		parser.parse(CONCISE_RECORD);

		final InOrder ordered = inOrder(emitter);
		verifyRecord(ordered);
	}

	@Test
	public void testShouldParseVerboselyFormattedRecords() {
		parser.parse(VERBOSE_RECORD);

		final InOrder ordered = inOrder(emitter);
		verifyRecord(ordered);
	}

	@Test
	public void testShouldParseMultilineFormattedRecords() {
		parser.parse(MULTILINE_RECORD);

		final InOrder ordered = inOrder(emitter);
		verifyRecord(ordered);
	}

	@Test
	public void testShouldIgnoreItemSeparatorAfterRecord() {
		parser.parse(CONCISE_RECORD + ", ");

		final InOrder ordered = inOrder(emitter);
		verifyRecord(ordered);
	}

	@Test(expected=FormatException.class)
	public void testShouldFailOnDoubleCloseRecord() {
		parser.parse("1 { lit: val }}");
	}

	@Test(expected=FormatException.class)
	public void testShouldFailOnGarbageAfterRecord() {
		parser.parse(CONCISE_RECORD + "Garbage");
	}

	@Test
	public void testShouldParseInputsContainingMoreThanOneRecord() {
		parser.parse(CONCISE_RECORD + CONCISE_RECORD);

		final InOrder ordered = inOrder(emitter);
		verifyRecord(ordered);
		verifyRecord(ordered);
	}

	@Test(expected=FormatException.class)
	public void testShouldFailOnIncompleteRecords() {
		parser.parse(BROKEN_RECORD);
	}

	@Test
	public void testShouldRecoverAfterIncompleteRecord() {
		// Try processing an incomplete record:
		try {
			parser.parse(BROKEN_RECORD);
		} catch (FormatException e) {
			// The decoder should recover automatically
		}

		// Test whether another record can be processed
		// afterwards:
		parser.parse(CONCISE_RECORD);

		final InOrder ordered = inOrder(emitter);
		verifyRecord(ordered);
	}

	@Test
	public void testShouldParseInputContainingNestedRecords() {
		parser.parse(OUTER_RECORD);

		final InOrder ordered = inOrder(emitter);
		ordered.verify(emitter).startGroup("outer", 0);
		ordered.verify(emitter).literal("nested", INNER_RECORD, 1);
		ordered.verify(emitter).literal("note", "I can has nezted records", 1);
		ordered.verify(emitter).endGroup(0);
	}

	@Test
	public void testPartialRecord() {
		parser.parse(PARTIAL_RECORD);

		final InOrder ordered = inOrder(emitter);
		verifyRecordContents(ordered, 0);
	}

	@Test(expected=FormatException.class)
	public void testIncompletePartialRecord() {
		parser.parse(BROKEN_PARTIAL_RECORD);
	}

	private void verifyRecord(final InOrder ordered) {
		ordered.verify(emitter).startGroup("1", 0);
		verifyRecordContents(ordered, 1);
		ordered.verify(emitter).endGroup(0);
	}

	private void verifyRecordContents(final InOrder ordered, final int nestingLevel) {
		ordered.verify(emitter).literal("lit1", "value 1", nestingLevel);
		ordered.verify(emitter).startGroup(" ent1", nestingLevel);
		ordered.verify(emitter).literal("lit2", "value {x}", nestingLevel + 1);
		ordered.verify(emitter).literal("lit\\3", "value 2 ", nestingLevel + 1);
		ordered.verify(emitter).endGroup(nestingLevel);
		ordered.verify(emitter).literal("lit4", "value '3'", nestingLevel);
	}

}
