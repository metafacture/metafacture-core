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
package org.culturegraph.mf.formeta.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MultilineFormatter}.
 *
 * @author Christoph Böhme
 *
 */
public final class MultilineFormatterTest {

	private static final String MULTILINE_RECORD =
			"'1' {\n" +
			"\t'lit1': 'value 1',\n" +
			"\t' ent1' {\n" +
			"\t\t'lit2': 'value {x}',\n" +
			"\t\t'lit\\\\3': 'value 2 '\n" +
			"\t},\n" +
			"\t'lit4': 'value \\'3\\''\n" +
			"}";

	private static final String INNER_RECORD =
			"inner{ lit1: value 1, ent1{ lit2: 'hello worlds\\'s end!' } }";

	private static final String OUTER_RECORD =
			"'outer' {\n" +
			"\t'nested': 'inner{ lit1: value 1, ent1{ lit2: \\'hello worlds\\\\\\'s end!\\' } }',\n" +
			"\t'note': 'nested records'\n" +
			"}";

	private MultilineFormatter multilineFormatter;

	@Before
	public void setup() {
		multilineFormatter = new MultilineFormatter();
	}

	@Test
	public void testShouldBuildRecordRepresentation() {
		multilineFormatter.startGroup("1");
		multilineFormatter.literal("lit1", "value 1");
		multilineFormatter.startGroup(" ent1");
		multilineFormatter.literal("lit2", "value {x}");
		multilineFormatter.literal("lit\\3", "value 2 ");
		multilineFormatter.endGroup();
		multilineFormatter.literal("lit4", "value '3'");
		multilineFormatter.endGroup();

		assertEquals(MULTILINE_RECORD, multilineFormatter.toString());
	}

	@Test
	public void testShouldCorrectlyEscapeNestedRecords() {
		multilineFormatter.startGroup("outer");
		multilineFormatter.literal("nested", INNER_RECORD);
		multilineFormatter.literal("note", "nested records");
		multilineFormatter.endGroup();

		assertEquals(OUTER_RECORD, multilineFormatter.toString());
	}

}
