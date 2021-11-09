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

package org.metafacture.metamorph.functions;

import static org.metafacture.metamorph.TestHelpers.assertMorph;

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Script}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class ScriptTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldExecuteJavascriptFunctions() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='data1'>" +
                "    <script file='org/metafacture/metamorph/functions/script-test.js' invoke='process' />" +
                "  </data>" +
                "  <data source='data2'>" +
                "    <script file='org/metafacture/metamorph/functions/script-test.js' invoke='process2' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "ABC");
                    i.literal("data2", "ABC");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data1", "ABC!");
                    o.get().literal("data2", "ABCABC");
                    o.get().endRecord();
                }
        );
    }

}
