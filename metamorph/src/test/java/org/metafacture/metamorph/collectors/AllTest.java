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
 * Tests for class {@link All}.
 *
 * @author Christoph Böhme
 */
public final class AllTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldFireOnlyIfAllElementsFire() {
        assertMorph(receiver,
                "<rules>" +
                "  <all>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </all>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data2", "C");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("", "true");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportUserdefinedNameAndValue() {
        assertMorph(receiver,
                "<rules>" +
                "  <all name='ALL' value='found all'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </all>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("ALL", "found all");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFireAgainIfValueChangesAndResetIsFalse() {
        assertMorph(receiver,
                "<rules>" +
                "  <all>" +
                "    <data source='entity.data1' />" +
                "    <data source='entity.data2' />" +
                "  </all>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data2", "C");
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
    public void shouldNotFireAgainIfOnlyOneValueChangesAndResetIsTrue() {
        assertMorph(receiver,
                "<rules>" +
                "  <all reset='true'>" +
                "    <data source='entity.data1' />" +
                "    <data source='entity.data2' />" +
                "  </all>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.literal("data2", "B");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data2", "B");
                    i.endEntity();
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
    public void shouldNotFireIfFlushingAnIncompleteCollection() {
        assertMorph(receiver,
                "<rules>" +
                "  <all flushWith='entity'>" +
                "    <data source='entity.data1' />" +
                "    <data source='entity.data2' />" +
                "  </all>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldResetWhenEntityChangesIfSameEntity() {
        assertMorph(receiver,
                "<rules>" +
                "  <all sameEntity='true'>" +
                "    <data source='entity.data2' />" +
                "    <data source='entity.data1' />" +
                "  </all>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data2", "A");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "A");
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
