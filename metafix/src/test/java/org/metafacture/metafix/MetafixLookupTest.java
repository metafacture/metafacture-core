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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

    private static final String LOOKUP = "lookup('title.*',";

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixLookupTest() {
    }

    @Test
    public void inline() {
        assertMap(
                LOOKUP + " Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)"
        );
    }

    @Test
    public void inlineMultilineIndent() {
        assertMap(
                LOOKUP,
                "  Aloha: Alohaeha,",
                "  Moin: 'Moin zäme',",
                "  __default: Tach)"
        );
    }

    @Test
    public void inlineDotNotationNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('data.title.*', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)"
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
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().literal("title", "Tach");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupInternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('title', 'Aloha')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupDeduplicatedInternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('title', 'Aloha', 'Aloha')",
                "uniq('title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupCopiedInternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('data', 'Aloha')",
                "set_array('title')",
                "copy_field('data', 'title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("data", "Aloha");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupCopiedDeduplicatedInternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('data', 'Aloha', 'Aloha')",
                "uniq('data')",
                "set_array('title')",
                "copy_field('data', 'title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("data", "Aloha");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupCopiedExternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('title')",
                "copy_field('data', 'title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("data", "Aloha");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("data", "Aloha");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupCopiedDeduplicatedExternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('data')",
                "set_array('title')",
                "copy_field('data', 'title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("data", "Aloha");
                i.literal("data", "Aloha");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("data", "Aloha");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupMovedDeduplicatedExternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('data')",
                "set_array('title')",
                "move_field('data', 'title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("data", "Aloha");
                i.literal("data", "Aloha");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupMovedExternalArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('title')",
                "move_field('data', 'title')",
                LOOKUP + " Aloha: Alohaeha)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("data", "Aloha");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/121
    public void shouldLookupArraySubFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('animals[].*.Aanimal', '" + TSV_MAP + "', 'sep_char': '\t')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("name", "Jake");
                i.literal("Aanimal", "Aloha");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Blacky");
                i.literal("Aanimal", "Hey");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("name", "Jake");
                o.get().literal("Aanimal", "Alohaeha");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Blacky");
                o.get().literal("Aanimal", "Tach");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void csv() {
        assertMap(
                LOOKUP + " '" + CSV_MAP + "')"
        );
    }

    @Test
    public void tsv() {
        assertMap(
                LOOKUP + " '" + TSV_MAP + "', sep_char:'\t')"
        );
    }

    @Test
    public void shouldLookupInSeparateInternalMap() {
        assertMap(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)",
                LOOKUP + " 'testMap')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMap() {
        assertMap(
                "put_filemap('" + CSV_MAP + "')",
                LOOKUP + " '" + CSV_MAP + "')"
        );
    }

    @Test
    public void shouldNotLookupInRelativeExternalFileMapFromInlineScript() {
        final String mapFile = "../maps/test.csv";

        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Cannot resolve relative path: " + mapFile, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    LOOKUP + " '" + mapFile + "')"
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
    public void shouldLookupInRelativeExternalFileMapFromExternalScript() {
        assertMap(
                "src/test/resources/org/metafacture/metafix/fixes/filemap_lookup.fix"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMapWithName() {
        assertMap(
                "put_filemap('" + CSV_MAP + "', 'testMap')",
                LOOKUP + " 'testMap')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMapWithOptions() {
        assertMap(
                "put_filemap('" + TSV_MAP + "', sep_char: '\t')",
                LOOKUP + " '" + TSV_MAP + "')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalFileMapWithNameAndOptions() {
        assertMap(
                "put_filemap('" + TSV_MAP + "', 'testMap', sep_char: '\t')",
                LOOKUP + " 'testMap')"
        );
    }

    @Test
    public void shouldDefineMultipleSeparateMaps() {
        assertMap(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)",
                "put_map('testMap2', __default: Hi)",
                LOOKUP + " 'testMap')"
        );
    }

    @Test
    public void shouldOverwriteExistingSeparateMap() {
        assertMap(
                "put_map('testMap', __default: Hi)",
                "put_filemap('" + CSV_MAP + "', 'testMap')",
                LOOKUP + " 'testMap')"
        );
    }

    @Test
    public void shouldIgnoreOptionsOnLookupInSeparateInternalMap() {
        assertMap(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)",
                LOOKUP + " 'testMap', __default: Hi)"
        );
    }

    @Test
    public void shouldIgnoreOptionsOnLookupInSeparateExternalFileMap() {
        assertMap(
                "put_filemap('" + CSV_MAP + "')",
                LOOKUP + " '" + CSV_MAP + "', sep_char: '\t')"
        );
    }

    @Test
    public void shouldNotLookupInExternalFileMapWithWrongOptions() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                LOOKUP + " '" + CSV_MAP + "', sep_char:'\t')"
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
                o.get().literal("title", "Aloha");
                o.get().literal("title", "Moin");
                o.get().literal("title", "Hey");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteLookupInExternalFileMapWithWrongOptions() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                LOOKUP + " '" + CSV_MAP + "', sep_char: '\t', delete: 'true')"
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
    // See https://github.com/metafacture/metafacture-fix/issues/149
    public void shouldKeepOriginalValueIfNotFoundAndNoDefault() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title.*', Aloha: 'Alohaeha', 'Moin': 'Moin zäme')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().literal("title", "Yo");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldUseDefaultValueIfNotFound() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title.*', Aloha: 'Alohaeha', 'Moin': 'Moin zäme', __default: Tach)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().literal("title", "Tach");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/149
    public void shouldDeleteNonFoundLookupOnDemand() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title.*', Aloha: Alohaeha, 'Moin': 'Moin zäme', delete: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/149
    public void shouldNotDeleteNonFoundLookupExplicitly() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title.*', Aloha: Alohaeha, 'Moin': 'Moin zäme', delete: 'false')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().literal("title", "Yo");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupAfterKeepingUnsuccessfulLookup() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title.*', Aloha: Alohaeha, 'Moin': 'Moin zäme')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Yo");
                i.literal("title", "Moin");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Yo");
                o.get().literal("title", "Moin zäme");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupAfterDeletingUnsuccessfulLookup() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title.*', Aloha: Alohaeha, 'Moin': 'Moin zäme', delete: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Yo");
                i.literal("title", "Moin");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/149
    public void shouldDeleteNonFoundLookupOnDemandNonRepeatedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title', Aloha: Alohaeha, 'Moin': 'Moin zäme', delete: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.endRecord();
                i.startRecord("2");
                i.literal("title", "Moin");
                i.endRecord();
                i.startRecord("3");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("title", "Moin zäme");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteNonFoundLookupOnDemandAndVacuum() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme')",
                "lookup('title.*', testMap, delete: 'true')",
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteNonFoundLookupOnDemandAndMove() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_map('testMap', Aloha: Alohaeha, 'Moin': 'Moin zäme')",
                "lookup('title.*', testMap, delete: 'true')",
                "move_field('title','t')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Yo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("t", "Alohaeha");
                o.get().literal("t", "Moin zäme");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIgnoreOptionsOnSubsequentLookupInExternalFileMap() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                LOOKUP + " '" + CSV_MAP + "')",
                LOOKUP + " '" + CSV_MAP + "', sep_char: '\t')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                f.apply(3).literal("title", "Tach");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotLookupInUnknownInternalMap() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                LOOKUP + " 'testMap')"
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
                o.get().literal("title", "Aloha");
                o.get().literal("title", "Moin");
                o.get().literal("title", "Hey");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteLookupInUnknownInternalMap() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                LOOKUP + " 'testMap', delete: 'true')"
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
    public void shouldFailLookupInUnknownExternalMap() {
        MetafixTestHelpers.assertProcessException(MorphExecutionException.class, "File not found: testMap.csv", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    LOOKUP + " 'testMap.csv')"
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
            )
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
                o.get().literal("title", "Alohaeha");
                o.get().literal("title", "Moin zäme");
                o.get().literal("title", "Tach");
                o.get().endRecord();
            }
        );
    }

}
