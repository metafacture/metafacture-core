package org.culturegraph.mf.stream.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

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

	private <T> ObjectReceiver<T> createObjectReceiverMock() {
		// There is no type safe to create a mock with Mockito#mock(Class).
		// Hence, we have to use an unchecked cast here:
		return (ObjectReceiver<T>) mock(ObjectReceiver.class);
	}

	static class EmptyPojo {

	}

	static class SimplePojo {

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

	static class NestedPojo {

		public String attribute;
		public SimplePojo simplePojo;

	}

	static class DoubleNestedPojo {

		public NestedPojo nestedPojo;

	}

	static class StringListPojo {

		public List<String> stringList;
		public String attribute;

	}

	static class IntegerListPojo {

		public List<Integer> integerList;

	}

	static class SimplePojoListPojo {

		private List<SimplePojo> simplePojoList;

		public void setSimplePojoList(final List<SimplePojo> simplePojoList) {
			this.simplePojoList = simplePojoList;
		}

	}

}
