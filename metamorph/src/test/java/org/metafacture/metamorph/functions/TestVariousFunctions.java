/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.metamorph.functions;

import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.TestHelpers;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for various Metamorph functions.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TestVariousFunctions {

    // TODO: This class need to be split into separate classes for each function!

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public TestVariousFunctions() {
    }

    @Test
    public void testRegexpFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data' name='island'>" +
                "    <regexp match='(\\w*) island' format='${1}' />" +
                "  </data>" +
                "  <data source='data' name='year'>" +
                "    <regexp match='\\d\\d\\d\\d' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "Aloha!");
                    i.literal("data", "Oahu island, Hawaii island, Maui island");
                    i.literal("data", "year 1960!");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("island", "Oahu");
                    o.get().literal("island", "Hawaii");
                    o.get().literal("island", "Maui");
                    o.get().literal("year", "1960");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testIsbnFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='isbn' name='withError'>" +
                "    <isbn to='isbn13' errorString='error' />" +
                "  </data>" +
                "  <data source='isbn' name='withoutError'>" +
                "    <isbn to='isbn13' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("isbn", "123 invalid");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("withError", "error");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testSplitFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data' name='island'>" +
                "    <split delimiter=',' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "Oahu,Hawaii,Maui");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("island", "Oahu");
                    o.get().literal("island", "Hawaii");
                    o.get().literal("island", "Maui");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testSubstringFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='a'>" +
                "    <substring start='3' end='5' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("a", "012345");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("a", "34");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testConstantFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data'>" +
                "    <constant value='Hawaii' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "Aloha");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "Hawaii");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testSetReplaceFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data'>" +
                "    <setreplace>" +
                "      <entry name='dt.' value='deutsch' />" +
                "      <entry name='frz.' value='französich' />" +
                "      <entry name='eng.' value='englisch' />" +
                "    </setreplace>" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "dt., frz. und eng.");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "deutsch, französich und englisch");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testCaseFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data'>" +
                "    <case to='upper' />" +
                "  </data>" +
                "  <data source='data'>" +
                "    <case to='lower' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "Aloha");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "ALOHA");
                    o.get().literal("data", "aloha");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testEqualsFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data' name='data1'>" +
                "    <equals string='Aloha' />" +
                "  </data>" +
                "  <data source='data' name='data2'>" +
                "    <not-equals string='Aloha' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "Aloha");
                    i.literal("data", "Hawaii");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data1", "Aloha");
                    o.get().literal("data2", "Hawaii");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testContainsFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data' name='data1'>" +
                "    <contains string='Periodical' />" +
                "  </data>" +
                "  <data source='data' name='data2'>" +
                "    <not-contains string='Periodical' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "1990 Periodical MultiVolumeBook");
                    i.literal("data", "2013 BibliographicResource Book Series");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data1", "1990 Periodical MultiVolumeBook");
                    o.get().literal("data2", "2013 BibliographicResource Book Series");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testBufferFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <combine name='greeting' value='${greet} ${island}' reset='false'>" +
                "    <data source='d1' name='greet' />" +
                "    <data source='d2' name='island'>" +
                "      <buffer />" +
                "    </data>" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("0");
                    i.literal("d1", "Aloha");
                    i.endRecord();
                    i.startRecord("1");
                    i.literal("d2", "Hawaii");
                    i.literal("d2", "Oahu");
                    i.literal("d1", "Aloha");
                    i.endRecord();
                    i.startRecord("2");
                    i.endRecord();
                    i.startRecord("3");
                    i.literal("d1", "Aloha");
                    i.endRecord();
                    i.startRecord("4");
                    i.literal("d2", "to all");
                    i.literal("d1", "Aloha");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("0");
                    o.get().endRecord();
                    o.get().startRecord("1");
                    o.get().literal("greeting", "Aloha Hawaii");
                    o.get().literal("greeting", "Aloha Oahu");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().endRecord();
                    o.get().startRecord("4");
                    o.get().literal("greeting", "Aloha to all");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testOccurrenceFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data' name='l2'>" +
                "    <occurrence only='lessThan 2' />" +
                "  </data>" +
                "  <data source='data' name='2'>" +
                "    <occurrence only='2' />" +
                "  </data>" +
                "  <data source='data' name='g2'>" +
                "    <occurrence only='moreThan 2' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("l2", "1");
                    o.get().literal("2", "2");
                    o.get().literal("g2", "3");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("l2", "1");
                    o.get().literal("2", "2");
                    o.get().literal("g2", "3");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testOccurrenceFunctionWithSameEntity() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='e.data' name='l2'>" +
                "    <occurrence only='lessThan 2' sameEntity='true' />" +
                "  </data>" +
                "  <data source='e.data' name='2'>" +
                "    <occurrence only='2' sameEntity='true' />" +
                "  </data>" +
                "  <data source='e.data' name='g2'>" +
                "    <occurrence only='moreThan 2' sameEntity='true' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("e");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endEntity();
                    i.startEntity("e");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("l2", "1");
                    o.get().literal("2", "2");
                    o.get().literal("g2", "3");
                    o.get().literal("l2", "1");
                    o.get().literal("2", "2");
                    o.get().literal("g2", "3");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testOccurrenceFunctionWithSameEntityInNestedEntitiesShouldChangeWithInnerEntities() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='o.i.data' name='l2'>" +
                "    <occurrence only='lessThan 2' sameEntity='true' />" +
                "  </data>" +
                "  <data source='o.i.data' name='2'>" +
                "    <occurrence only='2' sameEntity='true' />" +
                "  </data>" +
                "  <data source='o.i.data' name='g2'>" +
                "    <occurrence only='moreThan 2' sameEntity='true' />" +
                "  </data>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("o");
                    i.startEntity("i");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endEntity();
                    i.startEntity("i");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("l2", "1");
                    o.get().literal("2", "2");
                    o.get().literal("g2", "3");
                    o.get().literal("l2", "1");
                    o.get().literal("2", "2");
                    o.get().literal("g2", "3");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testCountFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <data source='data' name='count'>" +
                "    <count />" +
                "  </data>" +
                "  <choose flushWith='record'>" +
                "    <data source='datax' name='count'>" +
                "      <count />" +
                "    </data>" +
                "  </choose>" +
                "</rules>",
                i -> {
                    i.startRecord("0");
                    i.literal("datax", "1");
                    i.literal("datax", "2");
                    i.endRecord();
                    i.startRecord("1");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.endRecord();
                    i.startRecord("3");
                    i.literal("datax", "1");
                    i.literal("datax", "2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("0");
                    o.get().literal("count", "2");
                    o.get().endRecord();
                    o.get().startRecord("1");
                    o.get().literal("count", "1");
                    o.get().literal("count", "2");
                    o.get().literal("count", "3");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("count", "1");
                    o.get().literal("count", "2");
                    o.get().endRecord();
                    o.get().startRecord("3");
                    o.get().literal("count", "2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testNestedCountFunction() {
        TestHelpers.assertMorph(receiver,
                "<rules>" +
                "  <combine name='count' value='${count}' flushWith='record'>" +
                "    <data source='data' name='count'>" +
                "      <count />" +
                "    </data>" +
                "    <data source='fantasy' />" +
                "  </combine>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.literal("data", "3");
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data", "1");
                    i.literal("data", "2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("count", "3");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("count", "2");
                    o.get().endRecord();
                }
        );
    }

}
