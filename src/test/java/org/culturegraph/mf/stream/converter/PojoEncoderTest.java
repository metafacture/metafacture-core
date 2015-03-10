package org.culturegraph.mf.stream.converter;

import java.util.List;

import org.culturegraph.mf.stream.pipe.ObjectBuffer;
import org.junit.Assert;
import org.junit.Test;

public class PojoEncoderTest {

	public static class EmptyPojo {

	}

	@Test
	public void shouldEncodeEmptyEntityStreamToEmptyPojo() {
		final ObjectBuffer<EmptyPojo> objectBuffer = new ObjectBuffer<EmptyPojo>(
				1);
		final PojoEncoder<EmptyPojo> pojoEncoder = new PojoEncoder<EmptyPojo>(
				EmptyPojo.class);
		pojoEncoder.setReceiver(objectBuffer);
		pojoEncoder.startRecord("identifier");
		pojoEncoder.endRecord();
		Assert.assertNotNull(objectBuffer.pop());
	}

	public static class SimplePojo {
		public String firstStringAttribute;
		private String secondStringAttribute;
		public int firstIntegerAttribute;
		private int secondIntegerAttribute;

		public void setSecondStringAttribute(final String secondStringAttribute) {
			this.secondStringAttribute = secondStringAttribute;
		}

		public String getSecondStringAttribute() {
			return secondStringAttribute;
		}

		public int getSecondIntegerAttribute() {
			return secondIntegerAttribute;
		}

		public void setSecondIntegerAttribute(final int secondIntegerAttribute) {
			this.secondIntegerAttribute = secondIntegerAttribute;
		}
	}

	@Test
	public void shouldEncodeEntityStreamToSimplePojo() {
		final ObjectBuffer<SimplePojo> objectBuffer = new ObjectBuffer<SimplePojo>();
		final PojoEncoder<SimplePojo> pojoEncoder = new PojoEncoder<SimplePojo>(
				SimplePojo.class);
		pojoEncoder.setReceiver(objectBuffer);
		pojoEncoder.startRecord("identifier");
		pojoEncoder.literal("firstStringAttribute", "firstStringValue");
		pojoEncoder.literal("secondStringAttribute", "secondStringValue");
		pojoEncoder.literal("firstIntegerAttribute", "42");
		pojoEncoder.literal("secondIntegerAttribute", "23");
		pojoEncoder.endRecord();
		final SimplePojo simplePojo = objectBuffer.pop();
		Assert.assertNotNull(simplePojo);
		Assert.assertEquals("firstStringValue", simplePojo.firstStringAttribute);
		Assert.assertEquals("secondStringValue",
				simplePojo.getSecondStringAttribute());
		Assert.assertEquals(42, simplePojo.firstIntegerAttribute);
		Assert.assertEquals(23, simplePojo.getSecondIntegerAttribute());
	}

	public static class NestedPojo {
		public String attribute;
		public SimplePojo simplePojo;
	}

	public static class DoubleNestedPojo {
		public NestedPojo nestedPojo;
	}

	@Test
	public void shouldEncodeEntityStreamToDoubleNestedPojo() {
		final ObjectBuffer<DoubleNestedPojo> objectBuffer = new ObjectBuffer<DoubleNestedPojo>();
		final PojoEncoder<DoubleNestedPojo> pojoEncoder = new PojoEncoder<DoubleNestedPojo>(
				DoubleNestedPojo.class);
		pojoEncoder.setReceiver(objectBuffer);
		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("nestedPojo");
		pojoEncoder.startEntity("simplePojo");
		pojoEncoder.literal("firstStringAttribute", "firstStringValue");
		pojoEncoder.literal("secondStringAttribute", "secondStringValue");
		pojoEncoder.endEntity();
		pojoEncoder.literal("attribute", "value");
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();
		final DoubleNestedPojo doubleNestedPojo = objectBuffer.pop();
		Assert.assertNotNull(doubleNestedPojo);
		final NestedPojo nestedPojo = doubleNestedPojo.nestedPojo;
		Assert.assertNotNull(nestedPojo);
		Assert.assertEquals("value", nestedPojo.attribute);
		final SimplePojo innerPojo = nestedPojo.simplePojo;
		Assert.assertNotNull(innerPojo);
		Assert.assertEquals("firstStringValue", innerPojo.firstStringAttribute);
		Assert.assertEquals("secondStringValue",
				innerPojo.getSecondStringAttribute());
	}

	public static class StringListPojo {
		public List<String> stringList;
		public String attribute;
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithStringList() {
		final ObjectBuffer<StringListPojo> objectBuffer = new ObjectBuffer<StringListPojo>();
		final PojoEncoder<StringListPojo> pojoEncoder = new PojoEncoder<StringListPojo>(
				StringListPojo.class);
		pojoEncoder.setReceiver(objectBuffer);
		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("stringList");
		pojoEncoder.literal("firstElement", "firstValue");
		pojoEncoder.literal("secondElement", "secondValue");
		pojoEncoder.endEntity();
		pojoEncoder.literal("attribute", "value");
		pojoEncoder.endRecord();
		final StringListPojo stringListPojo = objectBuffer.pop();
		Assert.assertNotNull(stringListPojo);
		Assert.assertEquals("value", stringListPojo.attribute);
		final List<String> strings = stringListPojo.stringList;
		Assert.assertNotNull(strings);
		Assert.assertEquals(2, strings.size());
		Assert.assertEquals("firstValue", strings.get(0));
		Assert.assertEquals("secondValue", strings.get(1));
	}

	public static class IntegerListPojo {
		public List<Integer> integerList;
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithIntegerList() {
		final ObjectBuffer<IntegerListPojo> objectBuffer = new ObjectBuffer<IntegerListPojo>();
		final PojoEncoder<IntegerListPojo> pojoEncoder = new PojoEncoder<IntegerListPojo>(
				IntegerListPojo.class);
		pojoEncoder.setReceiver(objectBuffer);
		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("integerList");
		pojoEncoder.literal("firstElement", "42");
		pojoEncoder.literal("firstElement", "23");
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();
		final IntegerListPojo integerListPojo = objectBuffer.pop();
		Assert.assertNotNull(integerListPojo);
		final List<Integer> integers = integerListPojo.integerList;
		Assert.assertNotNull(integers);
		Assert.assertEquals(2, integers.size());
		Assert.assertEquals(42, integers.get(0).intValue());
		Assert.assertEquals(23, integers.get(1).intValue());
	}

	public static class SimplePojoListPojo {
		private List<SimplePojo> simplePojoList;

		public void setSimplePojoList(final List<SimplePojo> simplePojoList) {
			this.simplePojoList = simplePojoList;
		}
	}

	@Test
	public void shouldEncodeEntityStreamToPojoWithSimplePojoList() {
		final ObjectBuffer<SimplePojoListPojo> objectBuffer = new ObjectBuffer<SimplePojoListPojo>();
		final PojoEncoder<SimplePojoListPojo> pojoEncoder = new PojoEncoder<SimplePojoListPojo>(
				SimplePojoListPojo.class);
		pojoEncoder.setReceiver(objectBuffer);
		pojoEncoder.startRecord("identifier");
		pojoEncoder.startEntity("simplePojoList");
		pojoEncoder.startEntity("simplePojo");
		pojoEncoder.literal("firstStringAttribute", "firstStringValue");
		pojoEncoder.literal("secondStringAttribute", "secondStringValue");
		pojoEncoder.endEntity();
		pojoEncoder.startEntity("simplePojo");
		pojoEncoder.literal("firstStringAttribute", "thirdValue");
		pojoEncoder.endEntity();
		pojoEncoder.endEntity();
		pojoEncoder.endRecord();
		final SimplePojoListPojo simplePojoListPojo = objectBuffer.pop();
		Assert.assertNotNull(simplePojoListPojo);
		final List<SimplePojo> simplePojos = simplePojoListPojo.simplePojoList;
		Assert.assertNotNull(simplePojos);
		Assert.assertEquals(2, simplePojos.size());
		Assert.assertEquals("firstStringValue",
				simplePojos.get(0).firstStringAttribute);
		Assert.assertEquals("secondStringValue", simplePojos.get(0)
				.getSecondStringAttribute());
		Assert.assertEquals("thirdValue",
				simplePojos.get(1).firstStringAttribute);
	}

}
