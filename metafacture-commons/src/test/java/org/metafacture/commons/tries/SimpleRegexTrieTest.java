/*
 *  Copyright 2013, 2020 Pascal Christoph, hbz and others
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

package org.metafacture.commons.tries;

import org.junit.Assert;
import org.junit.Test;

/**
 * tests {@link SimpleRegexTrie}
 *
 * @author Pascal Christoph, Fabian Steeg
 *
 */
public final class SimpleRegexTrieTest {

    private static final String AACBB = "aacbb";
    private static final String ABCBB = "abcbb";

    public SimpleRegexTrieTest() {
    }

    @Test
    public void testWithSimpleCharacterClass() {
        final SimpleRegexTrie<String> trie = new SimpleRegexTrie<String>();
        trie.put("a[ab]cbb", "value");
        Assert.assertTrue("Expecting to find: " + AACBB, trie.get(AACBB).size() == 1);
        Assert.assertTrue("Expecting to find: " + ABCBB, trie.get(ABCBB).size() == 1);
    }

    @Test
    public void testWithEmptyCharacterClass() {
        final SimpleRegexTrie<String> trie = new SimpleRegexTrie<String>();
        // Should not be treated as character class (used for JSON arrays):
        final String key = "a[].1.b[].1";
        trie.put(key, "value");
        Assert.assertTrue("Expecting to find: " + key, trie.get(key).size() == 1);
    }
}
