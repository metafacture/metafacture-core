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
package org.culturegraph.mf.morph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;
import org.culturegraph.mf.types.MultiMap;
import org.culturegraph.mf.util.xml.Location;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for class {@link Metamorph}
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (rewrite)
 */
public final class MetamorphBasicTest {

	private static final String VALUE = "testValue";
	private static final String ENTITY_NAME = "testEntity";
	private static final String LITERAL_NAME = "testLiteral";
	private static final String NON_MATCHING_NAME = "nonMatching";
	private static final String OUTPUT_NAME = "outName";
	private static final String MAP_NAME = "testMap";

	private Metamorph metamorph;

	private TestNameValueReceiver testReceiver;

	@Before
	public void createSystemUnderTest() {
		metamorph = new Metamorph();
		metamorph.setReceiver(new DefaultStreamReceiver());
		testReceiver = new TestNameValueReceiver();
	}

	@Test
	public void shouldMapMatchingPath() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.literal(ENTITY_NAME + "." + LITERAL_NAME, VALUE);

		assertEquals(OUTPUT_NAME, testReceiver.name);
		assertEquals(VALUE, testReceiver.value);
	}

	@Test
	public void shouldNotMapNonMatchingPath() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.literal("nonMatching.path", VALUE);

		assertNull(testReceiver.name);
		assertNull(testReceiver.value);
	}

	@Test
	public void shouldMapMatchingLiteralInMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.startEntity(ENTITY_NAME);
		metamorph.literal(LITERAL_NAME, VALUE);

		assertEquals(OUTPUT_NAME, testReceiver.name);
		assertEquals(VALUE, testReceiver.value);
	}

	@Test
	public void shouldNotMapNonMatchingLiteralInMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.startEntity(ENTITY_NAME);
		metamorph.literal(NON_MATCHING_NAME, VALUE);

		assertNull(testReceiver.name);
		assertNull(testReceiver.value);
	}

	@Test
	public void shouldNotMapMatchingLiteralInNonMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.startEntity(NON_MATCHING_NAME);
		metamorph.literal(LITERAL_NAME, VALUE);

		assertNull(testReceiver.name);
		assertNull(testReceiver.value);
	}
	@Test
	public void shouldNotMapLiteralWithoutMatchingEntity() {
		setupSimpleMappingMorph();

		metamorph.startRecord("");
		metamorph.literal(LITERAL_NAME, VALUE);

		assertNull(testReceiver.name);
		assertNull(testReceiver.value);
	}

	/*
	 * Creates the Metamorph structure that corresponds with the Metamorph XML
	 * statement:
	 *     <data source="testEntity.testLiteral" name="outName" />
	 */
	private void setupSimpleMappingMorph() {
		final Data data = new Data();
		data.setName(OUTPUT_NAME);
		testReceiver.addNamedValueSource(data);
		metamorph.registerNamedValueReceiver(ENTITY_NAME + '.' + LITERAL_NAME, data);
	}

	@Test
	public void shouldReturnValueFromNestedMap() {
		final Map<String, String> map = new HashMap<>();
		map.put(OUTPUT_NAME, VALUE);

		metamorph.putMap(MAP_NAME, map);

		assertNotNull(metamorph.getMap(MAP_NAME));
		assertEquals(VALUE, metamorph.getValue(MAP_NAME, OUTPUT_NAME));
	}

	@Test
	public void shouldReturnDefaultValueIfMapIsKnownButNameIsUnknown() {
		final Map<String, String> map = new HashMap<>();
		map.put(MultiMap.DEFAULT_MAP_KEY, "defaultValue");

		metamorph.putMap(MAP_NAME, map);

		assertEquals("defaultValue", metamorph.getValue(MAP_NAME, "nameNotInMap"));
	}

	@Test
	public void testFeedback() {
		final Data data1;
		data1 = new Data();
		data1.setName("@feedback");
		metamorph.addNamedValueSource(data1);
		metamorph.registerNamedValueReceiver(LITERAL_NAME, data1);

		final Data data2 = new Data();
		data2.setName(OUTPUT_NAME);
		testReceiver.addNamedValueSource(data2);
		metamorph.registerNamedValueReceiver("@feedback", data2);

		metamorph.startRecord("");
		metamorph.literal(LITERAL_NAME, VALUE);
		assertEquals(OUTPUT_NAME, testReceiver.name);
		assertEquals(VALUE, testReceiver.value);
	}

	@Test(expected=IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionIfEntityIsNotClosed() {
		metamorph.startRecord("");
		metamorph.startEntity(ENTITY_NAME);
		metamorph.startEntity(ENTITY_NAME);
		metamorph.endEntity();
		metamorph.endRecord();  // Exception expected
	}

	private static final class TestNameValueReceiver
			implements NamedValueReceiver {

		String name;
		String value;

		@Override
		public void receive(final String name, final String value,
				final NamedValueSource source, final int recordCount,
				final int entityCount) {
			this.name = name;
			this.value = value;
		}

		@Override
		public void addNamedValueSource(final NamedValueSource namedValueSource) {
			namedValueSource.setNamedValueReceiver(this);
		}

		@Override
		public Location getSourceLocation() {
			// Nothing to do
			return null;
		}

		@Override
		public void setSourceLocation(final Location sourceLocation) {
			// Nothing to do
		}

	}

}
