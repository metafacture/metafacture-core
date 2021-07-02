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

package org.metafacture.metamorph;

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
 * Test Metafix binds.
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@Disabled // implement Fix-style binds (with or without collectors)
public class MetafixBindTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixBindTest() {
    }

    @Test
    public void ifInCollector() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do entity('author')",
                " map('name')",
                " if all_contain('name', 'University')", //
                "  add_field('type', 'Organization')", //
                " end",
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "A University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("type", "Organization");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void ifInCollectorMultiRecords() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do entity('author')",
                " map('name')",
                " if all_contain('name', 'University')", //
                "  add_field('type', 'Organization')", //
                " end",
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();
                //
                i.startRecord("2");
                i.literal("name", "A University");
                i.endRecord();
                //
                i.startRecord("3");
                i.literal("name", "Mary");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "Max");
                o.get().endEntity();
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().literal("type", "Organization");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().endRecord();
                //
                o.get().startRecord("3");
                o.get().startEntity("author");
                o.get().literal("name", "Mary");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void ifInCollectorChoose() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do choose(flushWith: 'record')",
                " if all_contain('name', 'University')", //
                "  add_field('type', 'Organization')", //
                " end", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max University");
                i.endRecord();
                //
                i.startRecord("2");
                i.literal("name", "Max Musterman");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("type", "Organization");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().endRecord();
            });
    }

    @Test
    public void ifInCollectorCombine() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do combine(name: 'fullName', value: '${first} ${last}')", //
                " if all_contain('author.type', 'Person')", //
                "  map('author.first', 'first')", //
                "  map('author.last', 'last')", //
                " end", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("type", "Organization");
                i.endEntity();
                i.endRecord();
                //
                i.startRecord("2");
                i.startEntity("author");
                i.literal("first", "Max");
                i.literal("last", "Musterman");
                i.literal("type", "DifferentiatedPerson");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().literal("fullName", "Max Musterman");
                o.get().endRecord();
            });
    }

}
