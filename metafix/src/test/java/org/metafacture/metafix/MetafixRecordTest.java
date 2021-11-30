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
 * Tests Metafix record level methods. Following the cheat sheet
 * examples at https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixRecordTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixRecordTest() {
    }

    @Test
    public void entitiesPassThrough() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"),
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
    public void internalIdUsage() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "copy_field('_id', id)"),
            i -> {
                i.startRecord("1");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("id", "1");
                o.get().endRecord();
            });
    }

    @Test
    public void entitiesPassThroughRepeatNestedEntity() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"),
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
                o.get().startEntity("1");
                o.get().literal("field", "value1");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("field", "value2");
                f.apply(3).endEntity();
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
                "add_field('my.name','nicolas')"),
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
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("my");
                o.get().startEntity("name");
                o.get().literal("1", "patrick");
                o.get().literal("2", "nicolas");
                f.apply(2).endEntity();
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("my");
                o.get().startEntity("name");
                o.get().literal("1", "max");
                o.get().literal("2", "patrick");
                o.get().literal("3", "nicolas");
                f.apply(2).endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().startEntity("my");
                o.get().startEntity("name");
                o.get().literal("1", "patrick");
                o.get().literal("2", "nicolas");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void move() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(// TODO: dot noation in move_field
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
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(// TODO dot notation in copy_field
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
    public void copyIntoArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('author[]')",
                "copy_field('your.name','author[].name')",
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
                o.get().startEntity("name");
                o.get().endEntity();
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
                o.get().startEntity("foo");
                o.get().literal("1", "a");
                o.get().literal("2", "b");
                o.get().literal("3", "c");
                o.get().literal("4", "d");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void reject() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists ('_metadata.error')",
                "  reject()",
                "end"),
            i -> {
                i.startRecord("1");
                i.literal("_metadata.error", "details");
                i.endRecord();
                i.startRecord("2");
                i.endRecord();
            }, o -> {
                o.get().startRecord("2");
                o.get().endRecord();
            });
    }

    @Test
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

    @Test
    public void vacuum() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"),
            i -> {
                i.startRecord("1");
                i.literal("1", "one");
                i.literal("2", "");
                i.literal("3", "tre");
                i.literal("4", "");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().literal("1", "one");
                o.get().literal("3", "tre");
                o.get().endRecord();
            });
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
                "vacuum()"),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "max");
                o.get().literal("2", "mo");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void accessArrayByIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name.2')"),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "max");
                o.get().literal("2", "MO");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void accessArrayImplicit() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name')"),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "MAX");
                o.get().literal("2", "MO");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    @Disabled("TODO: WDCD? explicit * for array fields?")
    public void accessArrayByWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('name.*')"),
            i -> {
                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("1", "MAX");
                o.get().literal("2", "MO");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void repeatToArrayOfObjects() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "vacuum()"),
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
                o.get().startEntity("author");
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
    public void accessArrayOfObjectsByIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('author.2.name')"),
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
                o.get().startEntity("author");
                o.get().startEntity("1");
                o.get().literal("name", "max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "MO");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    @Disabled("TODO: implement implicit iteration?")
    public void accessArrayOfObjectsByWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('author.*.name')",
                "vacuum()"),
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
                o.get().startEntity("author");
                o.get().startEntity("1");
                o.get().literal("name", "MAX");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "MO");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void accessArrayOfObjectsByDoListBind() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list('path':'author','var':'a')",
                "  upcase('a.name')",
                "end",
                "vacuum()"),
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
                o.get().startEntity("author");
                o.get().startEntity("1");
                o.get().literal("name", "MAX");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "MO");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

}
