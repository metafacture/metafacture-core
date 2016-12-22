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
package org.culturegraph.mf.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


/**
 * Tests for class {@link Histogram}.
 *
 * @author Christoph Böhme
 *
 */
public final class HistogramTest {

	private static final String RECORD_ID = "1";
	private static final String[] ENTITIES = { "Entity 1", "Entity 2" };
	private static final String[] LITERALS = { "Literal 1", "Literal 2" };
	private static final String[] VALUES = { "Value A", "Value B" };

	@Test
	public void testCountEntities() {
		final Histogram histogram = new Histogram();
		histogram.setCountEntities(true);

		assertTrue(histogram.isCountEntities());
		assertFalse(histogram.isCountLiterals());
		assertNull(histogram.getCountField());

		histogram.startRecord(RECORD_ID);
		histogram.startEntity(ENTITIES[0]);
		histogram.literal(LITERALS[0], VALUES[0]);
		histogram.endEntity();
		histogram.startEntity(ENTITIES[1]);
		histogram.startEntity(ENTITIES[0]);
		histogram.endEntity();
		histogram.endEntity();
		histogram.endRecord();
		histogram.closeStream();

		final Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put(ENTITIES[0], Integer.valueOf(2));
		expected.put(ENTITIES[1], Integer.valueOf(1));

		assertEquals(expected, histogram.getHistogram());
	}

	@Test
	public void testCountLiterals() {
		final Histogram histogram = new Histogram();
		histogram.setCountLiterals(true);

		assertFalse(histogram.isCountEntities());
		assertTrue(histogram.isCountLiterals());
		assertNull(histogram.getCountField());

		histogram.startRecord(RECORD_ID);
		histogram.startEntity(ENTITIES[0]);
		histogram.literal(LITERALS[0], VALUES[0]);
		histogram.endEntity();
		histogram.literal(LITERALS[0], VALUES[1]);
		histogram.literal(LITERALS[1], VALUES[0]);
		histogram.endRecord();
		histogram.closeStream();

		final Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put(LITERALS[0], Integer.valueOf(2));
		expected.put(LITERALS[1], Integer.valueOf(1));

		assertEquals(expected, histogram.getHistogram());
	}

	@Test
	public void testCountField() {
		final Histogram histogram = new Histogram();
		histogram.setCountField(LITERALS[0]);

		assertFalse(histogram.isCountEntities());
		assertFalse(histogram.isCountLiterals());
		assertEquals(LITERALS[0], histogram.getCountField());

		histogram.startRecord(RECORD_ID);
		histogram.startEntity(ENTITIES[0]);
		histogram.literal(LITERALS[0], VALUES[0]);
		histogram.endEntity();
		histogram.literal(LITERALS[0], VALUES[1]);
		histogram.literal(LITERALS[1], VALUES[0]);
		histogram.literal(LITERALS[0], VALUES[1]);
		histogram.endRecord();
		histogram.closeStream();

		final Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put(VALUES[0], Integer.valueOf(1));
		expected.put(VALUES[1], Integer.valueOf(2));

		assertEquals(expected, histogram.getHistogram());
	}

	@Test
	public void testCountFieldConstructor() {
		final Histogram histogram = new Histogram(LITERALS[0]);

		assertFalse(histogram.isCountEntities());
		assertFalse(histogram.isCountLiterals());
		assertEquals(LITERALS[0], histogram.getCountField());

	}


	@Test
	public void testResetStream() {
		final Histogram histogram = new Histogram();
		histogram.setCountEntities(true);

		assertTrue(histogram.isCountEntities());
		assertFalse(histogram.isCountLiterals());
		assertNull(histogram.getCountField());

		histogram.startRecord(RECORD_ID);
		histogram.startEntity(ENTITIES[0]);
		histogram.endEntity();
		histogram.endRecord();

		final Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put(ENTITIES[0], Integer.valueOf(1));

		assertEquals(expected, histogram.getHistogram());

		histogram.resetStream();

		expected.clear();

		assertEquals(expected, histogram.getHistogram());
	}

}
