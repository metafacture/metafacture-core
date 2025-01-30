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
 * Tests for class {@link Group}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class GroupTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public GroupTest() {
    }

    @Test
    public void shouldGroupToOverwriteNameAndValueOfContaintStatements() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <group name='group' value='constant'>" +
                "    <data source='data1' />" +
                "    <data source='data2' />" +
                "  </group>" +
                "  <data source='data3' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data1", "skjdfh");
                    i.literal("data2", "slkdjf");
                    i.literal("data3", "A");
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    f.apply(2).literal("group", "constant");
                    o.get().literal("data3", "A");
                    o.get().endRecord();
                }
        );
    }

}
