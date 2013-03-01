/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.converter;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.converter.MapToStream;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.culturegraph.mf.util.StreamConstants;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test {@link MapToStream}
 * 
 * @author Christoph Böhme
 * 
 */
public final class MapToStreamTest {

	private static final String RECORD_ID = "123";
	private static final int INT_RECORD_ID = 123;
	private static final int INT_ID_KEY = -1;
	private static final String[] KEYS = { "1", "2" };
	private static final String[] VALUES = { "10", "20" };
	private static final int[] INT_KEYS = { 1, 2 };
	private static final int[] INT_VALUES = { 10, 20 };

	@Test
	public void testStringStringMap() {
		final EventList expected = new EventList();
		expected.startRecord(RECORD_ID);
		expected.literal(KEYS[0], VALUES[0]);
		expected.literal(KEYS[1], VALUES[1]);
		expected.literal(StreamConstants.ID, RECORD_ID);
		expected.endRecord();
		
		final Map<String, String> map = new HashMap<String, String>();
		map.put(KEYS[0], VALUES[0]);
		map.put(KEYS[1], VALUES[1]);
		map.put(StreamConstants.ID, RECORD_ID);

		final MapToStream mapToStream = new MapToStream();
		
		Assert.assertEquals(StreamConstants.ID, mapToStream.getIdKey());
		
		checkResults(expected, mapToStream, map);
	}


	@Test
	public void testIntIntMap() {
		final EventList expected = new EventList();
		expected.startRecord(RECORD_ID);
		expected.literal(KEYS[0], VALUES[0]);
		expected.literal(KEYS[1], VALUES[1]);
		expected.literal(Integer.toString(INT_ID_KEY), RECORD_ID);
		expected.endRecord();
		
		final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(INT_KEYS[0], INT_VALUES[0]);
		map.put(INT_KEYS[1], INT_VALUES[1]);
		map.put(INT_ID_KEY, INT_RECORD_ID);
		
		final MapToStream mapToStream = new MapToStream();
		mapToStream.setIdKey(-1);
		
		Assert.assertEquals(-1, mapToStream.getIdKey());
		
		checkResults(expected, mapToStream, map);
	}
	
	@Test
	public void testStringIntMap() {
		final EventList expected = new EventList();
		expected.startRecord(null);
		expected.literal(KEYS[0], VALUES[0]);
		expected.literal(KEYS[1], VALUES[1]);
		expected.endRecord();
		
		final Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(KEYS[0], INT_VALUES[0]);
		map.put(KEYS[1], INT_VALUES[1]);
		
		final MapToStream mapToStream = new MapToStream();
		
		checkResults(expected, mapToStream, map);
	}
	
	private void checkResults(final EventList expected, final MapToStream mapToStream, final Map<?, ?> map) {
		
		final StreamValidator validator = new StreamValidator(expected.getEvents());
		
		mapToStream.setReceiver(validator);
		
		try {
			mapToStream.process(map);
			mapToStream.closeStream();
		} catch (FormatException e){
			Assert.fail(e.toString());
		}		
	}
}
