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

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.TestHelpers;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Concat}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class ConcatTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public ConcatTest() {
    }

    @Test
    public void shouldConcatenateValues() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <concat delimiter=', ' name='concat' prefix='{' postfix='}'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data1", "b");
                    i.literal("data2", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data1", "d");
                    i.literal("data1", "e");
                    i.literal("data2", "f");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("concat", "{a, b, c}");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("concat", "{d, e, f}");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitConcatenatedValueOnFlushEvent() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <concat delimiter=', ' name='concat' prefix='{' postfix='}' flushWith='d' reset='true'>" +
                "    <data source='d.1' />" +
                "  </concat>" +
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
                    o.get().literal("concat", "{a, b}");
                    o.get().literal("concat", "{e, f}");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitEmptyValues() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <concat name='concat' delimiter=', '>" +
                "    <data source='litA' />" +
                "    <data source='litB' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("litA", "");
                    i.literal("litB", "a");
                    i.literal("litA", "b");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("concat", ", a, b");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldReverseConcatenationIfReverseIsTrue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <concat name='concat' delimiter=', ' reverse='true'>" +
                "    <data source='litA' />" +
                "    <data source='litB' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("litA", "1");
                    i.literal("litB", "2");
                    i.literal("litA", "3");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("concat", "3, 2, 1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void prefixAndPostfixShouldWorkAsNormalIfReverseIsTrue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <concat name='concat' delimiter=', ' prefix='(' postfix=')' reverse='true'>" +
                "    <data source='litA' />" +
                "    <data source='litB' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("litA", "1");
                    i.literal("litB", "2");
                    i.literal("litA", "3");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("concat", "(3, 2, 1)");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue187_shouldUseEmptyDelimiterAsDefault() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <concat name='concat'>" +
                "    <data source='lit' />" +
                "  </concat>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit", "data1");
                    i.literal("lit", "data2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("concat", "data1data2");
                    o.get().endRecord();
                }
        );
    }

}
