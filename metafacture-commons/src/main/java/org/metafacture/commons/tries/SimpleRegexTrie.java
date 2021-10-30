/*
 * Copyright 2013, 2014 Pascal Christoph, hbz
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

import java.util.List;

/**
 * A wrapper for the {@link WildcardTrie} enabling the use of simple character
 * classes .
 *
 * @author Pascal Christoph
 *
 * @param <P>
 *            type of value stored
 */
public class SimpleRegexTrie<P> {

    // Non-empty character class, containing non-[] characters, e.g.
    // matches: `lit-[A]`, `lit-[AB]`, does not match: `a[].1`, `a[].1.b[].1`
    public static final String SIMPLE_CHARACTER_CLASS = ".*\\[[^\\[\\]]+\\].*";

    private final WildcardTrie<P> trie = new WildcardTrie<P>();

    /**
     * Creates an instance of {@link SimpleRegexTrie}.
     */
    public SimpleRegexTrie() {
    }

    /**
     * Enables the use of simple character classes like 'a[agt][ac]'. Calls the
     * method of {@link WildcardTrie} for further treatment.
     *
     * @param keys pattern of keys
     * @param value value to associate with the key pattern
     */
    public void put(final String keys, final P value) {
        if (keys.matches(SIMPLE_CHARACTER_CLASS)) {
            int charClassStart = keys.indexOf('[', 0);
            final int charClassEnd = keys.indexOf(']', 1);
            final String begin = keys.substring(0, charClassStart);
            for (; charClassStart < charClassEnd - 1; ++charClassStart) {
                final char middle = keys.charAt(charClassStart + 1);
                final String end = keys.substring(charClassEnd + 1, keys.length());
                put(begin + middle + end, value);
            }
        }
        else {
            trie.put(keys, value);
        }
    }

    /**
     * Gets the List of values identified by a key.
     *
     * @see WildcardTrie
     * @param key the key
     * @return the List of the key if the key exists, otherwise an empty List
     */
    public List<P> get(final String key) {
        return trie.get(key);
    }

}
