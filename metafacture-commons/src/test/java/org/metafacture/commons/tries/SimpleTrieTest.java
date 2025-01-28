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

package org.metafacture.commons.tries;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

/**
 * tests {@link SimpleTrie}
 *
 * @author Markus Michael Geipel
 *
 */
public final class SimpleTrieTest {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    public SimpleTrieTest() {
    }

    @Test
    public void testAdd() {
        final SimpleTrie<String> trie = new SimpleTrie<String>();
        Assert.assertNull(trie.get(KEY));
        trie.put(KEY, VALUE);
        Assert.assertEquals(VALUE, trie.get(KEY));
    }

    @Test
    public void testMultiAdd() {
        final SimpleTrie<String> trie = new SimpleTrie<String>();

        final String[] megacities =  {"Brisbane", "Sydney", "Melbourne", "Adelaide", "Perth", "Berlin", "Berlin Center", "Bremen", "Petersburg"};

        for (int i = 0; i < megacities.length; ++i) {
            final String city = megacities[i];
            trie.put(city, city.toUpperCase(Locale.US));
        }

        for (int i = 0; i < megacities.length; ++i) {
            final String city = megacities[i];
            Assert.assertEquals(city.toUpperCase(Locale.US), trie.get(city));
        }
    }

}
