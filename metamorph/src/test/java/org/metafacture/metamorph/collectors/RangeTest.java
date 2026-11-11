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
 * Tests for class {@link Range}.
 *
 * @author Christoph Böhme
 */
public final class RangeTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public RangeTest() {
    }

    @Test
    public void shouldOutputAllnNmbersbBetweenFirstAndLastInclusive() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <range name='range' flushWith='record'>" +
                "    <data source='first' />" +
                "    <data source='last' />" +
                "  </range>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("first", "1789");
                    i.literal("last", "1794");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("range", "1789");
                    o.get().literal("range", "1790");
                    o.get().literal("range", "1791");
                    o.get().literal("range", "1792");
                    o.get().literal("range", "1793");
                    o.get().literal("range", "1794");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldOutputFirstIfLastEqualsFirst() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <range name='range' flushWith='record'>" +
                "    <data source='first' />" +
                "    <data source='last' />" +
                "  </range>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("first", "1989");
                    i.literal("last", "1989");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("range", "1989");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldOutputMultipleRanges() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <range name='range' flushWith='record'>" +
                "    <data source='first' />" +
                "    <data source='last' />" +
                "  </range>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("first", "1789");
                    i.literal("last", "1792");
                    i.literal("first", "1794");
                    i.literal("last", "1799");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("range", "1789");
                    o.get().literal("range", "1790");
                    o.get().literal("range", "1791");
                    o.get().literal("range", "1792");
                    o.get().literal("range", "1794");
                    o.get().literal("range", "1795");
                    o.get().literal("range", "1796");
                    o.get().literal("range", "1797");
                    o.get().literal("range", "1798");
                    o.get().literal("range", "1799");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldRemoveDuplicateNumbersFromOverlappingRanges() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <range name='range' flushWith='record'>" +
                "    <data source='first' />" +
                "    <data source='last' />" +
                "  </range>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("first", "1789");
                    i.literal("last", "1792");
                    i.literal("first", "1790");
                    i.literal("last", "1791");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("range", "1789");
                    o.get().literal("range", "1790");
                    o.get().literal("range", "1791");
                    o.get().literal("range", "1792");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldUseUserdefinedIncrement() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <range name='range' increment='3' flushWith='record'>" +
                "    <data source='first' />" +
                "    <data source='last' />" +
                "  </range>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("first", "1789");
                    i.literal("last", "1799");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("range", "1789");
                    o.get().literal("range", "1792");
                    o.get().literal("range", "1795");
                    o.get().literal("range", "1798");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldAllowNegativeIncrements() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <range name='range' increment='-3' flushWith='record'>" +
                "    <data source='first' />" +
                "    <data source='last' />" +
                "  </range>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("first", "1799");
                    i.literal("last", "1789");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("range", "1799");
                    o.get().literal("range", "1796");
                    o.get().literal("range", "1793");
                    o.get().literal("range", "1790");
                    o.get().endRecord();
                }
        );
    }

}
