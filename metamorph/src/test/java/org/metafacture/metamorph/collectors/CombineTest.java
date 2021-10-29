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
 * Tests for class {@link Combine}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class CombineTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldCombineTwoValues() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combination' value='${one}${two}'>" +
                "    <data source='data2' name='one' />" +
                "    <data source='data1' name='two' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "a");
                    i.literal("data2", "b");
                    i.literal("data2", "c");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combination", "ba");
                    o.get().literal("combination", "ca");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldOnlyCombineValuesFromTheSameEntityIfSet() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combination' value='${A}${B}' sameEntity='true'>" +
                "    <data source='entity.data1' name='B' />" +
                "    <data source='entity.data2' name='A' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("data1", "b");
                    i.literal("data2", "a");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data2", "c");
                    i.literal("data2", "d");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data1", "e");
                    i.endEntity();
                    i.startEntity("entity");
                    i.literal("data2", "f");
                    i.literal("data1", "g");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combination", "ab");
                    o.get().literal("combination", "fg");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldResetCombinedValueIfResetIsTrue() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combination' value='${A}${B}' reset='true'>" +
                "    <data source='data1' name='B' />" +
                "    <data source='data2' name='A' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "b");
                    i.literal("data2", "a");
                    i.literal("data2", "c");
                    i.literal("data2", "d");
                    i.literal("data1", "e");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combination", "ab");
                    o.get().literal("combination", "de");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitCurrentValueOnFlushEvent() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combi' value='${one}${two}' flushWith='e' reset='true'>" +
                "    <data source='e.l' name='one' />" +
                "    <data source='e.m' name='two' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("e");
                    i.literal("l", "1");
                    i.endEntity();
                    i.startEntity("e");
                    i.literal("l", "2");
                    i.literal("m", "2");
                    i.endEntity();
                    i.startEntity("e");
                    i.literal("l", "3");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combi", "1");
                    o.get().literal("combi", "22");
                    o.get().literal("combi", "3");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotEmitCurrentValueOnFlushEventIfIncomplete() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='combi' value='${one}${two}' flushWith='e' flushIncomplete='false' reset='true'>" +
                "    <data source='e.l' name='one' />" +
                "    <data source='e.m' name='two' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("e");
                    i.literal("l", "1");
                    i.endEntity();
                    i.startEntity("e");
                    i.literal("l", "2");
                    i.literal("m", "2");
                    i.endEntity();
                    i.startEntity("e");
                    i.literal("l", "3");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("combi", "22");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldPostprocessCombinedValue() {
        assertMorph(receiver,
                "<rules>" +
                "  <combine name='outLit' value='${V}' flushWith='record'>" +
                "    <data name='V' source='inLit' />" +
                "    <postprocess>" +
                "      <case to='upper' />" +
                "    </postprocess>" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("inLit", "value");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("outLit", "VALUE");
                    o.get().endRecord();
                }
        );
    }

}
