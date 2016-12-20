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
package org.culturegraph.mf.metamorph.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.metamorph.InlineMorph;
import org.culturegraph.mf.metamorph.Metamorph;
import org.culturegraph.mf.metamorph.api.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


/**
 * Tests for class {@link Lookup}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (conversion of metamorph-test xml to java)
 */

public final class LookupTest {

	private static final String MAP_NAME = "Authors";
	private static final String MAP_NAME_WRONG = "Directors";
	private static final String KEY = "Franz";
	private static final String KEY_WRONG = "Josef";
	private static final String VALUE = "Kafka";

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private Maps maps;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void initMaps() {
		when(maps.getValue(MAP_NAME, KEY)).thenReturn(VALUE);
		when(maps.getValue(MAP_NAME, KEY_WRONG)).thenReturn(null);
		when(maps.getValue(MAP_NAME_WRONG, KEY)).thenReturn(null);
		when(maps.getValue(MAP_NAME_WRONG, KEY_WRONG)).thenReturn(null);
	}

	@Test
	public void shouldReturnNullIfMapNameIsDoesNotExist() {
		final Lookup lookup = new Lookup();
		lookup.setMaps(maps);

		lookup.setIn(MAP_NAME_WRONG);

		assertNull(lookup.process(KEY));
	}

	@Test
	public void shouldReturnValueIfMapAndKeyExist() {
		final Lookup lookup = new Lookup();
		lookup.setMaps(maps);

		lookup.setIn(MAP_NAME);

		assertEquals(VALUE, lookup.process(KEY));
	}

	@Test
	public void shouldReturnNullIfKeyDoesNotExist() {
		final Lookup lookup = new Lookup();
		lookup.setMaps(maps);

		lookup.setIn(MAP_NAME);

		assertNull(lookup.process(KEY_WRONG));
	}

	@Test
	public void shouldLookupValuesInLocalMap() {
		final Metamorph metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='1'>")
				.with("    <lookup>")
				.with("      <entry name='a' value='A' />")
				.with("    </lookup>")
				.with("  </data>")
				.with("  <data source='2'>")
				.with("    <lookup default='B'>")
				.with("      <entry name='a' value='A' />")
				.with("    </lookup>")
				.with("  </data>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("1", "a");
		metamorph.literal("1", "b");
		metamorph.literal("2", "a");
		metamorph.literal("2", "b");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("1", "A");
		ordered.verify(receiver).literal("2", "A");
		ordered.verify(receiver).literal("2", "B");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldLookupValuesInReferencedMap() {
		final Metamorph metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='1'>")
				.with("    <lookup in='map1' />")
				.with("  </data>")
				.with("  <data source='2'>")
				.with("    <lookup in='map2' />")
				.with("  </data>")
				.with("</rules>")
				.with("<maps>")
				.with("  <map name='map1'>")
				.with("    <entry name='a' value='A' />")
				.with("  </map>")
				.with("  <map name='map2' default='B'>")
				.with("    <entry name='a' value='A' />")
				.with("  </map>")
				.with("</maps>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("1", "a");
		metamorph.literal("1", "b");
		metamorph.literal("2", "a");
		metamorph.literal("2", "b");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("1", "A");
		ordered.verify(receiver).literal("2", "A");
		ordered.verify(receiver).literal("2", "B");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldLookupValuesInMetadata() {
		final Metamorph metamorph = InlineMorph.in(this)
				.with("<meta>")
				.with("  <name>Hawaii</name>")
				.with("</meta>")
				.with("<rules>")
				.with("  <data source='data'>")
				.with("    <lookup in='__meta' />")
				.with("  </data>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("data", "name");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("data", "Hawaii");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

}
