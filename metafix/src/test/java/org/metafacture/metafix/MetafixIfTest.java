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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Test Metafix `if` conditionals.
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class) // checkstyle-disable-line JavaNCSS
public class MetafixIfTest {

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
                o.get().literal("name", "Mary");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mary");
                o.get().literal("name", "Max");
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
                o.get().literal("name", "Mary");
                o.get().literal("name", "A University");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Great University");
                o.get().literal("name", "A University");
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
                "end"
            ),
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
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "Mary");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().literal("name", "Max");
                o.get().literal("name", "Mary");
                o.get().endEntity();
                o.get().literal("type", "Person");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("type", "Person");
                o.get().endRecord();
            }
        );
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
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
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
                o.get().literal("name", "Some University");
                o.get().literal("name", "Filibandrina");
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
                o.get().literal("name", "Max");
                o.get().literal("name", "A University");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Some University");
                o.get().literal("name", "University Filibandrina");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            });
    }

    @Test
    public void ifAnyMatchNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
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
    public void shouldNotImplicitlyMatchNestedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('author.name', '.*University.*')",
                "  add_field('author.type', 'Organization')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "A University");
                i.endEntity();
                i.endRecord();

                i.startRecord("2");
                i.startEntity("author");
                i.startEntity("name");
                i.literal("label", "Some University");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("author");
                o.get().startEntity("name");
                o.get().literal("label", "Some University");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
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

    @Test
    public void ifAnyMatchMultipleElsif() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "elsif any_match('name', 'Mary')",
                "  add_field('type', 'Person1')",
                "elsif any_match('name', 'Max')",
                "  add_field('type', 'Person2')",
                "elsif any_match('name', '[^ ]* [^ ]*')",
                "  add_field('type', 'Person')",
                "else",
                "  add_field('type', 'Unknown')",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mary Power");
                i.literal("name", "Max Power");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();

                i.startRecord("3");
                i.literal("name", "Filibandrina");
                i.endRecord();

                i.startRecord("4");
                i.literal("name", "Mary");
                i.literal("name", "Max Power");
                i.endRecord();

                i.startRecord("5");
                i.literal("name", "Max");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mary Power");
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

                o.get().startRecord("4");
                o.get().literal("name", "Mary");
                o.get().literal("name", "Max Power");
                o.get().literal("type", "Person1");
                o.get().endRecord();

                o.get().startRecord("5");
                o.get().literal("name", "Max");
                o.get().literal("type", "Person2");
                o.get().endRecord();
            });
    }

    private void shouldEqualAny(final String path) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_equal('" + path + "', 'University')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mary");
                i.literal("name", "University");
                i.literal("nome", "Max");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mary");
                o.get().literal("name", "University");
                o.get().literal("nome", "Max");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEqualAny() {
        shouldEqualAny("name");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyCharacterClass() {
        shouldEqualAny("n[ao]me");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyAlternation() {
        shouldEqualAny("name|nome");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyWildcard() {
        shouldEqualAny("n?me");
    }

    private void shouldEqualAnyNested(final String path) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_equal('" + path + "', 'University')",
                "  add_field('data.type', 'Organization')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("name", "Mary");
                i.literal("name", "University");
                i.literal("nome", "Max");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("name", "Mary");
                o.get().literal("name", "University");
                o.get().literal("nome", "Max");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEqualAnyNested() {
        shouldEqualAnyNested("data.name");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyNestedCharacterClass() {
        shouldEqualAnyNested("data.n[ao]me");
    }

    @Test
    public void shouldEqualAnyNestedAlternation() {
        shouldEqualAnyNested("data.name|data.nome");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyNestedWildcard() {
        shouldEqualAnyNested("data.n?me");
    }

    private void shouldEqualAnyListBind(final String path) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list(path: 'data', ^var: '$i')",
                "  if any_equal('" + path + "', 'University')",
                "    add_field('$i.type', 'Organization')",
                "  end",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("name", "Mary");
                i.literal("name", "University");
                i.literal("nome", "Max");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("name", "Mary");
                o.get().literal("name", "University");
                o.get().literal("nome", "Max");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEqualAnyListBind() {
        shouldEqualAnyListBind("$i.name");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyListBindCharacterClass() {
        shouldEqualAnyListBind("$i.n[ao]me");
    }

    @Test
    public void shouldEqualAnyListBindAlternation() {
        shouldEqualAnyListBind("$i.name|$i.nome");
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/115
    public void shouldEqualAnyListBindWildcard() {
        shouldEqualAnyListBind("$i.n?me");
    }

    @Test
    public void shouldContainImmediateField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('name')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mary");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Mary");
                i.literal("nome", "Max");
                i.endRecord();

                i.startRecord("3");
                i.literal("nome", "Max");
                i.endRecord();

                i.startRecord("4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mary");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mary");
                o.get().literal("nome", "Max");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("nome", "Max");
                o.get().endRecord();

                o.get().startRecord("4");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/117
    public void shouldContainNestedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('data.name')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("name", "Mary");
                i.literal("name", "A University");
                i.endEntity();
                i.endRecord();

                i.startRecord("2");
                i.startEntity("data");
                i.literal("name", "Mary");
                i.literal("nome", "Max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.literal("data", "Mary");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("name", "Mary");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("data");
                o.get().literal("name", "Mary");
                o.get().literal("nome", "Max");
                o.get().endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("data", "Mary");
                o.get().literal("name", "Max");
                o.get().endRecord();

                o.get().startRecord("4");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/117
    public void shouldContainNestedArrayField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('data[].*.name')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data[]");
                i.startEntity("1");
                i.literal("nome", "Mary");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "A University");
                i.endEntity();
                i.endEntity();
                i.endRecord();

                i.startRecord("2");
                i.startEntity("data[]");
                i.literal("1", "Mary");
                i.literal("2", "Max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.literal("data", "Mary");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("4");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("data[]");
                o.get().startEntity("1");
                o.get().literal("nome", "Mary");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "A University");
                f.apply(2).endEntity();
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("data[]");
                o.get().literal("1", "Mary");
                o.get().literal("2", "Max");
                o.get().endEntity();
                o.get().literal("type", "Organization"); // FIXME: `data[].*.name` must not return `[Mary, Max]`
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("data", "Mary");
                o.get().literal("name", "Max");
                o.get().endRecord();

                o.get().startRecord("4");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldResolveVariablesInIf() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_contain('name', 'Uni$[var]sity')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            ImmutableMap.of("var", "ver"),
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
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mary");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mary");
                o.get().literal("name", "Max");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldResolveVariablesInElsIf() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if any_match('name', '.*University.*')",
                "  add_field('type', 'Organization')",
                "elsif any_match('$[var]e', '[^ ]* [^ ]*')",
                "  add_field('type', 'Person')",
                "else",
                "  add_field('type', 'Unknown')",
                "end"
            ),
            ImmutableMap.of("var", "nam"),
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
            },
            o -> {
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
            }
        );
    }

    @Test
    public void shouldResolveVariablesInUnless() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "unless any_match('name', '.*Uni$[var]sity.*')",
                "  add_field('type', 'Person')",
                "end"
            ),
            ImmutableMap.of("var", "ver"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Some University");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().literal("type", "Person");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Some University");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldApplyCustomJavaPredicate() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if org.metafacture.metafix.util.TestPredicate(name, Test)",
                "  add_field('type', 'TEST')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Test");
                i.endRecord();

                i.startRecord("3");
                i.literal("test", "Some University");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Max");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Test");
                o.get().literal("type", "TEST");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("test", "Some University");
                o.get().literal("type", "TEST");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIncludeLocationAndTextInProcessExceptionInConditional() {
        final String text1 = "elsif exists()";
        final String text2 = "nothing()";
        final String message = "Error while executing Fix expression (at FILE, line 3): " + text1 + " " + text2;

        MetafixTestHelpers.assertThrows(FixProcessException.class, s -> s.replaceAll("file:/.+?\\.fix", "FILE"), message, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "if exists('animal')",
                    "nothing()",
                    text1,
                    text2,
                    "end"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("animals");
                    i.literal("1", "dog");
                    i.literal("2", "cat");
                    i.literal("3", "zebra");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldIncludeLocationAndTextInProcessExceptionInBody() {
        final String text = "add_field()";
        final String message = "Error while executing Fix expression (at FILE, line 4): " + text;

        MetafixTestHelpers.assertThrows(FixProcessException.class, s -> s.replaceAll("file:/.+?\\.fix", "FILE"), message, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "if exists('animal')",
                    "nothing()",
                    "elsif exists('animals')",
                    text,
                    "end"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("animals");
                    i.literal("1", "dog");
                    i.literal("2", "cat");
                    i.literal("3", "zebra");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void ifOnNonExistingIndexInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('animals[].2')",
                "  copy_field('animals[].2', 'animals2')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "elefant");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "elefant");
                o.get().endEntity();
                o.get().literal("animals2", "elefant");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void ifOnNonExistingIndexInRepeatedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('animal.2')",
                "  copy_field('animal.2', 'animal2')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "dog");
                i.literal("animal", "elefant");
                i.endRecord();
                i.startRecord("2");
                i.literal("animal", "dog");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "dog");
                o.get().literal("animal", "elefant");
                o.get().literal("animal2", "elefant");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("animal", "dog");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainStringInString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if in(foo,bar)",
                "  add_field(forty_two,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "42");
                i.literal("bar", "42");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "42");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "42");
                o.get().literal("bar", "42");
                o.get().literal("forty_two", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "42");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainStringInStringAlias() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_contained_in(foo,bar)",
                "  add_field(forty_two,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "42");
                i.literal("bar", "42");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "42");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "42");
                o.get().literal("bar", "42");
                o.get().literal("forty_two", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "42");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainStringInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if in(foo,bar)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "1");
                i.literal("bar", "1");
                i.literal("bar", "2");
                i.literal("bar", "3");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "42");
                i.literal("bar", "1");
                i.literal("bar", "2");
                i.literal("bar", "3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "1");
                o.get().literal("bar", "1");
                o.get().literal("bar", "2");
                o.get().literal("bar", "3");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "42");
                o.get().literal("bar", "1");
                o.get().literal("bar", "2");
                o.get().literal("bar", "3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainStringInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if in(foo,bar)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "name");
                i.startEntity("bar");
                i.literal("name", "Patrick");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "name");
                i.startEntity("bar");
                i.startEntity("deep");
                i.literal("name", "Nicolas");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("foo", "name");
                o.get().startEntity("bar");
                o.get().literal("name", "Patrick");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "name");
                o.get().startEntity("bar");
                o.get().startEntity("deep");
                o.get().literal("name", "Nicolas");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainArrayInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if in(foo,bar)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "1");
                i.literal("foo", "2");
                i.literal("bar", "1");
                i.literal("bar", "2");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.literal("foo", "2");
                i.literal("bar", "1");
                i.literal("bar", "2");
                i.literal("bar", "3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "1");
                o.get().literal("foo", "2");
                o.get().literal("bar", "1");
                o.get().literal("bar", "2");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "1");
                o.get().literal("foo", "2");
                o.get().literal("bar", "1");
                o.get().literal("bar", "2");
                o.get().literal("bar", "3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainHashInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if in(foo,bar)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("foo");
                i.literal("a", "b");
                i.endEntity();
                i.startEntity("bar");
                i.literal("a", "b");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.startEntity("foo");
                i.literal("a", "b");
                i.endEntity();
                i.startEntity("bar");
                i.literal("a", "b");
                i.literal("c", "d");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().literal("a", "b");
                o.get().endEntity();
                o.get().startEntity("bar");
                o.get().literal("a", "b");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("foo");
                o.get().literal("a", "b");
                o.get().endEntity();
                o.get().startEntity("bar");
                o.get().literal("a", "b");
                o.get().literal("c", "d");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportArrayAsArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_array(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "bar");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "bar");
                o.get().literal("foo", "1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportArrayEntityAsArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_array('foo[]')",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("foo[]");
                i.literal("1", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.startEntity("foo[]");
                i.endEntity();
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.endEntity();
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo[]");
                o.get().literal("1", "bar");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("foo[]");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportEmptyArrayAsArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array(foo)",
                "if is_array(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "bar");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().literal("test", "ok");
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldReportEmptyValueAsEmpty() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_empty(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "bar");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "   ");
                i.endRecord();
                i.startRecord("4");
                i.literal("foo", "bar");
                i.literal("foo", "");
                i.endRecord();
                i.startRecord("5");
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("6");
                i.startEntity("foo");
                i.literal("foo", "");
                i.endEntity();
                i.endRecord();
                i.startRecord("7");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "bar");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "   ");
                //o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().literal("foo", "bar");
                o.get().literal("foo", "");
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().startEntity("foo");
                o.get().literal("foo", "");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("7");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportEmptyArrayEntityAsEmpty() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_empty('foo[]')",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("foo[]");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.startEntity("foo[]");
                i.literal("1", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo[]");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("foo[]");
                o.get().literal("1", "bar");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportEmptyArrayAsEmpty() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array(foo)",
                "if is_empty(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "bar");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().literal("test", "ok");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportFalseStringAsFalse() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_false(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "false");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "true");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "bar");
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.literal("foo", "false");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.literal("foo", "true");
                i.literal("foo", "false");
                i.endRecord();
                i.startRecord("6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "false");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "true");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "bar");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().literal("foo", "false");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().literal("foo", "true");
                o.get().literal("foo", "false");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportFalseNumberAsFalse() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_false(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "42");
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.literal("foo", "0");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.literal("foo", "1");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "0");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "42");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().literal("foo", "0");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().literal("foo", "1");
                o.get().literal("foo", "0");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldReportHashAsHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_hash(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "bar");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "bar");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldReportNumberStringAsNumber() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_number(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "-1");
                i.endRecord();
                i.startRecord("4");
                i.literal("foo", "1.1");
                i.endRecord();
                i.startRecord("5");
                i.literal("foo", "-1.1");
                i.endRecord();
                i.startRecord("6");
                i.literal("foo", "1.1x");
                i.endRecord();
                i.startRecord("7");
                i.startEntity("foo");
                i.literal("foo", "1");
                i.endEntity();
                i.endRecord();
                i.startRecord("8");
                i.literal("foo", "1");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("9");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "0");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "-1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().literal("foo", "1.1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().literal("foo", "-1.1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().literal("foo", "1.1x");
                o.get().endRecord();
                o.get().startRecord("7");
                o.get().startEntity("foo");
                o.get().literal("foo", "1");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("8");
                o.get().literal("foo", "1");
                o.get().literal("foo", "0");
                o.get().endRecord();
                o.get().startRecord("9");
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldReportHashAsObject() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_object(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "bar");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.startEntity("foo");
                i.endEntity();
                i.endRecord();
                i.startRecord("6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "bar");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportEmptyHashAsObject() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_hash(foo)",
                "if is_object(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "bar");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("4");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().endEntity();
                o.get().literal("test", "ok");
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldReportStringAsString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_string(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "-1");
                i.endRecord();
                i.startRecord("4");
                i.literal("foo", "1.1");
                i.endRecord();
                i.startRecord("5");
                i.literal("foo", "-1.1");
                i.endRecord();
                i.startRecord("6");
                i.literal("foo", "1.1x");
                i.endRecord();
                i.startRecord("7");
                i.literal("foo", "bar");
                i.endRecord();
                i.startRecord("8");
                i.literal("foo", "");
                i.endRecord();
                i.startRecord("9");
                i.startEntity("foo");
                i.literal("foo", "bar");
                i.endEntity();
                i.endRecord();
                i.startRecord("10");
                i.literal("foo", "bar");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("11");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "1");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "0");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "-1");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().literal("foo", "1.1");
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().literal("foo", "-1.1");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().literal("foo", "1.1x");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("7");
                o.get().literal("foo", "bar");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("8");
                o.get().literal("foo", "");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("9");
                o.get().startEntity("foo");
                o.get().literal("foo", "bar");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("10");
                o.get().literal("foo", "bar");
                o.get().literal("foo", "0");
                o.get().endRecord();
                o.get().startRecord("11");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportTrueStringAsTrue() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_true(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "true");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "false");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "bar");
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.literal("foo", "true");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.literal("foo", "true");
                i.literal("foo", "false");
                i.endRecord();
                i.startRecord("6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "true");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "false");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "bar");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().literal("foo", "true");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().literal("foo", "true");
                o.get().literal("foo", "false");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReportTrueNumberAsTrue() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if is_true(foo)",
                "  add_field(test,ok)",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.literal("foo", "1");
                i.endRecord();
                i.startRecord("2");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("3");
                i.literal("foo", "42");
                i.endRecord();
                i.startRecord("4");
                i.startEntity("foo");
                i.literal("foo", "1");
                i.endEntity();
                i.endRecord();
                i.startRecord("5");
                i.literal("foo", "1");
                i.literal("foo", "0");
                i.endRecord();
                i.startRecord("6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "1");
                o.get().literal("test", "ok");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("foo", "0");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("foo", "42");
                o.get().endRecord();
                o.get().startRecord("4");
                o.get().startEntity("foo");
                o.get().literal("foo", "1");
                o.get().endEntity();
                o.get().endRecord();
                o.get().startRecord("5");
                o.get().literal("foo", "1");
                o.get().literal("foo", "0");
                o.get().endRecord();
                o.get().startRecord("6");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldContainString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if str_contain('name', 'a$[var]')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            ImmutableMap.of("var", "me"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mame");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Mame");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mame");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mame");
                o.get().literal("name", "Max");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEqualString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if str_equal('name', 'na$[var]')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            ImmutableMap.of("var", "me"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mame");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Mame");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mame");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mame");
                o.get().literal("name", "Max");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldMatchString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if str_match('name', '.*a$[var]')",
                "  add_field('type', 'Organization')",
                "end"
            ),
            ImmutableMap.of("var", "me"),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mame");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Mame");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mame");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mame");
                o.get().literal("name", "Max");
                o.get().literal("type", "Organization");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("type", "Organization");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldTestMacroVariable() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do put_macro('test')",
                "  if str_contain('name', 'a$[var]')",
                "    add_field('type', 'Organization: $[var]')",
                "  end",
                "end",
                "call_macro('test', 'var': 'm')",
                "call_macro('test', 'var': 'me')",
                "call_macro('test', 'var': 'mee')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mame");
                i.literal("name", "A University");
                i.endRecord();

                i.startRecord("2");
                i.literal("name", "Mame");
                i.literal("name", "Max");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "Mame");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization: m");
                o.get().literal("type", "Organization: me");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("name", "Mame");
                o.get().literal("name", "Max");
                o.get().literal("type", "Organization: m");
                o.get().literal("type", "Organization: me");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().literal("type", "Organization: m");
                o.get().literal("type", "Organization: me");
                o.get().endRecord();
            }
        );
    }

}
