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
 * Tests the non-field-streaming record functionality of Metafix via DSL.
 *
 * See https://github.com/metafacture/metafacture-fix/issues/35
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@Disabled // Temporarily disabled while working on adapting `if` from metamorph
public class MetafixRecordTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixRecordTest() {
    }

    @Test
    public void ifAnyMatch() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if any_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max Musterman");
                i.endRecord();
                //
                i.startRecord("2");
                i.literal("name", "Some University");
                i.literal("name", "Filibandrina");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAllMatch() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if all_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max Musterman");
                i.literal("name", "A University");
                i.endRecord();
                //
                i.startRecord("2");
                i.literal("name", "Some University");
                i.literal("name", "University Filibandrina");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if any_match('author.name.label', '.*University.*')", //
                "  add_field('author.type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.startEntity("name");
                i.literal("label", "Max Musterman");
                i.endEntity();
                i.endEntity();
                i.endRecord();
                //
                i.startRecord("2");
                i.startEntity("author");
                i.startEntity("name");
                i.literal("label", "Some University");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    @Disabled // TODO: fix events for this record order
    public void ifAnyMatchFirst() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if any_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Some University");
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
    @Disabled // TODO: support else block
    public void ifAnyMatchElse() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if any_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "else", //
                "  add_field('type', 'Person')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max Musterman");
                i.endRecord();
                //
                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("type", "Person");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    @Disabled // TODO: support elsif block
    public void ifAnyMatchElsif() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(//
                "if any_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "elsif any_match('name', '[^ ]* [^ ]*')", //
                "  add_field('type', 'Person')", //
                "else", //
                "  add_field('type', 'Unknown')", //
                "end"), //
            i -> {
                i.startRecord("1");
                i.literal("name", "Max Musterman");
                i.endRecord();
                //
                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();
                //
                i.startRecord("3");
                i.literal("name", "Filibandrina");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("type", "Person");
                o.get().endRecord();
                //
                o.get().startRecord("2");
                o.get().literal("type", "Organization");
                o.get().endRecord();
                //
                o.get().startRecord("3");
                o.get().literal("type", "Unknown");
                o.get().endRecord();
            });
    }
}
