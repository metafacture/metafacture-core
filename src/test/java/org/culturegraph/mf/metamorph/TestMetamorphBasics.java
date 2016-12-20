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
package org.culturegraph.mf.metamorph;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
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

	private Metamorph metamorph;

	@Test
	public void shouldUseCustomEntityMarker() {
		metamorph = InlineMorph.in(this)
				.with("<metamorph version='1' entityMarker='~'")
				.with("    xmlns='http://www.culturegraph.org/metamorph'>")
				.with("  <rules>")
				.with("    <data source='entity~literal' name='data' />")
				.with("  </rules>")
				.with("</metamorph>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.startEntity("entity");
		metamorph.literal("literal", "Aloha");
		metamorph.endEntity();
		metamorph.endRecord();

		verify(receiver).literal("data", "Aloha");
	}

	@Test
	public void shouldHandleUnmatchedLiteralsInElseSource() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='Sylt' name='Hawaii' />")
				.with("  <data source='_else' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("Langeoog", "Moin");
		metamorph.literal("Sylt", "Aloha");
		metamorph.literal("Baltrum", "Moin Moin");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("Langeoog", "Moin");
		ordered.verify(receiver).literal("Hawaii", "Aloha");
		ordered.verify(receiver).literal("Baltrum", "Moin Moin");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldMatchCharacterWithQuestionMarkWildcard() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='lit-?' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("lit", "Moin");
		metamorph.literal("lit-A", "Aloha");
		metamorph.literal("lit-B", "Aloha 'oe");
		metamorph.endRecord();

		verify(receiver).literal("lit-A", "Aloha");
		verify(receiver).literal("lit-B", "Aloha 'oe");
		verify(receiver, times(2)).literal(any(), any());
	}

	@Test
	public void shouldMatchCharactersInCharacterClass() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='lit-[AB]' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("lit-A", "Hawaii");
		metamorph.literal("lit-B", "Oahu");
		metamorph.literal("lit-C", "Fehmarn");
		metamorph.endRecord();

		verify(receiver).literal("lit-A", "Hawaii");
		verify(receiver).literal("lit-B", "Oahu");
		verify(receiver, times(2)).literal(any(), any());
	}

	@Test
	public void shouldReplaceVariables() {
		metamorph = InlineMorph.in(this)
				.with("<vars>")
				.with("  <var name='in' value='Honolulu' />")
				.with("  <var name='out' value='Hawaii' />")
				.with("</vars>")
				.with("<rules>")
				.with("  <data source='$[in]' name='$[out]' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("Honolulu", "Aloha");
		metamorph.endRecord();

		verify(receiver).literal("Hawaii", "Aloha");
	}

	@Test
	public void shouldAllowTreatingEntityEndEventsAsLiterals() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='e1' />")
				.with("  <data source='e1.e2' />")
				.with("  <data source='e1.e2.d' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("entity end info");
		metamorph.startEntity("e1");
		metamorph.startEntity("e2");
		metamorph.literal("d", "a");
		metamorph.endEntity();
		metamorph.endEntity();
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("entity end info");
		ordered.verify(receiver).literal("e1.e2.d", "a");
		ordered.verify(receiver).literal("e1.e2", "");
		ordered.verify(receiver).literal("e1", "");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

}
