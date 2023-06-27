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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tests Metafix lookup. Following the cheat sheet examples at
 * https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixLookupTest {
    private static final String CSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.csv";
    private static final String CSV_PATH = "/maps/test.csv";
    private static final String CSV_URL = "%s" + CSV_PATH;
    private static final String RDF_MAP = "src/test/resources/org/metafacture/metafix/maps/test.ttl";
    private static final String HCRT_RDF_MAP = "src/test/resources/org/metafacture/metafix/maps/hcrt.ttl";
    private static final String RDF_PATH = "/maps/rpb.ttl";
    private static final String RDF_URL = "%s" + RDF_PATH;
    private static final String TSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.tsv";
    private static final String LOOKUP = "lookup('title.*',";

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockRule(WireMockConfiguration.wireMockConfig()
        .jettyAcceptors(Runtime.getRuntime().availableProcessors())
        .dynamicPort());

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixLookupTest() {
    }

    @BeforeAll
    private static void setStubForWireMock() {
        WIRE_MOCK_SERVER.start();

        // stubs for RDF
        final UrlPattern rdfUrlPattern = WireMock.urlPathEqualTo(RDF_PATH);
        final String redirectToUrl = "/redirect" + RDF_PATH;
        final UrlPattern urlPatternRedirectToUrl = WireMock.urlPathEqualTo(redirectToUrl);
        WIRE_MOCK_SERVER.stubFor(WireMock.get(rdfUrlPattern)
            .willReturn(WireMock.temporaryRedirect(redirectToUrl)));
        final String rdfResponseBody = loadFile(RDF_PATH);
        WIRE_MOCK_SERVER.stubFor(WireMock.get(urlPatternRedirectToUrl)
            .willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "text/turtle")
                .withBody(rdfResponseBody)));

        // stub for CSV
        final UrlPattern csvUrlPattern = WireMock.urlPathEqualTo(CSV_PATH);
        final String csvResponseBody = loadFile(CSV_PATH);
        WIRE_MOCK_SERVER.stubFor(WireMock.get(csvUrlPattern).willReturn(WireMock.aResponse().withBody(csvResponseBody)));
    }

    private static String loadFile(final String path) {
        return new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(MetafixLookupTest.class.getResourceAsStream("." + path)))).lines().collect(Collectors.joining("\n"));
    }

    @AfterAll
    private static void tearDownWireMock() {
        WIRE_MOCK_SERVER.stop();
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
    public void shouldNotLookupCopiedInternalArrayWithAsterisk() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected Array or Hash, got String", () ->
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
                }
            )
        );
    }

    @Test
    public void shouldLookupCopiedInternalArrayWithAsteriskExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('data', 'Aloha')",
                "set_array('title')",
                "copy_field('data', 'title.$append')",
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
    public void shouldNotLookupCopiedDeduplicatedInternalArrayWithAsterisk() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected Array or Hash, got String", () ->
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
                }
            )
        );
    }

    @Test
    public void shouldLookupCopiedDeduplicatedInternalArrayWithAsteriskExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('data', 'Aloha', 'Aloha')",
                "uniq('data')",
                "set_array('title')",
                "copy_field('data', 'title.$append')",
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
    public void shouldLookupCopiedExternalArrayWithAsteriskExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('title')",
                "copy_field('data', 'title.$append')",
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
    public void shouldLookupCopiedDeduplicatedExternalArrayWithAsteriskExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('data')",
                "set_array('title')",
                "copy_field('data', 'title.$append')",
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
    public void shouldNotLookupMovedDeduplicatedExternalArrayWithAsterisk() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected Array or Hash, got String", () ->
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
                }
            )
        );
    }

    @Test
    public void shouldLookupMovedDeduplicatedExternalArrayWithAsteriskExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('data')",
                "set_array('title')",
                "move_field('data', 'title.$append')",
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
    public void shouldNotLookupMovedExternalArrayWithAsterisk() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected Array or Hash, got String", () ->
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
                }
            )
        );
    }

    @Test
    public void shouldLookupMovedExternalArrayWithAsteriskExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('title')",
                "move_field('data', 'title.$append')",
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
    public void csvViaUrl() {
        final String baseUrl = WIRE_MOCK_SERVER.baseUrl();
        final String mockedCsvUrl = String.format(CSV_URL, baseUrl);
        assertMap(
            LOOKUP + " '" + mockedCsvUrl + "')"
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

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldLookupInNestedArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_map('rswk-indicator', s: 'SubjectHeading')",
                "lookup('subject[].*.componentList[].*.type[].*', 'rswk-indicator')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("subject[]");
                i.startEntity("1");
                i.startEntity("componentList[]");
                i.startEntity("1");
                i.startEntity("type[]");
                i.literal("1", "s");
                i.endEntity();
                i.endEntity();
                i.startEntity("2");
                i.startEntity("type[]");
                i.literal("1", "s");
                i.endEntity();
                i.endEntity();
                i.startEntity("3");
                i.startEntity("type[]");
                i.literal("1", "s");
                i.endEntity();
                i.endEntity();
                i.startEntity("4");
                i.startEntity("type[]");
                i.literal("1", "s");
                i.endEntity();
                i.endEntity();
                i.startEntity("5");
                i.startEntity("type[]");
                i.literal("1", "s");
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().startEntity("componentList[]");
                o.get().startEntity("1");
                o.get().startEntity("type[]");
                o.get().literal("1", "SubjectHeading");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().startEntity("type[]");
                o.get().literal("1", "SubjectHeading");
                f.apply(2).endEntity();
                o.get().startEntity("3");
                o.get().startEntity("type[]");
                o.get().literal("1", "SubjectHeading");
                f.apply(2).endEntity();
                o.get().startEntity("4");
                o.get().startEntity("type[]");
                o.get().literal("1", "SubjectHeading");
                f.apply(2).endEntity();
                o.get().startEntity("5");
                o.get().startEntity("type[]");
                o.get().literal("1", "SubjectHeading");
                f.apply(5).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupInCopiedNestedArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_map('rswk-indicator', s: 'SubjectHeading')",
                "set_array('subject[]')",
                "set_array('subject[].$append.componentList[]')",
                "set_array('subject[].$last.componentList[].$append.type[]')",
                "do list(path: 'D', 'var': '$i')",
                "  copy_field('$i', 'subject[].$last.componentList[].$last.type[].$append')",
                "end",
                "lookup('subject[].*.componentList[].*.type[].*', 'rswk-indicator')",
                "retain('subject[]')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("D", "s");
                i.literal("D", "s");
                i.literal("D", "s");
                i.literal("D", "s");
                i.literal("D", "s");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().startEntity("componentList[]");
                o.get().startEntity("1");
                o.get().startEntity("type[]");
                o.get().literal("1", "SubjectHeading");
                o.get().literal("2", "SubjectHeading");
                o.get().literal("3", "SubjectHeading");
                o.get().literal("4", "SubjectHeading");
                o.get().literal("5", "SubjectHeading");
                f.apply(5).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFailLookupInUnknownExternalMap() {
        MetafixTestHelpers.assertProcessException(MorphExecutionException.class, s -> s.replaceAll(": /.+?/testMap\\.csv", ": FILE"), "File not found: FILE", () ->
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

    private void shouldPrintUnknown(final String args, final String defaultValue, final String expected) {
        MetafixTestHelpers.assertStdout(expected, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    LOOKUP + " Aloha: Alohaeha, 'Moin': 'Moin zäme'" + args + ", print_unknown: 'true')"
                ),
                i -> {
                    i.startRecord("rec1");
                    i.literal("name", "moe");
                    i.literal("title", "Aloha");
                    i.literal("title", "Moin");
                    i.literal("title", "Hey");
                    i.literal("title", "you");
                    i.literal("title", "there");
                    i.endRecord();

                    i.startRecord("rec2");
                    i.literal("name", "joe");
                    i.literal("title", "Aloha");
                    i.literal("title", "you");
                    i.literal("title", "too");
                    i.endRecord();
                },
                (o, f) -> {
                    final boolean delete = "__delete".equals(defaultValue);

                    o.get().startRecord("rec1");
                    o.get().literal("name", "moe");
                    o.get().literal("title", "Alohaeha");
                    o.get().literal("title", "Moin zäme");

                    if (defaultValue == null) {
                        o.get().literal("title", "Hey");
                        o.get().literal("title", "you");
                        o.get().literal("title", "there");
                    }
                    else if (!delete) {
                        f.apply(3).literal("title", defaultValue);
                    }

                    o.get().endRecord();

                    o.get().startRecord("rec2");
                    o.get().literal("name", "joe");
                    o.get().literal("title", "Alohaeha");

                    if (defaultValue == null) {
                        o.get().literal("title", "you");
                        o.get().literal("title", "too");
                    }
                    else if (!delete) {
                        f.apply(2).literal("title", defaultValue);
                    }

                    o.get().endRecord();
                }
            )
        );
    }

    @Test
    public void shouldPrintUnknown() {
        shouldPrintUnknown("", null, "Hey\nyou\nthere\nyou\ntoo\n");
    }

    @Test
    public void shouldPrintUnknownWithDefault() {
        shouldPrintUnknown(", __default: Tach", "Tach", "Hey\nyou\nthere\nyou\ntoo\n");
    }

    @Test
    public void shouldPrintUnknownWithDelete() {
        shouldPrintUnknown(", delete: 'true'", "__delete", "Hey\nyou\nthere\nyou\ntoo\n");
    }

    @Test
    public void shouldPrintUnknownWithPrefix() {
        shouldPrintUnknown(", prefix: '<%d:%s>'", null, "<1:rec1>Hey\n<1:rec1>you\n<1:rec1>there\n<2:rec2>you\n<2:rec2>too\n");
    }

    @Test
    public void shouldPrintUnknownWithPrefixAndIdField() {
        shouldPrintUnknown(", prefix: '<%d:%s>', id: 'name'", null, "<1:moe>Hey\n<1:moe>you\n<1:moe>there\n<2:joe>you\n<2:joe>too\n");
    }

    @Test
    public void shouldPrintUnknownWithHeader() {
        shouldPrintUnknown(", header: '<%d:%s>'", null, "<%d:%s>Hey\nyou\nthere\n<%d:%s>you\ntoo\n");
    }

    @Test
    public void shouldPrintUnknownWithFooter() {
        shouldPrintUnknown(", footer: '<%d:%s>'", null, "Hey\nyou\nthere<%d:%s>you\ntoo<%d:%s>");
    }

    @Test
    public void shouldPrintUnknownWithSeparator() {
        shouldPrintUnknown(", separator: '<%d:%s>'", null, "Hey<%d:%s>you<%d:%s>there\nyou<%d:%s>too\n");
    }

    @Test
    public void shouldPrintUnknownToFile() throws IOException {
        MetafixTestHelpers.assertTempFile("Hey\nyou\nthere\nyou\ntoo\n", p -> shouldPrintUnknown(", destination: '" + p + "'", null, ""));
    }

    @Test
    public void shouldPrintUnknownToFileWithoutAppend() throws IOException {
        MetafixTestHelpers.assertTempFile("you\ntoo\n", p -> shouldPrintUnknown(", destination: '" + p + "', append: 'false'", null, ""));
    }

    @Test
    public void shouldLookupInSeparateExternalRdfFileMapWithName() {
        assertRdfMap(
            "put_rdfmap('" + RDF_MAP + "', 'testMapSkosNotation', target: 'skos:notation')",
            "lookup('notation', 'testMapSkosNotation')"
        );
    }

    @Test
    public void shouldLookupInSeparateExternalRdfFileMapWithDifferentTargets() {
        assertRdfMapWithDifferentTargets(
            "put_rdfmap('" + RDF_MAP + "', 'testRdfMapSkosNotation', target: 'skos:notation')",
            "put_rdfmap('" + RDF_MAP + "', 'testRdfMapCreated', target: 'created', __default: '__default')",
            "lookup('notation', 'testRdfMapSkosNotation')",
            "lookup('created', 'testRdfMapCreated')"
        );
    }

    @Test
    public void shouldLookupInExternalRdfUseDefinedDefaultValueIfNotFound() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + RDF_MAP + "', 'rdfmap', target: 'created', __default: '0000-01-01')",
                "lookup('created', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("created", "https://w3id.org/kim/hochschulfaechersystematik/n4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("created", "0000-01-01");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupInExternalRdfUseDefaultValueIfNotFound() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + RDF_MAP + "', 'rdfmap', target: 'created', __default: '__default')",
                "lookup('created', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("created", "https://w3id.org/kim/hochschulfaechersystematik/n4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("created", "__default");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldLookupInExternalRdfMapGetObjectOfSubjectWithTargetedPredicate() {
        assertRdfMap(
            "put_rdfmap('" + RDF_MAP + "', 'rdfmap', target: 'skos:notation')",
            "lookup('notation', 'rdfmap')"
        );
    }

    @Test
    public void shouldExplicitLookupRdfUrlWithRedirection() {
        final String baseUrl = WIRE_MOCK_SERVER.baseUrl();
        final String mockedRdfUrl = String.format(RDF_URL, baseUrl);

        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + mockedRdfUrl + "', 'testMapSkosNotation', target: 'skos:prefLabel')",
                "lookup('prefLabel', 'testMapSkosNotation')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("prefLabel", "http://purl.org/lobid/rpb#n882022");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("prefLabel", "Presserecht");
                o.get().endRecord();
            }
        );
    }

    @Test // Scenario 1
    public void shouldLookupInExternalRdfMapGetObjectOfSubjectWithTargetedPredicateOfSpecificLanguage() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('prefLabel', 'https://w3id.org/kim/hochschulfaechersystematik/n4')",
                "put_rdfmap('" + RDF_MAP + "', 'rdfmap', target: 'skos:prefLabel', select_language: 'de')",
                "lookup('prefLabel.*', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("prefLabel", "Mathematik, Naturwissenschaften");
                o.get().endRecord();
            }
        );
    }

    @Test // Scenario 2
    public void shouldLookupInExternalRdfMapGetSubjectWithTargetedPredicateOfSpecificLanguage() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('id', 'Mathematics, Natural Sciences')",
                "put_rdfmap('" + RDF_MAP + "', 'rdfmap', target: 'skos:prefLabel', select_language: 'en')",
                "lookup('id.*', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("prefLabel", "Mathematics, Natural Science");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("prefLabel", "Mathematics, Natural Science");
                o.get().literal("id", "https://w3id.org/kim/hochschulfaechersystematik/n4");
                o.get().endRecord();
            }
        );
    }

    @Test // Scenario lookupRdfPropertyToProperty
    public void shouldLookupInExternalRdfMapGetPropertyOfSpecificLanguageWithTargetedPredicate() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + HCRT_RDF_MAP + "', 'rdfmap', target: 'skos:prefLabel', select_language: 'en')",
                "lookup('a', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("a", "Softwareanwendung");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("a", "Software Application");
                o.get().endRecord();
            }
        );
    }

    @Test // Scenario lookupRdfPropertyToSubject
    public void shouldLookupInExternalRdfMapGetSubjectOfPropertyWithTargetedPredicate() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + HCRT_RDF_MAP + "', 'rdfmap', target: 'skos:prefLabel')",
                "lookup('a', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("a", "Softwareanwendung");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("a", "https://w3id.org/kim/hcrt/application");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void lookupRdfDefinedPropertyToSubjectNonDefault() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + HCRT_RDF_MAP + "', 'rdfmap', target: 'skos:prefLabel', select_language: 'de')",
                "lookup('a', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Jake");
                i.literal("a", "Softwareanwendung");
                i.endRecord();
                i.startRecord("2");
                i.literal("name", "Noone");
                i.literal("a", "cat");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Jake");
                o.get().literal("a", "https://w3id.org/kim/hcrt/application");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("name", "Noone");
                o.get().literal("a", "cat");
                o.get().endRecord();
            }
        );
    }

    @Test // Scenario 3
    public void shouldLookupInExternalRdfMapGetObjectWithTargetedPredicateOfSpecificLanguageUsingNamespace() {
        shouldLookupInExternalRdfMapGetObjectWithTargetedPredicateOfSpecificLanguage("skos:prefLabel");
    }

    @Test // Scenario 3 without namespace
    public void shouldLookupInExternalRdfMapGetObjectWithTargetedPredicateOfSpecificLanguageWithoutNamespace() {
        shouldLookupInExternalRdfMapGetObjectWithTargetedPredicateOfSpecificLanguage("http://www.w3.org/2004/02/skos/core#prefLabel");
    }

    @Test
    public void shouldLookupRdfDefinedPropertyToSubject() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_rdfmap('" + HCRT_RDF_MAP + "', 'rdfmap', target: 'skos:prefLabel', select_language: 'de', select: 'subject')",
                "lookup('a', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Jake");
                i.literal("a", "Softwareanwendung");
                i.endRecord();
                i.startRecord("2");
                i.literal("name", "Blacky");
                i.literal("a", "Nachschlagewerk");
                i.endRecord();
                i.startRecord("3");
                i.literal("name", "Noone");
                i.literal("a", "cat");
                i.endRecord();
                i.startRecord("4");
                i.literal("name", "Noone_2");
                i.literal("a", "Assessment");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Jake");
                o.get().literal("a", "https://w3id.org/kim/hcrt/application");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("name", "Blacky");
                o.get().literal("a", "https://w3id.org/kim/hcrt/index");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("name", "Noone");
                o.get().literal("a", "cat");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().literal("name", "Noone_2");
                o.get().literal("a", "Assessment");
                o.get().endRecord();
            }
        );
    }

    private void shouldLookupInExternalRdfMapGetObjectWithTargetedPredicateOfSpecificLanguage(final String target) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('prefLabel', 'Mathematics, Natural Sciences')",
                "put_rdfmap('" + RDF_MAP + "', 'rdfmap', target: '" + target + "', select_language: 'de')",
                "lookup('prefLabel.*', 'rdfmap')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("prefLabel", "Mathematik, Naturwissenschaften");
                o.get().endRecord();
            }
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

    private void assertRdfMap(final String... fixDef) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(fixDef),
            i -> {
                i.startRecord("1");
                i.literal("notation", "https://w3id.org/kim/hochschulfaechersystematik/n4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("notation", "4");
                o.get().endRecord();
            }
        );
    }

    private void assertRdfMapWithDifferentTargets(final String... fixDef) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(fixDef),
            i -> {
                i.startRecord("1");
                i.literal("notation", "https://w3id.org/kim/hochschulfaechersystematik/n4");
                i.literal("created", "https://w3id.org/kim/hochschulfaechersystematik/n4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("notation", "4");
                o.get().literal("created", "__default");
                o.get().endRecord();
            }
        );
    }

}
