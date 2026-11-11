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
 * Tests for class {@link Any}.
 *
 * @author Christoph Böhme
 */
public final class AnyTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public AnyTest() {
    }

    @Test
    public void shouldFireOnlyIfAtLeastOneElementFires() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <any>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </any>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data2", "C");
                    i.endRecord();
                    i.startRecord("3");
                    i.literal("data3", "C");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("", "true");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("", "true");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportUserdefinedNameAndValue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <any name='ANY' value='found one'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </any>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("ANY", "found one");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFireAgainIfAdditionalValueIsReceivedAndResetIsFalse() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <any>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </any>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.literal("data2", "C");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("", "true");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFireAgainIfAdditionalValueIsReceivedAndResetIsTrue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <any reset='true'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </any>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.literal("data2", "C");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(3).literal("", "true");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFireAgainAfterFlushing() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <any flushWith='entity'>" +
                "    <data source='entity.data1' />" +
                "    <data source='entity.data2' />" +
                "  </any>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data2", "B");
                    i.endEntity();
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(2).literal("", "true");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFireIfFlushingAnUntriggeredCollection() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <any flushWith='entity'>" +
                "    <data source='entity.data1' />" +
                "    <data source='entity.data2' />" +
                "  </any>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data3", "A");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                }
        );
    }

}
