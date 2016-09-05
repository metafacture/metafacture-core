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
package org.culturegraph.mf.morph.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.types.MultiHashMap;
import org.culturegraph.mf.types.MultiMap;
import org.junit.Test;


/**
 * tests {@link Lookup}
 *
 * @author Markus Michael Geipel
 */

public final class LookupTest {

	private static final String MAP_NAME = "Authors";
	private static final String MAP_NAME_WRONG = "Directors";
	private static final String KEY = "Franz";
	private static final String KEY_WRONG = "Josef";
	private static final String VALUE = "Kafka";

	@Test
	public void testLookup() {
		final Lookup lookup = new Lookup();
		final MultiMap multiMapProvider = new MultiHashMap();
		final Map<String, String> map = new HashMap<String, String>();
		map.put(KEY, VALUE);

		multiMapProvider.putMap(MAP_NAME, map);

		lookup.setMultiMap(multiMapProvider);
		lookup.setIn(MAP_NAME_WRONG);
		assertNull(lookup.process(KEY));
		assertNull(lookup.process(KEY_WRONG));

		lookup.setIn(MAP_NAME);
		assertEquals(VALUE, lookup.process(KEY));
		assertNull(lookup.process(KEY_WRONG));

		map.put(MultiMap.DEFAULT_MAP_KEY, VALUE);
		assertEquals(VALUE, lookup.process(KEY_WRONG));
	}

}
