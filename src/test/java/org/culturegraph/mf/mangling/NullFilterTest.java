/*
 * Copyright 2016 hbz
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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link NullFilter}.
 *
 * @author Jens Wille
 *
 */
public final class NullFilterTest {

	private static final String RECORD_ID = "id";
	private static final String ENTITY_NAME = "entity-name";
	private static final String LITERAL_NAME = "literal-name";
	private static final String LITERAL_VALUE = "literal-value";

	private NullFilter nullFilter;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		nullFilter = new NullFilter();
		nullFilter.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		nullFilter.closeStream();
	}

	@Test
	public void shouldForwardAllEvents() {
		nullFilter.startRecord(RECORD_ID);
		nullFilter.startEntity(ENTITY_NAME);
		nullFilter.literal(LITERAL_NAME, LITERAL_VALUE);
		nullFilter.endEntity();
		nullFilter.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_NAME);
		ordered.verify(receiver).literal(LITERAL_NAME, LITERAL_VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();

		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldDiscardNullValues() {
		nullFilter.startRecord(RECORD_ID);
		nullFilter.startEntity(ENTITY_NAME);
		nullFilter.literal(LITERAL_NAME, LITERAL_VALUE);
		nullFilter.literal(LITERAL_NAME, null);
		nullFilter.endEntity();
		nullFilter.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_NAME);
		ordered.verify(receiver).literal(LITERAL_NAME, LITERAL_VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();

		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldReplaceNullValues() {
		nullFilter.setReplacement("replacement");

		nullFilter.startRecord(RECORD_ID);
		nullFilter.startEntity(ENTITY_NAME);
		nullFilter.literal(LITERAL_NAME, LITERAL_VALUE);
		nullFilter.literal(LITERAL_NAME, null);
		nullFilter.endEntity();
		nullFilter.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_NAME);
		ordered.verify(receiver).literal(LITERAL_NAME, LITERAL_VALUE);
		ordered.verify(receiver).literal(LITERAL_NAME, "replacement");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();

		verifyNoMoreInteractions(receiver);
	}

}
