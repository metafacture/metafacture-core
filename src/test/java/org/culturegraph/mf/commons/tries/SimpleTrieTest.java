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
package org.culturegraph.mf.commons.tries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Test;


/**
 * tests {@link SimpleTrie}
 *
 * @author Markus Michael Geipel
 *
 */
public final class SimpleTrieTest {

	private static final String KEY = "key";
	private static final String VALUE = "value";

	@Test
	public void testAdd() {
		final SimpleTrie<String> trie = new SimpleTrie<String>();
		assertNull(trie.get(KEY));
		trie.put(KEY, VALUE);
		assertEquals(VALUE, trie.get(KEY));
	}

	@Test
	public void testMultiAdd(){
		final SimpleTrie<String> trie = new SimpleTrie<String>();

		final String[] megacities =  { "Brisbane", "Sydney", "Melbourne", "Adelaide", "Perth", "Berlin", "Berlin Center", "Bremen", "Petersburg"};

		for (int i = 0; i < megacities.length; ++i) {
			final String city = megacities[i];
			trie.put(city, city.toUpperCase(Locale.US));
		}

		for (int i = 0; i < megacities.length; ++i) {
			final String city = megacities[i];
			assertEquals(city.toUpperCase(Locale.US), trie.get(city));
		}
	}

}
