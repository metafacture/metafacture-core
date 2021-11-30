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
 * Test Metafix `if` conditionals.
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixIfTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixIfTest() {
    }

    @Test
    public void ifAny() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_contain('name', 'University')",
                "  add_field('type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mary");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Mary");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "Mary");
                o.get().literal("2", "A University");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("name");
                o.get().literal("1", "Mary");
                o.get().literal("2", "Max");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAll() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if all_contain('name', 'University')",
                "  add_field('type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mary");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Great University");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "Mary");
                o.get().literal("2", "A University");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("name");
                o.get().literal("1", "Great University");
                o.get().literal("2", "A University");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void ifNone() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if none_contain('author.name', 'University')",
                "  add_field('type', 'Person')",
                "end"),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "Mary");
                i.literal("name", "A University");
                i.endEntity();
                i.endRecord();

                i.startRecord("2");
                i.startEntity("author");
                i.literal("name", "Max");
                i.literal("name", "Mary");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().startEntity("name");
                o.get().literal("1", "Mary");
                o.get().literal("2", "A University");
                f.apply(2).endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().startEntity("name");
                o.get().literal("1", "Max");
                o.get().literal("2", "Mary");
                f.apply(2).endEntity();
                o.get().literal("type", "Person");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("type", "Person");
                o.get().endRecord();
            });
    }

    @Test
    public void ifEqual() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if all_equal('name', 'University')",
                "  add_field('type', 'Organization')",
                "end"),
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
    public void ifContainMoveField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if all_contain('name', 'University')",
                "  move_field('name', 'orgName')",
                "end"),
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
                o.get().literal("orgName", "A University");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("name", "Mary");
                o.get().endRecord();
            });
    }

    @Test
    public void moveAndAddIfContain() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(// TODO: dot notation in contain etc.
                "move_field('name', 'author.name')",
                "if all_contain('author.name', 'University')",
                " add_field('author.type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "A University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void ifContainMultipleAddField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if all_contain('name', 'University')",
                "  add_field('type', 'Organization')",
                "  add_field('comment', 'type was guessed')",
                "end"),
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

    @Test
    public void ifAnyMatch() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.literal("name", "Filibandrina");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("name");
                o.get().literal("1", "Some University");
                o.get().literal("2", "Filibandrina");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAllMatch() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if all_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.literal("name", "University Filibandrina");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "Max");
                o.get().literal("2", "A University");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("name");
                o.get().literal("1", "Some University");
                o.get().literal("2", "University Filibandrina");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(// TODO: dot notation in match etc.
                "if any_match('author.name.label', '.*University.*')",
                "  add_field('author.type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.startEntity("name");
                i.literal("label", "Max");
                i.endEntity();
                i.endEntity();
                i.endRecord();

                i.startRecord("2");
                i.startEntity("author");
                i.startEntity("name");
                i.literal("label", "Some University");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().startEntity("name");
                o.get().literal("label", "Max");
                f.apply(2).endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().startEntity("name");
                o.get().literal("label", "Some University");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchFirstRecord() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Some University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Max");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Some University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Max");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchLastRecord() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Some University");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void unlessAnyMatch() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "unless any_match('name', '.*University.*')",
                "  add_field('type', 'Person')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().literal("type", "Person");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Some University");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchElse() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "else",
                "  add_field('type', 'Person')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().literal("type", "Person");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Some University");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchElsif() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "elsif any_match('name', '[^ ]* [^ ]*')",
                "  add_field('type', 'Person')",
                "else",
                "  add_field('type', 'Unknown')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max Power");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();

                i.startRecord("3");
                i.literal("name", "Filibandrina");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max Power");
                o.get().literal("type", "Person");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Some University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("name", "Filibandrina");
                o.get().literal("type", "Unknown");
                o.get().endRecord();
            });
    }
}
