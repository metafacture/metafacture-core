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

import org.culturegraph.mf.formeta.Formeta;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.types.Triple;
import org.culturegraph.mf.types.Triple.ObjectType;
import org.culturegraph.mf.util.StreamConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests {@link StreamToTriples}
 * 
 * @author Markus Geipel
 * 
 */
public final class StreamToTriplesTest {

	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String ENTITY_NAME = "ename";
	private static final String REC_ID = "id";
	private static final String REC_ALT_ID = "altid";
	private static final String RECORD_PREDICATE = "rec_pred";

	private StreamToTriples toTriples;
	
	@Mock
	private ObjectReceiver<Triple> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		toTriples = new StreamToTriples();
		toTriples.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		toTriples.closeStream();
	}
	
	@Test
	public void testShouldBuildTripleFromLiteral() {
		toTriples.startRecord(REC_ID);
		toTriples.literal(NAME, VALUE);
		toTriples.endRecord();

		Mockito.verify(receiver).process(new Triple(REC_ID, NAME, VALUE));
	}

	@Test
	public void testShouldEncodeEntities() {
		toTriples.startRecord(REC_ID);
		toTriples.startEntity(ENTITY_NAME);
		toTriples.literal(NAME, VALUE);
		toTriples.startEntity(ENTITY_NAME);
		toTriples.literal(NAME, VALUE);
		toTriples.endEntity();
		toTriples.endEntity();
		toTriples.endRecord();

		final String objectValue = 
				Formeta.GROUP_START +
					NAME + Formeta.NAME_VALUE_SEPARATOR + VALUE + Formeta.ITEM_SEPARATOR + 
					ENTITY_NAME + Formeta.GROUP_START + 
						NAME + Formeta.NAME_VALUE_SEPARATOR + VALUE + 
					Formeta.GROUP_END + 
				Formeta.GROUP_END;
		Mockito.verify(receiver).process(new Triple(REC_ID,  ENTITY_NAME, objectValue, ObjectType.ENTITY));
	}

	@Test
	public void testShouldRedirectOnMoveToInName() {
		toTriples.setRedirect(true);

		toTriples.startRecord(REC_ID);
		toTriples.literal("{to:" + REC_ALT_ID + "}" + NAME, VALUE);
		toTriples.endRecord();

		Mockito.verify(receiver).process(new Triple(REC_ALT_ID, NAME, VALUE));
	}

	@Test
	public void testShouldRedirectIfAltIdGiven() {
		toTriples.setRedirect(true);

		toTriples.startRecord(REC_ID);
		toTriples.literal(StreamConstants.ID, REC_ALT_ID);
		toTriples.literal(NAME, VALUE);
		toTriples.endRecord();

		Mockito.verify(receiver).process(new Triple(REC_ALT_ID, NAME, VALUE));
	}

	@Test
	public void testShouldEncodeWholeRecordsIfRecordPredicateIsGiven() {
		toTriples.setRecordPredicate(RECORD_PREDICATE);
		
		toTriples.startRecord(REC_ID);
		toTriples.startEntity(ENTITY_NAME);
		toTriples.literal(NAME, VALUE);
		toTriples.endEntity();
		toTriples.startEntity(ENTITY_NAME);
		toTriples.literal(NAME, VALUE);
		toTriples.endEntity();
		toTriples.endRecord();
		
		final String objectValue = 
				Formeta.GROUP_START + 
					ENTITY_NAME + Formeta.GROUP_START + 
						NAME + Formeta.NAME_VALUE_SEPARATOR + VALUE + 
					Formeta.GROUP_END +
					ENTITY_NAME + Formeta.GROUP_START + 
						NAME + Formeta.NAME_VALUE_SEPARATOR + VALUE + 
					Formeta.GROUP_END + 
				Formeta.GROUP_END;
		Mockito.verify(receiver).process(new Triple(REC_ID, RECORD_PREDICATE, objectValue, ObjectType.ENTITY));
	}
	
	@Test
	public void testShouldRedirectEvenIfRecordPredicateIsGiven() {
		toTriples.setRecordPredicate(RECORD_PREDICATE);
		toTriples.setRedirect(true);		

		toTriples.startRecord(REC_ID);
		toTriples.literal(StreamConstants.ID, REC_ALT_ID);
		toTriples.literal(NAME, VALUE);
		toTriples.endRecord();

		final String objectValue = 
				Formeta.GROUP_START + 
					NAME + Formeta.NAME_VALUE_SEPARATOR + VALUE + 
				Formeta.GROUP_END;
		Mockito.verify(receiver).process(new Triple(REC_ALT_ID, RECORD_PREDICATE, objectValue, ObjectType.ENTITY));
	}
}
