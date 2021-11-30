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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Tests Metafix lookup. Following the cheat sheet examples at
 * https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixLookupTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixLookupTest() {
    }

    @Test
    public void inline() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().literal("3", "Tach");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void inlineMultilineIndent() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title',",
                "  Aloha: Alohaeha,",
                "  Moin: 'Moin zäme')"),
            i -> {
                i.startRecord("1");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void inlineDotNotationNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('data.title', Aloha: Alohaeha, 'Moin': 'Moin zäme', __default: Tach)"),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().literal("3", "Tach");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void csv() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title', 'src/test/java/org/metafacture/metafix/maps/test.csv')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");

                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().literal("3", "Tach");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void tsv() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "lookup('title', 'src/test/java/org/metafacture/metafix/maps/test.tsv', sep_char:'\t')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "Aloha");
                i.literal("title", "Moin");
                i.literal("title", "Hey");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "Alohaeha");
                o.get().literal("2", "Moin zäme");
                o.get().literal("3", "Tach");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }
}
