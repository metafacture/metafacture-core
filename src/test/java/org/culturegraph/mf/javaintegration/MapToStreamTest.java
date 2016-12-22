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
package org.culturegraph.mf.javaintegration;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link MapToStream}.
 *
 * @author Christoph Böhme
 *
 */
public final class MapToStreamTest {

	@Mock
	private StreamReceiver receiver;

	private MapToStream mapToStream;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mapToStream = new MapToStream();
		mapToStream.setReceiver(receiver);
	}

	@Test
	public void shouldEmitEmptyRecordIfMapIsEmpty() {
		mapToStream.process(new HashMap<>());

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldEmitMapEntryAsLiteral() {
		final Map<String, String> map = new HashMap<>();
		map.put("key", "value");
		mapToStream.process(map);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal("key", "value");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldEmitAllMapEntriesAsLiterals() {
		final Map<String, String> map = new HashMap<>();
		map.put("key-1", "value-1");
		map.put("key-2", "value-2");
		mapToStream.process(map);

		verify(receiver).literal("key-1", "value-1");
		verify(receiver).literal("key-2", "value-2");
	}

	@Test
	public void shouldUseMapEntryWithIdKeyAsRecordId() {
		mapToStream.setIdKey("id");

		final Map<String, String> map = new HashMap<>();
		map.put("id", "id-1");
		mapToStream.process(map);

		verify(receiver).startRecord("id-1");
	}

	@Test
	public void shouldUseMapEntryWithDefaultIdNameAsRecordId() {
		final Map<String, String> map = new HashMap<>();
		map.put("_id", "id-1");
		mapToStream.process(map);

		verify(receiver).startRecord("id-1");
	}

	@Test
	public void shouldEmitEmptyRecordIdIfNoEntryWithIdKeyIsFoundInMap() {
		final Map<String, String> map = new HashMap<>();
		map.put("noid", "noid");
		mapToStream.process(map);

		verify(receiver).startRecord("");
	}

	@Test
	public void shouldConvertObjectsInMapToStrings() {
		mapToStream.setIdKey(-1);

		final Map<Integer, Integer> map = new HashMap<>();
		map.put(1, 11);
		map.put(2, 12);
		map.put(-1, 100);
		mapToStream.process(map);

		verify(receiver).startRecord("100");
		verify(receiver).literal("1", "11");
		verify(receiver).literal("2", "12");
	}

}
