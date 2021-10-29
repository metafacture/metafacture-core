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
 * Tests the {@code <if>} statement in collectors.
 *
 * @author Christoph Böhme
 */
public final class TestCollectorIf {

    // TODO: Can this be changed into a JUnit test for AbstractCollect?

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldOnlyFireIfConditionIsMet() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combined' value='${data1}-${data2}'>" +
                "    <if>" +
                "      <data source='data3' />" +
                "    </if>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "b");
                    i.literal("data3", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data1", "a");
                    i.literal("data2", "b");
                    i.literal("data4", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combined", "a-b");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldAllowToUseSameSourceInbodyAndCondition() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combined' value='${data1}-${data2}'>" +
                "    <if>" +
                "      <data source='data2'>" +
                "        <equals string='b' />" +
                "      </data>" +
                "    </if>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "b");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data1", "a");
                    i.literal("data2", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combined", "a-b");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldAllowQuantorsInIfStatements() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combined' value='${data1}-${data2}'>" +
                "    <if>" +
                "      <any>" +
                "        <data source='data3' />" +
                "        <data source='data4' />" +
                "      </any>" +
                "    </if>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "b");
                    i.literal("data3", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data1", "a");
                    i.literal("data2", "d");
                    i.literal("data4", "c");
                    i.endRecord();
                    i.startRecord("3");
                    i.literal("data1", "a");
                    i.literal("data2", "b");
                    i.literal("data5", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combined", "a-b");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("combined", "a-d");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldResetConditionWithCollector() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='result' value='${VAL}' reset='true'>" +
                "    <if>" +
                "      <data source='entity.data2' />" +
                "    </if>" +
                "    <data source='entity.data1' name='VAL' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "output");
                    i.literal("data2", "X");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "no-output");
                    i.literal("data3", "X");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("result", "output");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldResetConditionWithCollectorOnFlushWith() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='result' value='${VAL1}${VAL2}' reset='true' flushWith='entity'>" +
                "    <if>" +
                "      <data source='entity.data2' />" +
                "    </if>" +
                "    <data source='entity.data1' name='VAL1' />" +
                "    <data source='entity.data4' name='VAL2' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "output");
                    i.literal("data2", "X");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "no-output");
                    i.literal("data3", "X");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("result", "output");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldResetConditionWithCollectorOnSameEntity() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='result' value='${VAL1}+${VAL2}' sameEntity='true'>" +
                "    <if>" +
                "      <data source='entity.data2' />" +
                "    </if>" +
                "    <data source='entity.data1' name='VAL1' />" +
                "    <data source='entity.data4' name='VAL2' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "output");
                    i.literal("data2", "X");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "no-output");
                    i.literal("data3", "X");
                    i.literal("data4", "extra-output");
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
    public void shouldResetOnFlushWithIfConditionWasNotMet() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='result' value='${V1}${V2}' flushWith='entity' reset='true'>" +
                "    <if>" +
                "      <data source='entity.condition'>" +
                "        <equals string='true' />" +
                "      </data>" +
                "    </if>" +
                "    <data source='entity.literal1' name='V1' />" +
                "    <data source='entity.literal2' name='V2' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("condition", "false");
                    i.literal("literal1", "value1");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("condition", "true");
                    i.literal("literal2", "value2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("result", "value2");
                    o.get().endRecord();
                }
        );
    }

}
