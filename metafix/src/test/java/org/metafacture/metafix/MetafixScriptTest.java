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

import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultStreamReceiver;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Tests Metafix script level methods.
 */
@ExtendWith(MockitoExtension.class)
public class MetafixScriptTest {

    private static final String MAP_NAME = "testMap";

    private static final String CSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.csv";
    private static final String TSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.tsv";

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixScriptTest() {
    }

    @Test
    public void shouldPutSingleVariable() {
        assertVar("put_var('varName', 'value')",
                null,
                ImmutableMap.of("varName", "value"));
    }

    @Test
    public void shouldPutMultipleVariables() {
        assertVar("put_var('varName', 'value')\nput_var('varName2', 'value2')",
                null,
                ImmutableMap.of("varName", "value", "varName2", "value2"));
    }

    @Test
    public void shouldPutMultipleVariablesFromMap() {
        assertVar("put_vars(varName: 'value', varName2: 'value2')",
                null,
                ImmutableMap.of("varName", "value", "varName2", "value2"));
    }

    @Test
    public void shouldResolveVariablesInSingleVariable() {
        assertVar("put_var('varName', 'value$[var]')",
                ImmutableMap.of("var", "1"),
                ImmutableMap.of("varName", "value1"));
    }

    @Test
    public void shouldResolveVariablesInMultipleVariables() {
        assertVar("put_var('varName', 'value$[var]')\nput_var('$[varName]Var', 'value2')",
                ImmutableMap.of("var", "1"),
                ImmutableMap.of("varName", "value1", "value1Var", "value2"));
    }

    @Test
    public void shouldResolveVariablesInOptionsKeys() {
        assertVar("put_vars('varName$[var]': 'value')",
                ImmutableMap.of("var", "1"),
                ImmutableMap.of("varName1", "value"));
    }

    @Test
    public void shouldResolveVariablesInOptionsValues() {
        assertVar("put_vars('varName': 'value$[var]')",
                ImmutableMap.of("var", "1"),
                ImmutableMap.of("varName", "value1"));
    }

    @Test
    public void shouldResolveVariablesInOptionsFromPreviousMap() {
        assertVar("put_vars('varName': 'value$[var]')\nput_vars('$[varName]Var': 'value2')",
                ImmutableMap.of("var", "1"),
                ImmutableMap.of("varName", "value1", "value1Var", "value2"));
    }

    @Test
    public void shouldNotResolveVariablesInOptionsFromCurrentMap() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Variable 'varName' was not assigned!\nAssigned variables:\n{var=1}", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "put_vars(varName: 'value$[var]', '$[varName]Var': 'value2')"
                ),
                ImmutableMap.of("var", "1"),
                i -> {
                    i.startRecord("");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldPutEmptyInternalMap() {
        assertMap("put_map('" + MAP_NAME + "')", MAP_NAME);
    }

    @Test
    public void shouldPutMultipleInternalMaps() {
        assertFix("put_map('" + MAP_NAME + "')\nput_map('" + MAP_NAME + "2')", f -> {
            assertMap(f, MAP_NAME);
            assertMap(f, MAP_NAME + "2");
        });
    }

    @Test
    public void shouldPutInternalMapWithOptions() {
        assertMap("put_map('" + MAP_NAME + "', k1: 'v1', k2: 'v2')", MAP_NAME);
    }

    @Test
    public void shouldPutExternalFileMap() {
        assertMap("put_filemap('" + CSV_MAP + "')", CSV_MAP);
    }

    @Test
    public void shouldNotPutRelativeExternalFileMapFromInlineScript() {
        final String mapFile = "../maps/test.csv";

        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Cannot resolve relative path: " + mapFile, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "put_filemap('" + mapFile + "')"
                ),
                i -> {
                    i.startRecord("");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldPutRelativeExternalFileMapFromExternalScript() {
        assertMap("src/test/resources/org/metafacture/metafix/fixes/filemap.fix", MAP_NAME);
    }

    @Test
    public void shouldPutMultipleExternalFileMaps() {
        assertFix("put_filemap('" + CSV_MAP + "')\nput_filemap('" + TSV_MAP + "')", f -> {
            assertMap(f, CSV_MAP);
            assertMap(f, TSV_MAP);
        });
    }

    @Test
    public void shouldPutExternalFileMapWithName() {
        assertMap("put_filemap('" + CSV_MAP + "', '" + MAP_NAME + "')", MAP_NAME);
    }

    @Test
    public void shouldPutExternalFileMapWithOptions() {
        assertMap("put_filemap('" + TSV_MAP + "', sep_char: '\t')", TSV_MAP);
    }

    @Test
    public void shouldPutExternalFileMapWithNameAndOptions() {
        assertMap("put_filemap('" + TSV_MAP + "', '" + MAP_NAME + "', sep_char: '\t')", MAP_NAME);
    }

    @Test
    public void shouldDoNothing() {
        assertFix("nothing()", f -> { });
    }

    @Test
    public void shouldIncludeFixFile() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('trace')",
                "add_field('trace', 'before include')",
                "include('src/test/resources/org/metafacture/metafix/fixes/base.fix')",
                "add_field('trace', 'after include')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("record", "1");
                i.endRecord();

                i.startRecord("2");
                i.literal("record", "2");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("record", "1");
                o.get().literal("trace", "before include");
                o.get().literal("trace", "base 1");
                o.get().literal("trace", "after include");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("record", "2");
                o.get().literal("trace", "before include");
                o.get().literal("trace", "base 2");
                o.get().literal("trace", "after include");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIncludeFixFileInBind() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('trace')",
                "add_field('trace', 'before bind')",
                "do list(path: 'data', 'var': '$i')",
                "  paste('trace.$append', '~before include', '$i')",
                "  include('src/test/resources/org/metafacture/metafix/fixes/var.fix')",
                "  paste('trace.$append', '~after include', '$i')",
                "end",
                "add_field('trace', 'after bind')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("record", "1");
                i.literal("data", "marc");
                i.literal("data", "json");
                i.endRecord();

                i.startRecord("2");
                i.literal("record", "2");
                i.literal("data", "test");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("record", "1");
                o.get().literal("data", "marc");
                o.get().literal("data", "json");
                o.get().literal("trace", "before bind");
                o.get().literal("trace", "before include marc");
                o.get().literal("trace", "marc 1");
                o.get().literal("trace", "after include MARC");
                o.get().literal("trace", "before include json");
                o.get().literal("trace", "json 1");
                o.get().literal("trace", "after include JSON");
                o.get().literal("trace", "after bind");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("record", "2");
                o.get().literal("data", "test");
                o.get().literal("trace", "before bind");
                o.get().literal("trace", "before include test");
                o.get().literal("trace", "test 2");
                o.get().literal("trace", "after include TEST");
                o.get().literal("trace", "after bind");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotIncludeRelativeFixFileFromInlineScript() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Cannot resolve relative path: ./base.fix", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "include('src/test/resources/org/metafacture/metafix/fixes/include.fix')"
                ),
                i -> {
                    i.startRecord("");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldIncludeFixFileFromExternalScript() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "src/test/resources/org/metafacture/metafix/fixes/include.fix"
            ),
            i -> {
                i.startRecord("1");
                i.literal("record", "1");
                i.endRecord();

                i.startRecord("2");
                i.literal("record", "2");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("record", "1");
                o.get().literal("trace", "before include");
                o.get().literal("trace", "base 1");
                o.get().literal("trace", "after include");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("record", "2");
                o.get().literal("trace", "before include");
                o.get().literal("trace", "base 2");
                o.get().literal("trace", "after include");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIncludeNestedFixFileFromExternalScript() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "src/test/resources/org/metafacture/metafix/fixes/nested.fix"
            ),
            i -> {
                i.startRecord("1");
                i.literal("record", "1");
                i.endRecord();

                i.startRecord("2");
                i.literal("record", "2");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("record", "1");
                o.get().literal("trace", "before nested");
                o.get().literal("trace", "before include");
                o.get().literal("trace", "base 1");
                o.get().literal("trace", "after include");
                o.get().literal("trace", "after nested");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("record", "2");
                o.get().literal("trace", "before nested");
                o.get().literal("trace", "before include");
                o.get().literal("trace", "base 2");
                o.get().literal("trace", "after include");
                o.get().literal("trace", "after nested");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIncludeLocationAndTextInExecutionException() {
        final String fixFile = "src/test/resources/org/metafacture/metafix/fixes/error.fix";
        final String message = "Error while executing Fix expression (at FILE, line 2): append(\"animals\", \" is cool\")";

        MetafixTestHelpers.assertThrows(FixExecutionException.class, s -> s.replaceAll("file:/.+?" + Pattern.quote(fixFile), "FILE"), message, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(fixFile),
                i -> {
                    i.startRecord("1");
                    i.startEntity("animals");
                    i.literal("1", "dog");
                    i.literal("2", "cat");
                    i.literal("3", "zebra");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    private void assertStrictness(final Metafix.Strictness strictness, final String fixDef, final boolean stubLogging, final Consumer<Supplier<StreamReceiver>> out) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('before', '')",
                fixDef,
                "add_field('after', '')"
            ),
            i -> {
                final Metafix.Strictness strictnessSpy = Mockito.spy(strictness);
                i.setStrictness(strictnessSpy);

                if (stubLogging) {
                    Mockito.doNothing().when(strictnessSpy).log(Mockito.any(), Mockito.any());
                }

                i.startRecord("1");
                i.literal("data", "foo");
                i.endRecord();

                i.startRecord("2");
                i.literal("data", "foo");
                i.literal("data", "bar");
                i.endRecord();

                i.startRecord("3");
                i.literal("data", "bar");
                i.endRecord();
            },
            out
        );

        // TODO: Test logging statements
    }

    private void assertStrictness(final Metafix.Strictness strictness, final boolean stubLogging, final Consumer<Supplier<StreamReceiver>> out) {
        assertStrictness(strictness, "upcase('data')", stubLogging, out);
    }

    @Test
    public void shouldSkipExpressionOnExecutionException() {
        assertStrictness(Metafix.Strictness.EXPRESSION, true, o -> {
            o.get().startRecord("1");
            o.get().literal("data", "FOO");
            o.get().literal("before", "");
            o.get().literal("after", "");
            o.get().endRecord();

            o.get().startRecord("2");
            o.get().literal("data", "foo");
            o.get().literal("data", "bar");
            o.get().literal("before", "");
            o.get().literal("after", "");
            o.get().endRecord();

            o.get().startRecord("3");
            o.get().literal("data", "BAR");
            o.get().literal("before", "");
            o.get().literal("after", "");
            o.get().endRecord();
        });
    }

    @Test
    public void shouldSkipRecordOnExecutionException() {
        assertStrictness(Metafix.Strictness.RECORD, true, o -> {
            o.get().startRecord("1");
            o.get().literal("data", "FOO");
            o.get().literal("before", "");
            o.get().literal("after", "");
            o.get().endRecord();

            o.get().startRecord("3");
            o.get().literal("data", "BAR");
            o.get().literal("before", "");
            o.get().literal("after", "");
            o.get().endRecord();
        });
    }

    @Test
    public void shouldAbortProcessOnExecutionException() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
                assertStrictness(Metafix.Strictness.PROCESS, false, o -> {
                })
        );
    }

    @Test
    public void shouldAbortProcessOnProcessException() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "No enum constant org.metafacture.metafix.FixMethod.foo", () ->
                assertStrictness(Metafix.Strictness.EXPRESSION, "foo()", false, o -> {
                })
        );
    }

    private void assertVar(final String fixDef, final Map<String, String> vars, final Map<String, String> result) {
        assertFix(fixDef, vars, f -> result.forEach((k, v) -> Assertions.assertEquals(v, f.getVars().get(k))));
    }

    private void assertMap(final String fixDef, final String mapName) {
        assertFix(fixDef, f -> assertMap(f, mapName));
    }

    private void assertMap(final Metafix metafix, final String mapName) {
        Assertions.assertTrue(metafix.getMapNames().contains(mapName));
        Assertions.assertNotNull(metafix.getMap(mapName));
    }

    private void assertFix(final String fixDef, final Consumer<Metafix> consumer) {
        assertFix(fixDef, null, consumer);
    }

    private void assertFix(final String fixDef, final Map<String, String> vars, final Consumer<Metafix> consumer) {
        try {
            final Metafix metafix = vars == null ? new Metafix(fixDef) : new Metafix(fixDef, vars);

            // Prepare and trigger script execution
            metafix.setReceiver(new DefaultStreamReceiver());
            metafix.startRecord("");
            metafix.endRecord();

            consumer.accept(metafix);
        }
        catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
