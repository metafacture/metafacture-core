/*
 *  Copyright 2013, 2014 Pascal Christoph, hbz
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
package org.culturegraph.mf.commons.tries;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * tests {@link SimpleRegexTrie}
 *
 * @author Pascal Christoph
 *
 */
public final class SimpleRegexTrieTest {
	private static final String SCC = "aacbb|a[ab]bb";
	private static final String AACBB = "aacbb";

	@Test
	public void testWithSimpleCharacterClass() {
		final SimpleRegexTrie<String> trie = new SimpleRegexTrie<String>();
		trie.put(SCC, SCC);
		assertTrue(AACBB, trie.get(AACBB).size() == 1);
	}
}
