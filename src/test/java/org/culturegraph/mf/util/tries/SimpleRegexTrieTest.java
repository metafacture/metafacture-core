/*
 *  Copyright 2013 Pascal Christoph, hbz
 * Licensed under the Eclipse Public License 1.0 */
package org.culturegraph.mf.util.tries;

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
