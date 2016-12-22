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
package org.culturegraph.mf.mangling;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.StandardEventNames;
import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link RecordIdChanger}.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordIdChangerTest {

	private static final String OLD_RECORD_ID1 = "OLD ID 1";
	private static final String OLD_RECORD_ID2 = "OLD ID 2";
	private static final String NEW_RECORD_ID1 = "NEW ID 1";
	private static final String NEW_RECORD_ID2 = "NEW ID 2";
	private static final String ENTITY = "En";
	private static final String LITERAL_NAME = "Li";
	private static final String LITERAL_VALUE = "Va";

	private RecordIdChanger recordIdChanger;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		recordIdChanger = new RecordIdChanger();
		recordIdChanger.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		recordIdChanger.closeStream();
	}

	@Test
	public void testShouldChangeIdsOfRecords() {
		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		recordIdChanger.endRecord();

		recordIdChanger.startRecord(OLD_RECORD_ID2);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).endRecord();

		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldKeepRecordsWithoutIdLiteral() {
		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.literal(LITERAL_NAME, LITERAL_VALUE);
		recordIdChanger.endRecord();
		recordIdChanger.startRecord(OLD_RECORD_ID2);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(OLD_RECORD_ID1);
		ordered.verify(receiver).literal(LITERAL_NAME, LITERAL_VALUE);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldRemoveRecordsWithoutIdLiteral() {
		recordIdChanger.setKeepRecordsWithoutIdLiteral(false);

		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.literal(LITERAL_NAME, LITERAL_VALUE);
		recordIdChanger.endRecord();
		recordIdChanger.startRecord(OLD_RECORD_ID2);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldNotUseNestedIdLiteralAsNewId() {
		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.startEntity(ENTITY);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		recordIdChanger.endEntity();
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(OLD_RECORD_ID1);
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).literal(StandardEventNames.ID, NEW_RECORD_ID1);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldAcceptFullPathAsNewId() {
		recordIdChanger.setIdLiteral(ENTITY + "." + StandardEventNames.ID);

		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.startEntity(ENTITY);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		recordIdChanger.endEntity();
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldNotKeepIdLiteralByDefault() {
		recordIdChanger.setIdLiteral(StandardEventNames.ID);

		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldKeepIdLiteralIfConfigured() {
		recordIdChanger.setIdLiteral(StandardEventNames.ID);
		recordIdChanger.setKeepIdLiteral(true);

		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID1);
		ordered.verify(receiver).literal(StandardEventNames.ID, NEW_RECORD_ID1);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldUseLastIdLiteralAsNewId() {
		recordIdChanger.startRecord(OLD_RECORD_ID1);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID1);
		recordIdChanger.literal(StandardEventNames.ID, NEW_RECORD_ID2);
		recordIdChanger.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(NEW_RECORD_ID2);
		ordered.verify(receiver).endRecord();
	}

}
