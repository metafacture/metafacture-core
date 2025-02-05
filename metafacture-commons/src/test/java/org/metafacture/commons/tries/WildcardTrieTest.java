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

package org.metafacture.commons.tries;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for class {@link WildcardTrie}
 *
 * @author Markus Michael Geipel
 *
 */
public final class WildcardTrieTest {

    private static final String A = "a";
    private static final String AABB = "aabb";
    private static final String AACBB = "aacbb";
    private static final String AB = "ab";
    private static final String ABBC = "abbc";
    private static final String ABC = "abc";
    private static final String ACB = "acb";
    private static final String B = "b";
    private static final String CCB = "ccb";
    private static final String EMPTY = "";
    private static final String X = "x";
    private static final List<String> ALL = List.of(A, AABB, AACBB, AB, ABBC, ABC, ACB, B, CCB, EMPTY, X);

    private static final String AAQBB = "aa?bb";
    private static final String AA_STAR_BB = "aa*bb";
    private static final String A_STAR = "a*";
    private static final String A_STAR_B = "a*b";
    private static final String A_STAR_BC = "a*bc";
    private static final String STAR_B = "*b";

    private static final String NOT_FOUND_BY = " not found by ";
    private static final String FOUND_BY = " found by ";

    private WildcardTrie<String> trie;

    public WildcardTrieTest() {
    }

    @Before
    public void createSystemUnderTest() {
        trie = new WildcardTrie<>();
    }

    @Test
    public void testEmptyTrie() {
        assertTrie(null);
    }

    @Test
    public void testWithQWildcard() {
        assertList(ABC, ABC);
        assertTrie(AAQBB, AACBB);
        assertList(AABB, AABB);
        assertList(AACBB, AAQBB, AACBB);
    }

    @Test
    public void testWithStarWildcard() {
        assertTrie(A_STAR_B, AABB, AACBB, AB, ACB);
        assertList(AABB, A_STAR_B, AABB);
        assertList(AACBB, A_STAR_B, AACBB);
    }

    @Test
    public void testWithTrailingStarWildcard() {
        assertTrie(A_STAR, A, AABB, AACBB, AB, ABBC, ABC, ACB);
        assertList(AABB, A_STAR, AABB);
        assertList(AACBB, A_STAR, AACBB);
    }

    @Test
    public void testWithInitialStarWildcard() {
        assertTrie(STAR_B, AABB, AACBB, AB, ACB, B, CCB);
        assertList(AABB, STAR_B, AABB);
        assertList(AACBB, STAR_B, AACBB);
    }

    @Test
    public void testWithMultipleStarWildcards() {
        assertTrie(STAR_B, AABB, AACBB, AB, ACB, B, CCB);
        assertTrie(A_STAR, A, AABB, AACBB, AB, ABC, ABBC, ACB);
        assertTrie(A_STAR_B, AABB, AACBB, AB, ACB);
        assertList(AACBB, STAR_B, A_STAR, A_STAR_B, AACBB);

        assertTrie(AA_STAR_BB, AABB, AACBB);
        assertList(AACBB, STAR_B, A_STAR, A_STAR_B, AA_STAR_BB, AACBB);

        assertList(AB, STAR_B, A_STAR, A_STAR_B, AB);
        assertList(ACB, STAR_B, A_STAR, A_STAR_B, ACB);
        assertList(CCB, STAR_B, CCB);
    }

    @Test
    public void testOverlapWithWildcard() {
        assertTrie(A_STAR_BC, ABBC, ABC);
        assertList(ABC, A_STAR_BC, ABC);
        assertList(ABBC, A_STAR_BC, ABBC);
    }

    @Test
    public void testEmptyKey() {
        assertList(EMPTY, EMPTY);
    }

    @Test
    public void testWithOrAndWildcard() {
        assertTrie(ABC + WildcardTrie.OR_STRING + CCB, ABC, CCB);
    }

    private void assertTrie(final String key, final String... positive) {
        final List<String> negative = new ArrayList<>(ALL);

        if (key != null) {
            trie.put(key, key);
        }

        Arrays.stream(positive).forEach(k -> {
            negative.remove(k);
            Assert.assertTrue(k + NOT_FOUND_BY + key, trie.get(k).contains(key));
        });

        negative.forEach(k -> {
            Assert.assertFalse(k + FOUND_BY + key, trie.get(k).contains(key));
        });
    }

    private void assertList(final String key, final String... expected) {
        assertTrie(key, key);

        final List<String> actual = trie.get(key);
        Collections.sort(actual);

        Assert.assertEquals(Arrays.asList(expected), actual);
    }

}
