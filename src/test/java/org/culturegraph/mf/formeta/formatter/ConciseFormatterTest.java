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
 * Tests for {@link ConciseFormatter}.
 *
 * @author Christoph Böhme
 *
 */
public final class ConciseFormatterTest {

	private static final String CONCISE_RECORD =
			"1{lit1:value 1,' ent1'{lit2:value \\{x\\},lit\\\\3:'value 2 '}lit4:value \\'3\\'}";

	private static final String INNER_RECORD =
			"inner{ lit1: value 1, ent1{ lit2: 'hello worlds\\'s end!' } }";

	private static final String OUTER_RECORD =
			"outer{" +
			"nested:inner\\{ lit1\\: value 1\\, ent1\\{ lit2\\: \\'hello worlds\\\\\\'s end!\\' \\} \\}," +
			"note:nested records" +
			"}";

	private ConciseFormatter conciseFormatter;

	@Before
	public void setup() {
		conciseFormatter = new ConciseFormatter();
	}

	@Test
	public void testShouldBuildRecordRepresentation() {
		conciseFormatter.startGroup("1");
		conciseFormatter.literal("lit1", "value 1");
		conciseFormatter.startGroup(" ent1");
		conciseFormatter.literal("lit2", "value {x}");
		conciseFormatter.literal("lit\\3", "value 2 ");
		conciseFormatter.endGroup();
		conciseFormatter.literal("lit4", "value '3'");
		conciseFormatter.endGroup();

		assertEquals(CONCISE_RECORD, conciseFormatter.toString());
	}

	@Test
	public void testShouldCorrectlyEscapeNestedRecords() {
		conciseFormatter.startGroup("outer");
		conciseFormatter.literal("nested", INNER_RECORD);
		conciseFormatter.literal("note", "nested records");
		conciseFormatter.endGroup();

		assertEquals(OUTER_RECORD, conciseFormatter.toString());
	}

}
