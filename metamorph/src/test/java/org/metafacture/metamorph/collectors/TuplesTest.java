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
 * Tests for class {@link Tuples}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TuplesTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldEmitTwoAndThreeTuples() {
        assertMorph(receiver,
                "<rules>" +
                "  <tuples name='product'>" +
                "    <data source='1' />" +
                "    <data source='3' />" +
                "    <data source='2' />" +
                "  </tuples>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("1", "a");
                    i.literal("1", "b");
                    i.literal("2", "A");
                    i.literal("2", "B");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("3", "X");
                    i.literal("1", "c");
                    i.literal("1", "d");
                    i.literal("2", "C");
                    i.literal("3", "Y");
                    i.literal("2", "D");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("product", "aA");
                    o.get().literal("product", "bA");
                    o.get().literal("product", "aB");
                    o.get().literal("product", "bB");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("product", "cCX");
                    o.get().literal("product", "dCX");
                    o.get().literal("product", "cDX");
                    o.get().literal("product", "dDX");
                    o.get().literal("product", "cCY");
                    o.get().literal("product", "dCY");
                    o.get().literal("product", "cDY");
                    o.get().literal("product", "dDY");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldOnlyEmitTriplesWithMoreThanMinNValues() {
        assertMorph(receiver,
                "<rules>" +
                "  <tuples name='product' minN='3'>" +
                "    <data source='1' />" +
                "    <data source='3' />" +
                "    <data source='2' />" +
                "  </tuples>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("1", "a");
                    i.literal("1", "b");
                    i.literal("2", "A");
                    i.literal("2", "B");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("3", "X");
                    i.literal("1", "c");
                    i.literal("1", "d");
                    i.literal("2", "C");
                    i.literal("3", "Y");
                    i.literal("2", "D");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("product", "cCX");
                    o.get().literal("product", "dCX");
                    o.get().literal("product", "cDX");
                    o.get().literal("product", "dDX");
                    o.get().literal("product", "cCY");
                    o.get().literal("product", "dCY");
                    o.get().literal("product", "cDY");
                    o.get().literal("product", "dDY");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldEmitTuplesWithMinNIfNotAllStatementsFired() {
        assertMorph(receiver,
                "<rules>" +
                "  <tuples name='product' minN='1'>" +
                "    <data source='1' />" +
                "    <data source='3' />" +
                "    <data source='2' />" +
                "  </tuples>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("1", "a");
                    i.literal("1", "b");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("product", "a");
                    o.get().literal("product", "b");
                    o.get().endRecord();
                }
        );
    }

}
