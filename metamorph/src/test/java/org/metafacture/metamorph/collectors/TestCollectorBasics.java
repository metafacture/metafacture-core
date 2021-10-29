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

package org.metafacture.metamorph.collectors;

import static org.metafacture.metamorph.TestHelpers.assertMorph;

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.Metamorph;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests the basic functionality of Metamorph collectors.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TestCollectorBasics {

    // TODO: Can this be changed into a JUnit test for AbstractCollect?

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldSupportNestedCollectors() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='d' value='${1}${c}'>" +
                "    <data source='d1' name='1' />" +
                "    <combine name='c' value='${2}${3}'>" +
                "      <data source='d2' name='2' />" +
                "      <data source='d3' name='3' />" +
                "      <postprocess>" +
                "        <trim />" +
                "      </postprocess>" +
                "    </combine>" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("d1", "a");
                    i.literal("d2", "b");
                    i.literal("d3", "c ");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("d", "abc");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportNestedSameEntity() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='result' value='${value}${ch}' sameEntity='true'>" +
                "    <data source='rel.value' name='value' />" +
                "    <choose name='ch' flushWith='rel'>" +
                "      <data source='rel.ch' />" +
                "      <data source='rel'>" +
                "        <constant value='M' />" +
                "      </data>" +
                "    </choose>" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("rel");
                    i.literal("ch", "b");
                    i.literal("value", "a");
                    i.endEntity();
                    i.startEntity("rel");
                    i.literal("value", "B");
                    i.endEntity();
                    i.startEntity("rel");
                    i.literal("ch", "e");
                    i.literal("value", "d");
                    i.endEntity();
                    i.startEntity("rel");
                    i.literal("ch", "X");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("result", "ab");
                    o.get().literal("result", "BM");
                    o.get().literal("result", "de");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldAllowUsingAnArbitraryLiteralForFlush() {
        assertMorph(receiver,
                "<rules>" +
                "  <concat delimiter='' name='d' flushWith='f'>" +
                "    <data source='d' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("d", "1");
                    i.literal("d", "2");
                    i.literal("f", "");
                    i.literal("d", "3");
                    i.literal("d", "4");
                    i.literal("d", "5");
                    i.literal("f", "");
                    i.literal("d", "6");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("d", "12");
                    o.get().literal("d", "345");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldReceiveFlushingLiteralBeforeFlushEvent() {
        assertMorph(receiver,
                "<rules>" +
                "  <concat delimiter='' name='d' flushWith='f'>" +
                "    <data source='d' />" +
                "    <data source='f' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("f", "1");
                    i.literal("f", "2");
                    i.literal("d", "a");
                    i.literal("f", "3");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("d", "1");
                    o.get().literal("d", "2");
                    o.get().literal("d", "a3");
                    o.get().endRecord();
                }
        );
    }

}
