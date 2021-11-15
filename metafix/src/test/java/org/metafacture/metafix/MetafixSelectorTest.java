/*
 * Copyright 2021 Fabian Steeg, hbz
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

package org.metafacture.metafix;

import org.metafacture.framework.StreamReceiver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Test Metafix selectors.
 *
 * @author Fabian Steeg
 *
 */
@ExtendWith(MockitoExtension.class)
@Disabled("TODO: support Fix-style selectors https://github.com/LibreCat/Catmandu/wiki/Selectors")
public final class MetafixSelectorTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixSelectorTest() { }

    @Test
    public void reject() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "reject exists(error)"),
            i -> {
                i.startRecord("1");
                i.literal("error", "details");
                i.endRecord();

                i.startRecord("2");
                i.endRecord();
            }, o -> {
                o.get().startRecord("2");
                o.get().literal("name", "Mary");
                o.get().endRecord();
            });
    }

    @Test
    public void rejectWithExplicitConditional() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists(error)",
                "  reject()",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("error", "details");
                i.endRecord();

                i.startRecord("2");
                i.endRecord();
            }, o -> {
                o.get().startRecord("2");
                o.get().literal("name", "Mary");
                o.get().endRecord();
            });
    }

}
