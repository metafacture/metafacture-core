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

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
        assertMorph(
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
        assertMorph(
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
        testElseData(
                "<rules>" +
                "    <data source='_else'/>" +
                "</rules>"
        );
    }

    @Test
    public void shouldHandleUnmatchedLiteralsAndEntitiesInElseFlattenedSource() {
        testElseData(
                "<rules>" +
                "    <data source='_elseFlattened'/>" +
                "</rules>"
        );
    }

    private void testElseData(final String morphDef) {
        assertMorph(morphDef,
                i -> {
                    i.startRecord("1");
                    i.literal("Shikotan", "Aekap");
                    i.startEntity("Germany");
                    i.literal("Langeoog", "Moin");
                    i.endEntity();
                    i.startEntity("Germany");
                    i.literal("Baltrum", "Moin Moin");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("Shikotan", "Aekap");
                    o.get().literal("Germany.Langeoog", "Moin");
                    o.get().literal("Germany.Baltrum", "Moin Moin");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldHandleUnmatchedLiteralsAndEntitiesInElseNestedSource() {
        assertMorph(
                "<rules>" +
                "  <entity name='USA' >" +
                "    <data source='USA.Sylt' name='Hawaii' />" +
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
                    i.startEntity("USA");
                    i.literal("Sylt", "Aloha");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("Shikotan", "Aekap");
                    o.get().startEntity("Germany");
                    o.get().literal("Langeoog", "Moin");
                    //o.get().endEntity();
                    //o.get().startEntity("Germany");
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
    public void shouldMatchCharacterWithQuestionMarkWildcard() {
        assertMorph(
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
        assertMorph(
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
        assertMorph(
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
        assertMorph(
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

    private void assertMorph(final String morphDef, final Consumer<Metamorph> in, final Consumer<Supplier<StreamReceiver>> out) {
        final Metamorph metamorph = InlineMorph.in(this).with(morphDef).createConnectedTo(receiver);
        final InOrder ordered = Mockito.inOrder(receiver);

        try {
            in.accept(metamorph);
            out.accept(() -> ordered.verify(receiver));

            ordered.verifyNoMoreInteractions();
            //Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
