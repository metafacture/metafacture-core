/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.converter.PojoDecoder.MetafactureSource;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Test;

/**
 * Tests for {@link PojoDecoder}
 * 
 * @author Thomas Seidel
 * 
 */
public class PojoDecoderTest {

	private final static String firstFieldName = "firstField";
	private final static String secondFieldName = "secondField";
	private final static String firstFieldValue = "firstValue";
	private final static String secondFieldValue = "secondValue";

	private final static String innerPojoName = "innerPojo";

	private final static String metafactureSourceField = "metafactureSourceField";
	private final static String metafactureSourceName = "metafactureSourceName";
	private final static String metafactureSourceValue = "metafactureSourceValue";

	private final static String listFieldName = "listField";
	private final static String firstListFieldValue = "firstListFieldValue";
	private final static String secondListFieldValue = "secondListFieldValue";
	private final static String setFieldName = "setField";
	private final static String firstSetFieldValue = "firstSetFieldValue";
	private final static String secondSetFieldValue = "secondSetFieldValue";
	private final static String arrayFieldName = "arrayField";
	private final static String firstArrayFieldValue = "firstArrayFieldValue";
	private final static String secondArrayFieldValue = "secondArrayFieldValue";

	private final static String mapFieldName = "mapField";
	private final static String firstMapFieldKey = "fistMapFieldKey";
	private final static String firstdMapFieldValue = "fistMapFieldValue";
	private final static String secondMapFieldKey = "secondMapFieldKey";
	private final static String secondMapFieldValue = "secondMapFieldKey";

	private static class EmptyPojo {
	}

	// Suppress warnings for unused fields or getters. The ObjectDecoder uses
	// them to access the values.
	private static class SimplePojo {
		private String firstField;
		@SuppressWarnings("unused")
		public String secondField;

		@SuppressWarnings("unused")
		public String getFirstField() {
			return firstField;
		}

		public void setFirstField(final String firstField) {
			this.firstField = firstField;
		}

	}

	// Suppress warnings for unused fields or getters. The ObjectDecoder uses
	// them to access the values.
	private static class NestedPojo {
		private SimplePojo innerPojo;

		@SuppressWarnings("unused")
		public SimplePojo getInnerPojo() {
			return innerPojo;
		}

		public final void setInnerPojo(final SimplePojo innerPojo) {
			this.innerPojo = innerPojo;
		}
	}

	// Suppress warnings for unused fields or getters. The ObjectDecoder uses
	// them to access the values.
	private static class NestedMetafactureSourcePojo {
		private MetafactureSource metafactureSourceField;

		@SuppressWarnings("unused")
		public MetafactureSource getMetafactureSourceField() {
			return metafactureSourceField;
		}

		public void setMetafactureSourceField(
				final MetafactureSource metafactureSourceField) {
			this.metafactureSourceField = metafactureSourceField;
		}

	}

	// Suppress warnings for unused fields or getters. The ObjectDecoder uses
	// them to access the values.
	private static class SimpleCollectionAndArrayPojo {
		private List<String> listField;
		private Set<String> setField;
		private String[] arrayField;
		private String firstField;

		@SuppressWarnings("unused")
		public List<String> getListField() {
			return listField;
		}

		public void setListField(final List<String> listField) {
			this.listField = listField;
		}

		@SuppressWarnings("unused")
		public Set<String> getSetField() {
			return setField;
		}

		public void setSetField(final Set<String> setField) {
			this.setField = setField;
		}

		@SuppressWarnings("unused")
		public String[] getArrayField() {
			return arrayField;
		}

		public void setArrayField(final String[] arrayField) {
			this.arrayField = arrayField;
		}

		@SuppressWarnings("unused")
		public String getFirstField() {
			return firstField;
		}

		public void setFirstField(final String firstField) {
			this.firstField = firstField;
		}

	}

	private static class SimpleMapPojo {
		private Map<String, String> mapField;

		@SuppressWarnings("unused")
		public Map<String, String> getMapField() {
			return mapField;
		}

		public void setMapField(final Map<String, String> mapField) {
			this.mapField = mapField;
		}
	}

	@Test
	public void shouldDecodeNullObject() {
		// create validator
		final EventList expected = new EventList();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode null pojo and verify result
		final PojoDecoder<Object> pojoDecoder = new PojoDecoder<Object>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(null);
	}

	@Test
	public void shouldDecodeEmptyPojo() {
		// create pojo
		final EmptyPojo emptyPojo = new EmptyPojo();
		// create validator
		final EventList expected = new EventList();
		expected.startRecord("");
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode pojo and verify result
		final PojoDecoder<EmptyPojo> pojoDecoder = new PojoDecoder<EmptyPojo>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(emptyPojo);
	}

	@Test
	public void shouldDecodeSimplePojo() {
		// create pojo
		final SimplePojo simplePojo = new SimplePojo();
		simplePojo.setFirstField(firstFieldValue);
		simplePojo.secondField = secondFieldValue;
		// create validator
		final EventList expected = new EventList();
		expected.startRecord("");
		expected.literal(firstFieldName, firstFieldValue);
		expected.literal(secondFieldName, secondFieldValue);
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode pojo and verify result
		final PojoDecoder<SimplePojo> pojoDecoder = new PojoDecoder<SimplePojo>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(simplePojo);
	}

	@Test
	public void shouldDecodeNestedPojo() {
		// create pojo
		final SimplePojo simplePojo = new SimplePojo();
		simplePojo.setFirstField(firstFieldValue);
		simplePojo.secondField = secondFieldValue;
		final NestedPojo nestedPojo = new NestedPojo();
		nestedPojo.setInnerPojo(simplePojo);
		// create validator
		final EventList expected = new EventList();
		expected.startRecord("");
		expected.startEntity(innerPojoName);
		expected.literal(firstFieldName, firstFieldValue);
		expected.literal(secondFieldName, secondFieldValue);
		expected.endEntity();
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode pojo and verify result
		final PojoDecoder<NestedPojo> pojoDecoder = new PojoDecoder<NestedPojo>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(nestedPojo);
	}

	@Test
	public void shouldDecodePojoWithMetafactureSource() {
		// create pojo
		final MetafactureSource metafactureSource = new MetafactureSource() {
			@Override
			public void sendToStream(final StreamReceiver streamReceiver) {
				streamReceiver.literal(metafactureSourceName,
						metafactureSourceValue);
			}
		};
		final NestedMetafactureSourcePojo nestedMetafactureSourcePojo = new NestedMetafactureSourcePojo();
		nestedMetafactureSourcePojo
				.setMetafactureSourceField(metafactureSource);
		// create validator
		final EventList expected = new EventList();
		expected.startRecord("");
		expected.startEntity(metafactureSourceField);
		expected.literal(metafactureSourceName, metafactureSourceValue);
		expected.endEntity();
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode pojo and verify result
		final PojoDecoder<NestedMetafactureSourcePojo> pojoDecoder = new PojoDecoder<NestedMetafactureSourcePojo>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(nestedMetafactureSourcePojo);
	}

	@Test
	public void shouldDecodeSimpleCollectionAndArrayPojo() {
		// create pojo
		final List<String> listField = Arrays.asList(firstListFieldValue,
				secondListFieldValue);
		final Set<String> setField = new HashSet<String>(Arrays.asList(
				firstSetFieldValue, secondSetFieldValue));
		final String[] arrayField = { firstArrayFieldValue,
				secondArrayFieldValue };
		final SimpleCollectionAndArrayPojo simpleCollectionAndArrayPojo = new SimpleCollectionAndArrayPojo();
		simpleCollectionAndArrayPojo.setListField(listField);
		simpleCollectionAndArrayPojo.setSetField(setField);
		simpleCollectionAndArrayPojo.setArrayField(arrayField);
		simpleCollectionAndArrayPojo.setFirstField(firstFieldValue);
		// create validator
		final EventList expected = new EventList();
		expected.startRecord("");
		expected.literal(listFieldName, firstListFieldValue);
		expected.literal(listFieldName, secondListFieldValue);
		expected.literal(setFieldName, firstSetFieldValue);
		expected.literal(setFieldName, secondSetFieldValue);
		expected.literal(arrayFieldName, firstArrayFieldValue);
		expected.literal(arrayFieldName, secondArrayFieldValue);
		expected.literal(firstFieldName, firstFieldValue);
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode pojo and verify result
		final PojoDecoder<SimpleCollectionAndArrayPojo> pojoDecoder = new PojoDecoder<SimpleCollectionAndArrayPojo>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(simpleCollectionAndArrayPojo);
	}

	@Test
	public void shouldDecodeSimpleMapPojo() {
		// create pojo
		final Map<String, String> mapField = new HashMap<String, String>();
		mapField.put(firstMapFieldKey, firstdMapFieldValue);
		mapField.put(secondMapFieldKey, secondMapFieldValue);
		final SimpleMapPojo simpleMapPojo = new SimpleMapPojo();
		simpleMapPojo.setMapField(mapField);
		// create validator
		final EventList expected = new EventList();
		expected.startRecord("");
		expected.startEntity(mapFieldName);
		expected.literal(firstMapFieldKey, firstdMapFieldValue);
		expected.literal(secondMapFieldKey, secondMapFieldValue);
		expected.endEntity();
		expected.endRecord();
		final StreamValidator validator = new StreamValidator(
				expected.getEvents());
		// decode pojo and verify result
		final PojoDecoder<SimpleMapPojo> pojoDecoder = new PojoDecoder<SimpleMapPojo>();
		pojoDecoder.setReceiver(validator);
		pojoDecoder.process(simpleMapPojo);
	}
}
