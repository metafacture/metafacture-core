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

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.TestHelpers;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for looking up values in arbitrary {@link java.util.Map}
 * implementations.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class JavaMapTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public JavaMapTest() {
    }

    @Test
    public void shouldLookupValuesInJavaBackedMap() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='1'>" +
                "    <lookup in='map1' />" +
                "  </data>" +
                "</rules>" +
                "<maps>" +
                "  <javamap name='map1' class='org.metafacture.metamorph.maps.FileMap' files='org/metafacture/metamorph/maps/java-map-test.txt' />" +
                "</maps>",
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

}
