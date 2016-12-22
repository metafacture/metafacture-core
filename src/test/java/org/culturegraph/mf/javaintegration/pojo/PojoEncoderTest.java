/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.javaintegration.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for class {@link PojoEncoder}.
 *
 * @author Thomas Seidel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public class PojoEncoderTest {

	@Test
	public void shouldEncodeEmptyEntityStreamToEmptyPojo() {
		final ObjectReceiver<EmptyPojo> receiver = createObjectReceiverMock();
		final PojoEncoder<EmptyPojo> pojoEncoder = new PojoEncoder<>(
				EmptyPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.endRecord();

		verify(receiver).process(any(EmptyPojo.class));
	}

	@Test
	public void shouldEncodeEntityStreamToSimplePojo() {
		final ObjectReceiver<SimplePojo> receiver = createObjectReceiverMock();
		final PojoEncoder<SimplePojo> pojoEncoder = new PojoEncoder<>(
				SimplePojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.literal("stringField1", "value1");
		pojoEncoder.literal("stringField2", "value2");
		pojoEncoder.literal("intField1", "42");
		pojoEncoder.literal("intField2", "23");
		pojoEncoder.endRecord();

		final ArgumentCaptor<SimplePojo> objectCaptor =
				ArgumentCaptor.forClass(SimplePojo.class);
		verify(receiver).process(objectCaptor.capture());
		final SimplePojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertEquals("value1", encodedPojo.stringField1);
		assertEquals("value2", encodedPojo.getStringField2());
		assertEquals(42, encodedPojo.intField1);
		assertEquals(23, encodedPojo.getIntField2());
	}

	@Test
	public void shouldEncodeEntityStreamToDoubleNestedPojo() {
		final ObjectReceiver<DoubleNestedPojo> receiver =
				createObjectReceiverMock();
		final PojoEncoder<DoubleNestedPojo> pojoEncoder =
				new PojoEncoder<>(DoubleNestedPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("nestedPojo");
		pojoEncoder.startEntity("simplePojo");
		pojoEncoder.literal("stringField1", "value1");
		pojoEncoder.literal("stringField2", "value2");
		pojoEncoder.endEntity();
		pojoEncoder.literal("attribute", "value3");
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();

		final ArgumentCaptor<DoubleNestedPojo> objectCaptor =
				ArgumentCaptor.forClass(DoubleNestedPojo.class);
		verify(receiver).process(objectCaptor.capture());
		final DoubleNestedPojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertNotNull(encodedPojo.nestedPojo);
		assertEquals("value3", encodedPojo.nestedPojo.attribute);
		assertNotNull(encodedPojo.nestedPojo.simplePojo);
		assertEquals("value1", encodedPojo.nestedPojo.simplePojo.stringField1);
		assertEquals("value2", encodedPojo.nestedPojo.simplePojo.getStringField2());
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithStringList() {
		final ObjectReceiver<StringListPojo> receiver = createObjectReceiverMock();
		final PojoEncoder<StringListPojo> pojoEncoder =
				new PojoEncoder<>(StringListPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("stringList");
		pojoEncoder.literal("firstElement", "value1");
		pojoEncoder.literal("secondElement", "value2");
		pojoEncoder.endEntity();
		pojoEncoder.literal("attribute", "value3");
		pojoEncoder.endRecord();

		final ArgumentCaptor<StringListPojo> objectCaptor =
				ArgumentCaptor.forClass(StringListPojo.class);
		verify(receiver).process(objectCaptor.capture());
		final StringListPojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertEquals("value3", encodedPojo.attribute);
		assertNotNull(encodedPojo.stringList);
		assertEquals(2, encodedPojo.stringList.size());
		assertEquals("value1", encodedPojo.stringList.get(0));
		assertEquals("value2", encodedPojo.stringList.get(1));
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithIntegerList() {
		final ObjectReceiver<IntegerListPojo> receiver = createObjectReceiverMock();
		final PojoEncoder<IntegerListPojo> pojoEncoder =
				new PojoEncoder<>(IntegerListPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("integerList");
		pojoEncoder.literal("firstElement", "42");
		pojoEncoder.literal("firstElement", "23");
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();

		final ArgumentCaptor<IntegerListPojo> objectCaptor =
				ArgumentCaptor.forClass(IntegerListPojo.class);
		verify(receiver).process(objectCaptor.capture());
		final IntegerListPojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertNotNull(encodedPojo.integerList);
		assertEquals(2, encodedPojo.integerList.size());
		assertEquals(42, encodedPojo.integerList.get(0).intValue());
		assertEquals(23, encodedPojo.integerList.get(1).intValue());
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithSimplePojoList() {
		final ObjectReceiver<SimplePojoListPojo> receiver =
				createObjectReceiverMock();
		final PojoEncoder<SimplePojoListPojo> pojoEncoder =
				new PojoEncoder<>(SimplePojoListPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("simplePojoList");
		pojoEncoder.startEntity("simplePojo");
		pojoEncoder.literal("stringField1", "value1");
		pojoEncoder.literal("stringField2", "value2");
		pojoEncoder.endEntity();
		pojoEncoder.startEntity("simplePojo");
		pojoEncoder.literal("stringField1", "value3");
		pojoEncoder.endEntity();
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();

		final ArgumentCaptor<SimplePojoListPojo> objectCaptor =
				ArgumentCaptor.forClass(SimplePojoListPojo.class);
		verify(receiver).process(objectCaptor.capture());
		final SimplePojoListPojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertNotNull(encodedPojo.simplePojoList);
		assertEquals(2, encodedPojo.simplePojoList.size());
		assertEquals("value1", encodedPojo.simplePojoList.get(0).stringField1);
		assertEquals("value2", encodedPojo.simplePojoList.get(0).getStringField2());
		assertEquals("value3", encodedPojo.simplePojoList.get(1).stringField1);
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithStringMap() {
		final ObjectReceiver<StringMapPojo> receiver =
				createObjectReceiverMock();
		final PojoEncoder<StringMapPojo> pojoEncoder =
				new PojoEncoder<StringMapPojo>(StringMapPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("stringMap");
		pojoEncoder.literal("mapKey1", "mapValue1");
		pojoEncoder.literal("mapKey2", "mapValue2");
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();

		final ArgumentCaptor<StringMapPojo> objectCaptor =
				ArgumentCaptor.forClass(StringMapPojo.class);
		verify(receiver).process(objectCaptor.capture());
		final StringMapPojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertNotNull(encodedPojo.stringMap);
		assertEquals(2, encodedPojo.stringMap.size());
		assertEquals("mapValue1", encodedPojo.stringMap.get("mapKey1"));
		assertEquals("mapValue2", encodedPojo.stringMap.get("mapKey2"));
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithSimplePojoMap() {
		final ObjectReceiver<SimplePojoMapPojo> receiver =
				createObjectReceiverMock();
		final PojoEncoder<SimplePojoMapPojo> pojoEncoder =
				new PojoEncoder<>(SimplePojoMapPojo.class);
		pojoEncoder.setReceiver(receiver);

		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("simplePojoMap");
		pojoEncoder.startEntity("mapKeyA");
		pojoEncoder.literal("stringField1", "stringValueA1");
		pojoEncoder.literal("stringField2", "stringValueA2");
		pojoEncoder.endEntity();
		pojoEncoder.startEntity("mapKeyB");
		pojoEncoder.literal("stringField1", "stringValueB1");
		pojoEncoder.literal("stringField2", "stringValueB2");
		pojoEncoder.endEntity();
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();

		final ArgumentCaptor<SimplePojoMapPojo> objectCaptor =
				ArgumentCaptor.forClass(SimplePojoMapPojo.class);
		verify(receiver).process(objectCaptor.capture());
		final SimplePojoMapPojo encodedPojo = objectCaptor.getValue();
		assertNotNull(encodedPojo);
		assertNotNull(encodedPojo.simplePojoMap);
		assertEquals(2, encodedPojo.simplePojoMap.size());
		assertEquals("stringValueA1", encodedPojo.simplePojoMap.get("mapKeyA")
				.stringField1);
		assertEquals("stringValueA2", encodedPojo.simplePojoMap.get("mapKeyA")
				.stringField2);
		assertEquals("stringValueB1", encodedPojo.simplePojoMap.get("mapKeyB")
				.stringField1);
		assertEquals("stringValueB2", encodedPojo.simplePojoMap.get("mapKeyB")
				.stringField2);
	}

	private <T> ObjectReceiver<T> createObjectReceiverMock() {
		// There is no type safe to create a mock with Mockito#mock(Class).
		// Hence, we have to use an unchecked cast here:
		return (ObjectReceiver<T>) mock(ObjectReceiver.class);
	}

	public static class EmptyPojo {

	}

	public static class SimplePojo {

		public String stringField1;
		private String stringField2;
		public int intField1;
		private int intField2;

		public void setStringField2(final String stringField2) {
			this.stringField2 = stringField2;
		}
		public String getStringField2() {
			return stringField2;
		}

		public void setIntField2(final int intField2) {
			this.intField2 = intField2;
		}

		public int getIntField2() {
			return intField2;
		}

	}

	public static class NestedPojo {

		public String attribute;
		public SimplePojo simplePojo;

	}

	public static class DoubleNestedPojo {

		public NestedPojo nestedPojo;

	}

	public static class StringListPojo {

		public List<String> stringList;
		public String attribute;

	}

	public static class IntegerListPojo {

		public List<Integer> integerList;

	}

	public static class SimplePojoListPojo {

		private List<SimplePojo> simplePojoList;

		public void setSimplePojoList(final List<SimplePojo> simplePojoList) {
			this.simplePojoList = simplePojoList;
		}

	}

	public static class StringMapPojo {

		public Map<String, String> stringMap;

	}

	public static class SimplePojoMapPojo {

		public Map<String, SimplePojo> simplePojoMap;

	}

}
