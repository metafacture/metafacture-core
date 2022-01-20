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

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Tests Metafix field level methods. Following the cheat sheet examples at
 * https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixMethodTest {

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixMethodTest() {
    }

    @Test
    public void upcase() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('title')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "MARC");
                o.get().literal("2", "JSON");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void upcaseDotNotationNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('data.title')"),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endEntity();
                i.endRecord();
            }, (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().startEntity("title");
                o.get().literal("1", "MARC");
                o.get().literal("2", "JSON");
                f.apply(2).endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void downcase() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "downcase('title')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "MARC");
                i.literal("title", "Json");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "marc");
                o.get().literal("2", "json");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void capitalize() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "capitalize('title')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "Marc");
                o.get().literal("2", "Json");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void substring() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('title', '0', '2')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "m");
                o.get().literal("2", "j");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void substringWithVar() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('title', '0', '$[end]')"),
                ImmutableMap.of("end", "3"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "ma");
                o.get().literal("2", "js");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void trim() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('title')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title", "  marc  ");
                i.literal("title", "  json  ");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("title");
                o.get().literal("1", "marc");
                o.get().literal("2", "json");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void format() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "format(number,'%-5s: %s')"), // TODO actual number formatting with JSON-equiv record
            i -> {
                i.startRecord("1");
                i.literal("number", "41");
                i.literal("number", "15");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("number");
                o.get().literal("1", "41   : 15");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void parseText() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "parse_text(date, '(\\\\d{4})-(\\\\d{2})-(\\\\d{2})')"),
            i -> {
                i.startRecord("1");
                i.literal("date", "2015-03-07");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("date");
                o.get().literal("1", "2015");
                o.get().literal("2", "03");
                o.get().literal("3", "07");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void parseTextNamedGroups() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "parse_text(date, '(?<year>\\\\d{4})-(?<month>\\\\d{2})-(?<day>\\\\d{2})')"),
            i -> {
                i.startRecord("1");
                i.literal("date", "2015-03-07");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("date");
                o.get().literal("year", "2015");
                o.get().literal("month", "03");
                o.get().literal("day", "07");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void parseTextNestedNamedGroups() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "parse_text(data, '.(?<outer1>.(?<inner1>.).(?<inner2>.).).(?<outer2>.).')"),
            i -> {
                i.startRecord("1");
                i.literal("data", "abcdefghi");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("outer1", "bcdef");
                o.get().literal("inner1", "c");
                o.get().literal("inner2", "e");
                o.get().literal("outer2", "h");
                o.get().endEntity();
                o.get().endRecord();
            });
    }

    @Test
    public void parseTextMixedGroups() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "parse_text(data, '(?<a>.)(.)(?<c>.)')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("data", "abc");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("a", "a");
                o.get().literal("c", "c");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void parseTextEscapedGroups() {
        assertThrows(IllegalArgumentException.class, "No group with name <c>", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "parse_text(data, '(?<a>.)(.)\\\\(?<c>.\\\\)')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("data", "ab(<c>c)");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void parseTextQuotedGroups() {
        assertThrows(IllegalArgumentException.class, "No group with name <c>", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "parse_text(data, '(?<a>.)(.)\\\\Q(?<c>.)\\\\E')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("data", "ab(?<c>.)");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void alternation() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('title-1|title-2')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title-1", "  marc  ");
                i.literal("title-2", "  json  ");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("title-1", "marc");
                o.get().literal("title-2", "json");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void wildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('title-?')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title-1", "  marc  ");
                i.literal("title-2", "  json  ");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("title-1", "marc");
                o.get().literal("title-2", "json");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void characterClass() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('title-[12]')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.literal("title-1", "  marc  ");
                i.literal("title-2", "  json  ");
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().literal("title-1", "marc");
                o.get().literal("title-2", "json");
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void shouldDoNothing() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("title");
                o.get().literal("1", "marc");
                o.get().literal("2", "json");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAppendValue() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "append(title, ' ?!')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "metafix ?!");
                o.get().endRecord();
            }
        );
    }

    @Test
    // TODO: Fix order (`animols.name` should stay before `animols.type`)
    public void shouldAppendValueInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "append(animols.name, ' boss')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animols");
                i.literal("name", "bird");
                i.literal("type", "TEST");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animols");
                o.get().literal("type", "TEST");
                o.get().literal("name", "bird boss");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldAppendValueInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "append('animals[].1', ' is cool')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog is cool");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/100
    public void shouldAppendValueInEntireArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "append('animals[].*', ' is cool')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog is cool");
                o.get().literal("2", "cat is cool");
                o.get().literal("3", "zebra is cool");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @Disabled("See https://github.com/metafacture/metafacture-fix/issues/100")
    public void shouldNotAppendValueToArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "append('animals[]', ' is cool')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCountNumberOfValuesInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "count(numbers)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "41");
                i.literal("numbers", "42");
                i.literal("numbers", "6");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("numbers", "4");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCountNumberOfValuesInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "count(person)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("person");
                i.literal("name", "François");
                i.literal("age", "12");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("person", "2");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFilterArrayValues() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "filter(animals, '[Cc]at')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animals", "Lion");
                i.literal("animals", "Cat");
                i.literal("animals", "Tiger");
                i.literal("animals", "Bobcat");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals");
                o.get().literal("1", "Cat");
                o.get().literal("2", "Bobcat");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFilterArrayValuesInverted() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "filter(animals, '[Cc]at', invert: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animals", "Lion");
                i.literal("animals", "Cat");
                i.literal("animals", "Tiger");
                i.literal("animals", "Bobcat");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals");
                o.get().literal("1", "Lion");
                o.get().literal("2", "Tiger");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/100
    public void shouldFilterArrayObjectValues() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "filter('animals[]', '[Cc]at')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "Lion");
                i.literal("2", "Cat");
                i.literal("3", "Tiger");
                i.literal("4", "Bobcat");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "Cat");
                o.get().literal("2", "Bobcat");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldGetFirstIndexOfSubstring() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "index(animal, 'n')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("animal", "bunny");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animal", "2");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldGetIndexOfSubstring() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "index(title, 't')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "2");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldJoinArrayField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "join_field(numbers, '/')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "6");
                i.literal("numbers", "42");
                i.literal("numbers", "41");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("numbers", "6/42/41/6");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/100
    public void shouldJoinArrayObjectField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "join_field('animals[]', ',')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("animals[]", "dog,cat,zebra");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldPrependValue() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend(title, 'I love ')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "I love metafix");
                o.get().endRecord();
            }
        );
    }

    @Test
    // TODO: Fix order (`animols.name` should stay before `animols.type`)
    public void shouldPrependValueInHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend(animols.name, 'nice ')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animols");
                i.literal("name", "bird");
                i.literal("type", "TEST");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animols");
                o.get().literal("type", "TEST");
                o.get().literal("name", "nice bird");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldPrependValueInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend('animals[].1', 'cool ')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "cool dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/100
    public void shouldPrependValueInEntireArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend('animals[].*', 'cool ')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "cool dog");
                o.get().literal("2", "cool cat");
                o.get().literal("3", "cool zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @Disabled("See https://github.com/metafacture/metafacture-fix/issues/100")
    public void shouldNotPrependValueToArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend('animals[]', 'cool ')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cat");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all(title, '[aei]', 'X')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "mXtXfXx");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/issues/100
    public void shouldReplaceAllRegexesInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('animals[].*', a, QR)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.literal("1", "dog");
                i.literal("2", "cat");
                i.literal("3", "zebra");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "cQRt");
                o.get().literal("3", "zebrQR");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReverseString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "reverse(title)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "xifatem");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReverseArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "reverse(title)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("title");
                o.get().literal("1", "json");
                o.get().literal("2", "marc");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSortField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sort_field(numbers)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "6");
                i.literal("numbers", "42");
                i.literal("numbers", "41");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("numbers");
                o.get().literal("1", "41");
                o.get().literal("2", "42");
                o.get().literal("3", "6");
                o.get().literal("4", "6");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSortFieldNumerically() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sort_field(numbers, numeric: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "6");
                i.literal("numbers", "42");
                i.literal("numbers", "41");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("numbers");
                o.get().literal("1", "6");
                o.get().literal("2", "6");
                o.get().literal("3", "41");
                o.get().literal("4", "42");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFailToSortNumericallyWithInvalidNumber() {
        assertThrows(NumberFormatException.class, "For input string: \"x\"", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "sort_field(numbers, numeric: 'true')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("numbers", "6");
                    i.literal("numbers", "42");
                    i.literal("numbers", "x");
                    i.literal("numbers", "6");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldSortFieldInReverse() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sort_field(numbers, reverse: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "6");
                i.literal("numbers", "42");
                i.literal("numbers", "41");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("numbers");
                o.get().literal("1", "6");
                o.get().literal("2", "6");
                o.get().literal("3", "42");
                o.get().literal("4", "41");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSortFieldNumericallyInReverse() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sort_field(numbers, numeric: 'true', reverse: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "6");
                i.literal("numbers", "42");
                i.literal("numbers", "41");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("numbers");
                o.get().literal("1", "42");
                o.get().literal("2", "41");
                o.get().literal("3", "6");
                o.get().literal("4", "6");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSortFieldAndRemoveDuplicates() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sort_field(numbers, uniq: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "6");
                i.literal("numbers", "42");
                i.literal("numbers", "41");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("numbers");
                o.get().literal("1", "41");
                o.get().literal("2", "42");
                o.get().literal("3", "6");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSplitStringField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "split_field(date, '-')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("date", "1918-17-16");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("date");
                o.get().literal("1", "1918");
                o.get().literal("2", "17");
                o.get().literal("3", "16");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSplitArrayField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "split_field(date, '-')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("date", "1918-17-16");
                i.literal("date", "2021-22-23");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("date");
                o.get().startEntity("1");
                o.get().literal("1", "1918");
                o.get().literal("2", "17");
                o.get().literal("3", "16");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("1", "2021");
                o.get().literal("2", "22");
                o.get().literal("3", "23");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSplitHashField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "split_field(date, '-')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("date");
                i.literal("a", "1918-17-16");
                i.literal("b", "2021-22-23");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("date");
                o.get().startEntity("a");
                o.get().literal("1", "1918");
                o.get().literal("2", "17");
                o.get().literal("3", "16");
                o.get().endEntity();
                o.get().startEntity("b");
                o.get().literal("1", "2021");
                o.get().literal("2", "22");
                o.get().literal("3", "23");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @Disabled("See https://github.com/metafacture/metafacture-fix/issues/100")
    public void shouldSplitNestedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "split_field('others[].*.tools', '--')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("others[]");
                i.startEntity("1");
                i.literal("tools", "hammer--saw--bow");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("others[]");
                o.get().startEntity("1");
                o.get().startEntity("tools");
                o.get().literal("1", "hammer");
                o.get().literal("2", "saw");
                o.get().literal("3", "bow");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSumNumbers() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sum(numbers)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "41");
                i.literal("numbers", "42");
                i.literal("numbers", "6");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("numbers", "95");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRemoveDuplicates() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq(numbers)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("numbers", "41");
                i.literal("numbers", "42");
                i.literal("numbers", "6");
                i.literal("numbers", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("numbers");
                o.get().literal("1", "41");
                o.get().literal("2", "42");
                o.get().literal("3", "6");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    private void assertThrows(final Class<?> expectedClass, final String expectedMessage, final Executable executable) {
        final Throwable exception = Assertions.assertThrows(MetafactureException.class, executable).getCause();
        Assertions.assertSame(expectedClass, exception.getClass());
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

}
