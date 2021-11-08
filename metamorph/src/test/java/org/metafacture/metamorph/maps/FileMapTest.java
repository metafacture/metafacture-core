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

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

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

    private static String MORPH =
        "<rules>" +
        "  <data source='1'>" +
        "    <%s='map1' />" +
        "  </data>" +
        "</rules>" +
        "<maps>" +
        "  <filemap name='map1' files='org/metafacture/metamorph/maps/" +
        "file-map-test.txt' />" +
        "</maps>";

    @Test
    public void shouldLookupValuesInFileBasedMap() {
        assertMorph(receiver, String.format(MORPH, "lookup in"),
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
        assertMorph(receiver, String.format(MORPH, "whitelist map"),
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
        assertMorph(receiver, String.format(MORPH, "setreplace map"),
                i -> {
                    i.startRecord("1");
                    i.literal("1", "gw-fj: 1:2");
                    i.literal("1", "fj-gw: 4:0");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "Germany-Fiji: 1:2");
                    o.get().literal("1", "Fiji-Germany: 4:0");
                    o.get().endRecord();
                }
        );
    }

}
