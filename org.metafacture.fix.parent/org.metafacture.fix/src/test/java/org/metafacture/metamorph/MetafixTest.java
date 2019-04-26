/*
 * Copyright 2016–2019 Christoph Böhme and others
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
package org.metafacture.metamorph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.NamedValueReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for class {@link Metafix}.
 *
 * @author Markus Michael Geipel (MetamorphTest)
 * @author Christoph Böhme (rewrite MetamorphTest)
 * @author Fabian Steeg (adapt MetamorphTest for Metafix and JUnit 5)
 */
@ExtendWith(MockitoExtension.class)
public final class MetafixTest {

	@RegisterExtension
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private NamedValueReceiver namedValueReceiver;

	private Metafix metafix;

	@BeforeEach
	public void createSystemUnderTest() {
		metafix = new Metafix();
		metafix.setReceiver(new DefaultStreamReceiver());
	}

	@Disabled
	@Test
	public void shouldMapMatchingPath() {
		setupSimpleMappingMorph();

		metafix.startRecord("");
		metafix.literal("testEntity.testLiteral", "testValue");

		verify(namedValueReceiver).receive(eq("outName"), eq("testValue"), any(), anyInt(), anyInt());
	}

	@Test
	public void shouldNotMapNonMatchingPath() {
		setupSimpleMappingMorph();

		metafix.startRecord("");
		metafix.literal("nonMatching.path", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(), anyInt());
	}

	@Disabled
	@Test
	public void shouldMapMatchingLiteralInMatchingEntity() {
		setupSimpleMappingMorph();

		metafix.startRecord("");
		metafix.startEntity("testEntity");
		metafix.literal("testLiteral", "testValue");

		verify(namedValueReceiver).receive(eq("outName"), eq("testValue"), any(), anyInt(), anyInt());
	}

	@Test
	public void shouldNotMapNonMatchingLiteralInMatchingEntity() {
		setupSimpleMappingMorph();

		metafix.startRecord("");
		metafix.startEntity("testEntity");
		metafix.literal("nonMatching", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(), anyInt());
	}

	@Test
	public void shouldNotMapMatchingLiteralInNonMatchingEntity() {
		setupSimpleMappingMorph();

		metafix.startRecord("");
		metafix.startEntity("nonMatching");
		metafix.literal("testLiteral", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(), anyInt());
	}

	@Test
	public void shouldNotMapLiteralWithoutMatchingEntity() {
		setupSimpleMappingMorph();

		metafix.startRecord("");
		metafix.literal("testLiteral", "testValue");

		verify(namedValueReceiver, never()).receive(any(), any(), any(), anyInt(), anyInt());
	}

	/**
	 * Creates the Metamorph structure that corresponds to the Metamorph XML
	 * statement {@code <data source="testEntity.testLiteral" name="outName" />}.
	 */
	private void setupSimpleMappingMorph() {
		final Data data = new Data();
		data.setName("outName");
		data.setNamedValueReceiver(namedValueReceiver);
		metafix.registerNamedValueReceiver("testEntity" + '.' + "testLiteral", data);
	}

	@Disabled
	@Test
	public void shouldReturnValueFromNestedMap() {
		final Map<String, String> map = new HashMap<>();
		map.put("outName", "testValue");

		metafix.putMap("testMap", map);

		assertNotNull(metafix.getMap("testMap"));
		assertEquals("testValue", metafix.getValue("testMap", "outName"));
	}

	@Disabled
	@Test
	public void shouldReturnDefaultValueIfMapIsKnownButNameIsUnknown() {
		final Map<String, String> map = new HashMap<>();
		map.put(Maps.DEFAULT_MAP_KEY, "defaultValue");

		metafix.putMap("testMap", map);

		assertEquals("defaultValue", metafix.getValue("testMap", "nameNotInMap"));
	}

	@Disabled
	@Test
	public void shouldFeedbackLiteralsStartingWithAtIntoMetamorph() {
		final Data data1;
		data1 = new Data();
		data1.setName("@feedback");
		metafix.addNamedValueSource(data1);
		metafix.registerNamedValueReceiver("testLiteral", data1);

		final Data data2 = new Data();
		data2.setName("outName");
		data2.setNamedValueReceiver(namedValueReceiver);
		metafix.registerNamedValueReceiver("@feedback", data2);

		metafix.startRecord("");
		metafix.literal("testLiteral", "testValue");

		verify(namedValueReceiver).receive(eq("outName"), eq("testValue"), any(), anyInt(), anyInt());
	}

	@Disabled
	@Test
	public void shouldThrowIllegalStateExceptionIfEntityIsNotClosed() {
		assertThrows(ArithmeticException.class, () -> {
			metafix.startRecord("");
			metafix.startEntity("testEntity");
			metafix.startEntity("testEntity");
			metafix.endEntity();
			metafix.endRecord(); // Exception expected
		});
	}

}
