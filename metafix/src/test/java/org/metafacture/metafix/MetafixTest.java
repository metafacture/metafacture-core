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

import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metafix.api.FixRegistry;
import org.metafacture.metafix.bind.ListAs;
import org.metafacture.metafix.bind.Once;
import org.metafacture.metamorph.api.Maps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    @Test
    // See issue metafacture-fix#79
    public void shouldThrowExceptionForInvalidFixFile() {
        final String fixFile = "src/test/resources/org/metafacture/metafix/fixes/invalid.fix";
        MetafixTestHelpers.assertThrows(FixParseException.class, "Invalid FixStandaloneSetup resource: " + fixFile, () -> new Metafix(fixFile));

        // TODO: Test logging statements
    }

    @Test
    public void shouldMatchTwoDigitIndexInPath() {
        final String wildcardPath = "field.*.field";
        final String concretePath = "field.10.field";
        final Value value = new Value("").withPathSet(concretePath);
        final FixPath result = new FixPath(wildcardPath).to(value, 15);
        // `value` has a `concretePath` matching the `wildcardPath`, so
        // `result` should be that path, not a path using the passed index:
        Assertions.assertEquals(concretePath, result.toString());
    }

    @Test
    public void shouldRegisterNamedCommand() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz = Once.class;

        Assertions.assertFalse(registry.isRegisteredCommand(name));
        Assertions.assertTrue(registry.registerCommand(name, clazz));
        Assertions.assertTrue(registry.isRegisteredCommand(name));
    }

    @Test
    public void shouldRegisterAnnotatedCommand() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "test_command";
        final Class<?> clazz = TestCommand1.class;

        Assertions.assertFalse(registry.isRegisteredCommand(name));
        Assertions.assertEquals(name, registry.registerCommand(clazz));
        Assertions.assertTrue(registry.isRegisteredCommand(name));
    }

    @Test
    public void shouldNotRegisterSameAnnotatedCommandRepeatedly() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "test_command";
        final Class<?> clazz = TestCommand1.class;

        Assertions.assertEquals(name, registry.registerCommand(clazz));
        Assertions.assertNull(registry.registerCommand(clazz));
    }

    @Test
    public void shouldNotRegisterUnannotatedCommand() {
        final FixRegistry registry = new Metafix().getRegistry();
        final Class<?> clazz = TestCommand2.class;

        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerCommand(clazz));
    }

    @Test
    public void shouldNotRegisterCommandThatIsNoCommand() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz = MetafixTest.class;

        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerCommand(name, clazz));
    }

    @Test
    public void shouldNotRegisterSameCommandRepeatedly() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz = Once.class;

        Assertions.assertTrue(registry.registerCommand(name, clazz));
        Assertions.assertFalse(registry.registerCommand(name, clazz));
    }

    @Test
    public void shouldRegisterSameCommandInDifferentRegistry() {
        final FixRegistry registry1 = new Metafix().getRegistry();
        final FixRegistry registry2 = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz = Once.class;

        Assertions.assertTrue(registry1.registerCommand(name, clazz));
        Assertions.assertTrue(registry2.registerCommand(name, clazz));
    }

    @Test
    public void shouldRegisterSameCommandRepeatedlyAfterUnregistering() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz = Once.class;

        Assertions.assertTrue(registry.registerCommand(name, clazz));
        Assertions.assertEquals(clazz, registry.unregisterBind(name));
        Assertions.assertTrue(registry.registerCommand(name, clazz));
    }

    @Test
    public void shouldRegisterSameCommandUnderDifferentName() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name1 = "bla";
        final String name2 = "blub";
        final Class<?> clazz = Once.class;

        Assertions.assertTrue(registry.registerCommand(name1, clazz));
        Assertions.assertTrue(registry.registerCommand(name2, clazz));
    }

    @Test
    public void shouldNotRegisterDifferentCommandUnderSameName() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz1 = Once.class;
        final Class<?> clazz2 = ListAs.class;

        Assertions.assertTrue(registry.registerCommand(name, clazz1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.registerCommand(name, clazz2));
    }

    @Test
    public void shouldRegisterDifferentCommandUnderSameNameInDifferentRegistry() {
        final FixRegistry registry1 = new Metafix().getRegistry();
        final FixRegistry registry2 = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz1 = Once.class;
        final Class<?> clazz2 = ListAs.class;

        Assertions.assertTrue(registry1.registerCommand(name, clazz1));
        Assertions.assertTrue(registry2.registerCommand(name, clazz2));
    }

    @Test
    public void shouldNotRegisterCommandWithoutName() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = null;
        final Class<?> clazz = Once.class;

        Assertions.assertThrows(NullPointerException.class, () -> registry.registerCommand(name, clazz));
    }

    @Test
    public void shouldNotRegisterCommandWithoutClass() {
        final FixRegistry registry = new Metafix().getRegistry();
        final String name = "bla";
        final Class<?> clazz = null;

        Assertions.assertThrows(NullPointerException.class, () -> registry.registerCommand(name, clazz));
    }

    @FixCommand("test_command")
    private static class TestCommand1 implements FixFunction {

        private TestCommand1() {
        }

        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            // nothing to do
        }

    }

    private static class TestCommand2 extends TestCommand1 {

        private TestCommand2() {
        }

    }

}
