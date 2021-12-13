/*
 * Copyright 2021 hbz NRW
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

package org.metafacture.metafix;

import org.metafacture.metamorph.api.Maps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests Metafix API methods.
 */
public class MetafixTest {

    private static final String MAP_NAME = "testMap";
    private static final String KEY = "outName";
    private static final String VALUE = "testValue";

    public MetafixTest() {
    }

    @Test
    public void shouldPutVar() {
        final Metafix metafix = new Metafix();
        metafix.getVars().put(KEY, VALUE);

        Assertions.assertEquals(VALUE, metafix.getVars().get(KEY));
    }

    @Test
    public void shouldPutVarWithMutableMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(KEY, VALUE);

        final Metafix metafix = new Metafix(map);
        metafix.getVars().put(KEY + "2", VALUE + "2");

        Assertions.assertEquals(VALUE, metafix.getVars().get(KEY));
        Assertions.assertEquals(VALUE + "2", metafix.getVars().get(KEY + "2"));
    }

    @Test
    public void shouldPutVarWithImmutableMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(KEY, VALUE);

        final Metafix metafix = new Metafix(Collections.unmodifiableMap(map));
        metafix.getVars().put(KEY + "2", VALUE + "2");

        Assertions.assertEquals(VALUE, metafix.getVars().get(KEY));
        Assertions.assertEquals(VALUE + "2", metafix.getVars().get(KEY + "2"));
    }

    @Test
    public void shouldGetMapNames() {
        final Metafix metafix = new Metafix();
        metafix.putMap(MAP_NAME, new HashMap<>());
        metafix.putMap(MAP_NAME + "2", new HashMap<>());

        final Collection<String> actualNames = metafix.getMapNames();
        final Collection<String> expectedNames = Arrays.asList(MAP_NAME, MAP_NAME + "2");

        Assertions.assertTrue(actualNames.containsAll(expectedNames), "missing names");
        Assertions.assertTrue(expectedNames.containsAll(actualNames), "unexpected names");
    }

    @Test
    public void shouldGetMap() {
        final Map<String, String> map = new HashMap<>();

        final Metafix metafix = new Metafix();
        metafix.putMap(MAP_NAME, map);

        Assertions.assertSame(map, metafix.getMap(MAP_NAME));
    }

    @Test
    public void shouldGetEmptyUnknownMap() {
        final Metafix metafix = new Metafix();
        Assertions.assertEquals(new HashMap<>(), metafix.getMap(MAP_NAME));
    }

    @Test
    public void shouldPutValueIntoNewMap() {
        final Metafix metafix = new Metafix();
        metafix.putValue(MAP_NAME, KEY, VALUE);

        Assertions.assertNotNull(metafix.getMap(MAP_NAME));
        Assertions.assertEquals(VALUE, metafix.getValue(MAP_NAME, KEY));
    }

    @Test
    public void shouldPutValueIntoExistingMap() {
        final Map<String, String> map = new HashMap<>();

        final Metafix metafix = new Metafix();
        metafix.putMap(MAP_NAME, map);
        metafix.putValue(MAP_NAME, KEY, VALUE);

        Assertions.assertEquals(VALUE, map.get(KEY));
        Assertions.assertEquals(VALUE, metafix.getValue(MAP_NAME, KEY));
    }

    @Test
    public void shouldGetValueFromMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(KEY, VALUE);

        final Metafix metafix = new Metafix();
        metafix.putMap(MAP_NAME, map);

        Assertions.assertEquals(VALUE, metafix.getValue(MAP_NAME, KEY));
    }

    @Test
    public void shouldNotGetValueFromUnknownMap() {
        final Metafix metafix = new Metafix();
        Assertions.assertNull(metafix.getValue(MAP_NAME, KEY));
    }

    @Test
    public void shouldNotGetValueForUnknownKey() {
        final Metafix metafix = new Metafix();
        metafix.putMap(MAP_NAME, new HashMap<>());

        Assertions.assertNull(metafix.getValue(MAP_NAME, KEY));
    }

    @Test
    public void shouldGetDefaultValueForUnknownKey() {
        final Map<String, String> map = new HashMap<>();
        map.put(Maps.DEFAULT_MAP_KEY, VALUE);

        final Metafix metafix = new Metafix();
        metafix.putMap(MAP_NAME, map);

        Assertions.assertEquals(VALUE, metafix.getValue(MAP_NAME, KEY));
    }

}
