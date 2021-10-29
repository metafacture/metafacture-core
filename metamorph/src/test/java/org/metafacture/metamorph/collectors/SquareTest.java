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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Square}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class SquareTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldEmitSquaresOfInputValues() {
        assertMorph(receiver,
                "<rules>" +
                "  <square delimiter=',' name='square' prefix='{' postfix='}'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </square>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "b");
                    i.literal("data1", "a");
                    i.literal("data2", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data1", "1");
                    i.literal("data1", "2");
                    i.literal("data2", "3");
                    i.literal("data2", "4");
                    i.literal("data2", "5");
                    i.literal("data2", "6");
                    i.literal("data2", "7");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("square", "{a,c}");
                    o.get().literal("square", "{b,c}");
                    o.get().literal("square", "{a,b}");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("square", "{1,7}");
                    o.get().literal("square", "{2,7}");
                    o.get().literal("square", "{3,7}");
                    o.get().literal("square", "{4,7}");
                    o.get().literal("square", "{5,7}");
                    o.get().literal("square", "{6,7}");
                    o.get().literal("square", "{1,6}");
                    o.get().literal("square", "{2,6}");
                    o.get().literal("square", "{3,6}");
                    o.get().literal("square", "{4,6}");
                    o.get().literal("square", "{5,6}");
                    o.get().literal("square", "{1,5}");
                    o.get().literal("square", "{2,5}");
                    o.get().literal("square", "{3,5}");
                    o.get().literal("square", "{4,5}");
                    o.get().literal("square", "{1,4}");
                    o.get().literal("square", "{2,4}");
                    o.get().literal("square", "{3,4}");
                    o.get().literal("square", "{1,3}");
                    o.get().literal("square", "{2,3}");
                    o.get().literal("square", "{1,2}");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitSquaresOnFlushEvent() {
        assertMorph(receiver,
                "<rules>" +
                "  <square delimiter=',' name='square' prefix='{' postfix='}' flushWith='d'>" +
                "    <data source='d.1' />" +
                "  </square>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("d");
                    i.literal("1", "a");
                    i.literal("1", "b");
                    i.endEntity();
                    i.startEntity("d");
                    i.literal("1", "e");
                    i.literal("1", "f");
                    i.endEntity();
                    i.startEntity("d");
                    i.literal("2", "e");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("square", "{a,b}");
                    o.get().literal("square", "{e,f}");
                    o.get().endRecord();
                }
        );
    }

}
