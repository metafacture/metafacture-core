/*
 * Copyright 2021 Fabian Steeg, hbz
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
import org.metafacture.metamorph.api.MorphExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Tests Metafix lookup. Following the cheat sheet examples at
 * https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixLookupTest {

    private static final String CSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.csv";
    private static final String TSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.tsv";

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixLookupTest() {
    }

    @Test
    public void inline() {
        assertMap(
                "lookup('title', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)"
        );
    }

    @Test
    public void inlineMultilineIndent() {
        assertMap(
                "lookup('title',",
                "  Aloha: Alohaeha,",
                "  Moin: 'Moin zäme',",
                "  __default: Tach)"
        );
    }

    @Test
    public void inlineDotNotationNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('data.title', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().literal("3", "Tach");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void csv() {
        assertMap(
                "lookup('title', '" + CSV_MAP + "')"
        );
    }

    @Test
    public void tsv() {
        assertMap(
                "lookup('title', '" + TSV_MAP + "', sep_char:'\t')"
        );
    }

    @Test
    public void shouldLookupInSeparateInternalMap() {
        assertMap(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)",
                "lookup('title', 'testMap')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMap() {
        assertMap(
                "put_filemap('" + CSV_MAP + "')",
                "lookup('title', '" + CSV_MAP + "')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMapWithName() {
        assertMap(
                "put_filemap('" + CSV_MAP + "', 'testMap')",
                "lookup('title', 'testMap')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMapWithOptions() {
        assertMap(
                "put_filemap('" + TSV_MAP + "', sep_char: '\t')",
                "lookup('title', '" + TSV_MAP + "')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMapWithNameAndOptions() {
        assertMap(
                "put_filemap('" + TSV_MAP + "', 'testMap', sep_char: '\t')",
                "lookup('title', 'testMap')"
        );
    }

    @Test
    public void shouldDefineMultipleSeparateMaps() {
        assertMap(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)",
                "put_map('testMap2', __default: Hi)",
                "lookup('title', 'testMap')"
        );
    }

    @Test
    public void shouldOverwriteExistingSeparateMap() {
        assertMap(
                "put_map('testMap', __default: Hi)",
                "put_filemap('" + CSV_MAP + "', 'testMap')",
                "lookup('title', 'testMap')"
        );
    }

    @Test
    public void shouldIgnoreOptionsOnLookupInSeparateInternalMap() {
        assertMap(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)",
                "lookup('title', 'testMap', __default: Hi)"
        );
    }

    @Test
    public void shouldIgnoreOptionsOnLookupInSeparateExternalFileMap() {
        assertMap(
                "put_filemap('" + CSV_MAP + "')",
                "lookup('title', '" + CSV_MAP + "', sep_char: '\t')"
        );
    }

    @Test
    public void shouldNotLookupInExternalFileMapWithWrongOptions() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title', '" + CSV_MAP + "', sep_char: '\t')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIgnoreOptionsOnSubsequentLookupInExternalFileMap() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title', '" + CSV_MAP + "')",
                "lookup('title', '" + CSV_MAP + "', sep_char: '\t')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("title");
                o.get().literal("1", "Tach");
                o.get().literal("2", "Tach");
                o.get().literal("3", "Tach");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFailLookupInUnknownNamedMap() {
        Assertions.assertThrows(MorphExecutionException.class, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "lookup('title', 'testMap')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("title", "Aloha");
                    i.literal("title", "Moin");
                    i.literal("title", "Hey");
                    i.endRecord();
                },
                o -> {
                }
            ),
            "File not found: testMap"
        );
    }

    private void assertMap(final String... fixDef) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(fixDef),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().literal("3", "Tach");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

}
