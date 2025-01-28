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

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.TestHelpers;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests basic functionality of Metamorph functions.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TestFunctionBasics {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public TestFunctionBasics() {
    }

    @Test
    public void shouldSupportFunctionChainingInDataStatements() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data'>" +
                "    <trim />" +
                "    <replace pattern=' ' with='X' />" +
                "    <replace pattern='a' with='A' />" +
                "    <regexp match='Abc' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", " abc ");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data", " abc ");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "Abc");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("data", "Abc");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldSupportFunctionChainingInEntities() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <choose>" +
                "    <data source='data'>" +
                "      <trim />" +
                "      <replace pattern=' ' with='X' />" +
                "      <replace pattern='a' with='A' />" +
                "      <regexp match='Abc' />" +
                "    </data>" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", " abc ");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data", " abc ");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "Abc");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("data", "Abc");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldUseJavaClassesAsFunctions() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data'>" +
                "    <java class='org.metafacture.metamorph.functions.Compose' prefix='Hula ' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "Aloha");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "Hula Aloha");
                    o.get().endRecord();
                }
        );
    }

}
