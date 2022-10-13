/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.metamorph.maps;

import static org.metafacture.metamorph.TestHelpers.assertMorph;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.Consumer;

/**
 * Tests for class {@link FileMap}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class FileMapTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    private static String MAPS = "org/metafacture/metamorph/maps/";

    private static String MORPH =
        "<rules>" +
        "  <data source='1'>" +
        "    <%s='map1' />" +
        "  </data>" +
        "</rules>" +
        "<maps>" +
        "  <filemap name='map1' files='" + MAPS + "%s' %s/>" +
        "</maps>";

    @Test
    public void shouldLookupValuesInFileBasedMap() {
        assertMorph(receiver, buildMorph("lookup in", ""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw");
                    i.literal("1", "fj");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "Germany");
                    o.get().literal("1", "Fiji");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldWhitelistValuesInFileBasedMap() {
        assertMorph(receiver, buildMorph("whitelist map", ""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw");
                    i.literal("1", "fj");
                    i.literal("1", "bla");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "gw");
                    o.get().literal("1", "fj");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldReplaceValuesUsingFileBasedMap() {
        assertMorph(receiver, buildMorph("setreplace map", ""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw-fj: 1:1");
                    i.literal("1", "fj-gw: 0:0");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "Germany-Fiji: 1:1");
                    o.get().literal("1", "Fiji-Germany: 0:0");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldReplaceCommaSeparatedValuesUsingFileBasedMapSetting() {
        assertMorph(receiver, buildMorph("setreplace map", "separator=\",\""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw");
                    i.literal("1", "ry\tRyukyuIslands");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "gw");
                    o.get().literal("1", "Southern");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldReplaceEmptyValuesUsingFileBasedMapSetting() {
        assertMorph(receiver, buildMorph("setreplace map", "allowEmptyValues=\"true\""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "zz");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotReplaceEmptyValuesUsingFileBasedMapSetting() {
        assertMorph(receiver, buildMorph("setreplace map", ""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "zz");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "zz");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldLookupValuesInGzipFileMap() {
        assertMorph(receiver, buildMorph("lookup in", "file-map-test.txt.gz", ""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw");
                    i.literal("1", "fj");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "Germany");
                    o.get().literal("1", "Fiji");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotLookupValuesInBlockedGzipFileMapWithoutDecompressConcatenated() {
        assertMorph(receiver, buildMorph("lookup in", "file-map-test.txt.bgzf", ""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw");
                    i.literal("1", "fj");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldLookupValuesInBlockedGzipFileMap() {
        assertMorph(receiver, buildMorph("lookup in", "file-map-test.txt.bgzf", "decompressConcatenated=\"true\""),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw");
                    i.literal("1", "fj");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "Germany");
                    o.get().literal("1", "Fiji");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldLoadFile() {
        assertMap(379, i -> {
            Assert.assertEquals("Puerto Rico", i.get("pr"));
            Assert.assertNull(i.get("zz"));
        });
    }

    @Test
    public void shouldLoadFileWithEmptyValues() {
        assertMap(380, i -> {
            i.setAllowEmptyValues(true);

            Assert.assertEquals("Puerto Rico", i.get("pr"));
            Assert.assertEquals("", i.get("zz"));
        });
    }

    @Test
    public void shouldLoadFileWithSeparator() {
        assertMap(99, i -> {
            i.setSeparator(" ");

            Assert.assertNull(i.get("pp\tPapua"));
            Assert.assertEquals("Rico", i.get("pr\tPuerto"));
        });
    }

    @Test
    public void shouldLoadFileWithKeyColumn() {
        assertMap(21, i -> {
            i.setSeparator(" ");
            i.setKeyColumn(2);

            Assert.assertEquals("New", i.get("Guinea"));
        });
    }

    @Test
    public void shouldLoadFileWithValueColumn() {
        assertMap(24, i -> {
            i.setSeparator(" ");
            i.setValueColumn(2);

            Assert.assertEquals("Guinea", i.get("pp\tPapua"));
        });
    }

    @Test
    public void shouldLoadFileWithKeyAndValueColumn() {
        assertMap(66, i -> {
            i.setSeparator(" ");
            i.setKeyColumn(1);
            i.setValueColumn(0);

            Assert.assertEquals("pr\tPuerto", i.get("Rico"));
        });
    }

    @Test
    public void shouldLoadFileWithTrailingColumns() {
        assertMap(149, i -> {
            i.setSeparator(" ");
            i.setAllowTrailingColumns(true);

            Assert.assertEquals("New", i.get("pp\tPapua"));
        });
    }

    @Test
    public void shouldNotLoadFileWithOutOfRangeKeyColumn() {
        assertMap(0, i -> {
            i.setKeyColumn(2);
        });
    }

    @Test
    public void shouldNotLoadFileWithOutOfRangeValueColumn() {
        assertMap(0, i -> {
            i.setValueColumn(2);
        });
    }

    private void assertMap(final int size, final Consumer<FileMap> consumer) {
        final FileMap fileMap = new FileMap();
        fileMap.setFile(MAPS + "file-map-test-columns.txt");

        consumer.accept(fileMap);
        Assert.assertEquals(size, fileMap.keySet().size());
    }

    private String buildMorph(final String data, final String options) {
        return buildMorph(data, "file-map-test.txt", options);
    }

    private String buildMorph(final String data, final String map, final String options) {
        return String.format(MORPH, data, map, options);
    }

}
