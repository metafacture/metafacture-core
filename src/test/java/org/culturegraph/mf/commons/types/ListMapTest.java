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
package org.culturegraph.mf.commons.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * Tests for class {@link ListMap}.
 *
 * @author Markus Michael Geipel
 *
 */
public final class ListMapTest {
	private static final String VALUE1 = "v1";
	private static final String NAME1 = "n1";
	private static final String VALUE2 = "v2";
	private static final String NAME2 = "n2";

	@Test
	public void test() {
		final ListMap<String, String> listMap = new ListMap<String, String>();

		assertNull(listMap.getFirst(NAME1));
		listMap.add(NAME1, VALUE1);
		assertNotNull(listMap.getFirst(NAME1));
		assertEquals(VALUE1, listMap.getFirst(NAME1));

		listMap.add(NAME1, VALUE2);
		assertNotNull(listMap.getFirst(NAME1));
		assertEquals(VALUE1, listMap.getFirst(NAME1));

		assertNotNull(listMap.get(NAME1));
		assertEquals(2, listMap.get(NAME1).size());
		assertTrue(listMap.get(NAME1).contains(VALUE2));

		assertNotNull(listMap.get(NAME2));
		assertEquals(0, listMap.get(NAME2).size());

		listMap.add(NAME2, VALUE2);
		assertNotNull(listMap.getFirst(NAME2));
		listMap.clearKey(NAME2);
		assertNull(listMap.getFirst(NAME2));
		assertNotNull(listMap.getFirst(NAME1));

		listMap.clearAllKeys();
		assertNull(listMap.getFirst(NAME1));
	}
}
