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
 * Tests for class {@link DateFormat}.
 *
 * @author Michael Büchner (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class DateFormatTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldMakeOutputFormatConfigurable() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='date'>" +
                "    <dateformat outputformat='LONG' language='en' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("date", "23.04.1564");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("date", "April 23, 1564");
                    o.get().endRecord();
                }
        );
    }

}
