/*
 * Copyright 2013, 2019 Deutsche Nationalbibliothek and others
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

import org.metafacture.framework.StreamReceiver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests the basic functionality of Metafix via DSL.
 *
 * @author Christoph Böhme (MetamorphTest)
 * @author Fabian Steeg (MetafixDslTest)
 */
@ExtendWith(MockitoExtension.class)
public class MetafixDslTest {

    private static final String LITERAL_A = "lit-A";
    private static final String LITERAL_ALOHA = "Aloha";
    private static final String LITERAL_B = "lit-B";
    private static final String LITERAL_HAWAII = "Hawaii";
    private static final String LITERAL_MOIN = "Moin";

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    private Metafix metafix;

    public MetafixDslTest() {
    }

    @Test
    public void map() {
        metafix = fix("map(a,b)");

        metafix.startRecord("1");
        metafix.literal("a", LITERAL_ALOHA);
        metafix.endRecord();

        final InOrder ordered = Mockito.inOrder(streamReceiver);
        ordered.verify(streamReceiver).startRecord("1");
        Mockito.verify(streamReceiver).literal("b", LITERAL_ALOHA);
        ordered.verify(streamReceiver).endRecord();
    }

    @Test
    public void shouldHandleUnmatchedLiteralsInElseSource() {
        metafix = fix("map(Sylt,Hawaii)\n" + "map(_else)");

        metafix.startRecord("1");
        metafix.literal("Langeoog", LITERAL_MOIN);
        metafix.literal("Sylt", LITERAL_ALOHA);
        metafix.literal("Baltrum", "Moin Moin");
        metafix.endRecord();

        final InOrder ordered = Mockito.inOrder(streamReceiver);
        ordered.verify(streamReceiver).startRecord("1");
        ordered.verify(streamReceiver).literal("Langeoog", LITERAL_MOIN);
        ordered.verify(streamReceiver).literal(LITERAL_HAWAII, LITERAL_ALOHA);
        ordered.verify(streamReceiver).literal("Baltrum", "Moin Moin");
        ordered.verify(streamReceiver).endRecord();
    }

    @Test
    public void shouldAllowTreatingEntityEndEventsAsLiterals() {
        metafix = fix("map(e1)\n" + "map(e1.e2)\n" + "map(e1.e2.d)");

        metafix.startRecord("entity end info");
        metafix.startEntity("e1");
        metafix.startEntity("e2");
        metafix.literal("d", "a");
        metafix.endEntity();
        metafix.endEntity();
        metafix.endRecord();

        final InOrder ordered = Mockito.inOrder(streamReceiver);
        ordered.verify(streamReceiver).startRecord("entity end info");
        ordered.verify(streamReceiver).literal("e1.e2.d", "a");
        ordered.verify(streamReceiver).literal("e1.e2", "");
        ordered.verify(streamReceiver).literal("e1", "");
        ordered.verify(streamReceiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    @Disabled // Fix syntax
    public void shouldUseCustomEntityMarker() {
        metafix = fix("map(entity~literal,data)");

        metafix.startRecord("1");
        metafix.startEntity("entity");
        metafix.literal("literal", LITERAL_ALOHA);
        metafix.endEntity();
        metafix.endRecord();

        Mockito.verify(streamReceiver).literal("data", LITERAL_ALOHA);
    }

    @Test
    @Disabled // Fix syntax
    public void shouldMatchCharacterWithQuestionMarkWildcard() {
        metafix = fix("map(lit-?)");

        metafix.startRecord("1");
        metafix.literal("lit", LITERAL_MOIN);
        metafix.literal(LITERAL_A, LITERAL_ALOHA);
        metafix.literal(LITERAL_B, "Aloha 'oe");
        metafix.endRecord();

        Mockito.verify(streamReceiver).literal(LITERAL_A, LITERAL_ALOHA);
        Mockito.verify(streamReceiver).literal(LITERAL_B, "Aloha 'oe");
        Mockito.verify(streamReceiver, Mockito.times(2)).literal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @Disabled // Fix syntax
    public void shouldMatchCharactersInCharacterClass() {
        metafix = fix("map(lit-[AB])");

        metafix.startRecord("1");
        metafix.literal(LITERAL_A, LITERAL_HAWAII);
        metafix.literal(LITERAL_B, "Oahu");
        metafix.literal("lit-C", "Fehmarn");
        metafix.endRecord();

        Mockito.verify(streamReceiver).literal(LITERAL_A, LITERAL_HAWAII);
        Mockito.verify(streamReceiver).literal(LITERAL_B, "Oahu");
        Mockito.verify(streamReceiver, Mockito.times(2)).literal(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @Disabled // Fix syntax
    public void shouldReplaceVariables() {
        metafix = fix("vars(in: Honolulu, out: Hawaii)\n" + "map($[in],$[out])");

        metafix.startRecord("1");
        metafix.literal("Honolulu", LITERAL_ALOHA);
        metafix.endRecord();

        Mockito.verify(streamReceiver).literal(LITERAL_HAWAII, LITERAL_ALOHA);
    }

    private Metafix fix(final String fixString) {
        System.out.println("\nFix string: " + fixString);
        final Metafix result = new Metafix(fixString);
        result.setReceiver(streamReceiver);
        return result;
    }

}
