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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Test Metafix binds.
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
@ExtendWith(MetafixToDo.Extension.class)
public class MetafixBindTest {

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixBindTest() {
    }

    @Test
    public void doList() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path': 'name', 'var': 'n')",
                " upcase('n')",
                " trim('n')",
                " copy_field('n', 'author')",
                "end",
                "remove_field('name')"),
            i -> {
                i.startRecord("1");
                i.literal("name", " A University");
                i.literal("name", "Max ");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "MAX");
                o.get().endRecord();
            });
    }

    @Test
    public void doListExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author')",
                "do list('path': 'name', 'var': 'n')",
                " upcase('n')",
                " trim('n')",
                " copy_field('n', 'author.$append')",
                "end",
                "remove_field('name')"),
            i -> {
                i.startRecord("1");
                i.literal("name", " A University");
                i.literal("name", "Max ");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "A UNIVERSITY");
                o.get().literal("author", "MAX");
                o.get().endRecord();
            });
    }

    @Test
    public void doListFullRecordInScope() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path': 'name', 'var': 'n')",
                " if any_equal('type','book')",
                "  paste('title','~Book:','n')",
                " else",
                "  paste('title','~Journal:','n')",
                " end",
                "end",
                "retain('title')"),
            i -> {
                i.startRecord("1");
                i.literal("type", "book");
                i.literal("name", "A book");
                i.endRecord();
                i.startRecord("2");
                i.literal("type", "journal");
                i.literal("name", "A journal");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Book: A book");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("title", "Journal: A journal");
                o.get().endRecord();
            });
    }

    @Test
    public void bindingScopeWithVar() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path':'foo','var':'loop')",
                " copy_field('test','loop.baz')",
                " copy_field('loop.bar','loop.qux')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("foo");
                i.literal("bar", "1");
                i.endEntity();
                i.startEntity("foo");
                i.literal("bar", "2");
                i.endEntity();
                i.literal("test", "42");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().literal("bar", "1");
                o.get().literal("baz", "42");
                o.get().literal("qux", "1");
                o.get().endEntity();
                o.get().startEntity("foo");
                o.get().literal("bar", "2");
                o.get().literal("baz", "42");
                o.get().literal("qux", "2");
                o.get().endEntity();
                o.get().literal("test", "42");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void bindingScopeWithoutVar() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path':'foo')",
                " copy_field('test','baz')",
                " copy_field('bar','qux')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("foo");
                i.literal("bar", "1");
                i.endEntity();
                i.startEntity("foo");
                i.literal("bar", "2");
                i.endEntity();
                i.literal("test", "42");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("foo");
                o.get().literal("bar", "1");
                o.get().literal("qux", "1");
                o.get().endEntity();
                o.get().startEntity("foo");
                o.get().literal("bar", "2");
                o.get().literal("qux", "2");
                o.get().endEntity();
                o.get().literal("test", "42");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void doListPathWithDots() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path': 'some.name', 'var': 'n')",
                " upcase('n')",
                " trim('n')",
                " copy_field('n', 'author')",
                "end",
                "remove_field('some')"),
            i -> {
                i.startRecord("1");
                i.startEntity("some");
                i.literal("name", " A University");
                i.literal("name", "Max ");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "MAX");
                o.get().endRecord();
            });
    }

    @Test
    public void doListPathWithDotsExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author')",
                "do list('path': 'some.name', 'var': 'n')",
                " upcase('n')",
                " trim('n')",
                " copy_field('n', 'author.$append')",
                "end",
                "remove_field('some')"),
            i -> {
                i.startRecord("1");
                i.startEntity("some");
                i.literal("name", " A University");
                i.literal("name", "Max ");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "A UNIVERSITY");
                o.get().literal("author", "MAX");
                o.get().endRecord();
            });
    }

    @Test
    public void doListWithAppendAndLast() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "do list('path': 'creator', 'var': 'c')",
                " copy_field('c.name', 'author[].$append.name')",
                " add_field('author[].$last.type', 'Default')",
                "end",
                "remove_field('creator')"),
            i -> {
                i.startRecord("1");
                i.startEntity("creator");
                i.literal("name", "A University");
                i.endEntity();
                i.startEntity("creator");
                i.literal("name", "Max");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "A University");
                o.get().literal("type", "Default");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Max");
                o.get().literal("type", "Default");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void doListEntitesToLiterals() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path': 'creator', 'var': 'c')",
                " upcase('c.name')",
                " trim('c.name')",
                " copy_field('c.name', 'author')",
                "end",
                "remove_field('creator')"),
            i -> {
                i.startRecord("1");
                i.startEntity("creator");
                i.literal("name", " A University");
                i.endEntity();
                i.startEntity("creator");
                i.literal("name", "Max ");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "MAX");
                o.get().endRecord();
            });
    }

    @Test
    public void doListEntitesToLiteralsExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author')",
                "do list('path': 'creator', 'var': 'c')",
                " upcase('c.name')",
                " trim('c.name')",
                " copy_field('c.name', 'author.$append')",
                "end",
                "remove_field('creator')"),
            i -> {
                i.startRecord("1");
                i.startEntity("creator");
                i.literal("name", " A University");
                i.endEntity();
                i.startEntity("creator");
                i.literal("name", "Max ");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "A UNIVERSITY");
                o.get().literal("author", "MAX");
                o.get().endRecord();
            });
    }

    @Test
    public void doListEntitesToEntities() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "do list('path': 'creator', 'var': 'c')",
                " copy_field('c.name', 'author[].$append.name')",
                " if all_contain('c.name', 'University')",
                "  add_field('author[].$last.type', 'Organization')",
                " else",
                "  add_field('author[].$last.type', 'Person')", //",
                " end",
                "end",
                "remove_field('creator')"),
            i -> {
                i.startRecord("1");
                i.startEntity("creator");
                i.literal("name", "A University");
                i.endEntity();
                i.startEntity("creator");
                i.literal("name", "Max");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "A University");
                o.get().literal("type", "Organization");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Max");
                o.get().literal("type", "Person");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void wildcardForNestedEntities() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "do list('path': 'creator', 'var': 'c')",
                " if any_match('c.role.*.roleTerm.*.value','aut|cre')",
                "  copy_field('c.name', 'author[].$append.name')",
                " end",
                "end",
                "remove_field('creator')"),
            i -> {
                i.startRecord("1");
                i.startEntity("creator");
                i.literal("name", "A University");
                i.startEntity("role");
                i.startEntity("roleTerm");
                i.literal("value", "aut");
                i.endEntity();
                i.startEntity("roleTerm");
                i.literal("value", "tau");
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.startEntity("creator");
                i.literal("name", "Max");
                i.startEntity("role");
                i.startEntity("roleTerm");
                i.literal("value", "cre");
                i.endEntity();
                i.startEntity("roleTerm");
                i.literal("value", "rec");
                i.endEntity();
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Max");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    public void doListIndexedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path': 'name[]', 'var': 'n')",
                " copy_field('n', 'author')",
                "end",
                "remove_field('name[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("name[]");
                i.literal("1", "A University");
                i.literal("2", "Max");
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("author");
                o.get().literal("1", "A University");
                o.get().literal("2", "Max");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void doListIndexedArrayToArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "do list('path': 'name[]', 'var': 'n')",
                " copy_field('n', 'author[].$append.name')",
                "end",
                "remove_field('name[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("name[]");
                i.literal("1", "A University");
                i.literal("2", "Max");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Max");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void doListIndexedArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path': 'name[]', 'var': 'n')",
                " copy_field('n.name', 'author')",
                "end",
                "remove_field('name[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("name[]");
                i.startEntity("1");
                i.literal("name", "A University");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Max");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "Max");
                o.get().endRecord();
            });
    }

    @Test
    public void doListIndexedArrayOfObjectsExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author')",
                "do list('path': 'name[]', 'var': 'n')",
                " copy_field('n.name', 'author.$append')",
                "end",
                "remove_field('name[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("name[]");
                i.startEntity("1");
                i.literal("name", "A University");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Max");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("author", "A University");
                o.get().literal("author", "Max");
                o.get().endRecord();
            });
    }

    @Test
    public void doListIndexedArrayOfObjectsToArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "do list('path': 'name[]', 'var': 'n')",
                " copy_field('n.name', 'author[].$append.name')",
                "end",
                "remove_field('name[]')"),
            i -> {
                i.startRecord("1");
                i.startEntity("name[]");
                i.startEntity("1");
                i.literal("name", "A University");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Max");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("author[]");
                o.get().startEntity("1");
                o.get().literal("name", "A University");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Max");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    private void shouldIterateOverList(final String path, final int expectedCount) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('trace')",
                "do list(path: '" + path + "', 'var': '$i')",
                "  add_field('trace.$append', 'true')",
                "end",
                "retain('trace')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("name", "Mary");
                i.literal("name", "University");
                i.literal("nome", "Max");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                f.apply(expectedCount).literal("trace", "true");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIterateOverList() {
        shouldIterateOverList("name", 2);
    }

    @Test
    // See issue metafacture-fix#119
    public void shouldIterateOverListWithCharacterClass() {
        shouldIterateOverList("n[ao]me", 3);
    }

    @Test
    // See issue metafacture-fix#119
    public void shouldIterateOverListWithAlternation() {
        shouldIterateOverList("name|nome", 3);
    }

    @Test
    // See issue metafacture-fix#119
    public void shouldIterateOverListWithWildcard() {
        shouldIterateOverList("n?me", 3);
    }

    private void shouldIterateOverListOfHashes(final String path, final int expectedCount) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('trace')",
                "do list(path: '" + path + "', 'var': '$i')",
                "  add_field('trace.$append', 'true')",
                "end",
                "retain('trace')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("name");
                i.literal("value", "Mary");
                i.endEntity();
                i.startEntity("name");
                i.literal("value", "University");
                i.endEntity();
                i.startEntity("nome");
                i.literal("value", "Max");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                f.apply(expectedCount).literal("trace", "true");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldIterateOverListOfHashes() {
        shouldIterateOverListOfHashes("name.value", 2);
    }

    @Test
    // See issue metafacture-fix#119
    public void shouldIterateOverListOfHashesWithCharacterClass() {
        shouldIterateOverListOfHashes("n[ao]me.value", 3);
    }

    @Test
    // See issue metafacture-fix#119
    @MetafixToDo("See issue #603")
    public void shouldIterateOverListOfHashesWithAlternation() {
        shouldIterateOverListOfHashes("name.value|nome.value", 3);
    }

    @Test
    // See issue metafacture-fix#119
    public void shouldIterateOverListOfHashesWithWildcard() {
        shouldIterateOverListOfHashes("n?me.value", 3);
    }

    @Test // checkstyle-disable-line JavaNCSS
    // See issue metafacture-fix#119
    public void shouldPerformComplexOperationWithPathWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('coll[]')",
                "do list(path: 'feld?', 'var': '$i')",
                "  add_field('coll[].$append.feldtest', 'true')",
                "  copy_field('$i.a.value', 'coll[].$last.a')",
                "end",
                "retain('coll[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("feldA");
                i.startEntity("a");
                i.literal("value", "Dog");
                i.endEntity();
                i.endEntity();
                i.startEntity("feldA");
                i.startEntity("a");
                i.literal("value", "Ape");
                i.endEntity();
                i.endEntity();
                i.startEntity("feldA");
                i.startEntity("a");
                i.literal("value", "Giraffe");
                i.endEntity();
                i.endEntity();
                i.startEntity("feldB");
                i.startEntity("a");
                i.literal("value", "Crocodile");
                i.endEntity();
                i.endEntity();
                i.startEntity("feldB");
                i.startEntity("a");
                i.literal("value", "Human");
                i.endEntity();
                i.endEntity();
                i.startEntity("feldB");
                i.startEntity("a");
                i.literal("value", "Bird");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("feldtest", "true");
                o.get().literal("a", "Dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("feldtest", "true");
                o.get().literal("a", "Ape");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("feldtest", "true");
                o.get().literal("a", "Giraffe");
                o.get().endEntity();
                o.get().startEntity("4");
                o.get().literal("feldtest", "true");
                o.get().literal("a", "Crocodile");
                o.get().endEntity();
                o.get().startEntity("5");
                o.get().literal("feldtest", "true");
                o.get().literal("a", "Human");
                o.get().endEntity();
                o.get().startEntity("6");
                o.get().literal("feldtest", "true");
                o.get().literal("a", "Bird");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDoListAsWithSingleList() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('sourceOrga[]')",
                "do list_as(orgId: 'ccm:university[]')",
                "  copy_field(orgId, 'sourceOrga[].$append.id')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("ccm:university[]");
                i.literal("1", "https://ror.org/0304hq317");
                i.literal("2", "https://ror.org/014nnvj65");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("ccm:university[]");
                o.get().literal("1", "https://ror.org/0304hq317");
                o.get().literal("2", "https://ror.org/014nnvj65");
                o.get().endEntity();
                o.get().startEntity("sourceOrga[]");
                o.get().startEntity("1");
                o.get().literal("id", "https://ror.org/0304hq317");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("id", "https://ror.org/014nnvj65");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDoListAsWithMultipleLists() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('sourceOrga[]')",
                "do list_as(orgId: 'ccm:university[]', orgName: 'ccm:university_DISPLAYNAME[]', orgLoc: 'ccm:university_LOCATION[]')",
                "  copy_field(orgId, 'sourceOrga[].$append.id')",
                "  copy_field(orgName, 'sourceOrga[].$last.name')",
                "  copy_field(orgLoc, 'sourceOrga[].$last.location')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("ccm:university[]");
                i.literal("1", "https://ror.org/0304hq317");
                i.literal("2", "https://ror.org/014nnvj65");
                i.endEntity();
                i.startEntity("ccm:university_DISPLAYNAME[]");
                i.literal("1", "Gottfried Wilhelm Leibniz Universität Hannover");
                i.literal("2", "Technische Hochschule Köln");
                i.endEntity();
                i.startEntity("ccm:university_LOCATION[]");
                i.literal("1", "Hannover");
                i.literal("2", "Köln");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("ccm:university[]");
                o.get().literal("1", "https://ror.org/0304hq317");
                o.get().literal("2", "https://ror.org/014nnvj65");
                o.get().endEntity();
                o.get().startEntity("ccm:university_DISPLAYNAME[]");
                o.get().literal("1", "Gottfried Wilhelm Leibniz Universität Hannover");
                o.get().literal("2", "Technische Hochschule Köln");
                o.get().endEntity();
                o.get().startEntity("ccm:university_LOCATION[]");
                o.get().literal("1", "Hannover");
                o.get().literal("2", "Köln");
                o.get().endEntity();
                o.get().startEntity("sourceOrga[]");
                o.get().startEntity("1");
                o.get().literal("id", "https://ror.org/0304hq317");
                o.get().literal("name", "Gottfried Wilhelm Leibniz Universität Hannover");
                o.get().literal("location", "Hannover");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("id", "https://ror.org/014nnvj65");
                o.get().literal("name", "Technische Hochschule Köln");
                o.get().literal("location", "Köln");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldDoListAsWithMultipleListsOfDifferentSizes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('sourceOrga[]')",
                "do list_as(orgId: 'ccm:university[]', orgName: 'ccm:university_DISPLAYNAME[]', orgLoc: 'ccm:university_LOCATION[]')",
                "  add_hash('sourceOrga[].$append')",
                "  copy_field(orgId, 'sourceOrga[].$last.id')",
                "  copy_field(orgName, 'sourceOrga[].$last.name')",
                "  copy_field(orgLoc, 'sourceOrga[].$last.location')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("ccm:university[]");
                i.literal("1", "https://ror.org/0304hq317");
                i.literal("2", "https://ror.org/014nnvj65");
                i.endEntity();
                i.startEntity("ccm:university_DISPLAYNAME[]");
                i.literal("1", "Gottfried Wilhelm Leibniz Universität Hannover");
                i.literal("2", "Technische Hochschule Köln");
                i.literal("3", "Universität zu Köln");
                i.literal("4", "Stadtbibliothek Köln");
                i.endEntity();
                i.startEntity("ccm:university_LOCATION[]");
                i.literal("1", "Hannover");
                i.literal("2", "Köln");
                i.literal("3", "Köln");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("ccm:university[]");
                o.get().literal("1", "https://ror.org/0304hq317");
                o.get().literal("2", "https://ror.org/014nnvj65");
                o.get().endEntity();
                o.get().startEntity("ccm:university_DISPLAYNAME[]");
                o.get().literal("1", "Gottfried Wilhelm Leibniz Universität Hannover");
                o.get().literal("2", "Technische Hochschule Köln");
                o.get().literal("3", "Universität zu Köln");
                o.get().literal("4", "Stadtbibliothek Köln");
                o.get().endEntity();
                o.get().startEntity("ccm:university_LOCATION[]");
                o.get().literal("1", "Hannover");
                o.get().literal("2", "Köln");
                o.get().literal("3", "Köln");
                o.get().endEntity();
                o.get().startEntity("sourceOrga[]");
                o.get().startEntity("1");
                o.get().literal("id", "https://ror.org/0304hq317");
                o.get().literal("name", "Gottfried Wilhelm Leibniz Universität Hannover");
                o.get().literal("location", "Hannover");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("id", "https://ror.org/014nnvj65");
                o.get().literal("name", "Technische Hochschule Köln");
                o.get().literal("location", "Köln");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("name", "Universität zu Köln");
                o.get().literal("location", "Köln");
                o.get().endEntity();
                o.get().startEntity("4");
                o.get().literal("name", "Stadtbibliothek Köln");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldExecuteOnlyOnce() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do once()",
                "  add_field(executed, 'true')",
                "end",
                "do once()",
                "  add_field(never_executed, 'true')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
                i.startRecord("2");
                i.endRecord();
                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("executed", "true");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldExecuteOnlyOncePerFixInstance() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do once()",
                "  add_field(executed, 'true')",
                "end",
                "include('src/test/resources/org/metafacture/metafix/fixes/once.fix')"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
                i.startRecord("2");
                i.endRecord();
                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("executed", "true");
                o.get().literal("included", "true");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("included", "true");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().literal("included", "true");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldExecuteOnlyOncePerIdentifier() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do once()",
                "  add_field(executed, 'true')",
                "end",
                "do once()",
                "  add_field(never_executed, 'true')",
                "end",
                "do once('setup')",
                "  add_field(setup, 'true')",
                "end",
                "do once('setup')",
                "  add_field(already_setup, 'true')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
                i.startRecord("2");
                i.endRecord();
                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("executed", "true");
                o.get().literal("setup", "true");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldExecuteOnlyOnceConditionally() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists(execute)",
                "  do once()",
                "    add_field(executed, 'false')",
                "  end",
                "  do once('setup')",
                "    add_field(setup, 'true')",
                "  end",
                "else",
                "  do once()",
                "    add_field(already_executed, 'true')",
                "  end",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
                i.startRecord("2");
                i.literal("execute", "too late");
                i.endRecord();
                i.startRecord("3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("already_executed", "true");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("execute", "too late");
                o.get().literal("setup", "true");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldExecuteCustomJavaContext() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do org.metafacture.metafix.util.TestContext(test, data: '42')",
                "  add_field(title,'marc')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("BEFORE", "test");
                o.get().literal("title", "marc");
                o.get().literal("AFTER", "42");
                o.get().endRecord();
            }
        );
    }

}
