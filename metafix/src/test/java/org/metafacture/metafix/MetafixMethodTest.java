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

package org.metafacture.metafix; // checkstyle-disable-line JavaNCSS

import org.metafacture.framework.StreamReceiver;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Tests Metafix field level methods. Following the cheat sheet examples at
 * https://github.com/LibreCat/Catmandu/wiki/Fixes-Cheat-Sheet
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class) // checkstyle-disable-line JavaNCSS
@ExtendWith(MetafixToDo.Extension.class)
public class MetafixMethodTest {

    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=daLgsPSvD9A";

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixMethodTest() {
    }

    @Test
    public void shouldUpcaseString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('title')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "MARC");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldUpcaseDotNotationNested() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "upcase('data.title')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "marc");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", "MARC");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDowncaseString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "downcase('title')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "MARC");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "marc");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldDowncaseStringsInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "downcase('title.*')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "MARC");
                i.literal("title", "Json");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "marc");
                o.get().literal("title", "json");
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("NumberFormatException: For input string: '*'")
    public void shouldFilterArrayWithAsteriskAtEnd() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "filter('title[].*', 'faust')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("title[]");
                i.startEntity("1[]");
                i.literal("1", "faust");
                i.endEntity();
                i.startEntity("2[]");
                i.literal("1", "räuber");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("title[]");
                o.get().startEntity("1[]");
                o.get().literal("1", "faust");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCapitalizeString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "capitalize('title')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Marc");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCapitalizeStringsInArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "capitalize('title.*')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.literal("title", "json");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "Marc");
                o.get().literal("title", "Json");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotCapitalizeArray() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "capitalize('title')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("title", "marc");
                    i.literal("title", "json");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldGetSubstringOfString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('rel', '5', '3')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("rel", "grandson");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("rel", "son");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldGetSubstringOfStringWithoutLength() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('rel', '5')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("rel", "grandson");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("rel", "son");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldGetSubstringOfTruncatedString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('rel', '5', '6')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("rel", "grandson");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("rel", "son");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotGetSubstringOutsideOfString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('rel', '9', '3')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("rel", "grandson");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("rel", "grandson");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldGetSubstringWithVar() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "substring('rel', '0', '$[length]')"
            ),
            ImmutableMap.of("length", "5"),
            i -> {
                i.startRecord("1");
                i.literal("rel", "grandson");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("rel", "grand");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldTrimStringSingle() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('title')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "  marc  ");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "marc");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotTrimRepeatedField() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "trim('data.title')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("title", "  marc  ");
                    i.literal("title", "  json  ");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldNotTrimIndexedArray() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "trim('data.title[]')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.startEntity("title[]");
                    i.literal("1", "  marc  ");
                    i.literal("2", "  json  ");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldNotTrimHash() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Hash", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "trim('data.title')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.startEntity("title");
                    i.literal("1", "  marc  ");
                    i.literal("2", "  json  ");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/pull/133
    public void shouldNotTrimStringInImplicitArrayOfHashes() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected Hash, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "trim('data.title')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("title", "  marc  ");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("title", "  json  ");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().startEntity("data");
                    o.get().literal("title", "  marc  ");
                    o.get().endEntity();
                    o.get().startEntity("data");
                    o.get().literal("title", "  json  ");
                    o.get().endEntity();
                    o.get().endRecord();
                }
            )
        );
    }

    @Test
    // See https://github.com/metafacture/metafacture-fix/pull/133
    public void shouldTrimStringInExplicitArrayOfHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('data.*.title')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "  marc  ");
                i.endEntity();
                i.startEntity("data");
                i.literal("title", "  json  ");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", "marc");
                o.get().endEntity();
                o.get().startEntity("data");
                o.get().literal("title", "json");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
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
                o.get().literal("number", "41   : 15");
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
                o.get().literal("date", "2015");
                o.get().literal("date", "03");
                o.get().literal("date", "07");
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
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "No group with name <c>", () ->
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
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "No group with name <c>", () ->
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
    public void wildcardSingleChar() {
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
    public void wildcardMultiChar() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('title*')"),
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
    public void wildcardNestedPartialSingle() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('work.title-?')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("work");
                i.literal("title-1", "  marc  ");
                i.literal("title-2", "  json  ");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("work");
                o.get().literal("title-1", "marc");
                o.get().literal("title-2", "json");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    public void wildcardNestedPartialMulti() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('work.title*')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("work");
                i.literal("title-1", "  marc  ");
                i.literal("title-2", "  json  ");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("work");
                o.get().literal("title-1", "marc");
                o.get().literal("title-2", "json");
                o.get().endEntity();
                o.get().endRecord();

                o.get().startRecord("3");
                o.get().endRecord();
            });
    }

    @Test
    @MetafixToDo("See issue #601")
    public void wildcardFullFieldNonIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('*')"),
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
    @MetafixToDo("See issue #601")
    public void wildcardNestedFullFieldNonIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "trim('work.*')"),
            i -> {
                i.startRecord("1");
                i.endRecord();

                i.startRecord("2");
                i.startEntity("work");
                i.literal("title-1", "  marc  ");
                i.literal("title-2", "  json  ");
                i.endEntity();
                i.endRecord();

                i.startRecord("3");
                i.endRecord();
            }, o -> {
                o.get().startRecord("1");
                o.get().endRecord();

                o.get().startRecord("2");
                o.get().startEntity("work");
                o.get().literal("title-1", "marc");
                o.get().literal("title-2", "json");
                o.get().endEntity();
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
                o.get().literal("title", "marc");
                o.get().literal("title", "json");
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
                o.get().literal("name", "bird boss");
                o.get().literal("type", "TEST");
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
    // See issue metafacture-fix#100
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
    public void shouldNotAppendValueToArray() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "append('animals[]', 'another one')"
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
                }
            )
        );
    }

    @Test
    // See issue metafacture-fix#100
    public void shouldNotAppendValueToHash() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Hash", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "append('animals', ' is cool')"
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
    // See issue metafacture-fix#123
    public void shouldIncludeLocationAndTextInExecutionException() {
        final String text = "append('animals', ' is cool')";
        final String message = "Error while executing Fix expression (at FILE, line 2): " + text;

        MetafixTestHelpers.assertThrows(FixExecutionException.class, s -> s.replaceAll("file:/.+?\\.fix", "FILE"), message, () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "# comment",
                    text,
                    "nothing()"
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
                o.get().literal("animals", "Cat");
                o.get().literal("animals", "Bobcat");
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
                o.get().literal("animals", "Lion");
                o.get().literal("animals", "Tiger");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertNullFromJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "null");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "null");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertStringFromJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "\"eeny meeny miny moe\"");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "eeny meeny miny moe");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertArrayFromJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "[\"eeny\",\"meeny\",\"miny\",\"moe\"]");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "eeny");
                o.get().literal("test", "meeny");
                o.get().literal("test", "miny");
                o.get().literal("test", "moe");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertNestedArrayFromJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "{\"zoo[]\":[{\"animals[]\":[[\"ant\",\"dog\"],\"cat\",[\"fish\",[\"zebra\",\"horse\"],\"hippo\"],\"giraffe\"]}]}");
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
    public void shouldConvertHashFromJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "{\"a\":[\"eeny\",\"meeny\"],\"b\":\"miny\",\"c\":{\"d\":\"moe\"}}");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("test");
                o.get().literal("a", "eeny");
                o.get().literal("a", "meeny");
                o.get().literal("b", "miny");
                o.get().startEntity("c");
                o.get().literal("d", "moe");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertHashFromPrettyJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test",
                        "{\n" +
                        "  \"a\" : [ \"eeny\", \"meeny\" ],\n" +
                        "  \"b\" : \"miny\",\n" +
                        "  \"c\" : {\n" +
                        "    \"d\" : \"moe\"\n" +
                        "  }\n" +
                        "}"
                );
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("test");
                o.get().literal("a", "eeny");
                o.get().literal("a", "meeny");
                o.get().literal("b", "miny");
                o.get().startEntity("c");
                o.get().literal("d", "moe");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotConvertInvalidObjectFromJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "eeny meeny miny moe");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "eeny meeny miny moe");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertInvalidObjectFromJsonWithErrorString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "from_json(test, error_string: 'invalid')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "eeny meeny miny moe");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "invalid");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
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
    public void shouldFlattenFlatArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "flatten('flat')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("flat", "1");
                i.literal("flat", "2");
                i.literal("flat", "3");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("flat", "1");
                o.get().literal("flat", "2");
                o.get().literal("flat", "3");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFlattenFlatHash() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "flatten('flat')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("flat");
                i.literal("a", "1");
                i.literal("a", "2");
                i.literal("a", "3");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("flat");
                o.get().literal("a", "1");
                o.get().literal("a", "2");
                o.get().literal("a", "3");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFlattenFlatString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "flatten('flat')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("flat", "1");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("flat", "1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFlattenNestedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "flatten('deep[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("deep[]");
                i.literal("1", "1");
                i.startEntity("2[]");
                i.literal("1", "2");
                i.literal("2", "3");
                i.endEntity();
                i.startEntity("3[]");
                i.startEntity("1[]");
                i.literal("1", "4");
                i.literal("2", "5");
                i.endEntity();
                i.literal("2", "6");
                i.endEntity();
                i.literal("4", "7");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("deep[]");
                o.get().literal("1", "1");
                o.get().literal("2", "2");
                o.get().literal("3", "3");
                o.get().literal("4", "4");
                o.get().literal("5", "5");
                o.get().literal("6", "6");
                o.get().literal("7", "7");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFlattenNestedArrayWithHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "flatten('deep[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("deep[]");
                i.literal("1", "1");
                i.startEntity("2");
                i.literal("a", "2");
                i.literal("a", "3");
                i.endEntity();
                i.startEntity("3[]");
                i.startEntity("1");
                i.literal("a", "4");
                i.literal("a", "5");
                i.endEntity();
                i.literal("2", "6");
                i.endEntity();
                i.literal("4", "7");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("deep[]");
                o.get().literal("1", "1");
                o.get().startEntity("2");
                o.get().literal("a", "2");
                o.get().literal("a", "3");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("a", "4");
                o.get().literal("a", "5");
                o.get().endEntity();
                o.get().literal("4", "6");
                o.get().literal("5", "7");
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
    public void shouldCleanseISBN10() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "isbn(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "ISBN: 1-.93.3-988-31-2 EUro 17.70");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "1933988312");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertISBN10ToISBN13() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "isbn(test, to: 'isbn13')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "1933988312");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "9781933988313");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertISBN13ToISBN10() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "isbn(test, to: 'isbn10')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "9781933988313");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "1933988312");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotProcessInvalidISBN10() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "isbn(test, verify_check_digit: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "1933988313");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSetInvalidISBN10ToErrorString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "isbn(test, verify_check_digit: 'true', error_string: 'invalid')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "1933988313");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "invalid");
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
    public void shouldJoinSingleField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "join_field(number, '/')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("number", "6");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("number", "6");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
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
                o.get().startEntity("animals[]");
                o.get().literal("1", "dog,cat,zebra");
                o.get().endEntity();
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
                o.get().literal("name", "nice bird");
                o.get().literal("type", "TEST");
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
    // See issue metafacture-fix#100
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
    // See issue #601
    public void shouldPrependValueInNestedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend('nestedTest[].*.test[].*', 'Number ')"
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
                o.get().literal("1", "Number One");
                o.get().literal("2", "Number Two");
                o.get().literal("3", "Number Three");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().startEntity("test[]");
                o.get().literal("1", "Number 4");
                o.get().literal("2", "Number 5");
                o.get().literal("3", "Number 6");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue #601
    public void shouldPrependValueInArraySubField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "prepend('coll[].*.a', 'HELLO ')"
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
                o.get().literal("a", "HELLO Dog");
                o.get().literal("b", "Dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("a", "HELLO Ape");
                o.get().literal("b", "Ape");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("a", "HELLO Giraffe");
                o.get().endEntity();
                o.get().startEntity("4");
                o.get().literal("a", "HELLO Crocodile");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
    public void shouldNotPrependValueToArray() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "prepend('animals[]', 'the first one')"
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
                }
            )
        );
    }

    @Test
    public void shouldNotPrependValueToArrayWithWildcard() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "prepend('animal?[]', 'the first one')"
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
                }
            )
        );
    }

    @Test
    public void shouldNotPrependValueToArrayWithWildcardNested() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "prepend('some.animal?[]', 'the first one')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("some");
                    i.startEntity("animals[]");
                    i.literal("1", "dog");
                    i.literal("2", "cat");
                    i.literal("3", "zebra");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
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
    public void shouldReplaceAllRegexesWithMatch() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all(title, '[aei]', 'X$0Y')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "mXeYtXaYfXiYx");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesWithGroupNumber() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all(title, '([aei])', 'X$1Y')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "mXeYtXaYfXiYx");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesWithGroupName() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all(title, '(?<letter>[aei])', 'X${letter}Y')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "metafix");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "mXeYtXaYfXiYx");
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#100
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
    // See issue #601
    public void shouldReplaceAllRegexesInArraySubField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('coll[].*.a', 'o', '__')"
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
                o.get().literal("a", "D__g");
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
                o.get().literal("a", "Cr__c__dile");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue metafacture-fix#255
    public void shouldFailToReplaceAllInRepeatedSubfieldOfObjectWithAsterisk() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Can't find: 2 in: null", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "replace_all('predecessor[].*.label', 'Vorg. ---> ', '')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("predecessor[]");
                    i.startEntity("1");
                    i.literal("label", "Früher u.d.T.");
                    i.literal("label", "Gewaltprävention in Schule und Jugendhilfe");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldNotInsertOptionalArraySubFieldWithAsteriskInReplaceAll() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('coll[].*.b', 'x', 'y')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("coll[]");
                i.startEntity("1");
                i.literal("a", "Dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("b", "Ape");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "Dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("b", "Ape");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotInsertOptionalRepeatedHashSubFieldWithAsteriskInReplaceAll() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('coll.*.b', 'x', 'y')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("coll");
                i.literal("a", "Dog");
                i.endEntity();
                i.startEntity("coll");
                i.literal("b", "Ape");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("coll");
                o.get().literal("a", "Dog");
                o.get().endEntity();
                o.get().startEntity("coll");
                o.get().literal("b", "Ape");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void replaceAllInOptionalSubfieldInArrayOfObjectsWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('RSWK[].*.subjectGenre', '[.]$', '')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("RSWK[]");
                i.startEntity("1");
                i.literal("subjectTopicName", "Nonprofit organizations");
                i.endEntity();
                i.startEntity("2");
                i.literal("subjectTopicName", "Nonprofit organizations");
                i.literal("subjectGenre", "Case studies.");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("RSWK[]");
                o.get().startEntity("1");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().literal("subjectGenre", "Case studies");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void inDoBindCopyFieldWithVarInSourceAndTarget() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('RSWK[]')",
                "do list(path: '650??', 'var': '$i')",
                "  copy_field('$i.a', 'RSWK[].$append.subjectTopicName')",
                "  copy_field('$i.v', 'RSWK[].$last.subjectGenre')",
                "end",
                "retain('RSWK[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("650  ");
                i.literal("a", "Nonprofit organizations");
                i.literal("x", "Management.");
                i.endEntity();
                i.startEntity("650  ");
                i.literal("a", "Nonprofit organizations");
                i.literal("x", "Management");
                i.literal("v", "Case studies.");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("RSWK[]");
                o.get().startEntity("1");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().literal("subjectGenre", "Case studies.");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    private void replaceAllWithWildcardAfterCopyFieldWithVarInSourceAndTarget(final boolean explicitArray) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                explicitArray ? "set_array('RSWK[]')" : "",
                "do list(path: '650??', 'var': '$i')",
                "  copy_field('$i.a', 'RSWK[].$append.subjectTopicName')",
                "  copy_field('$i.v', 'RSWK[].$last.subjectGenre')",
                "end",
                "replace_all('RSWK[].*.subjectGenre', '[.]$', '')",
                "retain('RSWK[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("650  ");
                i.literal("a", "Nonprofit organizations");
                i.literal("x", "Management.");
                i.endEntity();
                i.startEntity("650  ");
                i.literal("a", "Nonprofit organizations");
                i.literal("x", "Management");
                i.literal("v", "Case studies.");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("RSWK[]");
                o.get().startEntity("1");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().literal("subjectGenre", "Case studies");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void replaceAllWithWildcardAfterCopyFieldWithVarInSourceAndTarget() {
        replaceAllWithWildcardAfterCopyFieldWithVarInSourceAndTarget(true);
    }

    @Test
    public void replaceAllWithWildcardAfterCopyFieldWithVarInSourceAndTargetImplicitArray() {
        replaceAllWithWildcardAfterCopyFieldWithVarInSourceAndTarget(false);
    }

    @Test
    public void multipleReplaceAllWithWildcardAfterCopyFieldWithVarInSourceAndTarget() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('RSWK[]')",
                "do list(path: '650??', 'var': '$i')",
                "  copy_field('$i.a', 'RSWK[].$append.subjectTopicName')",
                "  copy_field('$i.v', 'RSWK[].$last.subjectGenre')",
                "end",
                "replace_all('RSWK[].*.subjectGenre', '[.]$', '')",
                "replace_all('RSWK[].*.subjectGenre', '[.]$', '')",
                "retain('RSWK[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("650  ");
                i.literal("a", "Nonprofit organizations");
                i.literal("x", "Management.");
                i.endEntity();
                i.startEntity("650  ");
                i.literal("a", "Nonprofit organizations");
                i.literal("x", "Management");
                i.literal("v", "Case studies.");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("RSWK[]");
                o.get().startEntity("1");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("subjectTopicName", "Nonprofit organizations");
                o.get().literal("subjectGenre", "Case studies");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void copyFieldToSubfieldOfArrayOfObjectsWithIndexImplicitAppend() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Can't find: 1 in: null", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "set_array('test[]')",
                    "copy_field('key', 'test[].1.field')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("key", "value");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void copyFieldToSubfieldOfArrayOfObjectsWithExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('test[]')",
                "copy_field('key', 'test[].$append.field')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("key", "value");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("key", "value");
                o.get().startEntity("test[]");
                o.get().startEntity("1");
                o.get().literal("field", "value");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void copyFieldToSubfieldOfArrayOfStringsWithIndexImplicitAppend() {
        MetafixTestHelpers.assertProcessException(IndexOutOfBoundsException.class, "Index 0 out of bounds for length 0", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "set_array('test[]')",
                    "copy_field('key', 'test[].1')"
                ),
                i -> {
                    i.startRecord("1");
                    i.literal("key", "value");
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void copyFieldToSubfieldOfArrayOfStringsWithExplicitAppend() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('test[]')",
                "copy_field('key', 'test[].$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("key", "value");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("key", "value");
                o.get().startEntity("test[]");
                o.get().literal("1", "value");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // We currently fail on unresolved references, see MetafixRecordTests#assertThrowsOnEmptyArray
    public void addFieldIntoArrayOfObjectsWithLastWildcardMissingError() {
        MetafixTestHelpers.assertProcessException(IllegalArgumentException.class, "Can't find: $last in: null", () -> {
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "add_field('animals[].$last.key', 'value')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("animals[]");
                    i.endEntity();
                    i.endRecord();
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    o.get().startEntity("animals[]");
                    o.get().endEntity();
                    o.get().endRecord();
                }
            );
        });
    }

    @Test
    public void addFieldIntoArrayOfObjectsWithLastWildcardAllEmpty() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('animals[].$last')",
                "  add_field('animals[].$last.key', 'value')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldIntoArrayOfObjectsWithLastWildcardLastEmpty() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "if exists('animals[].$last')",
                "  add_field('animals[].$last.key', 'value')",
                "end"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("animals[]");
                i.startEntity("1");
                i.literal("name", "Jake");
                i.literal("type", "dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Blacky");
                i.literal("type", "bird");
                i.endEntity();
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.startEntity("animals[]");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("animals[]");
                o.get().startEntity("1");
                o.get().literal("name", "Jake");
                o.get().literal("type", "dog");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Blacky");
                o.get().literal("type", "bird");
                o.get().literal("key", "value");
                f.apply(2).endEntity();
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().startEntity("animals[]");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInCopiedArraySubField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('coll[]')",
                "copy_field('a', 'coll[].$append.a')",
                "replace_all('coll[].*.a', 'o', '__')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("a", "Dog");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("a", "Dog");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "D__g");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInMovedArraySubField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('coll[]')",
                "move_field('a', 'coll[].$append.a')",
                "replace_all('coll[].*.a', 'o', '__')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("a", "Dog");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "D__g");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInCopiedArraySubFieldOriginal() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('coll[]')",
                "copy_field('a', 'coll[].$append.a')",
                "replace_all('a', 'o', '__')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("a", "Dog");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("a", "D__g");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "Dog");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInListCopiedArraySubField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('coll[]')",
                "do list(path: 'a', 'var': '$i')",
                "  copy_field('$i', 'coll[].$append.a')",
                "end",
                "replace_all('coll[].*.a', 'o', '__')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("a", "Dog");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("a", "Dog");
                o.get().startEntity("coll[]");
                o.get().startEntity("1");
                o.get().literal("a", "D__g");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCopyBindVarWithDollarAfterLookup() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('@coll[]')",
                "do list(path: 'a', 'var': '$i')",
                "  lookup('$i.name')",
                "  copy_field('$i.name', '@coll[].$append')",
                "end",
                "remove_field('a')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("a");
                i.literal("name", "Dog");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("@coll[]");
                o.get().literal("1", "Dog");
                f.apply(1).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCopyToFieldWithIndexAndReservedFieldName() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('a[].*.test[]', 'test')",
                "copy_field('some', 'a[].1.test[].$append')",
                "remove_field('some')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("some", "thing");
                i.startEntity("a[]");
                i.startEntity("1");
                i.literal("name", "Dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Cat");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("a[]");
                o.get().startEntity("1");
                o.get().literal("name", "Dog");
                o.get().startEntity("test[]");
                o.get().literal("1", "test");
                o.get().literal("2", "thing");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Cat");
                o.get().startEntity("test[]");
                o.get().literal("1", "test");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCopyToFieldWithTwoReservedFieldNames() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "set_array('a[].*.test[]', 'test')",
                "copy_field('some', 'a[].$first.test[].$append')",
                "remove_field('some')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("some", "thing");
                i.startEntity("a[]");
                i.startEntity("1");
                i.literal("name", "Dog");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Cat");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("a[]");
                o.get().startEntity("1");
                o.get().literal("name", "Dog");
                o.get().startEntity("test[]");
                o.get().literal("1", "test");
                o.get().literal("2", "thing");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "Cat");
                o.get().startEntity("test[]");
                o.get().literal("1", "test");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldMoveFieldToPathWithIndexAndReservedField() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "move_field('b', 'names[].1.$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("b", "b");
                i.startEntity("names[]");
                i.startEntity("1[]");
                i.literal("1", "a");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("names[]");
                o.get().startEntity("1[]");
                o.get().literal("1", "a");
                o.get().literal("2", "b");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldMoveFieldToPathWithTwoReservedFields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "move_field('b', 'names[].$first.$append')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("b", "b");
                i.startEntity("names[]");
                i.startEntity("1[]");
                i.literal("1", "a");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("names[]");
                o.get().startEntity("1[]");
                o.get().literal("1", "a");
                o.get().literal("2", "b");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    // See issue #601
    public void shouldReplaceAllRegexesInNestedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('nestedTest[].*.test[].*', 'o', '__')"
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
                o.get().literal("2", "Tw__");
                o.get().literal("3", "Three");
                f.apply(2).endEntity();
                o.get().startEntity("2");
                o.get().startEntity("test[]");
                o.get().literal("1", "4");
                o.get().literal("2", "5");
                o.get().literal("3", "6");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void copyFieldWithoutReplaceAllString() {
        copyFieldWithReplaceAllString(false);
    }

    @Test
    public void copyFieldWithReplaceAllString() {
        copyFieldWithReplaceAllString(true);
    }

    private void copyFieldWithReplaceAllString(final boolean replaceAll) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list(path: 'contribution[]', 'var': '$i')",
                "  copy_field('$i.label', '$i.agent.altLabel')",
                "end",
                "do list(path: 'subject[]', 'var': '$i')",
                "  copy_field('$i.label', '$i.altLabel')",
                "end",
                replaceAll ? "replace_all('contribution[].*.agent.altLabel', 't', '')" : "",
                replaceAll ? "replace_all('subject[].*.altLabel', 't', '')" : ""
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("contribution[]");
                i.startEntity("1");
                i.literal("label", "contribution");
                i.endEntity();
                i.endEntity();
                i.startEntity("subject[]");
                i.startEntity("1");
                i.literal("label", "subject");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("contribution[]");
                o.get().startEntity("1");
                o.get().literal("label", "contribution");
                o.get().startEntity("agent");
                o.get().literal("altLabel", replaceAll ? "conribuion" : "contribution");
                f.apply(3).endEntity();
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().literal("label", "subject");
                o.get().literal("altLabel", replaceAll ? "subjec" : "subject");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void addFieldWithoutReplaceAllArray() {
        addFieldWithReplaceAllArray(false);
    }

    @Test
    public void addFieldWithReplaceAllArray() {
        addFieldWithReplaceAllArray(true);
    }

    private void addFieldWithReplaceAllArray(final boolean replaceAll) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list(path: 'contribution[]', 'var': '$i')",
                "  set_array('$i.agent.altLabel[]')",
                "  add_field('$i.agent.altLabel[].$append', 'contribution')",
                "end",
                "do list(path: 'subject[]', 'var': '$i')",
                "  set_array('$i.altLabel[]')",
                "  add_field('$i.altLabel[].$append', 'subject')",
                "end",
                replaceAll ? "replace_all('contribution[].*.agent.altLabel[].*', 't', '')" : "",
                replaceAll ? "replace_all('subject[].*.altLabel[].*', 't', '')" : ""
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("contribution[]");
                i.startEntity("1");
                i.literal("label", "contribution");
                i.endEntity();
                i.endEntity();
                i.startEntity("subject[]");
                i.startEntity("1");
                i.literal("label", "subject");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("contribution[]");
                o.get().startEntity("1");
                o.get().literal("label", "contribution");
                o.get().startEntity("agent");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "conribuion" : "contribution");
                f.apply(4).endEntity();
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().literal("label", "subject");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "subjec" : "subject");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setArrayWithoutReplaceAll() {
        setArrayWithReplaceAll(false);
    }

    @Test
    public void setArrayWithReplaceAll() {
        setArrayWithReplaceAll(true);
    }

    private void setArrayWithReplaceAll(final boolean replaceAll) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list(path: 'contribution[]', 'var': '$i')",
                "  set_array('$i.agent.altLabel[]', 'contribution')",
                "end",
                "do list(path: 'subject[]', 'var': '$i')",
                "  set_array('$i.altLabel[]', 'subject')",
                "end",
                replaceAll ? "replace_all('contribution[].*.agent.altLabel[].*', 't', '')" : "",
                replaceAll ? "replace_all('subject[].*.altLabel[].*', 't', '')" : ""
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("contribution[]");
                i.startEntity("1");
                i.literal("label", "contribution");
                i.endEntity();
                i.endEntity();
                i.startEntity("subject[]");
                i.startEntity("1");
                i.literal("label", "subject");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("contribution[]");
                o.get().startEntity("1");
                o.get().literal("label", "contribution");
                o.get().startEntity("agent");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "conribuion" : "contribution");
                f.apply(4).endEntity();
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().literal("label", "subject");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "subjec" : "subject");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void copyFieldWithoutReplaceAllArray() {
        copyFieldWithReplaceAllArray(false);
    }

    @Test
    public void copyFieldWithReplaceAllArray() {
        copyFieldWithReplaceAllArray(true);
    }

    private void copyFieldWithReplaceAllArray(final boolean replaceAll) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list(path: 'contribution[]', 'var': '$i')",
                "  set_array('$i.agent.altLabel[]')",
                "  copy_field('$i.label', '$i.agent.altLabel[].$append')",
                "end",
                "do list(path: 'subject[]', 'var': '$i')",
                "  set_array('$i.altLabel[]')",
                "  copy_field('$i.label', '$i.altLabel[].$append')",
                "end",
                replaceAll ? "replace_all('contribution[].*.agent.altLabel[].*', 't', '')" : "",
                replaceAll ? "replace_all('subject[].*.altLabel[].*', 't', '')" : ""
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("contribution[]");
                i.startEntity("1");
                i.literal("label", "contribution");
                i.endEntity();
                i.endEntity();
                i.startEntity("subject[]");
                i.startEntity("1");
                i.literal("label", "subject");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("contribution[]");
                o.get().startEntity("1");
                o.get().literal("label", "contribution");
                o.get().startEntity("agent");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "conribuion" : "contribution");
                f.apply(4).endEntity();
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().literal("label", "subject");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "subjec" : "subject");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void pasteWithoutReplaceAll() {
        pasteWithReplaceAll(false);
    }

    @Test
    public void pasteWithReplaceAll() {
        pasteWithReplaceAll(true);
    }

    private void pasteWithReplaceAll(final boolean replaceAll) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "do list(path: 'contribution[]', 'var': '$i')",
                "  set_array('$i.agent.altLabel[]')",
                "  paste('$i.agent.altLabel[].$append', '$i.label', '~!')",
                "end",
                "do list(path: 'subject[]', 'var': '$i')",
                "  set_array('$i.altLabel[]')",
                "  paste('$i.altLabel[].$append', '$i.label', '~!')",
                "end",
                replaceAll ? "replace_all('contribution[].*.agent.altLabel[].*', ' !', '')" : "",
                replaceAll ? "replace_all('subject[].*.altLabel[].*', ' !', '')" : ""
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("contribution[]");
                i.startEntity("1");
                i.literal("label", "contribution");
                i.endEntity();
                i.endEntity();
                i.startEntity("subject[]");
                i.startEntity("1");
                i.literal("label", "subject");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("contribution[]");
                o.get().startEntity("1");
                o.get().literal("label", "contribution");
                o.get().startEntity("agent");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "contribution" : "contribution !");
                f.apply(4).endEntity();
                o.get().startEntity("subject[]");
                o.get().startEntity("1");
                o.get().literal("label", "subject");
                o.get().startEntity("altLabel[]");
                o.get().literal("1", replaceAll ? "subject" : "subject !");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInArrayByIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('names.2', 'a', 'X')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("names", "Max");
                i.literal("names", "Jake");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("names", "Max");
                o.get().literal("names", "JXke");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInArrayByArrayWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('names.$last', 'a', 'X')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("names", "Max");
                i.literal("names", "Jake");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("names", "Max");
                o.get().literal("names", "JXke");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInArraySubFieldByIndex() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('names[].2.name', 'a', 'X')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("names[]");
                i.startEntity("1");
                i.literal("name", "Max");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Jake");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("names[]");
                o.get().startEntity("1");
                o.get().literal("name", "Max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "JXke");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldReplaceAllRegexesInArraySubFieldByArrayWildcard() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "replace_all('names[].$last.name', 'a', 'X')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("names[]");
                i.startEntity("1");
                i.literal("name", "Max");
                i.endEntity();
                i.startEntity("2");
                i.literal("name", "Jake");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("names[]");
                o.get().startEntity("1");
                o.get().literal("name", "Max");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("name", "JXke");
                f.apply(2).endEntity();
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
                o.get().literal("title", "json");
                o.get().literal("title", "marc");
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("See issue #601")
    public void shouldReverseArrayOfStringsWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "reverse('test[].*')"
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
                o.get().literal("1", "enO");
                o.get().literal("2", "owT");
                o.get().literal("3", "eerhT");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("java.lang.ArrayIndexOutOfBoundsException: 0; see issue #601")
    public void shouldReverseArrayOfHashesWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "reverse('ANIMALS[].*')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("ANIMALS[]");
                i.startEntity("1");
                i.literal("Aanimal", "dog");
                i.literal("name", "Jake");
                i.endEntity();
                i.startEntity("2");
                i.literal("Aanimal", "parrot");
                i.literal("name", "Blacky");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("ANIMALS[]");
                o.get().startEntity("1");
                o.get().literal("Aanimal", "parrot");
                o.get().literal("name", "Blacky");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("Aanimal", "dog");
                o.get().literal("name", "Jake");
                f.apply(2).endEntity();
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
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("numbers", "41");
                o.get().literal("numbers", "42");
                f.apply(2).literal("numbers", "6");
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
            (o, f) -> {
                o.get().startRecord("1");
                f.apply(2).literal("numbers", "6");
                o.get().literal("numbers", "41");
                o.get().literal("numbers", "42");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldFailToSortNumericallyWithInvalidNumber() {
        MetafixTestHelpers.assertExecutionException(NumberFormatException.class, "For input string: \"x\"", () ->
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
            (o, f) -> {
                o.get().startRecord("1");
                f.apply(2).literal("numbers", "6");
                o.get().literal("numbers", "42");
                o.get().literal("numbers", "41");
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
            (o, f) -> {
                o.get().startRecord("1");
                o.get().literal("numbers", "42");
                o.get().literal("numbers", "41");
                f.apply(2).literal("numbers", "6");
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
                o.get().literal("numbers", "41");
                o.get().literal("numbers", "42");
                o.get().literal("numbers", "6");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSortArrayFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sort_field('OTHERS[].*.dnimals[]')"
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
                o.get().startEntity("dnimals[]");
                o.get().literal("1", "cat");
                o.get().literal("2", "dog");
                o.get().literal("3", "zebra");
                o.get().endEntity();
                o.get().startEntity("dumbers[]");
                o.get().literal("1", "7");
                o.get().literal("2", "2");
                o.get().literal("3", "1");
                o.get().literal("4", "10");
                f.apply(3).endEntity();
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
                o.get().literal("date", "1918");
                o.get().literal("date", "17");
                o.get().literal("date", "16");
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
            o -> {
                o.get().startRecord("1");
                o.get().literal("date", "1918");
                o.get().literal("date", "17");
                o.get().literal("date", "16");
                o.get().literal("date", "2021");
                o.get().literal("date", "22");
                o.get().literal("date", "23");
                o.get().endRecord();
            }
        );
    }

    @Test
    @MetafixToDo("Arrays in arrays need to be preserved. See disabled isArray in FixPath#appendIn.")
    public void moveToNestedArray() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "move_field('date[]', 'd[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("date[]");
                i.startEntity("1[]");
                i.literal("1", "1918");
                i.literal("2", "17");
                i.literal("3", "16");
                i.endEntity();
                i.startEntity("2[]");
                i.literal("1", "2021");
                i.literal("2", "22");
                i.literal("3", "23");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("d[]");
                o.get().startEntity("1[]");
                o.get().literal("1", "1918");
                o.get().literal("2", "17");
                o.get().literal("3", "16");
                o.get().endEntity();
                o.get().startEntity("2[]");
                o.get().literal("1", "2021");
                o.get().literal("2", "22");
                o.get().literal("3", "23");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldSplitMarkedArrayFieldIntoArrayOfArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "split_field('date[]', '-')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("date[]", "1918-17-16");
                i.literal("date[]", "2021-22-23");
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("date[]");
                o.get().startEntity("1[]");
                o.get().literal("1", "1918");
                o.get().literal("2", "17");
                o.get().literal("3", "16");
                o.get().endEntity();
                o.get().startEntity("2[]");
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
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("date");
                o.get().literal("a", "1918");
                o.get().literal("a", "17");
                o.get().literal("a", "16");
                o.get().literal("b", "2021");
                o.get().literal("b", "22");
                o.get().literal("b", "23");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
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
                o.get().literal("tools", "hammer");
                o.get().literal("tools", "saw");
                o.get().literal("tools", "bow");
                f.apply(2).endEntity();
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
    public void shouldSumArrayFieldWithAsterisk() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "sum('OTHERS[].*.dumbers[]')"
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
                o.get().startEntity("dnimals[]");
                o.get().literal("1", "dog");
                o.get().literal("2", "zebra");
                o.get().literal("3", "cat");
                o.get().endEntity();
                o.get().startEntity("dumbers[]");
                o.get().literal("1", "20");
                f.apply(3).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertStringToJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "eeny meeny miny moe");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "\"eeny meeny miny moe\"");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertArrayToJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.literal("test", "eeny");
                i.literal("test", "meeny");
                i.literal("test", "miny");
                i.literal("test", "moe");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "[\"eeny\",\"meeny\",\"miny\",\"moe\"]");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertNestedArrayToJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_json(test)"
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
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "{\"zoo[]\":[{\"animals[]\":[[\"ant\",\"dog\"],\"cat\",[\"fish\",[\"zebra\",\"horse\"],\"hippo\"],\"giraffe\"]}]}");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertHashToJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_json(test)"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("test");
                i.literal("a", "eeny");
                i.literal("a", "meeny");
                i.literal("b", "miny");
                i.startEntity("c");
                i.literal("d", "moe");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test", "{\"a\":[\"eeny\",\"meeny\"],\"b\":\"miny\",\"c\":{\"d\":\"moe\"}}");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldConvertHashToPrettyJson() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_json(test, pretty: 'true')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("test");
                i.literal("a", "eeny");
                i.literal("a", "meeny");
                i.literal("b", "miny");
                i.startEntity("c");
                i.literal("d", "moe");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("test",
                        "{\n" +
                        "  \"a\" : [ \"eeny\", \"meeny\" ],\n" +
                        "  \"b\" : \"miny\",\n" +
                        "  \"c\" : {\n" +
                        "    \"d\" : \"moe\"\n" +
                        "  }\n" +
                        "}"
                );
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRemoveDuplicateStrings() {
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
                o.get().literal("numbers", "41");
                o.get().literal("numbers", "42");
                o.get().literal("numbers", "6");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRemoveDuplicateArraysAtDifferentPath() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('arrays[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("arrays[]");
                i.startEntity("1");
                i.literal("number", "41");
                i.literal("number", "23");
                i.endEntity();
                i.startEntity("2");
                i.literal("number", "42");
                i.literal("number", "23");
                i.endEntity();
                i.startEntity("3");
                i.literal("number", "6");
                i.literal("number", "23");
                i.endEntity();
                i.startEntity("4");
                i.literal("number", "6");
                i.literal("number", "23");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("arrays[]");
                o.get().startEntity("1");
                o.get().literal("number", "41");
                o.get().literal("number", "23");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("number", "42");
                o.get().literal("number", "23");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("number", "6");
                o.get().literal("number", "23");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRemoveDuplicateArrays() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('arrays[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("arrays[]");
                i.startEntity("1");
                i.literal("number", "41");
                i.literal("number", "23");
                i.endEntity();
                i.startEntity("2");
                i.literal("number", "42");
                i.literal("number", "23");
                i.endEntity();
                i.startEntity("3");
                i.literal("number", "6");
                i.literal("number", "23");
                i.endEntity();
                i.startEntity("3");
                i.literal("number", "6");
                i.literal("number", "23");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("arrays[]");
                o.get().startEntity("1");
                o.get().literal("number", "41");
                o.get().literal("number", "23");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("number", "42");
                o.get().literal("number", "23");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("number", "6");
                o.get().literal("number", "23");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRemoveDuplicateHashesAtDifferentPath() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('hashes[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("hashes[]");
                i.startEntity("1");
                i.literal("number", "41");
                i.endEntity();
                i.startEntity("2");
                i.literal("number", "42");
                i.endEntity();
                i.startEntity("3");
                i.literal("number", "6");
                i.endEntity();
                i.startEntity("4");
                i.literal("number", "6");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("hashes[]");
                o.get().startEntity("1");
                o.get().literal("number", "41");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("number", "42");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("number", "6");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldRemoveDuplicateHashes() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uniq('hashes[]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("hashes[]");
                i.startEntity("1");
                i.literal("number", "41");
                i.endEntity();
                i.startEntity("2");
                i.literal("number", "42");
                i.endEntity();
                i.startEntity("3");
                i.literal("number", "6");
                i.endEntity();
                i.startEntity("3");
                i.literal("number", "6");
                i.endEntity();
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("hashes[]");
                o.get().startEntity("1");
                o.get().literal("number", "41");
                o.get().endEntity();
                o.get().startEntity("2");
                o.get().literal("number", "42");
                o.get().endEntity();
                o.get().startEntity("3");
                o.get().literal("number", "6");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldApplyCustomJavaFunction() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "org.metafacture.metafix.util.TestFunction(data, foo: '42', bar: 'baz')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "marc");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "marc");
                o.get().literal("test", "DATA");
                o.get().literal("foo", "42");
                o.get().literal("bar", "baz");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldUriEncodeString() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uri_encode('title')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("title", "café");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("title", "caf%C3%A9");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldUriEncodePathSegment() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uri_encode('id')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("id", "/DE-A96:% (3)#!");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("id", "%2FDE-A96%3A%25+%283%29%23%21");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldUriEncodePathSegmentWithoutPlusForSpace() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uri_encode('id', plus_for_space:'false')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("id", "/DE-A96:% (3)#!");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("id", "%2FDE-A96%3A%25%20%283%29%23%21");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldUriEncodePathSegmentWithoutSafeChars() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "uri_encode('id', safe_chars:'')"
            ),
            i -> {
                i.startRecord("1");
                i.literal("id", "/DE-A96:% (3)#!");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().literal("id", "%2FDE%2DA96%3A%25+%283%29%23%21");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldTransformStringToBase64() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_base64('data.title')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "this-is-a-test");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", "dGhpcy1pcy1hLXRlc3Q=");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldTransformUrlSafeToBase64() {
        urlToBase64(",url_safe:'true'", "aHR0cHM6Ly93d3cueW91dHViZS5jb20vd2F0Y2g_dj1kYUxnc1BTdkQ5QQ==");
    }

    @Test
    public void shouldTransformNotUrlSafeToBase64AsDefault() {
        urlToBase64("", "aHR0cHM6Ly93d3cueW91dHViZS5jb20vd2F0Y2g/dj1kYUxnc1BTdkQ5QQ==");
    }

    private void urlToBase64(final String option, final String expected) {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_base64('data.title'" + option + ")"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", YOUTUBE_URL);
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", expected);
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test // checkstyle-disable-line JavaNCSS
    public void shouldCreateVariableFromLiteralValue() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_var('data.title', 'testVar')",
                "add_field('testResult', 'This is a $[testVar]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "test");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.endRecord();
                i.startRecord("3");
                i.startEntity("data");
                i.literal("title", "final-test");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", "test");
                o.get().endEntity();
                o.get().literal("testResult", "This is a test");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("testResult", "This is a ");
                o.get().endRecord();
                o.get().startRecord("3");
                o.get().startEntity("data");
                o.get().literal("title", "final-test");
                o.get().endEntity();
                o.get().literal("testResult", "This is a final-test");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldCreateVariableFromLiteralValueWithDefault() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "to_var('data.title', 'testVar', 'default': 'n/a')",
                "add_field('testResult', 'This is a $[testVar]')"
            ),
            i -> {
                i.startRecord("1");
                i.startEntity("data");
                i.literal("title", "test");
                i.endEntity();
                i.endRecord();
                i.startRecord("2");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("data");
                o.get().literal("title", "test");
                o.get().endEntity();
                o.get().literal("testResult", "This is a test");
                o.get().endRecord();
                o.get().startRecord("2");
                o.get().literal("testResult", "This is a n/a");
                o.get().endRecord();
            }
        );
    }

    @Test
    public void shouldNotCreateVariableFromArrayValue() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Array", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "to_var('data.title', 'testVar')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("title", "test1");
                    i.literal("title", "test2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

    @Test
    public void shouldNotCreateVariableFromHashValue() {
        MetafixTestHelpers.assertExecutionException(IllegalStateException.class, "Expected String, got Hash", () ->
            MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                    "to_var('data.title', 'testVar')"
                ),
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.startEntity("title");
                    i.literal("key", "value");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                }
            )
        );
    }

}
