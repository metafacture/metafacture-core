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

import java.util.Map;

/**
 * tests {@link CharMap}
 *
 * @author Markus Michael Geipel
 *
 */
public final class CharMapTest {

    private static final String UML = "umlaut";

    public CharMapTest() {
    }

    @Test
    public void testEmptyMap() {
        final Map<Character, Integer> map = new CharMap<Integer>();

        for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; ++i) {
            Assert.assertNull(map.get(Byte.valueOf(i)));
        }
    }

    @Test
    public void testSingleEntry() {
        final Map<Character, String> map = new CharMap<String>();
        final char beite = 'ü';
        map.put(Character.valueOf(beite), UML);

        Assert.assertEquals(UML, map.get(Character.valueOf(beite)));
    }

    @Test
    public void testFullMap() {
        final Map<Character, Integer> map = new CharMap<Integer>();

        for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; ++i) {
            map.put(Character.valueOf(i), Integer.valueOf(i));
        }

        for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; ++i) {
            Assert.assertEquals(Integer.valueOf(i), map.get(Character.valueOf(i)));
        }
    }

    @Test
    public void testMixedMap() {
        final Map<Character, Integer> map = new CharMap<Integer>();

        for (char i = 0; i < Character.MAX_VALUE - 1; i += 2) {
            map.put(Character.valueOf(i), Integer.valueOf(i));
        }

        for (char i = 0; i < Character.MAX_VALUE - 1; i += 2) {
            Assert.assertEquals(Integer.valueOf(i), map.get(Character.valueOf(i)));
        }
    }

}
