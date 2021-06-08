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
 * Tests the `if` Metamorph mechanism adapted for Metafix via DSL.
 *
 * See https://github.com/metafacture/metafacture-fix/issues/10
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class MetafixIfTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixIfTest() {
    }

    @Test
    public void ifTopLevel() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "map('name')",
                "if contains('name', 'University')", //
                "  add_field('type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "A University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifTopLevelWithEqualsFunction() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if equals('name', 'University')", //
                "  add_field('type', 'Organization')", //
                "end", //
                "map('name')"),
            i -> {
                i.startRecord("1");
                i.literal("name", "University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "University");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifInCollector() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do entity('author')",
                " map('name')",
                " if contains('name', 'University')", //
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
    public void ifTopLevelMultiRecords() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "map('name')",
                "if contains('name', 'University')", //
                "  add_field('type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();
                i.startRecord("2");
                i.literal("name", "A University");
                i.endRecord();
                i.startRecord("3");
                i.literal("name", "Mary");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("name", "Mary");
                o.get().endRecord();
            });
    }

    @Test
    public void ifTopLevelMultiRecordsMapField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if contains('name', 'University')", //
                "  map('name', 'orgName')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();
                i.startRecord("2");
                i.literal("name", "A University");
                i.endRecord();
                i.startRecord("3");
                i.literal("name", "Mary");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("orgName", "A University");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    @Disabled // TODO: in collector, `map` not firing when `if` fails
    public void ifInCollectorMultiRecords() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do entity('author')",
                " map('name')",
                " if contains('name', 'University')", //
                "  add_field('type', 'Organization')", //
                " end",
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();
                i.startRecord("2");
                i.literal("name", "A University");
                i.endRecord();
                i.startRecord("3");
                i.literal("name", "Mary");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "Max");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().literal("type", "Organization");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("author");
                o.get().literal("name", "Mary");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // Without `entity`, but nested.entity.syntax: two entities, same name
    public void mapAndTestNestedTwoEntities() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "map('name', 'author.name')",
                "if contains('name', 'University')", //
                " add_field('author.type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "A University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().startEntity("author");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // Top-level `if` constructs internally wrap a choose collector:
    public void ifInCollectorChoose() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "do choose(flushWith: 'record')",
                " if contains('name', 'University')", //
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
                " if contains('author.type', 'Person')", //
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

    @Test
    @Disabled // TODO: second `add_field` not firing
    public void ifTopLevelMultipleAddField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "map('name')",
                "if contains('name', 'University')", //
                "  add_field('type', 'Organization')", //
                "  add_field('comment', 'type was guessed')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "A University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().literal("comment", "type was guessed");
                o.get().endRecord();
            });
    }

}
