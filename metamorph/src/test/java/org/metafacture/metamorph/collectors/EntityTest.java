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
import org.metafacture.metamorph.Entity;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Entity}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class EntityTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldEmitEntities() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='entity'>" +
                "    <data source='data1' name='l1' />" +
                "    <data source='data2' name='l2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1x");
                    i.literal("data1", "a1");
                    i.literal("data1", "a2");
                    i.literal("data2", "b");
                    i.endRecord();
                    i.startRecord("2x");
                    i.literal("data2", "c");
                    i.literal("data1", "d");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1x");
                    o.get().startEntity("entity");
                    o.get().literal("l1", "a1");
                    o.get().literal("l1", "a2");
                    o.get().literal("l2", "b");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2x");
                    o.get().startEntity("entity");
                    o.get().literal("l2", "c");
                    o.get().literal("l1", "d");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitEntityOnFlushEvent() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='entity' flushWith='record'>" +
                "    <data source='d1' name='l1' />" +
                "    <data source='d2' name='l2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("d1", "a");
                    i.literal("d1", "b");
                    i.literal("d2", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("d2", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("entity");
                    o.get().literal("l1", "a");
                    o.get().literal("l1", "b");
                    o.get().literal("l2", "c");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().startEntity("entity");
                    o.get().literal("l2", "c");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotEmitEntityOnFlushEventIfIncomplete() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='entity' flushWith='record' flushIncomplete='false'>" +
                "    <data source='d1' name='l1' />" +
                "    <data source='d2' name='l2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("d1", "a");
                    i.literal("d1", "b");
                    i.literal("d2", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("d2", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("entity");
                    o.get().literal("l1", "a");
                    o.get().literal("l1", "b");
                    o.get().literal("l2", "c");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitEntityOnEachFlushEvent() {
        assertMorph(receiver,
                "<metamorph version='1' entityMarker='.'" +
                "    xmlns='http://www.culturegraph.org/metamorph'>" +
                "  <rules>" +
                "    <entity name='entity' flushWith='E' reset='true'>" +
                "      <data source='E.d1' name='l1' />" +
                "      <data source='E.d2' name='l2' />" +
                "    </entity>" +
                "  </rules>" +
                "</metamorph>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("E");
                    i.literal("d1", "a");
                    i.literal("d2", "b");
                    i.endEntity();
                    i.startEntity("E");
                    i.literal("d1", "c");
                    i.literal("d2", "d");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("entity");
                    o.get().literal("l1", "a");
                    o.get().literal("l2", "b");
                    o.get().endEntity();
                    o.get().startEntity("entity");
                    o.get().literal("l1", "c");
                    o.get().literal("l2", "d");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportNestedEntities() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='e1' flushWith='record'>" +
                "    <data source='d1' />" +
                "    <data source='d2' />" +
                "    <entity name='e2' flushWith='record'>" +
                "      <data source='d3' />" +
                "      <data source='d4' />" +
                "    </entity>" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("d1", "a");
                    i.literal("d2", "b");
                    i.literal("d3", "c");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("d1", "d");
                    i.literal("d2", "e");
                    i.literal("d3", "f");
                    i.endRecord();
                    i.startRecord("3");
                    i.literal("d1", "a");
                    i.literal("d3", "c");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    o.get().startEntity("e1");
                    o.get().literal("d1", "a");
                    o.get().literal("d2", "b");
                    o.get().startEntity("e2");
                    o.get().literal("d3", "c");
                    f.apply(2).endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().startEntity("e1");
                    o.get().literal("d1", "d");
                    o.get().literal("d2", "e");
                    o.get().startEntity("e2");
                    o.get().literal("d3", "f");
                    f.apply(2).endEntity();
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().startEntity("e1");
                    o.get().literal("d1", "a");
                    o.get().startEntity("e2");
                    o.get().literal("d3", "c");
                    f.apply(2).endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportMultipleNestedEntities() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='uber' flushWith='record'>" +
                "    <data source='d' name='l' />" +
                "    <entity name='unter' sameEntity='true'>" +
                "      <data source='E.d1' name='l' />" +
                "      <data source='E.d2' name='l' />" +
                "    </entity>" +
                "    <entity name='void' sameEntity='true'>" +
                "      <data source='nothing' />" +
                "    </entity>" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1x");
                    i.startEntity("E");
                    i.literal("d1", "a");
                    i.literal("d2", "b");
                    i.endEntity();
                    i.startEntity("E");
                    i.literal("d1", "x");
                    i.endEntity();
                    i.startEntity("E");
                    i.literal("d1", "c");
                    i.literal("d2", "d");
                    i.endEntity();
                    i.literal("d", "c");
                    i.endRecord();
                    i.startRecord("2x");
                    i.literal("d", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1x");
                    o.get().startEntity("uber");
                    o.get().startEntity("unter");
                    o.get().literal("l", "a");
                    o.get().literal("l", "b");
                    o.get().endEntity();
                    o.get().startEntity("unter");
                    o.get().literal("l", "c");
                    o.get().literal("l", "d");
                    o.get().endEntity();
                    o.get().literal("l", "c");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2x");
                    o.get().startEntity("uber");
                    o.get().literal("l", "c");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportDeeplyNestedEnities() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='1'>" +
                "    <data source='1' />" +
                "    <entity name='2'>" +
                "      <data source='2' />" +
                "      <entity name='3'>" +
                "        <entity name='4'>" +
                "          <data source='4' />" +
                "        </entity>" +
                "      </entity>" +
                "    </entity>" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1x");
                    i.literal("1", "a");
                    i.literal("2", "b");
                    i.literal("4", "c");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1x");
                    o.get().startEntity("1");
                    o.get().literal("1", "a");
                    o.get().startEntity("2");
                    o.get().literal("2", "b");
                    o.get().startEntity("3");
                    o.get().startEntity("4");
                    o.get().literal("4", "c");
                    f.apply(4).endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldGetNameFromDataInEntityName() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity>" +
                "    <entity-name>" +
                "      <data source='data1'>" +
                "        <compose prefix='entity:' />" +
                "      </data>" +
                "    </entity-name>" +
                "    <data source='data1' name='l1' />" +
                "    <data source='data2' name='l2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1x");
                    i.literal("data1", "a1");
                    i.literal("data1", "a2");
                    i.literal("data2", "b");
                    i.endRecord();
                    i.startRecord("2x");
                    i.literal("data2", "c");
                    i.literal("data1", "d");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1x");
                    o.get().startEntity("entity:a2");
                    o.get().literal("l1", "a1");
                    o.get().literal("l1", "a2");
                    o.get().literal("l2", "b");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2x");
                    o.get().startEntity("entity:d");
                    o.get().literal("l2", "c");
                    o.get().literal("l1", "d");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldGetNameFromCollectInEntityName() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity>" +
                "    <entity-name>" +
                "      <combine name='' value='entity:${a},${b}'>" +
                "        <data source='data1' name='a' />" +
                "        <data source='data2' name='b' />" +
                "      </combine>" +
                "    </entity-name>" +
                "    <data source='data1' name='l1' />" +
                "    <data source='data2' name='l2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1x");
                    i.literal("data1", "a1");
                    i.literal("data1", "a2");
                    i.literal("data2", "b");
                    i.endRecord();
                    i.startRecord("2x");
                    i.literal("data2", "c");
                    i.literal("data1", "d");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1x");
                    o.get().startEntity("entity:a2,b");
                    o.get().literal("l1", "a1");
                    o.get().literal("l1", "a2");
                    o.get().literal("l2", "b");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2x");
                    o.get().startEntity("entity:d,c");
                    o.get().literal("l2", "c");
                    o.get().literal("l1", "d");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldResetNameToNameAttribute() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='defaultName' flushWith='record'>" +
                "    <entity-name>" +
                "      <data source='data3' />" +
                "    </entity-name>" +
                "    <data source='data1' name='l1' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1x");
                    i.literal("data1", "a");
                    i.literal("data3", "dynamicName");
                    i.endRecord();
                    i.startRecord("2x");
                    i.literal("data1", "b");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1x");
                    o.get().startEntity("dynamicName");
                    o.get().literal("l1", "a");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2x");
                    o.get().startEntity("defaultName");
                    o.get().literal("l1", "b");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitEmptyStringIfEntityNameIsNotSet() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity>" +
                "    <data source='in' name='out' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("in", "a");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("");
                    o.get().literal("out", "a");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitEntityContentsAgainIfResetIsFalse() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='entity'>" +
                "    <data source='lit1' />" +
                "    <data source='lit2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit1", "const");
                    i.literal("lit2", "1");
                    i.literal("lit2", "2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("entity");
                    o.get().literal("lit1", "const");
                    o.get().literal("lit2", "1");
                    o.get().endEntity();
                    o.get().startEntity("entity");
                    o.get().literal("lit1", "const");
                    o.get().literal("lit2", "1");
                    o.get().literal("lit2", "2");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotEmitEntityContentsAgainIfResetIsTrue() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='entity' reset='true'>" +
                "    <data source='lit1' />" +
                "    <data source='lit2' />" +
                "  </entity>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit1", "const");
                    i.literal("lit2", "1");
                    i.literal("lit2", "2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("entity");
                    o.get().literal("lit1", "const");
                    o.get().literal("lit2", "1");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

}
