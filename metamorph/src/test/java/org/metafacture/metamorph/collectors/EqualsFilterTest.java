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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link EqualsFilter}.
 *
 * @author Thomas Haidlas (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class EqualsFilterTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public EqualsFilterTest() {
    }

    @Test
    public void shouldEmitValueIfAllReceivedValuesAreEqual() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='data1' name='one' />" +
                "    <data source='data2' name='two' />" +
                "    <data source='data3' name='three' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.literal("data3", "a");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitNothingIfReceivedValuesDiffer() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='data1' name='one' />" +
                "    <data source='data2' name='two' />" +
                "    <data source='data3' name='three' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.literal("data3", "b");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(0).literal(ArgumentMatchers.eq("equalsFiltered"), ArgumentMatchers.any());
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFireIfOnlyASingleValueIsReceived() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='data1' name='one' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldIgnoreLiteralsNotListedInStatements() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='data1' name='one' />" +
                "    <data source='data2' name='two' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.literal("data3", "b");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFireIfValuesInEntityAreEqual() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='field.data1' name='one' />" +
                "    <data source='field.data2' name='two' />" +
                "    <data source='field.data3' name='three' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("field");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.literal("data3", "a");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFireIfValuesInEntityAreNotEqual() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='field.data1' name='one' />" +
                "    <data source='field.data2' name='two' />" +
                "    <data source='field.data3' name='three' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("field");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.literal("data3", "b");
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
    public void shouldFireIfLiteralsInEntitiesAreReceivedThatAreNotListedInStatements() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}'>" +
                "    <data source='field1.data1' name='one' />" +
                "    <data source='field1.data2' name='two' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("field1");
                    i.literal("data1", "a");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("field1");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("3");
                    i.startEntity("field1");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFireOnFlush() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}' flushWith='field1.data2'>" +
                "    <data source='field1.data1' name='one' />" +
                "    <data source='field1.data2' name='two' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("field1");
                    i.literal("data1", "a");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("field1");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("3");
                    i.startEntity("field1");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("equalsFiltered", "");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFireOnFlushIfIncomplete() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <equalsFilter name='equalsFiltered' value='${one}' flushWith='field1.data2' flushIncomplete='false'>" +
                "    <data source='field1.data1' name='one' />" +
                "    <data source='field1.data2' name='two' />" +
                "  </equalsFilter>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("field1");
                    i.literal("data1", "a");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("field1");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("3");
                    i.startEntity("field1");
                    i.literal("data1", "a");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().literal("equalsFiltered", "a");
                    o.get().endRecord();
                }
        );
    }

}
