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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
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
public final class MetafixDslTest {

	@RegisterExtension
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private StreamReceiver streamReceiver;

	private Metafix metafix;

	@Test
	public void map() {
		metafix = fix("map(a,b)");

		metafix.startRecord("1");
		metafix.literal("a", "Aloha");
		metafix.endRecord();

		final InOrder ordered = inOrder(streamReceiver);
		ordered.verify(streamReceiver).startRecord("1");
		verify(streamReceiver).literal("b", "Aloha");
		ordered.verify(streamReceiver).endRecord();
	}

	@Test
	public void shouldHandleUnmatchedLiteralsInElseSource() {
		metafix = fix("map(Sylt,Hawaii)\n" + "map(_else)");

		metafix.startRecord("1");
		metafix.literal("Langeoog", "Moin");
		metafix.literal("Sylt", "Aloha");
		metafix.literal("Baltrum", "Moin Moin");
		metafix.endRecord();

		final InOrder ordered = inOrder(streamReceiver);
		ordered.verify(streamReceiver).startRecord("1");
		ordered.verify(streamReceiver).literal("Langeoog", "Moin");
		ordered.verify(streamReceiver).literal("Hawaii", "Aloha");
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

		final InOrder ordered = inOrder(streamReceiver);
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
		metafix.literal("literal", "Aloha");
		metafix.endEntity();
		metafix.endRecord();

		verify(streamReceiver).literal("data", "Aloha");
	}

	@Test
	@Disabled // Fix syntax
	public void shouldMatchCharacterWithQuestionMarkWildcard() {
		metafix = fix("map(lit-?)");

		metafix.startRecord("1");
		metafix.literal("lit", "Moin");
		metafix.literal("lit-A", "Aloha");
		metafix.literal("lit-B", "Aloha 'oe");
		metafix.endRecord();

		verify(streamReceiver).literal("lit-A", "Aloha");
		verify(streamReceiver).literal("lit-B", "Aloha 'oe");
		verify(streamReceiver, times(2)).literal(any(), any());
	}

	@Test
	@Disabled // Fix syntax
	public void shouldMatchCharactersInCharacterClass() {
		metafix = fix("map(lit-[AB])");

		metafix.startRecord("1");
		metafix.literal("lit-A", "Hawaii");
		metafix.literal("lit-B", "Oahu");
		metafix.literal("lit-C", "Fehmarn");
		metafix.endRecord();

		verify(streamReceiver).literal("lit-A", "Hawaii");
		verify(streamReceiver).literal("lit-B", "Oahu");
		verify(streamReceiver, times(2)).literal(any(), any());
	}

	@Test
	@Disabled // Fix syntax
	public void shouldReplaceVariables() {
		metafix = fix("vars(in: Honolulu, out: Hawaii)\n" + "map($[in],$[out])");

		metafix.startRecord("1");
		metafix.literal("Honolulu", "Aloha");
		metafix.endRecord();

		verify(streamReceiver).literal("Hawaii", "Aloha");
	}

	private Metafix fix(String fixString) {
		System.out.println("\nFix string: " + fixString);
		Metafix result = new Metafix(fixString);
		result.setReceiver(streamReceiver);
		return result;
	}

}
