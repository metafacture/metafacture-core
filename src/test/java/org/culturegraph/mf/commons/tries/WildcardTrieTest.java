/*
 * Copyright 2016 Christoph Böhme
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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link WildcardTrie}
 *
 * @author Markus Michael Geipel
 *
 */
public final class WildcardTrieTest {

	private static final String ABC = "abc";
	private static final String CCB = "ccb";
	private static final String AAQBB = "aa?bb";
	private static final String A_STAR_B = "a*b";
	private static final String A_STAR_BC = "a*bc";
	private static final String AA_STAR_BB = "aa*bb";
	private static final String STAR_B = "*b";
	private static final String A_STAR = "a*";
	private static final String AACBB = "aacbb";
	private static final String AABB = "aabb";
	private static final String AB = "ab";
	private static final String NOT_FOUND_BY = " not found by ";
	private static final String FOUND_BY = " found by ";

	private WildcardTrie<String> trie;

	@Before
	public void createSystemUnderTest() {
		trie = new WildcardTrie<>();
	}

	@Test
	public void testWithQWildcard() {
		assertTrue(trie.get("").isEmpty());
		assertTrue(trie.get("x").isEmpty());

		trie.put(ABC, ABC);
		assertTrue(trie.get(ABC).contains(ABC));

		trie.put(AAQBB, AAQBB);
		assertTrue(trie.get(AACBB).contains(AAQBB));
		assertTrue(trie.get(AABB).isEmpty());

		trie.put(AABB, AABB);
		assertTrue(trie.get(AABB).contains(AABB));
		assertTrue(trie.get(AABB).size() == 1);

		trie.put(AACBB, AACBB);
		assertTrue(trie.get(AACBB).contains(AACBB));
		assertTrue(trie.get(AACBB).contains(AAQBB));
	}

	@Test
	public void testWithStarWildcard() {
		trie.put(A_STAR_B, A_STAR_B);
		assertTrue(AACBB + NOT_FOUND_BY + A_STAR_B, trie.get(AACBB).contains(A_STAR_B));
		assertTrue(AABB + NOT_FOUND_BY + A_STAR_B, trie.get(AABB).contains(A_STAR_B));
		assertTrue(AB + NOT_FOUND_BY + A_STAR_B, trie.get(AB).contains(A_STAR_B));
		assertTrue(ABC + FOUND_BY + A_STAR_B, trie.get(ABC).isEmpty());
		assertTrue(CCB + FOUND_BY + A_STAR_B, trie.get(CCB).isEmpty());

		trie.put(AABB, AABB);
		assertTrue(trie.get(AABB).contains(AABB));
		assertEquals(2, trie.get(AABB).size());

		trie.put(AACBB, AACBB);
		assertTrue(trie.get(AACBB).contains(AACBB));
		assertTrue(trie.get(AACBB).contains(A_STAR_B));
	}

	@Test
	public void testWithTrailingStarWildcard() {
		trie.put(A_STAR, A_STAR);
		assertTrue(AACBB + NOT_FOUND_BY + A_STAR, trie.get(AACBB).contains(A_STAR));
		assertTrue(AABB + NOT_FOUND_BY + A_STAR, trie.get(AABB).contains(A_STAR));
		assertTrue(AB + NOT_FOUND_BY + A_STAR, trie.get(AB).contains(A_STAR));
		assertTrue(ABC + NOT_FOUND_BY + A_STAR_B, trie.get(ABC).contains(A_STAR));
		assertTrue(CCB + FOUND_BY + A_STAR_B, trie.get(CCB).isEmpty());

		trie.put(AABB, AABB);
		assertTrue(trie.get(AABB).contains(AABB));
		assertEquals(2, trie.get(AABB).size());

		trie.put(AACBB, AACBB);
		assertTrue(trie.get(AACBB).contains(AACBB));
		assertTrue(trie.get(AACBB).contains(A_STAR));
	}

	@Test
	public void testWithInitialStarWildcard() {
		trie.put(STAR_B, STAR_B);
		assertTrue(AACBB + NOT_FOUND_BY + STAR_B, trie.get(AACBB).contains(STAR_B));
		assertTrue(AABB + NOT_FOUND_BY + STAR_B, trie.get(AABB).contains(STAR_B));

		assertTrue(ABC + FOUND_BY + A_STAR_B, trie.get(ABC).isEmpty());
		assertTrue(CCB + NOT_FOUND_BY + A_STAR_B, trie.get(CCB).contains(STAR_B));

		trie.put(AABB, AABB);
		assertTrue(trie.get(AABB).contains(AABB));
		assertEquals(2, trie.get(AABB).size());

		trie.put(AACBB, AACBB);
		assertTrue(trie.get(AACBB).contains(AACBB));
		assertTrue(trie.get(AACBB).contains(STAR_B));
	}

	@Test
	public void testWithMultipleStarWildcards() {
		trie.put(STAR_B, STAR_B);
		trie.put(A_STAR, A_STAR);
		trie.put(A_STAR_B, A_STAR_B);

		assertEquals(3, trie.get(AACBB).size());

		trie.put(AA_STAR_BB, AA_STAR_BB);
		assertEquals(4, trie.get(AACBB).size());

		assertEquals(3, trie.get(AB).size());
		assertEquals(1, trie.get(CCB).size());

		assertEquals(3, trie.get("acb").size());
	}

	@Test
	public void testOverlapWithWildcard() {
		trie.put(ABC, ABC);
		trie.put(A_STAR_BC, A_STAR_BC);

		assertEquals(2, trie.get(ABC).size());
		assertEquals(1, trie.get("abbc").size());
	}

	@Test
	public void testEmptyKey() {
		trie.put("", ABC);
		assertEquals(1, trie.get("").size());
	}

	@Test
	public void testWithOrAndWildcard() {
		final String key = ABC + WildcardTrie.OR_STRING + CCB;
		trie.put(key, "");
		assertTrue(ABC + NOT_FOUND_BY + key, trie.get(ABC).contains(""));
		assertTrue(CCB + NOT_FOUND_BY + key, trie.get(CCB).contains(""));

		assertTrue(AABB + FOUND_BY + key, trie.get(AABB).isEmpty());
		assertTrue(AB + FOUND_BY + key, trie.get(AB).isEmpty());
	}

}
