/*
 * Copyright 2017 Christoph Böhme
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

package org.metafacture.metamorph.functions;

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.TestHelpers;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Split}.
 *
 * @author Christoph Böhme
 */
public final class SplitTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public SplitTest() {
    }

    @Test
    public void issue265_shouldWorkIfLastFunctionInCombineStatement() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <combine name='out' value='${v}'>" +
                "    <data source='in' name='v'>" +
                "      <split delimiter=' ' />" +
                "    </data>" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("in", "1 2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("out", "1");
                    o.get().literal("out", "2");
                    o.get().endRecord();
                }
        );
    }

}
