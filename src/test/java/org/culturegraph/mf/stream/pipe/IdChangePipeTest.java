/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.stream.pipe;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.StandardEventNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link IdChangePipe}.
 *
 * @author Christoph Böhme
 *
 */
public final class IdChangePipeTest {

	private static final String OLD_RECORD_ID1 = "OLD ID 1";
	private static final String OLD_RECORD_ID2 = "OLD ID 2";
	private static final String NEW_RECORD_ID1 = "NEW ID 1";
	private static final String NEW_RECORD_ID2 = "NEW ID 2";
	private static final String ENTITY = "En";
	private static final String LITERAL_NAME = "Li";
	private static final String LITERAL_VALUE = "Va";

	private IdChangePipe idChangePipe;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		idChangePipe = new IdChangePipe();
		idChangePipe.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		idChangePipe.closeStream();
	}

	@Test
	public void testShouldChangeIdsOfRecords() {
		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		idChangePipe.endRecord();

		idChangePipe.startRecord(OLD_RECORD_ID2);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).endRecord();

		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldKeepRecordsWithoutIdLiteral() {
		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.literal(LITERAL_NAME, LITERAL_VALUE);
		idChangePipe.endRecord();
		idChangePipe.startRecord(OLD_RECORD_ID2);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(OLD_RECORD_ID1);
		ordered.verify(receiver).literal(LITERAL_NAME, LITERAL_VALUE);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldRemoveRecordsWithoutIdLiteral() {
		idChangePipe.setKeepRecordsWithoutIdLiteral(false);

		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.literal(LITERAL_NAME, LITERAL_VALUE);
		idChangePipe.endRecord();
		idChangePipe.startRecord(OLD_RECORD_ID2);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldNotUseNestedIdLiteralAsNewId() {
		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.startEntity(ENTITY);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		idChangePipe.endEntity();
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(OLD_RECORD_ID1);
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).literal(StandardEventNames.ID, NEW_RECORD_ID1);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldAcceptFullPathAsNewId() {
		idChangePipe.setIdLiteral(ENTITY + "." + StandardEventNames.ID);

		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.startEntity(ENTITY);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		idChangePipe.endEntity();
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldNotKeepIdLiteralByDefault() {
		idChangePipe.setIdLiteral(StandardEventNames.ID);

		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldKeepIdLiteralIfConfigured() {
		idChangePipe.setIdLiteral(StandardEventNames.ID);
		idChangePipe.setKeepIdLiteral(true);

		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).literal(StandardEventNames.ID, NEW_RECORD_ID1);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldUseLastIdLiteralAsNewId() {
		idChangePipe.startRecord(OLD_RECORD_ID1);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		idChangePipe.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		idChangePipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
	}

}
