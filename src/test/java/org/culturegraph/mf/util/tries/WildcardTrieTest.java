/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
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
package org.culturegraph.mf.util.tries;

import junit.framework.Assert;

import org.culturegraph.mf.util.tries.WildcardTrie;
import org.junit.Test;


/**
 * tests {@link WildcardTrie}
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

	@Test
	public void testWithQWildcard() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();
		Assert.assertTrue(trie.get("").isEmpty());
		Assert.assertTrue(trie.get("x").isEmpty());

		trie.put(ABC, ABC);
		Assert.assertTrue(trie.get(ABC).contains(ABC));

		trie.put(AAQBB, AAQBB);
		Assert.assertTrue(trie.get(AACBB).contains(AAQBB));
		Assert.assertTrue(trie.get(AABB).isEmpty());

		trie.put(AABB, AABB);
		Assert.assertTrue(trie.get(AABB).contains(AABB));
		Assert.assertTrue(trie.get(AABB).size() == 1);

		trie.put(AACBB, AACBB);
		Assert.assertTrue(trie.get(AACBB).contains(AACBB));
		Assert.assertTrue(trie.get(AACBB).contains(AAQBB));
	}

	@Test
	public void testWithStarWildcard() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();

		trie.put(A_STAR_B, A_STAR_B);
		Assert.assertTrue(AACBB + NOT_FOUND_BY + A_STAR_B, trie.get(AACBB).contains(A_STAR_B));
		Assert.assertTrue(AABB + NOT_FOUND_BY + A_STAR_B, trie.get(AABB).contains(A_STAR_B));
		Assert.assertTrue(AB + NOT_FOUND_BY + A_STAR_B, trie.get(AB).contains(A_STAR_B));
		Assert.assertTrue(ABC + FOUND_BY + A_STAR_B, trie.get(ABC).isEmpty());
		Assert.assertTrue(CCB + FOUND_BY + A_STAR_B, trie.get(CCB).isEmpty());

		trie.put(AABB, AABB);
		Assert.assertTrue(trie.get(AABB).contains(AABB));
		Assert.assertEquals(2, trie.get(AABB).size());

		trie.put(AACBB, AACBB);
		Assert.assertTrue(trie.get(AACBB).contains(AACBB));
		Assert.assertTrue(trie.get(AACBB).contains(A_STAR_B));
	}
	
	@Test
	public void testWithTrailingStarWildcard() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();

		trie.put(A_STAR, A_STAR);
		Assert.assertTrue(AACBB + NOT_FOUND_BY + A_STAR, trie.get(AACBB).contains(A_STAR));
		Assert.assertTrue(AABB + NOT_FOUND_BY + A_STAR, trie.get(AABB).contains(A_STAR));
		Assert.assertTrue(AB + NOT_FOUND_BY + A_STAR, trie.get(AB).contains(A_STAR));
		Assert.assertTrue(ABC + NOT_FOUND_BY + A_STAR_B, trie.get(ABC).contains(A_STAR));
		Assert.assertTrue(CCB + FOUND_BY + A_STAR_B, trie.get(CCB).isEmpty());

		trie.put(AABB, AABB);
		Assert.assertTrue(trie.get(AABB).contains(AABB));
		Assert.assertEquals(2, trie.get(AABB).size());

		trie.put(AACBB, AACBB);
		Assert.assertTrue(trie.get(AACBB).contains(AACBB));
		Assert.assertTrue(trie.get(AACBB).contains(A_STAR));
	}
	
	@Test
	public void testWithInitialStarWildcard() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();

		trie.put(STAR_B, STAR_B);
		Assert.assertTrue(AACBB + NOT_FOUND_BY + STAR_B, trie.get(AACBB).contains(STAR_B));
		Assert.assertTrue(AABB + NOT_FOUND_BY + STAR_B, trie.get(AABB).contains(STAR_B));

		Assert.assertTrue(ABC + FOUND_BY + A_STAR_B, trie.get(ABC).isEmpty());
		Assert.assertTrue(CCB + NOT_FOUND_BY + A_STAR_B, trie.get(CCB).contains(STAR_B));

		trie.put(AABB, AABB);
		Assert.assertTrue(trie.get(AABB).contains(AABB));
		Assert.assertEquals(2, trie.get(AABB).size());

		trie.put(AACBB, AACBB);
		Assert.assertTrue(trie.get(AACBB).contains(AACBB));
		Assert.assertTrue(trie.get(AACBB).contains(STAR_B));
	}
	
	@Test
	public void testWithMultipleStarWildcards() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();
		
		trie.put(STAR_B, STAR_B);
		trie.put(A_STAR, A_STAR);
		trie.put(A_STAR_B, A_STAR_B);
		
		Assert.assertEquals(3, trie.get(AACBB).size());
		
		
		trie.put(AA_STAR_BB, AA_STAR_BB);
		Assert.assertEquals(4, trie.get(AACBB).size());
		
		Assert.assertEquals(3, trie.get(AB).size());
		Assert.assertEquals(1, trie.get(CCB).size());
		
		Assert.assertEquals(3, trie.get("acb").size());
	}
	
	@Test
	public void testOverlapWithWildcard() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();
		
		trie.put(ABC,ABC);
		trie.put(A_STAR_BC,A_STAR_BC);

		Assert.assertEquals(2, trie.get(ABC).size());
		Assert.assertEquals(1, trie.get("abbc").size());
	}
	
	@Test
	public void testEmptyKey() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();
		
		trie.put("",ABC);
		Assert.assertEquals(1, trie.get("").size());
	}
	
	@Test
	public void testWithOrAndWildcard() {
		final WildcardTrie<String> trie = new WildcardTrie<String>();

		final String key = ABC + WildcardTrie.OR_STRING + CCB; 
		trie.put(key, "");
		Assert.assertTrue(ABC  + NOT_FOUND_BY + key, trie.get(ABC).contains(""));
		Assert.assertTrue(CCB + NOT_FOUND_BY + key, trie.get(CCB).contains(""));

		Assert.assertTrue(AABB + FOUND_BY + key, trie.get(AABB).isEmpty());
		Assert.assertTrue(AB + FOUND_BY + key, trie.get(AB).isEmpty());
	}

}
