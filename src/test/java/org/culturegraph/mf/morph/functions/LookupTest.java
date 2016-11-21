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
package org.culturegraph.mf.morph.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.culturegraph.mf.morph.api.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


/**
 * tests {@link Lookup}
 *
 * @author Markus Michael Geipel
 */

public final class LookupTest {

	private static final String MAP_NAME = "Authors";
	private static final String MAP_NAME_WRONG = "Directors";
	private static final String KEY = "Franz";
	private static final String KEY_WRONG = "Josef";
	private static final String VALUE = "Kafka";

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	private Lookup lookup = new Lookup();
	@Mock
	private Maps maps;

	@Before
	public void initMocksAndSystemUnderTest() {
		when(maps.getValue(MAP_NAME, KEY)).thenReturn(VALUE);
		when(maps.getValue(MAP_NAME, KEY_WRONG)).thenReturn(null);
		when(maps.getValue(MAP_NAME_WRONG, KEY)).thenReturn(null);
		when(maps.getValue(MAP_NAME_WRONG, KEY_WRONG)).thenReturn(null);
		lookup.setMaps(maps);
	}

	@Test
	public void shouldReturnNullIfMapNameIsDoesNotExist() {
		lookup.setIn(MAP_NAME_WRONG);

		assertNull(lookup.process(KEY));
	}

	@Test
	public void shouldReturnValueIfMapAndKeyExist() {
		lookup.setIn(MAP_NAME);

		assertEquals(VALUE, lookup.process(KEY));
	}

	@Test
	public void shouldReturnNullIfKeyDoesNotExist() {
		lookup.setIn(MAP_NAME);

		assertNull(lookup.process(KEY_WRONG));
	}

}
