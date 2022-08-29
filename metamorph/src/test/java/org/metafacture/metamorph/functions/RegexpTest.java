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
 * Tests for class {@link Regexp}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class RegexpTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldMatchAndReplaceUsingRegularExpressions() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='001.' name='subject'>" +
                "    <regexp match='.*' format='resource:P${0}' />" +
                "  </data>" +
                "  <data source='001.' name='subject'>" +
                "    <regexp match='.*' format='${1}' />" +
                "  </data>" +
                "  <data source='001.' name='subject'>" +
                "    <regexp match='.*' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("001");
                    i.literal("", "184000");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("subject", "resource:P184000");
                    o.get().literal("subject", "");
                    o.get().literal("subject", "184000");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldIgnoreEmptyMatchGroups() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='s'>" +
                "    <regexp match='aa(bb*)?(cc*)(dd*)' format='${1}${2}${3}' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("s", "aaccdd");
                    i.literal("s", "ax");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("s", "ccdd");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldIgnoreNullValues() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='s'>" +
                "    <regexp match='a.*' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("s", "aaccdd");
                    i.literal("s", null);
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("s", "aaccdd");
                    o.get().endRecord();
                }
        );
    }

}
