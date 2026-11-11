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
 * Tests for class {@link None}.
 *
 * @author Christoph Böhme
 */
public final class NoneTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public NoneTest() {
    }

    @Test
    public void shouldFireOnlyifNoElementFired() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <none>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </none>" +
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
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().literal("", "true");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportUserdefinedNameAndValue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <none name='NONE' value='found none'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </none>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data3", "A");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("NONE", "found none");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFireAgainIfFlushedTwoTimesAndResetIsFalse() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <none flushWith='entity'>" +
                "    <data source='data2' />" +
                "  </none>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "A");
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
    public void shouldFireAgainIfFlushedTwoTimesAndTesetIsTrue() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <none flushWith='entity' reset='true'>" +
                "    <data source='data2' />" +
                "  </none>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "A");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "A");
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
    public void shouldResetWhenEntityChangesIfSameEntity() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <none sameEntity='true'>" +
                "    <data source='entity.data2' />" +
                "  </none>" +
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
                    o.get().literal("", "true");
                    o.get().endRecord();
                }
        );
    }

}
