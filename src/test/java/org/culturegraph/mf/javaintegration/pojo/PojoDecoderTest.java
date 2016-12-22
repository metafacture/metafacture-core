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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link PojoDecoder}.
 *
 * @author Thomas Seidel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public class PojoDecoderTest {

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldDecodeNullObject() {
		final PojoDecoder<Object> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		pojoDecoder.process(null);

		verifyZeroInteractions(receiver);
	}

	@Test
	public void shouldDecodeEmptyPojo() {
		final PojoDecoder<EmptyPojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		pojoDecoder.process(new EmptyPojo());

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodeSimplePojo() {
		final PojoDecoder<SimplePojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final SimplePojo simplePojo = new SimplePojo();
		simplePojo.setFirstField("value1");
		simplePojo.secondField = "value2";
		pojoDecoder.process(simplePojo);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal("secondField", "value2");
		ordered.verify(receiver).literal("firstField", "value1");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodeNestedPojo() {
		final PojoDecoder<NestedPojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final SimplePojo simplePojo = new SimplePojo();
		simplePojo.setFirstField("value1");
		simplePojo.secondField = "value2";
		final NestedPojo nestedPojo = new NestedPojo();
		nestedPojo.setInnerPojo(simplePojo);
		pojoDecoder.process(nestedPojo);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("innerPojo");
		ordered.verify(receiver).literal("secondField", "value2");
		ordered.verify(receiver).literal("firstField", "value1");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodePojoWithMetafactureSource() {
		final PojoDecoder<MetafactureSourcePojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final MetafactureSourcePojo mfSourcePojo = new MetafactureSourcePojo();
		mfSourcePojo.setMetafactureSourceField(
				streamReceiver -> streamReceiver.literal("literal", "value"));
		pojoDecoder.process(mfSourcePojo);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("metafactureSourceField");
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodeSimpleArrayPojo() {
		final PojoDecoder<SimpleArrayPojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final String[] valueArray = { "array-value1", "array-value2"};
		final SimpleArrayPojo simpleArrayPojo = new SimpleArrayPojo();
		simpleArrayPojo.setArrayField(valueArray);
		pojoDecoder.process(simpleArrayPojo);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal("arrayField", "array-value1");
		ordered.verify(receiver).literal("arrayField", "array-value2");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodeSimpleListPojo() {
		final PojoDecoder<SimpleListPojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final List<String> valueList = Arrays.asList("list-value1", "list-value2");
		final SimpleListPojo simpleListPojo = new SimpleListPojo();
		simpleListPojo.setListField(valueList);
		pojoDecoder.process(simpleListPojo);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal("listField", "list-value1");
		ordered.verify(receiver).literal("listField", "list-value2");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodeSimpleSetPojo() {
		final PojoDecoder<SimpleSetPojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final Set<String> valueSet = new HashSet<>(Arrays.asList(
				"set-value1", "set-value2"));
		final SimpleSetPojo simpleSetPojo = new SimpleSetPojo();
		simpleSetPojo.setSetField(valueSet);
		pojoDecoder.process(simpleSetPojo);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal("setField", "set-value1");
		ordered.verify(receiver).literal("setField", "set-value2");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldDecodeSimpleMapPojo() {
		final PojoDecoder<SimpleMapPojo> pojoDecoder = new PojoDecoder<>();
		pojoDecoder.setReceiver(receiver);

		final Map<String, String> mapField = new HashMap<>();
		mapField.put("key1", "value1");
		mapField.put("key2", "value2");
		final SimpleMapPojo simpleMapPojo = new SimpleMapPojo();
		simpleMapPojo.setMapField(mapField);
		pojoDecoder.process(simpleMapPojo);

		final ArgumentCaptor<String> nameCaptor =
				ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> valueCaptor =
				ArgumentCaptor.forClass(String.class);
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("mapField");
		ordered.verify(receiver, times(2)).literal(
				nameCaptor.capture(), valueCaptor.capture());
		assertEquals(mapField.get(nameCaptor.getAllValues().get(0)),
				valueCaptor.getAllValues().get(0));
		assertEquals(mapField.get(nameCaptor.getAllValues().get(1)),
				valueCaptor.getAllValues().get(1));
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	private static class EmptyPojo {
	}

	private static class SimplePojo {

		private String firstField;
		public String secondField;

		public String getFirstField() {
			return firstField;
		}

		public void setFirstField(final String firstField) {
			this.firstField = firstField;
		}

	}

	private static class NestedPojo {

		private SimplePojo innerPojo;

		public SimplePojo getInnerPojo() {
			return innerPojo;
		}

		public final void setInnerPojo(final SimplePojo innerPojo) {
			this.innerPojo = innerPojo;
		}
	}

	private static class MetafactureSourcePojo {

		private MetafactureSource metafactureSourceField;

		public MetafactureSource getMetafactureSourceField() {
			return metafactureSourceField;
		}

		public void setMetafactureSourceField(
				final MetafactureSource metafactureSourceField) {
			this.metafactureSourceField = metafactureSourceField;
		}

	}

	private static class SimpleArrayPojo {

		private String[] arrayField;

		public String[] getArrayField() {
			return arrayField;
		}

		public void setArrayField(final String[] arrayField) {
			this.arrayField = arrayField;
		}

	}

	private static class SimpleListPojo {

		private List<String> listField;

		public List<String> getListField() {
			return listField;
		}

		public void setListField(final List<String> listField) {
			this.listField = listField;
		}

	}

	private static class SimpleSetPojo {

		private Set<String> setField;

		public Set<String> getSetField() {
			return setField;
		}

		public void setSetField(final Set<String> setField) {
			this.setField = setField;
		}

	}

	private static class SimpleMapPojo {

		private Map<String, String> mapField;

		public Map<String, String> getMapField() {
			return mapField;
		}

		public void setMapField(final Map<String, String> mapField) {
			this.mapField = mapField;
		}

	}

}
