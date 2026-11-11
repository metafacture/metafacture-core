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

package org.metafacture.metamorph.functions;

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.TestHelpers;
import org.metafacture.metamorph.api.Maps;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Lookup}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (conversion of metamorph-test xml to java)
 */

public final class LookupTest {

    private static final String MAP_NAME = "Authors";
    private static final String MAP_NAME_WRONG = "Directors";
    private static final String KEY = "Franz";
    private static final String KEY_WRONG = "Josef";
    private static final String VALUE = "Kafka";

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Maps maps;

    @Mock
    private StreamReceiver receiver;

    public LookupTest() {
    }

    @Test
    public void shouldReturnNullIfMapNameDoesNotExist() {
        final Lookup lookup = new Lookup();
        lookup.setMaps(maps);

        lookup.setIn(MAP_NAME_WRONG);

        Assert.assertNull(lookup.process(KEY));
    }

    @Test
    public void shouldReturnValueIfMapAndKeyExist() {
        final Lookup lookup = new Lookup();
        lookup.setMaps(maps);

        lookup.setIn(MAP_NAME);
        Mockito.when(maps.getValue(MAP_NAME, KEY)).thenReturn(VALUE);

        Assert.assertEquals(VALUE, lookup.process(KEY));
    }

    @Test
    public void shouldReturnNullIfKeyDoesNotExist() {
        final Lookup lookup = new Lookup();
        lookup.setMaps(maps);

        lookup.setIn(MAP_NAME);

        Assert.assertNull(lookup.process(KEY_WRONG));
    }

    @Test
    public void shouldLookupValuesInLocalMap() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='1'>" +
                "    <lookup>" +
                "      <entry name='a' value='A' />" +
                "    </lookup>" +
                "  </data>" +
                "  <data source='2'>" +
                "    <lookup default='B'>" +
                "      <entry name='a' value='A' />" +
                "    </lookup>" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("1", "a");
                    i.literal("1", "b");
                    i.literal("2", "a");
                    i.literal("2", "b");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "A");
                    o.get().literal("2", "A");
                    o.get().literal("2", "B");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldLookupValuesInReferencedMap() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='1'>" +
                "    <lookup in='map1' />" +
                "  </data>" +
                "  <data source='2'>" +
                "    <lookup in='map2' />" +
                "  </data>" +
                "</rules>" +
                "<maps>" +
                "  <map name='map1'>" +
                "    <entry name='a' value='A' />" +
                "  </map>" +
                "  <map name='map2' default='B'>" +
                "    <entry name='a' value='A' />" +
                "  </map>" +
                "</maps>",
                i -> {
                    i.startRecord("1");
                    i.literal("1", "a");
                    i.literal("1", "b");
                    i.literal("2", "a");
                    i.literal("2", "b");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("1", "A");
                    o.get().literal("2", "A");
                    o.get().literal("2", "B");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldLookupValuesInMetadata() {
        TestHelpers.assertMorph(receiver,
                "<meta>" +
                "  <name>Hawaii</name>" +
                "</meta>" +
                "<rules>" +
                "  <data source='data'>" +
                "    <lookup in='__meta' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "name");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "Hawaii");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue372_shouldFilterMissingValue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='litA'>" +
                "    <lookup>" +
                "      <entry name='cat' value='mammal' />" +
                "      <entry name='dog' value='mammal' />" +
                "    </lookup>" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("litA", "cat");
                    i.literal("litA", "dog");
                    i.literal("litA", "dragon");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(2).literal("litA", "mammal");
                    o.get().endRecord();
                }
        );
    }

}
