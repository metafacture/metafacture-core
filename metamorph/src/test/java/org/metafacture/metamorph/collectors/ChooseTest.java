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
 * Tests for class {@link Choose}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class ChooseTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public ChooseTest() {
    }

    @Test
    public void shouldChooseValueOfTopMostFiringStatement() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data1", "B");
                    i.literal("data2", "A");
                    i.endRecord();
                    i.startRecord("3");
                    i.literal("data2", "C");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data1", "A");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("data1", "B");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().literal("data2", "C");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldMakeChooseDecisionOnFlushEvent() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose flushWith='entity'>" +
                "    <data source='entity.data1' />" +
                "    <data source='entity.data2' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "B");
                    i.literal("data2", "A");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data2", "C");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("dataX", "X");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("entity.data1", "A");
                    o.get().literal("entity.data1", "B");
                    o.get().literal("entity.data2", "C");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue110_shouldOutputFallBackIfFlushedWithEntity() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose name='chosen' flushWith='record|entity'>" +
                "    <data source='entity.data1' />" +
                "    <data source='L' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("L", "V");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("chosen", "V");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue210_issue49_shouldRepeatedlyEmitNamedValueIfResetIsFalse() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose flushWith='flush' reset='false'>" +
                "    <data source='lit1' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit1", "data1");
                    i.literal("flush", "first");
                    i.literal("flush", "second");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(2).literal("lit1", "data1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue210_shouldResetAfterEmittingNamedValueIfResetIsTrue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose flushWith='flush' reset='true'>" +
                "    <data source='lit1' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit1", "data1");
                    i.literal("flush", "first");
                    i.literal("flush", "second");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(1).literal("lit1", "data1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue210_shouldResetAfterEmittingNamedValueByDefault() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose flushWith='flush'>" +
                "    <data source='lit1' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit1", "data1");
                    i.literal("flush", "first");
                    i.literal("flush", "second");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(1).literal("lit1", "data1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue250_shouldResetOnEntityChangeIfSameEntityIsTrue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose sameEntity='true'>" +
                "    <data source='entity.lit1' />" +
                "    <data source='entity.lit2' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("lit1", "data1");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("lit2", "data2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("entity.lit2", "data2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue250_shouldNotResetOnEntityChangeIfSameEntityIsFalse() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose sameEntity='false'>" +
                "    <data source='entity.lit1' />" +
                "    <data source='entity.lit2' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("lit1", "data1");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("lit2", "data2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("entity.lit1", "data1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue250_shouldNotResetOnEntityChangeByDefault() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose>" +
                "    <data source='entity.lit1' />" +
                "    <data source='entity.lit2' />" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("lit1", "data1");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("lit2", "data2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("entity.lit1", "data1");
                    o.get().endRecord();
                }
        );
    }

}
