/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import static org.metafacture.metamorph.TestHelpers.assertMorph;

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests the basic functionality of Metamorph.
 *
 * @author Christoph Böhme
 */
public final class TestMetamorphBasics {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldUseCustomEntityMarker() {
        assertMorph(receiver,
                "<metamorph version='1' entityMarker='~'" +
                "    xmlns='http://www.culturegraph.org/metamorph'>" +
                "  <rules>" +
                "    <data source='entity~literal' name='data' />" +
                "  </rules>" +
                "</metamorph>",
                i -> {
                    i.startRecord("1");
                    i.startEntity("entity");
                    i.literal("literal", "Aloha");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("data", "Aloha");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldHandleUnmatchedLiteralsInElseSource() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='Sylt' name='Hawaii' />" +
                "  <data source='_else' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("Langeoog", "Moin");
                    i.literal("Sylt", "Aloha");
                    i.literal("Baltrum", "Moin Moin");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("Langeoog", "Moin");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().literal("Baltrum", "Moin Moin");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldHandleUnmatchedLiteralsAndEntitiesInElseSource() {
        testElseData("_else");
    }

    @Test
    public void shouldHandleUnmatchedLiteralsAndEntitiesInElseFlattenedSource() {
        testElseData("_elseFlattened");
    }

    private void testElseData(final String elseKeyword) {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='Germany'>" +
                "    <data source='Germany.Sylt' name='Hawaii' />" +
                "    <data source='Germany.Borkum' />" +
                "  </entity>" +
                "  <data source='" + elseKeyword + "'/>" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("@id", "123");
                    i.literal("Shikotan", "Aekap");
                    i.startEntity("Germany");
                    i.literal("Langeoog", "Moin");
                    i.literal("Sylt", "Aloha");
                    i.literal("Borkum", "Tach");
                    i.endEntity();
                    i.startEntity("Germany");
                    i.literal("@foo", "bar");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("@id", "123");
                    o.get().literal("Shikotan", "Aekap");
                    o.get().literal("Germany.Langeoog", "Moin");
                    o.get().startEntity("Germany");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().literal("Germany.Borkum", "Tach");
                    o.get().endEntity();
                    o.get().literal("Germany.@foo", "bar");
                    o.get().literal("Germany.Baltrum", "Moin Moin");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void issue338_shouldPreserveSameEntitiesInElseNestedSource() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='_elseNested' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit1", "val1");
                    i.startEntity("ent1");
                    i.literal("lit2", "val2");
                    i.literal("lit3", "val3");
                    i.endEntity();
                    i.literal("lit4", "val4");
                    i.startEntity("ent2");
                    i.literal("lit5", "val5");
                    i.literal("lit6", "val6");
                    i.literal("lit7", "val7");
                    i.endEntity();
                    i.startEntity("ent2"); // sic!
                    i.literal("lit8", "val8");
                    i.literal("lit9", "val9");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("ent1");
                    i.literal("lit1", "val1");
                    i.literal("lit2", "val2");
                    i.endEntity();
                    i.startEntity("ent2");
                    i.literal("lit3", "val3");
                    i.literal("lit4", "val4");
                    i.literal("lit5", "val5");
                    i.literal("lit6", "val6");
                    i.endEntity();
                    i.startEntity("ent3");
                    i.literal("lit7", "val7");
                    i.literal("lit8", "val8");
                    i.endEntity();
                    i.literal("lit9", "val9");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit1", "val1");
                    o.get().startEntity("ent1");
                    o.get().literal("lit2", "val2");
                    o.get().literal("lit3", "val3");
                    o.get().endEntity();
                    o.get().literal("lit4", "val4");
                    o.get().startEntity("ent2");
                    o.get().literal("lit5", "val5");
                    o.get().literal("lit6", "val6");
                    o.get().literal("lit7", "val7");
                    o.get().endEntity();
                    o.get().startEntity("ent2");
                    o.get().literal("lit8", "val8");
                    o.get().literal("lit9", "val9");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().startEntity("ent1");
                    o.get().literal("lit1", "val1");
                    o.get().literal("lit2", "val2");
                    o.get().endEntity();
                    o.get().startEntity("ent2");
                    o.get().literal("lit3", "val3");
                    o.get().literal("lit4", "val4");
                    o.get().literal("lit5", "val5");
                    o.get().literal("lit6", "val6");
                    o.get().endEntity();
                    o.get().startEntity("ent3");
                    o.get().literal("lit7", "val7");
                    o.get().literal("lit8", "val8");
                    o.get().endEntity();
                    o.get().literal("lit9", "val9");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldHandleUnmatchedLiteralsAndEntitiesInElseNestedSource() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='USA' >" +
                "    <data source='USA.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <data source='_elseNested' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("@id", "123");
                    i.literal("Shikotan", "Aekap");
                    i.startEntity("Germany");
                    i.literal("@foo", "bar");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA");
                    i.literal("Sylt", "Aloha");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("@id", "123");
                    o.get().literal("Shikotan", "Aekap");
                    o.get().startEntity("Germany");
                    o.get().literal("@foo", "bar");
                    o.get().literal("Langeoog", "Moin");
                    o.get().literal("Baltrum", "Moin Moin");
                    o.get().endEntity();
                    o.get().startEntity("USA");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldHandlePartiallyUnmatchedLiteralsAndEntitiesInElseNestedSource() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='USA1'>" +
                "    <data source='USA1.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <entity name='USA2'>" +
                "    <data source='USA2.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <entity name='USA3'>" +
                "    <data source='USA3.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <entity name='USA4'>" +
                "    <data source='USA4.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <data source='_elseNested' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("Shikotan", "Aekap");
                    i.startEntity("Germany");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA1");
                    i.literal("Sylt", "Aloha");
                    i.endEntity();
                    i.startEntity("USA2");
                    i.literal("Sylt", "Aloha");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA3");
                    i.literal("Langeoog", "Moin");
                    i.literal("Sylt", "Aloha");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA4");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.literal("Sylt", "Aloha");
                    i.endEntity();
                    i.endRecord();
                },
                (o, f) -> {
                    // Pass-through coordinates with morph whether to start/end an entity
                    final boolean coordinatesWithEntity = false;

                    // Pass-through and morph entities are separated (one ends when the other starts)
                    final boolean separatesFromEntity = false;

                    o.get().startRecord("1");
                    o.get().literal("Shikotan", "Aekap");
                    o.get().startEntity("Germany");
                    o.get().literal("Langeoog", "Moin");
                    o.get().literal("Baltrum", "Moin Moin");
                    o.get().endEntity();
                    o.get().startEntity("USA1");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().endEntity();
                    o.get().startEntity("USA2");
                    o.get().literal("Hawaii", "Aloha");
                    if (!coordinatesWithEntity) {
                        o.get().endEntity();
                        o.get().startEntity("USA2");
                    }
                    o.get().literal("Langeoog", "Moin");
                    o.get().literal("Baltrum", "Moin Moin");
                    o.get().endEntity();
                    o.get().startEntity("USA3");
                    o.get().literal("Langeoog", "Moin");
                    if (!coordinatesWithEntity) {
                        o.get().startEntity("USA3");
                    }
                    else if (separatesFromEntity) {
                        o.get().endEntity();
                        o.get().startEntity("USA3");
                    }
                    o.get().literal("Hawaii", "Aloha");
                    if (!coordinatesWithEntity) {
                        o.get().endEntity();
                    }
                    else if (separatesFromEntity) {
                        o.get().endEntity();
                        o.get().startEntity("USA3");
                    }
                    o.get().literal("Baltrum", "Moin Moin");
                    o.get().endEntity();
                    o.get().startEntity("USA4");
                    o.get().literal("Langeoog", "Moin");
                    o.get().literal("Baltrum", "Moin Moin");
                    if (!coordinatesWithEntity) {
                        o.get().startEntity("USA4");
                    }
                    else if (separatesFromEntity) {
                        o.get().endEntity();
                        o.get().startEntity("USA4");
                    }
                    o.get().literal("Hawaii", "Aloha");
                    if (!coordinatesWithEntity) {
                        f.apply(2).endEntity();
                    }
                    else {
                        o.get().endEntity();
                    }
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotHandleDataByElseNestedSourceIfDataBelongingToEntityIsRuledByMorph() {
        assertMorph(receiver,
                "<rules>" +
                "  <entity name='USA1'>" +
                "    <data source='USA1.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <entity name='USA2' sameEntity='true' flushWith='USA2'>" +
                "    <data source='USA2.Sylt' name='Hawaii' />" +
                "    <data source='USA2.Langeoog' name='Langeoog' />" +
                "  </entity>" +
                "  <entity name='USA3' sameEntity='true' flushWith='USA3'>" +
                "    <data source='USA3.Sylt' name='Hawaii' />" +
                "  </entity>" +
                "  <data source='_elseNested' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("Shikotan", "Aekap");
                    i.startEntity("Germany");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA1");
                    i.literal("Sylt", "Aloha");
                    i.endEntity();
                    i.startEntity("USA2");
                    i.literal("Sylt", "Aloha");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA2");
                    i.literal("Langeoog", "Moin");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.startEntity("USA3");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("Shikotan", "Aekap");
                    o.get().startEntity("Germany");
                    o.get().literal("Langeoog", "Moin");
                    o.get().literal("Baltrum", "Moin Moin");
                    o.get().endEntity();
                    o.get().startEntity("USA1");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().endEntity();
                    o.get().startEntity("USA2");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().literal("Langeoog", "Moin");
                    o.get().endEntity();
                    o.get().startEntity("USA2");
                    o.get().literal("Langeoog", "Moin");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    // https://github.com/hagbeck/metafacture-sandbox/tree/master/else-nested-entities
    @Test
    public void shouldHandleUseCaseSandboxElseNestedEntities() {
        assertMorph(receiver,
                "<rules>" +
                  "<entity name='85640' flushWith='85640.3' reset='true'>" +
                    "<data source='85640.u' name='u'>" +
                      "<contains string='hdl.handle.net/2003/' />" +
                      "<replace pattern='http://' with='https://' />" +
                    "</data>" +
                    "<data source='85640.u' name='u'>" +
                      "<contains string='doi.org/10.17877/' />" +
                      "<replace pattern='http://' with='https://' />" +
                      "<replace pattern='dx.doi.org' with='doi.org' />" +
                    "</data>" +
                    "<data source='85640.x' name='x' />" +
                    "<data source='85640.3' name='3' />" +
                  "</entity>" +
                  "<entity name='8564 ' flushWith='8564 .3' reset='true'>" +
                    "<data source='8564 .u' name='u'>" +
                      "<contains string='hdl.handle.net/2003/' />" +
                      "<replace pattern='http://' with='https://' />" +
                    "</data>" +
                    "<data source='8564 .u' name='u'>" +
                      "<contains string='doi.org/10.17877/' />" +
                      "<replace pattern='http://' with='https://' />" +
                      "<replace pattern='dx.doi.org' with='doi.org' />" +
                    "</data>" +
                    "<data source='8564 .x' name='x' />" +
                    "<data source='8564 .3' name='3' />" +
                  "</entity>" +
                  "<data source='_elseNested' />" +
                "</rules>",
                i -> {
                    i.startRecord("ID1687931");
                    i.literal("leader", "00564nam a2200024 c 4500");
                    i.literal("001", "1687931");
                    i.literal("007", "|| ||||||||||||||||||||");
                    i.literal("008", "||||||nuuuuuuuu|||||| | |||||||||||und||");
                    i.startEntity("035  ");
                    i.literal("a", "(UNION_SEAL)HT019953476");
                    i.endEntity();
                    i.startEntity("24500");
                    i.literal("a", "Design and analysis of an asymmetric mutation operator");
                    i.literal("c", "Thomas Jansen and Dirk Sudholt");
                    i.endEntity();
                    i.startEntity("8564 ");
                    i.literal("z", "Freie Internetressource");
                    i.endEntity();
                    i.startEntity("85640");
                    i.literal("u", "http://hdl.handle.net/2003/22116");
                    i.literal("x", "Resolving-System");
                    i.literal("3", "Volltext");
                    i.endEntity();
                    i.startEntity("85640");
                    i.literal("u", "http://dx.doi.org/10.17877/DE290R-14123");
                    i.literal("x", "Resolving-System");
                    i.literal("3", "Volltext");
                    i.endEntity();
                    i.startEntity("9801 ");
                    i.literal("e", "HBZ");
                    i.endEntity();
                    i.startEntity("997  ");
                    i.literal("a", "20190130");
                    i.endEntity();
                    i.startEntity("9984 ");
                    i.literal("z", "Freie Internetressource");
                    i.endEntity();
                },
                o -> {
                    o.get().startRecord("ID1687931");
                    o.get().literal("leader", "00564nam a2200024 c 4500");
                    o.get().literal("001", "1687931");
                    o.get().literal("007", "|| ||||||||||||||||||||");
                    o.get().literal("008", "||||||nuuuuuuuu|||||| | |||||||||||und||");
                    o.get().startEntity("035  ");
                    o.get().literal("a", "(UNION_SEAL)HT019953476");
                    o.get().endEntity();
                    o.get().startEntity("24500");
                    o.get().literal("a", "Design and analysis of an asymmetric mutation operator");
                    o.get().literal("c", "Thomas Jansen and Dirk Sudholt");
                    o.get().endEntity();
                    o.get().startEntity("8564 ");
                    o.get().literal("z", "Freie Internetressource");
                    o.get().endEntity();
                    o.get().startEntity("85640");
                    o.get().literal("u", "https://hdl.handle.net/2003/22116");
                    o.get().literal("x", "Resolving-System");
                    o.get().literal("3", "Volltext");
                    o.get().endEntity();
                    o.get().startEntity("85640");
                    o.get().literal("u", "https://doi.org/10.17877/DE290R-14123");
                    o.get().literal("x", "Resolving-System");
                    o.get().literal("3", "Volltext");
                    o.get().endEntity();
                    o.get().startEntity("9801 ");
                    o.get().literal("e", "HBZ");
                    o.get().endEntity();
                    o.get().startEntity("997  ");
                    o.get().literal("a", "20190130");
                    o.get().endEntity();
                    o.get().startEntity("9984 ");
                    o.get().literal("z", "Freie Internetressource");
                    o.get().endEntity();
                }
        );
    }


    @Test
    public void shouldMatchCharacterWithQuestionMarkWildcard() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='lit-?' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("lit", "Moin");
                    i.literal("lit-A", "Aloha");
                    i.literal("lit-B", "Aloha 'oe");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit-A", "Aloha");
                    o.get().literal("lit-B", "Aloha 'oe");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldMatchCharactersInCharacterClass() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='lit-[AB]' />" +
                "</rules>",
                i ->  {
                    i.startRecord("1");
                    i.literal("lit-A", "Hawaii");
                    i.literal("lit-B", "Oahu");
                    i.literal("lit-C", "Fehmarn");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit-A", "Hawaii");
                    o.get().literal("lit-B", "Oahu");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldReplaceVariables() {
        assertMorph(receiver,
                "<vars>" +
                "  <var name='in' value='Honolulu' />" +
                "  <var name='out' value='Hawaii' />" +
                "</vars>" +
                "<rules>" +
                "  <data source='$[in]' name='$[out]' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("Honolulu", "Aloha");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("Hawaii", "Aloha");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldAllowTreatingEntityEndEventsAsLiterals() {
        assertMorph(receiver,
                "<rules>" +
                "  <data source='e1' />" +
                "  <data source='e1.e2' />" +
                "  <data source='e1.e2.d' />" +
                "</rules>",
                i -> {
                    i.startRecord("entity end info");
                    i.startEntity("e1");
                    i.startEntity("e2");
                    i.literal("d", "a");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("entity end info");
                    o.get().literal("e1.e2.d", "a");
                    o.get().literal("e1.e2", "");
                    o.get().literal("e1", "");
                    o.get().endRecord();
                }
        );
    }

}
