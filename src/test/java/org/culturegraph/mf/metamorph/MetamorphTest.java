/*
 * Copyright 2016 Christoph Böhme
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;
import org.culturegraph.mf.metamorph.api.Maps;
import org.culturegraph.mf.metamorph.api.NamedValueReceiver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


/**
 * Tests for class {@link Metamorph}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (rewrite)
 */
public final class MetamorphTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private NamedValueReceiver namedValueReceiver;

	private Metamorph metamorph;

	@Before
	public void createSystemUnderTest() {
		metamorph = new Metamorph();
		metamorph.setReceiver(new DefaultStreamReceiver());
	}

	@Test
	public void shouldMapMatchingPath() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.literal("testEntity.testLiteral", "testValue");

		verify(namedValueReceiver).receive(eq("outName"), eq("testValue"),
				any(), anyInt(), anyInt());
	}

	@Test
	public void shouldNotMapNonMatchingPath() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.literal("nonMatching.path", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(),
				anyInt());
	}

	@Test
	public void shouldMapMatchingLiteralInMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.startEntity("testEntity");
		metamorph.literal("testLiteral", "testValue");

		verify(namedValueReceiver).receive(eq("outName"), eq("testValue"),
				any(), anyInt(), anyInt());
	}

	@Test
	public void shouldNotMapNonMatchingLiteralInMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.startEntity("testEntity");
		metamorph.literal("nonMatching", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(),
				anyInt());
	}

	@Test
	public void shouldNotMapMatchingLiteralInNonMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.startEntity("nonMatching");
		metamorph.literal("testLiteral", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(),
				anyInt());
	}
	@Test
	public void shouldNotMapLiteralWithoutMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.literal("testLiteral", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(),
				anyInt());
	}

	/**
	 * Creates the Metamorph structure that corresponds to the Metamorph XML
	 * statement {@code <data source="testEntity.testLiteral" name="outName" />}.
	 */
	private void setupSimpleMappingMorph() {
		final Data data = new Data();
		data.setName("outName");
		data.setNamedValueReceiver(namedValueReceiver);
		metamorph.registerNamedValueReceiver("testEntity" + '.' + "testLiteral", data);
	}

	@Test
	public void shouldReturnValueFromNestedMap() {
		final Map<String, String> map = new HashMap<>();
		map.put("outName", "testValue");

		metamorph.putMap("testMap", map);

		assertNotNull(metamorph.getMap("testMap"));
		assertEquals("testValue", metamorph.getValue("testMap", "outName"));
	}

	@Test
	public void shouldReturnDefaultValueIfMapIsKnownButNameIsUnknown() {
		final Map<String, String> map = new HashMap<>();
		map.put(Maps.DEFAULT_MAP_KEY, "defaultValue");

		metamorph.putMap("testMap", map);

		assertEquals("defaultValue", metamorph.getValue("testMap", "nameNotInMap"));
	}

	@Test
	public void shouldFedbackLiteralsStartingWithAtIntoMetamorph() {
		final Data data1;
		data1 = new Data();
		data1.setName("@feedback");
		metamorph.addNamedValueSource(data1);
		metamorph.registerNamedValueReceiver("testLiteral", data1);

		final Data data2 = new Data();
		data2.setName("outName");
		data2.setNamedValueReceiver(namedValueReceiver);
		metamorph.registerNamedValueReceiver("@feedback", data2);

		metamorph.startRecord("");
		metamorph.literal("testLiteral", "testValue");

		verify(namedValueReceiver).receive(eq("outName"), eq("testValue"),
				any(), anyInt(), anyInt());
	}

	@Test(expected=IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfEntityIsNotClosed() {
		metamorph.startRecord("");
		metamorph.startEntity("testEntity");
		metamorph.startEntity("testEntity");
		metamorph.endEntity();
		metamorph.endRecord();  // Exception expected
	}

}
