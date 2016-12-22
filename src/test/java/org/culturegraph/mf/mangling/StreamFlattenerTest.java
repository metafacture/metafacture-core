/*
 * Copyright 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.mangling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamFlattener}.
 *
 * @author Christoph Böhme
 *
 */
public class StreamFlattenerTest {

	@Mock
	private StreamReceiver receiver;

	private StreamFlattener flattener;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		flattener = new StreamFlattener();
		flattener.setReceiver(receiver);
	}

	@Test
	public void shouldFlattenEntitiesAndUseEntityPathForLiteralNames() {
		flattener.startRecord("1");
		flattener.startEntity("granny");
		flattener.literal("me", "value1");
		flattener.startEntity("mommy");
		flattener.literal("myself", "value2");
		flattener.endEntity();
		flattener.endEntity();
		flattener.literal("andI", "value3");
		flattener.endRecord();
		flattener.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("granny.me", "value1");
		ordered.verify(receiver).literal("granny.mommy.myself", "value2");
		ordered.verify(receiver).literal("andI", "value3");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).closeStream();
	}

	@Test
	public void getCurrentPath_shouldReturnPathToCurrentEntity() {
		flattener.startRecord("1");
		assertEquals("", flattener.getCurrentPath());
		flattener.startEntity("granny");
		assertEquals("granny", flattener.getCurrentPath());
		flattener.literal("me", "value1");
		assertEquals("granny", flattener.getCurrentPath());
		flattener.startEntity("mommy");
		assertEquals("granny.mommy", flattener.getCurrentPath());
		flattener.literal("myself", "value2");
		assertEquals("granny.mommy", flattener.getCurrentPath());
		flattener.endEntity();
		assertEquals("granny", flattener.getCurrentPath());
		flattener.endEntity();
		assertEquals("", flattener.getCurrentPath());
		flattener.literal("andI", "value3");
		assertEquals("", flattener.getCurrentPath());
		flattener.endRecord();
		assertEquals("", flattener.getCurrentPath());
		flattener.closeStream();
	}

	@Test
	public void getCurrentEntityName_shouldReturnNameOfCurrentEntity() {
		flattener.startRecord("1");
		assertNull(flattener.getCurrentEntityName());
		flattener.startEntity("granny");
		assertEquals("granny", flattener.getCurrentEntityName());
		flattener.literal("me", "value1");
		assertEquals("granny", flattener.getCurrentEntityName());
		flattener.startEntity("mommy");
		assertEquals("mommy", flattener.getCurrentEntityName());
		flattener.literal("myself", "value2");
		assertEquals("mommy", flattener.getCurrentEntityName());
		flattener.endEntity();
		assertEquals("granny", flattener.getCurrentEntityName());
		flattener.endEntity();
		assertNull(flattener.getCurrentEntityName());
		flattener.literal("andI", "value3");
		assertNull(flattener.getCurrentEntityName());
		flattener.endRecord();
		assertNull(flattener.getCurrentEntityName());
		flattener.closeStream();
	}

	@Test
	public void setEntityMarker_shouldChangeMarkerBetweenEntities() {
		flattener.setEntityMarker("-");

		flattener.startRecord("1");
		flattener.startEntity("granny");
		flattener.startEntity("mommy");
		assertEquals("granny-mommy", flattener.getCurrentPath());
	}

}
