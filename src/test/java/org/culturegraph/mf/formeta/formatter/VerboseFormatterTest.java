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
 * Tests for {@link VerboseFormatter}.
 *
 * @author Christoph Böhme
 *
 */
public final class VerboseFormatterTest {

	private static final String VERBOSE_RECORD =
			"1{ lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' }";

	private static final String INNER_RECORD =
			"inner{ lit1: value 1, ent1{ lit2: 'hello worlds\\'s end!' } }";

	private static final String OUTER_RECORD =
			"outer{ " +
			"nested: 'inner{ lit1: value 1, ent1{ lit2: \\'hello worlds\\\\\\'s end!\\' } }', " +
			"note: 'nested records'" +
			" }";

	private VerboseFormatter verboseFormatter;

	@Before
	public void setup() {
		verboseFormatter = new VerboseFormatter();
	}

	@Test
	public void testShouldBuildRecordRepresentation() {
		verboseFormatter.startGroup("1");
		verboseFormatter.literal("lit1", "value 1");
		verboseFormatter.startGroup(" ent1");
		verboseFormatter.literal("lit2", "value {x}");
		verboseFormatter.literal("lit\\3", "value 2 ");
		verboseFormatter.endGroup();
		verboseFormatter.literal("lit4", "value '3'");
		verboseFormatter.endGroup();

		assertEquals(VERBOSE_RECORD, verboseFormatter.toString());
	}

	@Test
	public void testShouldCorrectlyEscapeNestedRecords() {
		verboseFormatter.startGroup("outer");
		verboseFormatter.literal("nested", INNER_RECORD);
		verboseFormatter.literal("note", "nested records");
		verboseFormatter.endGroup();

		assertEquals(OUTER_RECORD, verboseFormatter.toString());
	}

}
