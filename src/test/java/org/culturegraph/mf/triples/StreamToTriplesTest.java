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
package org.culturegraph.mf.triples;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StandardEventNames;
import org.culturegraph.mf.framework.objects.Triple;
import org.culturegraph.mf.framework.objects.Triple.ObjectType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamToTriples}.
 *
 * @author Markus Geipel
 * @author Christoph Böhme
 *
 */
public final class StreamToTriplesTest {

	@Mock
	private ObjectReceiver<Triple> receiver;

	private StreamToTriples streamToTriples;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		streamToTriples = new StreamToTriples();
		streamToTriples.setReceiver(receiver);
	}

	@Test
	public void shouldBuildTripleFromLiteral() {
		streamToTriples.startRecord("id");
		streamToTriples.literal("literal", "value");
		streamToTriples.endRecord();

		Mockito.verify(receiver).process(new Triple("id", "literal", "value"));
	}

	@Test
	public void shouldEncodeEntities() {
		streamToTriples.startRecord("id");
		streamToTriples.startEntity("entity1");
		streamToTriples.literal("literal1", "value1");
		streamToTriples.startEntity("entity2");
		streamToTriples.literal("literal2", "value2");
		streamToTriples.endEntity();
		streamToTriples.endEntity();
		streamToTriples.endRecord();

		final String encodedEntity =
				"{literal1:value1,entity2{literal2:value2}}";
		Mockito.verify(receiver).process(
				new Triple("id",  "entity1", encodedEntity, ObjectType.ENTITY));
	}

	@Test
	public void shouldRedirectOnMoveToInName() {
		streamToTriples.setRedirect(true);

		streamToTriples.startRecord("id");
		streamToTriples.literal("{to:altId}literal", "value");
		streamToTriples.endRecord();

		Mockito.verify(receiver).process(new Triple("altId", "literal", "value"));
	}

	@Test
	public void shouldRedirectIfAltIdGiven() {
		streamToTriples.setRedirect(true);

		streamToTriples.startRecord("id");
		streamToTriples.literal(StandardEventNames.ID, "altId");
		streamToTriples.literal("literal", "value");
		streamToTriples.endRecord();

		Mockito.verify(receiver).process(new Triple("altId", "literal", "value"));
	}

	@Test
	public void shouldEncodeWholeRecordsIfRecordPredicateIsGiven() {
		streamToTriples.setRecordPredicate("record");

		streamToTriples.startRecord("id");
		streamToTriples.startEntity("entity1");
		streamToTriples.literal("literal1", "value1");
		streamToTriples.endEntity();
		streamToTriples.startEntity("entity2");
		streamToTriples.literal("literal2", "value2");
		streamToTriples.endEntity();
		streamToTriples.endRecord();

		final String encodedRecord =
				"{entity1{literal1:value1}entity2{literal2:value2}}";
		Mockito.verify(receiver).process(
				new Triple("id", "record", encodedRecord, ObjectType.ENTITY));
	}

	@Test
	public void shouldRedirectEvenIfRecordPredicateIsGiven() {
		streamToTriples.setRecordPredicate("record");
		streamToTriples.setRedirect(true);

		streamToTriples.startRecord("id");
		streamToTriples.literal(StandardEventNames.ID, "altId");
		streamToTriples.literal("literal", "value");
		streamToTriples.endRecord();

		final String encodedRecord = "{literal:value}";
		Mockito.verify(receiver).process(
				new Triple("altId", "record", encodedRecord, ObjectType.ENTITY));
	}

}
