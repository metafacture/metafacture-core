/*
 * Copyright 2021, 2023 Fabian Steeg, hbz
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

package org.metafacture.metafix; // checkstyle-disable-line JavaNCSS

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.api.MorphBuildException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tests Metafix record level methods. Following the cheat sheet
 * examples at https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class) // checkstyle-disable-line JavaNCSS
@ExtendWith(MetafixToDo.Extension.class)
public class MetafixRecordTest {

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixRecordTest() {
    }

    @Test
    public void entitiesPassThrough() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"),
            i -> {
                i.startRecord("1");
                i.startEntity("deep");
                i.startEntity("nested");
                i.literal("field", "value");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("deep");
                o.get().startEntity("nested");
                o.get().literal("field", "value");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void shouldNotEmitVirtualFieldsByDefault() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEmitVirtualFieldsWhenRetained() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "retain('_id')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("_id", "1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEmitVirtualFieldsWhenCopied() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('_id', id)"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("id", "1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEmitVirtualFieldsWhenAdded() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('_id', 'id')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("_id", "id");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void entitiesPassThroughRepeatNestedEntity() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"),
            i -> {
                i.startRecord("1");
                i.startEntity("deep");
                i.startEntity("nested");
                i.literal("field", "value1");
                i.endEntity();
                i.startEntity("nested");
                i.literal("field", "value2");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("deep");
                o.get().startEntity("nested");
                o.get().literal("field", "value1");
                o.get().endEntity();
                o.get().startEntity("nested");
                o.get().literal("field", "value2");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void setEmpty() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_field('my.nested.name','patrick')",
                "set_field('your.nested.name','nicolas')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().startEntity("nested");
                o.get().literal("name", "patrick");
                f.apply(2).endEntity();
                o.get().startEntity("your");
                o.get().startEntity("nested");
                o.get().literal("name", "nicolas");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void setExisting() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_field('my.nested.name','patrick')",
                "set_field('your.nested.name','nicolas')"),
            i -> {
                i.startRecord("1");
                i.startEntity("my");
                i.startEntity("nested");
                i.literal("name", "max");
                i.endEntity();
                i.endEntity();
                i.startEntity("your");
                i.startEntity("nested");
                i.literal("name", "mo");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().startEntity("nested");
                o.get().literal("name", "patrick");
                f.apply(2).endEntity();
                o.get().startEntity("your");
                o.get().startEntity("nested");
                o.get().literal("name", "nicolas");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void add() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('my.name','patrick')",
                "add_field('my.name','nicolas')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("my");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("my");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().startEntity("my");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithAppendInNewArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('my.name')",
                "add_field('my.name.$append','patrick')",
                "add_field('my.name.$append','nicolas')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("my");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("name", "patrick");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("my");
                o.get().literal("name", "patrick");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().startEntity("my");
                o.get().literal("name", "patrick");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithAppendInImplicitArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('my.name.$append','patrick')",
                "add_field('my.name.$append','nicolas')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("my");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("name", "patrick");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("my");
                o.get().literal("name", "max");
                o.get().literal("name", "patrick");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().startEntity("my");
                o.get().literal("name", "patrick");
                o.get().literal("name", "nicolas");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithPrependInImplicitArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('my.name.$prepend','patrick')",
                "add_field('my.name.$prepend','nicolas')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("my");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("name", "nicolas");
                o.get().literal("name", "patrick");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("my");
                o.get().literal("name", "nicolas");
                o.get().literal("name", "patrick");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().startEntity("my");
                o.get().literal("name", "nicolas");
                o.get().literal("name", "patrick");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithLastInNonArray() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected Array or Hash, got String", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "add_field('my.name.$last','patrick')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("my");
                    i.literal("name", "max");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void addWithAppendInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('names.$append','patrick')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("names", "max");
                i.literal("names", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("names", "max");
                o.get().literal("names", "mo");
                o.get().literal("names", "patrick");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithAppendInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('author.names.$append','patrick')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("names", "max");
                i.literal("names", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("names", "max");
                o.get().literal("names", "mo");
                o.get().literal("names", "patrick");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithAppendInArrayWithSubfieldFromRepeatedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('authors.$append.name','patrick')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("authors");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("authors");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("authors");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("authors");
                o.get().literal("name", "mo");
                o.get().endEntity();
                o.get().startEntity("authors");
                o.get().literal("name", "patrick");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addWithAppendInArrayWithSubfieldFromIndexedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('authors[].$append.name','patrick')"),
            i -> {
                i.startRecord("1");
                i.startEntity("authors[]");
                i.startEntity("1");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "mo");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("authors[]");
                o.get().startEntity("1");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "mo");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("name", "patrick");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void simpleAppendWithArrayOfStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals[].$append', 'duck')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "cat");
                i.literal("2", "dog");
                i.literal("3", "fox");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "cat");
                o.get().literal("2", "dog");
                o.get().literal("3", "fox");
                o.get().literal("4", "duck");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void complexAppendWithArrayOfStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('others', 'animals[].$append')",
                "move_field('fictional', 'animals[].$append')",
                "add_field('animals[].$append', 'earthworm')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.endEntity();
                i.literal("others", "human");
                i.literal("fictional", "unicorn");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "human");
                o.get().literal("4", "unicorn");
                o.get().literal("5", "earthworm");
                o.get().endEntity();
                o.get().literal("others", "human");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#92
    public void complexAppendWithArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('others', 'animals[].$append')",
                "move_field('fictional', 'animals[].$append')",
                "add_field('animals[].$append.animal', 'earthworm')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("animal", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("animal", "cat");
                i.endEntity();
                i.endEntity();
                i.startEntity("others");
                i.literal("animal", "human");
                i.endEntity();
                i.startEntity("fictional");
                i.literal("animal", "unicorn");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("animal", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("animal", "cat");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("animal", "human");
                o.get().endEntity();
                o.get().startEntity("4");
                o.get().literal("animal", "unicorn");
                o.get().endEntity();
                o.get().startEntity("5");
                o.get().literal("animal", "earthworm");
                f.apply(2).endEntity();
                o.get().startEntity("others");
                o.get().literal("animal", "human");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void appendWithWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('stringimals[]')",
                "copy_field('?nimal', 'stringimals[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "dog");
                i.literal("bnimal", "cat");
                i.literal("cnimal", "zebra");
                i.literal("dnimol", "bunny");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "dog");
                o.get().literal("bnimal", "cat");
                o.get().literal("cnimal", "zebra");
                o.get().literal("dnimol", "bunny");
                o.get().startEntity("stringimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#99
    public void simpleCopyWithWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('?nimal', 'animal')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "dog");
                i.endRecord();
                i.startRecord("2");
                i.literal("bnimal", "cat");
                i.endRecord();
                i.startRecord("3");
                i.literal("cnimal", "zebra");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "dog");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("bnimal", "cat");
                o.get().literal("animal", "cat");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("cnimal", "zebra");
                o.get().literal("animal", "zebra");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void appendWithMultipleWildcards() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('stringimals[]')",
                "copy_field('?ni??l', 'stringimals[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "dog");
                i.literal("bnimal", "cat");
                i.literal("cnimal", "zebra");
                i.literal("dnimol", "bunny");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "dog");
                o.get().literal("bnimal", "cat");
                o.get().literal("cnimal", "zebra");
                o.get().literal("dnimol", "bunny");
                o.get().startEntity("stringimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().literal("4", "bunny");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void appendWithAsteriksWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('stringimals[]')",
                "copy_field('*al', 'stringimals[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "dog");
                i.literal("bnimal", "cat");
                i.literal("cnimal", "zebra");
                i.literal("dnimol", "bunny");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "dog");
                o.get().literal("bnimal", "cat");
                o.get().literal("cnimal", "zebra");
                o.get().literal("dnimol", "bunny");
                o.get().startEntity("stringimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void appendWithBracketWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('stringimals[]')",
                "copy_field('[ac]nimal', 'stringimals[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "dog");
                i.literal("bnimal", "cat");
                i.literal("cnimal", "zebra");
                i.literal("dnimol", "bunny");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "dog");
                o.get().literal("bnimal", "cat");
                o.get().literal("cnimal", "zebra");
                o.get().literal("dnimol", "bunny");
                o.get().startEntity("stringimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#89
    public void appendWithAsteriksWildcardAtTheEnd() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('stringimals[]')",
                "copy_field('ani*', 'stringimals[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animals", "dog");
                i.literal("animals", "cat");
                i.literal("animals", "zebra");
                i.literal("animal", "bunny");
                i.startEntity("animols");
                i.literal("name", "bird");
                i.literal("type", "TEST");
                i.endEntity();
                i.literal("ANIMALS", "dragon and unicorn");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("animals", "dog");
                o.get().literal("animals", "cat");
                o.get().literal("animals", "zebra");
                o.get().literal("animal", "bunny");
                o.get().startEntity("animols");
                o.get().literal("name", "bird");
                o.get().literal("type", "TEST");
                o.get().endEntity();
                o.get().literal("ANIMALS", "dragon and unicorn");
                o.get().startEntity("stringimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().literal("4", "bunny");
                o.get().startEntity("5");
                o.get().literal("name", "bird");
                o.get().literal("type", "TEST");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("See https://github.com/metafacture/metafacture-fix/pull/113")
    public void shouldCopyArrayFieldWithoutAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('TEST_TWO[]')",
                "copy_field('test[]', 'TEST_TWO[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("test[]");
                i.literal("1", "One");
                i.literal("2", "Two");
                i.literal("3", "Three");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("test[]");
                o.get().literal("1", "One");
                o.get().literal("2", "Two");
                o.get().literal("3", "Three");
                o.get().endEntity();
                o.get().startEntity("TEST_TWO[]");
                o.get().startEntity("1");
                o.get().literal("1", "One");
                o.get().literal("2", "Two");
                o.get().literal("3", "Three");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("See issue metafacture-fix#113")
    public void copyFieldArrayOfObjectsAndListNewArrayOfObjectsAndMoveSubfield() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('author[]','creator[]')",
                "do list(path:'creator[]')",
                "  move_field('name','label')",
                "end",
                "retain('creator[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("author[]");
                i.startEntity("1");
                i.literal("name", "A University");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("creator[]");
                o.get().startEntity("1");
                o.get().literal("label", "A University");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // See issue #601
    public void shouldCopyArrayFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('TEST_TWO[]')",
                "copy_field('test[].*', 'TEST_TWO[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("test[]");
                i.literal("1", "One");
                i.literal("2", "Two");
                i.literal("3", "Three");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("test[]");
                o.get().literal("1", "One");
                o.get().literal("2", "Two");
                o.get().literal("3", "Three");
                o.get().endEntity();
                o.get().startEntity("TEST_TWO[]");
                o.get().literal("1", "One");
                o.get().literal("2", "Two");
                o.get().literal("3", "Three");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue #601
    public void shouldCopyNestedArrayFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('TEST_4[]')",
                "copy_field('nestedTest[].*.test[].*', 'TEST_4[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("nestedTest[]");
                i.startEntity("1");
                i.startEntity("test[]");
                i.literal("1", "One");
                i.literal("2", "Two");
                i.literal("3", "Three");
                i.endEntity();
                i.endEntity();
                i.startEntity("2");
                i.startEntity("test[]");
                i.literal("1", "4");
                i.literal("2", "5");
                i.literal("3", "6");
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("nestedTest[]");
                o.get().startEntity("1");
                o.get().startEntity("test[]");
                o.get().literal("1", "One");
                o.get().literal("2", "Two");
                o.get().literal("3", "Three");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().startEntity("test[]");
                o.get().literal("1", "4");
                o.get().literal("2", "5");
                o.get().literal("3", "6");
                f.apply(3).endEntity();
                o.get().startEntity("TEST_4[]");
                o.get().literal("1", "One");
                o.get().literal("2", "Two");
                o.get().literal("3", "Three");
                o.get().literal("4", "4");
                o.get().literal("5", "5");
                o.get().literal("6", "6");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue #601
    public void shouldCopyArraySubFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('TEST_5[]')",
                "copy_field('coll[].*.b', 'TEST_5[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("coll[]");
                i.startEntity("1");
                i.literal("a", "Dog");
                i.literal("b", "Dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("a", "Ape");
                i.literal("b", "Ape");
                i.endEntity();
                i.startEntity("3");
                i.literal("a", "Giraffe");
                i.endEntity();
                i.startEntity("4");
                i.literal("a", "Crocodile");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "Dog");
                o.get().literal("b", "Dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("a", "Ape");
                o.get().literal("b", "Ape");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("a", "Giraffe");
                o.get().endEntity();
                o.get().startEntity("4");
                o.get().literal("a", "Crocodile");
                f.apply(2).endEntity();
                o.get().startEntity("TEST_5[]");
                o.get().literal("1", "Dog");
                o.get().literal("2", "Ape");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToFirstObjectInRepeatedFields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals.$first.kind','nice')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("animals");
                i.literal("name", "cat");
                i.endEntity();
                i.startEntity("animals");
                i.literal("name", "fox");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals");
                o.get().literal("name", "dog");
                o.get().literal("kind", "nice");
                o.get().endEntity();
                o.get().startEntity("animals");
                o.get().literal("name", "cat");
                o.get().endEntity();
                o.get().startEntity("animals");
                o.get().literal("name", "fox");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToLastObjectInRepeatedFields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals.$last.kind','nice')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("animals");
                i.literal("name", "cat");
                i.endEntity();
                i.startEntity("animals");
                i.literal("name", "fox");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("animals");
                o.get().literal("name", "cat");
                o.get().endEntity();
                o.get().startEntity("animals");
                o.get().literal("name", "fox");
                o.get().literal("kind", "nice");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToObjectByIndexInRepeatedFields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals.2.kind','nice')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("animals");
                i.literal("name", "cat");
                i.endEntity();
                i.startEntity("animals");
                i.literal("name", "fox");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("animals");
                o.get().literal("name", "cat");
                o.get().literal("kind", "nice");
                o.get().endEntity();
                o.get().startEntity("animals");
                o.get().literal("name", "fox");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToFirstObjectInIndexedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals[].$first.kind','nice')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "cat");
                i.endEntity();
                i.startEntity("3");
                i.literal("name", "fox");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("name", "dog");
                o.get().literal("kind", "nice");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "cat");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("name", "fox");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToLastObjectInIndexedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals[].$last.kind','nice')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "cat");
                i.endEntity();
                i.startEntity("3");
                i.literal("name", "fox");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "cat");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("name", "fox");
                o.get().literal("kind", "nice");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToObjectByIndexInIndexedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('animals[].2.kind','nice')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "cat");
                i.endEntity();
                i.startEntity("3");
                i.literal("name", "fox");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "cat");
                o.get().literal("kind", "nice");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("name", "fox");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldToFirstObjectMissing() {
        assertThrowsOnEmptyArray("$first");
    }

    @Test
    public void addFieldToLastObjectMissing() {
        assertThrowsOnEmptyArray("$last");
    }

    @Test
    public void addFieldToObjectByIndexMissing() {
        assertThrowsOnEmptyArray("2");
    }

    private void assertThrowsOnEmptyArray(final String index) {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Can't find: " + index + " in: null", () -> {
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "add_field('animals[]." + index + ".kind','nice')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("animals[]");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            );
        });
    }

    @Test
    public void shouldAddArraySubFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "add_field('coll[].*.c', 'test')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("coll[]");
                i.startEntity("1");
                i.literal("a", "Dog");
                i.literal("b", "Dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("a", "Ape");
                i.literal("b", "Ape");
                i.endEntity();
                i.startEntity("3");
                i.literal("a", "Giraffe");
                i.endEntity();
                i.startEntity("4");
                i.literal("a", "Crocodile");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "Dog");
                o.get().literal("b", "Dog");
                o.get().literal("c", "test");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("a", "Ape");
                o.get().literal("b", "Ape");
                o.get().literal("c", "test");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("a", "Giraffe");
                o.get().literal("c", "test");
                o.get().endEntity();
                o.get().startEntity("4");
                o.get().literal("a", "Crocodile");
                o.get().literal("c", "test");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void move() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "move_field('my.name','your.name')",
                "move_field('missing','whatever')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("my");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("my");
                o.get().endEntity();
                o.get().startEntity("your");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void copy() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('your.name','your.name2')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("your");
                o.get().literal("name", "max");
                o.get().literal("name2", "max");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void copyIntoArrayOfStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                // "set_array('author')", <- results in separate objects/entities here
                "copy_field('your.name','author.name[]')",
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "maxi-mi");
                i.literal("name", "maxi-ma");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().startEntity("name[]");
                o.get().literal("1", "maxi-mi");
                o.get().literal("2", "maxi-ma");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void copyArrayOfStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('your','author')",
                "remove_field('your')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "maxi-mi");
                i.literal("name", "maxi-ma");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "maxi-mi");
                o.get().literal("name", "maxi-ma");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void renameArrayOfStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "move_field('your','author')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "maxi-mi");
                i.literal("name", "maxi-ma");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "maxi-mi");
                o.get().literal("name", "maxi-ma");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void copyArrayOfHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('author', 'authors[]')",
                "remove_field('author')"),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("author");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("authors[]");
                o.get().startEntity("1");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void renameArrayOfHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "move_field('author', 'authors[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("author");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("authors[]");
                o.get().startEntity("1");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void copyIntoImplicitArrayAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('your.name','author[].$append.name')",
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("your");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void copyIntoImplicitArrayPrepend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('your.name','author[].$prepend.name')",
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("your");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "mo");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "max");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void copyIntoExplicitArrayAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "copy_field('your.name','author[].$append.name')",
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("your");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void copyIntoArrayTopLevel() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "copy_field('your.name', 'author[]')",
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "maxi-mi");
                i.literal("name", "maxi-ma");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().literal("1", "maxi-mi");
                o.get().literal("2", "maxi-ma");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // See issue metafacture-fix#106
    public void shouldCopyMarkedArrayOfStringsIntoUnmarkedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('animal_string_Array[]', 'animals_repeated_SimpleField')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animal_string_Array[]");
                i.literal("1", "dog");
                i.literal("2", "elefant");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animal_string_Array[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "elefant");
                o.get().endEntity();
                o.get().literal("animals_repeated_SimpleField", "dog");
                o.get().literal("animals_repeated_SimpleField", "elefant");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#106
    public void shouldCopyMarkedArrayOfHashesIntoUnmarkedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('animal_object_Array[]', 'animals_repeated_ObjectField')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animal_object_Array[]");
                i.startEntity("1");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "elefant");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animal_object_Array[]");
                o.get().startEntity("1");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "elefant");
                f.apply(2).endEntity();
                o.get().startEntity("animals_repeated_ObjectField");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("animals_repeated_ObjectField");
                o.get().literal("name", "elefant");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#106
    public void shouldCopyMarkedArrayOfHashesIntoMarkedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('animal_object_Array[]', 'test_animal_object_Array[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animal_object_Array[]");
                i.startEntity("1");
                i.literal("name", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "elefant");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animal_object_Array[]");
                o.get().startEntity("1");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "elefant");
                f.apply(2).endEntity();
                o.get().startEntity("test_animal_object_Array[]");
                o.get().startEntity("1");
                o.get().literal("name", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "elefant");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void removeLiteral() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "remove_field('your.name')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("your");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void removeLiteralAndEntity() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "remove_field('your.name')",
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void removeEntity() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "remove_field('your')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("your");
                i.literal("name", "max");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void removeArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "remove_field('name')"),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
            });
    }

    @Test
    public void removeArrayElementsByWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "remove_field('name.*')"),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();
            });
    }

    @Test
    public void setArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('foo[]','a','b','c')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo[]");
                o.get().literal("1", "a");
                o.get().literal("2", "b");
                o.get().literal("3", "c");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // See issue metafacture-fix#111
    public void setArrayReplaceExisting() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('foo[]','a','b','c')"),
            i -> {
                i.startRecord("1");
                i.startEntity("foo[]");
                i.literal("1", "A");
                i.literal("2", "B");
                i.literal("3", "C");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo[]");
                o.get().literal("1", "a");
                o.get().literal("2", "b");
                o.get().literal("3", "c");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // See issue metafacture-fix#130
    public void setArrayInArrayWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('foo[].*.test[]', 'test')"),
            i -> {
                i.startRecord("1");
                i.startEntity("foo[]");
                i.startEntity("1");
                i.literal("id", "A");
                i.endEntity();
                i.startEntity("2");
                i.literal("id", "B");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("foo[]");
                o.get().startEntity("1");
                o.get().literal("id", "A");
                o.get().startEntity("test[]");
                o.get().literal("1", "test");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().literal("id", "B");
                o.get().startEntity("test[]");
                o.get().literal("1", "test");
                f.apply(3).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void setHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_hash('foo','a': 'b','c': 'd')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().literal("a", "b");
                o.get().literal("c", "d");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    // See issue metafacture-fix#111
    public void setHashReplaceExisting() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_hash('foo','a': 'b','c': 'd')"),
            i -> {
                i.startRecord("1");
                i.startEntity("foo");
                i.literal("a", "B");
                i.literal("c", "D");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().literal("a", "b");
                o.get().literal("c", "d");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void paste() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "paste('my.string','m.n.z','m.n.a','m.n.b','m.n.c','m.n.d','m.n.e')",
                "remove_field('m')"),
            i -> {
                i.startRecord("1");
                i.startEntity("m");
                i.startEntity("n");
                i.literal("a", "eeny");
                i.literal("b", "meeny");
                i.literal("c", "miny");
                i.literal("d", "moe");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("string", "eeny meeny miny moe");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void pasteWithCustomSep() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "paste('my.string','a','b','c','d','join_char': ', ')",
                "remove_field('a','b','c','d')"),
            i -> {
                i.startRecord("1");
                i.literal("a", "eeny");
                i.literal("b", "meeny");
                i.literal("c", "miny");
                i.literal("d", "moe");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("string", "eeny, meeny, miny, moe");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void pasteWithLiteralStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "paste('my.string','~Hi','a','~how are you?')",
                "remove_field('a','b','c','d')"),
            i -> {
                i.startRecord("1");
                i.literal("a", "eeny");
                i.literal("b", "meeny");
                i.literal("c", "miny");
                i.literal("d", "moe");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().literal("string", "Hi eeny how are you?");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    private void shouldPrintRecord(final String before, final String args, final String after, final Consumer<Supplier<StreamReceiver>> consumer, final String expected) {
        MetafixTestHelpers.assertStdout(expected, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    before,
                    "print_record(" + args + ")",
                    after
                ),
                i -> {
                    i.startRecord("rec1");
                    i.literal("a", "eeny");
                    i.literal("a", "meeny");
                    i.startEntity("c");
                    i.literal("d", "moe");
                    i.endEntity();
                    i.endRecord();
                }, o -> {
                    o.get().startRecord("rec1");
                    o.get().literal("a", "eeny");
                    o.get().literal("a", "meeny");
                    o.get().startEntity("c");
                    o.get().literal("d", "moe");
                    o.get().endEntity();

                    if (consumer != null) {
                        consumer.accept(o);
                    }

                    o.get().endRecord();
                }
            )
        );
    }

    @Test
    public void shouldPrintRecord() {
        shouldPrintRecord("", "", "", null,
                "{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}\n");
    }

    @Test
    public void shouldPrintRecordWithPrefix() {
        shouldPrintRecord("", "'<%d:%s>'", "", null,
                "<1:rec1>{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}\n");
    }

    @Test
    public void shouldPrintRecordWithPrefixAndIdField() {
        shouldPrintRecord("", "'<%d:%s>', id: 'c.d'", "", null,
                "<1:moe>{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}\n");
    }

    @Test
    public void shouldPrintRecordWithHeader() {
        shouldPrintRecord("", "header: '<%d:%s>'", "", null,
                "<%d:%s>{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}\n");
    }

    @Test
    public void shouldPrintRecordWithFooter() {
        shouldPrintRecord("", "footer: '<%d:%s>'", "", null,
                "{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}<%d:%s>");
    }

    @Test
    public void shouldPrintRecordWithPrettyPrinting() {
        shouldPrintRecord("", "pretty: 'true'", "", null,
                "{\n" +
                "  \"a\" : [ \"eeny\", \"meeny\" ],\n" +
                "  \"c\" : {\n" +
                "    \"d\" : \"moe\"\n" +
                "  }\n" +
                "}\n"
        );
    }

    @Test
    public void shouldPrintRecordAfterTransformation() {
        shouldPrintRecord("add_field('x', '23')", "", "",
                o -> o.get().literal("x", "23"),
                "{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"},\"x\":\"23\"}\n");
    }

    @Test
    public void shouldPrintRecordBeforeTransformation() {
        shouldPrintRecord("", "", "add_field('x', '23')",
                o -> o.get().literal("x", "23"),
                "{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}\n");
    }

    @Test
    public void shouldPrintRecordToFile() throws IOException {
        MetafixTestHelpers.assertTempFile(
                "{\"a\":[\"eeny\",\"meeny\"],\"c\":{\"d\":\"moe\"}}\n",
                p -> shouldPrintRecord("", "destination: '" + p + "'", "", null, ""));
    }

    @Test
    public void hashFromArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('foo','a','b','c','d')",
                "hash('foo')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().literal("a", "b");
                o.get().literal("c", "d");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void arrayFromHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_hash('foo','a': 'b','c': 'd')",
                "array('foo')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("foo", "a");
                o.get().literal("foo", "b");
                o.get().literal("foo", "c");
                o.get().literal("foo", "d");
                o.get().endRecord();
            });
    }

    @Test
    public void shouldCallMacro() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('test')",
                "do put_macro('test')",
                "  add_field('test.$append', '42')",
                "end",
                "call_macro('test')",
                "call_macro('test')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                f.apply(2).literal("test", "42");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotCallUnknownMacro() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Macro 'test' undefined!", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "call_macro('test')",
                    "call_macro('test')"
                ),
                i -> {
                    i.startRecord("1");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldCallMacroWithVariables() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('test')",
                "put_vars(a: '1', b: '2')", // global variables
                "do put_macro('test', b: '22', c: '33')", // "static" local variables
                "  add_field('test.$append', '$[a]-$[b]-$[c]-$[d]')",
                "end",
                "call_macro('test', c: '333', d: '444')", // "dynamic" local variables
                "call_macro('test', b: '555', d: '666')",
                "add_field('vars', '$[a]-$[b]')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "1-22-333-444");
                o.get().literal("test", "1-555-33-666");
                o.get().literal("vars", "1-2");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCallMacroWithVariablesPassedToNestedBinds() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_vars(a: '1', b: '2')", // global variables
                "do put_macro('test', b: '22', c: '33')", // "static" local variables
                "  do once()",
                "    add_field('test', '$[a]-$[b]-$[c]-$[d]')",
                "  end",
                "end",
                "call_macro('test', c: '333', d: '444')", // "dynamic" local variables
                "call_macro('test', b: '555', d: '666')",
                "add_field('vars', '$[a]-$[b]')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "1-22-333-444");
                o.get().literal("vars", "1-2");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCallMacroWithVariablesPassedToNestedConditionals() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "put_vars(a: '1', b: '2')", // global variables
                "do put_macro('test', b: '22', c: '33')", // "static" local variables
                "  if any_equal('cond', '$[d]')",
                "    add_field('test', '$[a]-$[b]-$[c]-$[d]')",
                "  end",
                "end",
                "call_macro('test', c: '333', d: '444')", // "dynamic" local variables
                "call_macro('test', b: '555', d: '666')",
                "add_field('vars', '$[a]-$[b]')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("cond", "666");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("cond", "666");
                o.get().literal("test", "1-555-33-666");
                o.get().literal("vars", "1-2");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotLeakVariablesFromMacro() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Variable 'c' was not assigned!\nAssigned variables:\n{a=1, b=2}", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "put_vars(a: '1', b: '2')", // global variables
                    "do put_macro('test', b: '22', c: '33')", // "static" local variables
                    "end",
                    "call_macro('test', c: '333', d: '444')", // "dynamic" local variables
                    "call_macro('test', b: '555', d: '666')",
                    "add_field('test', '$[a]-$[b]-$[c]-$[d]')"
                ),
                i -> {
                    i.startRecord("1");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldCallNestedMacro() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do put_macro('test1', c: '23')",
                "  add_field('test$[a]', '42')",
                "  call_macro('test2', b: '$[b]', c: '$[c]')",
                "end",
                "do put_macro('test2')",
                "  add_field('test$[b]', '$[c]')",
                "end",
                "call_macro('test1', a: '1', b: '2')",
                "call_macro('test1', a: '3', b: '4')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test1", "42");
                o.get().literal("test2", "23");
                o.get().literal("test3", "42");
                o.get().literal("test4", "23");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIncludeCallerLocationAndTextForExceptionInMacro() {
        final String format = "Error while executing Fix expression (at FILE, line %d): %s";

        final String text1 = "call_macro('test')";
        final String text2 = "append('animals', ' is cool')";
        final String message = String.format(format, 4, text1) + " -> " + String.format(format, 2, text2);

        MetafixTestHelpers.assertThrows(FixExecutionException.class, s -> s.replaceAll("file:/.+?\\.fix", "FILE"), message, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "do put_macro('test')",
                    text2,
                    "end",
                    text1
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
    public void shouldIncludeCallerLocationAndTextForExceptionInIncludedMacro() {
        final String format = "Error while executing Fix expression (at FILE, line %d): %s";

        final String text1 = "call_macro('test')";
        final String text2 = "append('animals', ' is cool')";
        final String message = String.format(format, 2, text1) + " -> " + String.format(format, 2, text2);

        MetafixTestHelpers.assertThrows(FixExecutionException.class, s -> s.replaceAll("file:/.+?\\.fix", "FILE"), message, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "include('src/test/resources/org/metafacture/metafix/fixes/macro.fix')",
                    text1
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
    public void reject() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('_metadata.error')",
                "  reject()",
                "end"),
            i -> {
                i.startRecord("1");
                i.startEntity("_metadata");
                i.literal("error", "details");
                i.endEntity();
                i.endRecord();

                i.startRecord("2");
                i.endRecord();
            }, o -> {
                o.get().startRecord("2");
                o.get().endRecord();
            });
    }

    @Test
    @MetafixToDo("Is set_array with $append something we need/want? WDCD?")
    public void appendArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('nums[]', '1')",
                "set_array('nums[].$append', '2', '3')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("nums[]");
                o.get().literal("1", "1");
                o.get().literal("2", "2");
                o.get().literal("3", "3");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void mixedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('@context[]', 'https://w3id.org/kim/lrmi-profile/draft/context.jsonld')",
                "set_hash('@context[].$append', '@language': 'de')"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("@context[]");
                o.get().literal("1", "https://w3id.org/kim/lrmi-profile/draft/context.jsonld");
                o.get().startEntity("2");
                o.get().literal("@language", "de");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void retain() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "retain('1','3')"),
            i -> {
                i.startRecord("1");
                i.literal("1", "one");
                i.literal("2", "two");
                i.literal("3", "tre");
                i.literal("4", "for");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("1", "one");
                o.get().literal("3", "tre");
                o.get().endRecord();
            });
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void retainNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "retain('a.b.c','a.[cd].b','b[].2.c','c[].*.a','c[].2.b')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("a");
                i.startEntity("a");
                i.literal("b", "1");
                i.literal("c", "2");
                i.endEntity();
                i.startEntity("b");
                i.literal("c", "1");
                i.literal("d", "2");
                i.endEntity();
                i.startEntity("c");
                i.literal("b", "1");
                i.literal("c", "2");
                i.endEntity();
                i.endEntity();
                i.startEntity("b[]");
                i.startEntity("1");
                i.literal("a", "1");
                i.literal("b", "2");
                i.endEntity();
                i.startEntity("2");
                i.literal("a", "1");
                i.literal("b", "2");
                i.literal("c", "3");
                i.endEntity();
                i.startEntity("3");
                i.literal("c", "4");
                i.endEntity();
                i.endEntity();
                i.startEntity("c[]");
                i.startEntity("1");
                i.literal("a", "1");
                i.literal("b", "2");
                i.endEntity();
                i.startEntity("2");
                i.literal("a", "1");
                i.literal("b", "2");
                i.literal("c", "3");
                i.endEntity();
                i.startEntity("3");
                i.literal("c", "4");
                i.endEntity();
                i.endEntity();
                i.startEntity("d");
                i.literal("e", "5");
                i.endEntity();
                i.literal("e", "6");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("a");
                o.get().startEntity("b");
                o.get().literal("c", "1");
                o.get().endEntity();
                o.get().startEntity("c");
                o.get().literal("b", "1");
                f.apply(2).endEntity();
                o.get().startEntity("b[]");
                o.get().startEntity("1");
                o.get().literal("c", "3");
                f.apply(2).endEntity();
                o.get().startEntity("c[]");
                o.get().startEntity("1");
                o.get().literal("a", "1");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("a", "1");
                o.get().literal("b", "2");
                o.get().endEntity();
                o.get().startEntity("3");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void retainNestedReservedFields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "retain('b[].$first.b','c[].$last')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("b[]");
                i.startEntity("1");
                i.literal("a", "1");
                i.literal("b", "2");
                i.endEntity();
                i.startEntity("2");
                i.literal("a", "1");
                i.literal("b", "2");
                i.literal("c", "3");
                i.endEntity();
                i.startEntity("3");
                i.literal("c", "4");
                i.endEntity();
                i.endEntity();
                i.startEntity("c[]");
                i.startEntity("1");
                i.literal("a", "1");
                i.literal("b", "2");
                i.endEntity();
                i.startEntity("2");
                i.literal("a", "1");
                i.literal("b", "2");
                i.literal("c", "3");
                i.endEntity();
                i.startEntity("3");
                i.literal("c", "4");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("b[]");
                o.get().startEntity("1");
                o.get().literal("b", "2");
                f.apply(2).endEntity();
                o.get().startEntity("c[]");
                o.get().startEntity("1");
                o.get().literal("c", "4");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("1[]");
                i.literal("1", "one");
                i.endEntity();
                i.startEntity("2[]");
                i.endEntity();
                i.startEntity("3[]");
                i.literal("1", "tre");
                i.endEntity();
                i.startEntity("4[]");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("1[]");
                o.get().literal("1", "one");
                o.get().endEntity();
                o.get().startEntity("3[]");
                o.get().literal("1", "tre");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("1");
                i.literal("1", "one");
                i.endEntity();
                i.startEntity("2");
                i.endEntity();
                i.startEntity("3");
                i.literal("1", "tre");
                i.endEntity();
                i.startEntity("4");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("1");
                o.get().literal("1", "one");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("1", "tre");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.literal("1", "one");
                i.literal("2", "");
                i.literal("3", "tre");
                i.literal("4", "");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("1", "one");
                o.get().literal("3", "tre");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyNestedArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("arrays");
                i.startEntity("1[]");
                i.literal("1", "one");
                i.endEntity();
                i.startEntity("2[]");
                i.endEntity();
                i.startEntity("3[]");
                i.literal("1", "tre");
                i.endEntity();
                i.startEntity("4[]");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("arrays");
                o.get().startEntity("1[]");
                o.get().literal("1", "one");
                o.get().endEntity();
                o.get().startEntity("3[]");
                o.get().literal("1", "tre");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyNestedHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("hashes");
                i.startEntity("1");
                i.literal("1", "one");
                i.endEntity();
                i.startEntity("2");
                i.endEntity();
                i.startEntity("3");
                i.literal("1", "tre");
                i.endEntity();
                i.startEntity("4");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("hashes");
                o.get().startEntity("1");
                o.get().literal("1", "one");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("1", "tre");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyNestedStrings() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("nested");
                i.literal("1", "one");
                i.literal("2", "");
                i.literal("3", "tre");
                i.literal("4", "");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("nested");
                o.get().literal("1", "one");
                o.get().literal("3", "tre");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyDeeplyNestedArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("arrays[]");
                i.endEntity();
                i.startEntity("hashes");
                i.startEntity("foo[]");
                i.endEntity();
                i.endEntity();
                i.literal("me", "1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("me", "1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDeleteEmptyArraysInArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("arrays[]");
                i.startEntity("1[]");
                i.endEntity();
                i.startEntity("2[]");
                i.literal("1", ":-P yuck");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("arrays[]");
                o.get().startEntity("1[]");
                o.get().literal("1", ":-P yuck");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void nulls() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "retain('1','2','3')"),
            i -> {
                i.startRecord("1");
                i.literal("1", "one");
                i.literal("2", "");
                i.literal("3", null);
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("1", "one");
                o.get().literal("2", "");
                o.get().endRecord();
            });
    }

    @Test
    public void repeatToArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "max");
                o.get().literal("name", "mo");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void accessArrayByIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name.2')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "max");
                o.get().literal("name", "MO");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void transformSingleField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "MAX");
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("Same name, is replaced. Repeated fields to array?")
    public void transformRepeatedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "MAX");
                o.get().literal("name", "MO");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldEmitNestedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("test");
                i.startEntity("zoo[]");
                i.startEntity("1");
                i.startEntity("animals[]");
                i.startEntity("1[]");
                i.literal("1", "ant");
                i.literal("2", "dog");
                i.endEntity();
                i.literal("2", "cat");
                i.startEntity("3[]");
                i.literal("1", "fish");
                i.startEntity("2[]");
                i.literal("1", "zebra");
                i.literal("2", "horse");
                i.endEntity();
                i.literal("3", "hippo");
                i.endEntity();
                i.literal("4", "giraffe");
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("test");
                o.get().startEntity("zoo[]");
                o.get().startEntity("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1[]");
                o.get().literal("1", "ant");
                o.get().literal("2", "dog");
                o.get().endEntity();
                o.get().literal("2", "cat");
                o.get().startEntity("3[]");
                o.get().literal("1", "fish");
                o.get().startEntity("2[]");
                o.get().literal("1", "zebra");
                o.get().literal("2", "horse");
                o.get().endEntity();
                o.get().literal("3", "hippo");
                o.get().endEntity();
                o.get().literal("4", "giraffe");
                f.apply(4).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void emitEntityForRepeatedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "max");
                o.get().literal("2", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void dontEmitEntityForSingleField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);
                i.startRecord("1");
                i.literal("name", "max");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "max");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setRepeatedFieldsToEntitiesAndSetEntityMemberName() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);
                i.setEntityMemberName("*");

                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("*", "max");
                o.get().literal("*", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setRepeatedFieldsToEntitiesAndSetEntityMemberNameWithNumericalSubfield() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);
                i.setEntityMemberName("*");

                i.startRecord("1");
                i.startEntity("1001 ");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 ");
                o.get().startEntity("1");
                o.get().literal("*", "max");
                o.get().literal("*", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setRepeatedFieldsToEntitiesWithNumericalSubfields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);

                i.startRecord("1");
                i.startEntity("1001 ");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 ");
                o.get().startEntity("1");
                o.get().literal("1", "max");
                o.get().literal("2", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setEntityMemberNameNoArrayMarkerOrEntity() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setEntityMemberName("*"); // no arrays or entities, no effect

                i.startRecord("1");
                i.startEntity("1001 ");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 ");
                o.get().literal("1", "max");
                o.get().literal("1", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setEntityMemberNameWithArrayMarker() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setEntityMemberName("*");

                i.startRecord("1");
                i.startEntity("1001 []");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 []");
                o.get().literal("*", "max");
                o.get().literal("*", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    public void shouldNotAccessArrayImplicitly() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "upcase('name')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("name", "max");
                    i.literal("name", "mo");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldAccessArrayByWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name.*')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("name", "MAX");
                o.get().literal("name", "MO");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void repeatToArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("author");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("author");
                o.get().literal("name", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void accessArrayOfObjectsByIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('author.2.name')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("author");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("author");
                o.get().literal("name", "MO");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void accessArrayOfObjectsByWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('author.*.name')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("author");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "MAX");
                o.get().endEntity();
                o.get().startEntity("author");
                o.get().literal("name", "MO");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void accessArrayOfObjectsByDoListBind() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path':'author','var':'a')",
                "  upcase('a.name')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("author");
                i.literal("name", "max");
                i.endEntity();
                i.startEntity("author");
                i.literal("name", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("name", "MAX");
                o.get().endEntity();
                o.get().startEntity("author");
                o.get().literal("name", "MO");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAddRandomNumber() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "random(test, '100')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "marc");
                o.get().literal("title", "json");
                o.get().literal(ArgumentMatchers.eq("test"), ArgumentMatchers.argThat(i -> Integer.parseInt(i) < 100));
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
    public void shouldReplaceExistingValueWithRandomNumber() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "random(others, '100')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("others", "human");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal(ArgumentMatchers.eq("others"), ArgumentMatchers.argThat(i -> Integer.parseInt(i) < 100));
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAddRandomNumberToMarkedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "random('animals[].$append', '100')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "cat");
                i.literal("2", "dog");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "cat");
                o.get().literal("2", "dog");
                o.get().literal(ArgumentMatchers.eq("3"), ArgumentMatchers.argThat(i -> Integer.parseInt(i) < 100));
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAddObjectWithRandomNumberToMarkedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('bnimals[]')",
                "random('bnimals[].$append.number', '100')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("bnimals[]");
                o.get().startEntity("1");
                o.get().literal(ArgumentMatchers.eq("number"), ArgumentMatchers.argThat(i -> Integer.parseInt(i) < 100));
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAddRandomNumberToUnmarkedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "random('animals.$append', '100')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animals", "cat");
                i.literal("animals", "dog");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animals", "cat");
                o.get().literal("animals", "dog");
                o.get().literal(ArgumentMatchers.eq("animals"), ArgumentMatchers.argThat(i -> Integer.parseInt(i) < 100));
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("See issue metafacture-fix#100")
    public void shouldNotAppendRandomNumberToHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "random('animals.$append', '100')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals");
                i.literal("1", "cat");
                i.literal("2", "dog");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals");
                o.get().literal("1", "cat");
                o.get().literal("2", "dog");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRenameFieldsInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "rename(your, '[ae]', X)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("your");
                i.literal("name", "nicolas");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("your");
                o.get().literal("nXmX", "nicolas");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
    public void shouldRecursivelyRenameFieldsInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "rename(others, ani, QR)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("others");
                i.literal("animal", "human");
                i.literal("canister", "metall");
                i.startEntity("area");
                i.literal("ani", "test");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("others");
                o.get().literal("QRmal", "human");
                o.get().literal("cQRster", "metall");
                o.get().startEntity("area");
                o.get().literal("QR", "test");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
    public void shouldRecursivelyRenameFieldsInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "rename('animals[]', ani, XY)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("animal", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("animal", "cat");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("XYmal", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("XYmal", "cat");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("java.lang.ArrayIndexOutOfBoundsException: 0; see issue metafacture-fix#100")
    public void shouldRenameAllFieldsInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "rename('.', ani, XY)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals");
                i.literal("animal", "dog");
                i.literal("animal", "cat");
                i.endEntity();
                i.startEntity("others");
                i.literal("animal", "human");
                i.literal("canister", "metall");
                i.startEntity("area");
                i.literal("ani", "test");
                i.endEntity();
                i.endEntity();
                i.startEntity("fictional");
                i.literal("animal", "unicorn");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("XYmals");
                o.get().literal("XYmal", "dog");
                o.get().literal("XYmal", "cat");
                o.get().endEntity();
                o.get().startEntity("others");
                o.get().literal("XYmal", "human");
                o.get().literal("cXYster", "metall");
                o.get().startEntity("area");
                o.get().literal("XY", "test");
                f.apply(2).endEntity();
                o.get().startEntity("fictional");
                o.get().literal("XYmal", "unicorn");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("java.lang.ArrayIndexOutOfBoundsException: 0; see issue #601")
    public void shouldRenameArrayFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "rename('OTHERS[].*', 'd', 'XY')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("OTHERS[]");
                i.startEntity("1");
                i.literal("name", "Jake");
                i.startEntity("dnimals[]");
                i.literal("1", "dog");
                i.literal("2", "zebra");
                i.literal("3", "cat");
                i.endEntity();
                i.startEntity("dumbers[]");
                i.literal("1", "7");
                i.literal("2", "2");
                i.literal("3", "1");
                i.literal("4", "10");
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("OTHERS[]");
                o.get().startEntity("1");
                o.get().literal("name", "Jake");
                o.get().startEntity("XYnimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "zebra");
                o.get().literal("3", "cat");
                o.get().endEntity();
                o.get().startEntity("XYumbers[]");
                o.get().literal("1", "7");
                o.get().literal("2", "2");
                o.get().literal("3", "1");
                o.get().literal("4", "10");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    private void shouldNotAddTimestamp(final String options, final String message) {
        MetafixTestHelpers.assertProcessException(MorphBuildException.class, message, () -> shouldAddTimestamp(options, ""));
    }

    private void shouldAddTimestamp(final String options, final String pattern) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "timestamp(test" + options + ")"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal(ArgumentMatchers.eq("test"), ArgumentMatchers.matches(pattern));
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAddTimestamp() {
        shouldAddTimestamp("", "\\d+");
    }

    @Test
    public void shouldAddTimestampWithFormat() {
        shouldAddTimestamp(", format: 'yyyy-MM-dd'", "\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    public void shouldNotAddTimestampWithUnsupportedFormat() {
        shouldNotAddTimestamp(", format: \"'\"", "The date/time format ''' is not supported. ");
    }

    @Test
    public void shouldAddTimestampWithLanguage() {
        shouldAddTimestamp(", format: 'yyyy-MM-dd G', language: 'pl'", "\\d{4}-\\d{2}-\\d{2} n\\.e\\.");
    }

    @Test
    public void shouldNotAddTimestampWithUnsupportedLanguage() {
        shouldNotAddTimestamp(", language: '--'", "Language '--' not supported.");
    }

    @Test
    public void shouldAddTimestampWithTimezone() {
        shouldAddTimestamp(", format: 'yyyy-MM-dd Z', timezone: 'UTC'", "\\d{4}-\\d{2}-\\d{2} \\+0000");
    }

    @Test
    @MetafixToDo("See https://github.com/metafacture/metafacture-fix/pull/170")
    public void shouldNotSplitLiteralName() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.literal("123. ", "foo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("123. ", "foo");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotSplitEntityName() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("123. ");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("123. ");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

}
