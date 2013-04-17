/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.pipe.bib;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests {@link PicaItemSplitter}.
 * 
 * @author Christoph Böhme
 *
 */
public final class PicaItemSplitterTest {

	private static final String RECORD_ID = "1";
	private static final String ENTITY = "001B";
	private static final String LITERAL = "a";
	private static final String VALUE = "val";
	private static final String ITEM_MARKER_ENTITY = "101@";
	private static final String ENTITY_WITH_SUFFIX1 = "002A/01";
	private static final String ENTITY_WITH_SUFFIX2 = "002A/02";
	private static final String ENTITY_WITH_SUFFIX_STRIPPED = "002A";
	
	private PicaItemSplitter picaItemSplitter;
	
	@Mock
	private StreamReceiver receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		picaItemSplitter = new PicaItemSplitter();
		picaItemSplitter.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		picaItemSplitter.closeStream();
	}

	@Test
	public void testShouldSplitAtItemMarkerEntities() {
		picaItemSplitter.startRecord(RECORD_ID);
		emitEntity();
		emitItemMarkerEntity();
		emitEntity();
		emitItemMarkerEntity();
		emitEntity();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyEntity(ordered);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyEntity(ordered);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyEntity(ordered);
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldCreateEmptyRecordsIfNoContentIsBeforeOrAfterItemMarkers() {
		picaItemSplitter.startRecord(RECORD_ID);
		emitItemMarkerEntity();
		emitItemMarkerEntity();
		picaItemSplitter.endRecord();
		

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldNotCreateTwoEmptyRecordsIfFirstEntityAfterItemMarkerHasSuffix() {
		picaItemSplitter.startRecord(RECORD_ID);
		emitEntity();
		emitItemMarkerEntity();
		emitSuffixedEntity1();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyEntity(ordered);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySuffixedEntityStripped(ordered);
		ordered.verify(receiver).endRecord();	
		verifyNoMoreInteractions(receiver);
	}
	
	@Test
	public void testShouldRemoveSuffix() {
		picaItemSplitter.startRecord(RECORD_ID);
		emitItemMarkerEntity();
		emitSuffixedEntity1();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySuffixedEntityStripped(ordered);
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldSplitWhenSuffixChanges() {
		picaItemSplitter.startRecord(RECORD_ID);
		emitItemMarkerEntity();		
		emitSuffixedEntity1();
		emitSuffixedEntity2();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySuffixedEntityStripped(ordered);
		ordered.verify(receiver).endRecord();		
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifySuffixedEntityStripped(ordered);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldOnlySplitOnSuffixChangeAfterFirstItemMarkerEntity() {
		picaItemSplitter.startRecord(RECORD_ID);
		emitEntity();
		emitSuffixedEntity1();
		emitSuffixedEntity2();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		verifyEntity(ordered);
		verifySuffixedEntity1(ordered);
		verifySuffixedEntity2(ordered);
		ordered.verify(receiver).endRecord();
		verifyNoMoreInteractions(receiver);
	}
	
	private void emitEntity() {
		picaItemSplitter.startEntity(ENTITY);
		picaItemSplitter.literal(LITERAL, VALUE);
		picaItemSplitter.endEntity();		
	}
	
	private void emitSuffixedEntity1() {
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX1);
		picaItemSplitter.literal(LITERAL, VALUE);
		picaItemSplitter.endEntity();		
	}
	
	private void emitSuffixedEntity2() {
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX2);
		picaItemSplitter.literal(LITERAL, VALUE);
		picaItemSplitter.endEntity();		
	}
	
	private void emitItemMarkerEntity() {
		picaItemSplitter.startEntity(ITEM_MARKER_ENTITY);
		picaItemSplitter.literal(LITERAL, VALUE);
		picaItemSplitter.endEntity();
	}
	
	private void verifyEntity(final InOrder ordered) {
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).literal(LITERAL, VALUE);
		ordered.verify(receiver).endEntity();		
	}
	
	private void verifySuffixedEntity1(final InOrder ordered) {
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX1);
		ordered.verify(receiver).literal(LITERAL, VALUE);
		ordered.verify(receiver).endEntity();		
	}
	
	private void verifySuffixedEntity2(final InOrder ordered) {
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX2);
		ordered.verify(receiver).literal(LITERAL, VALUE);
		ordered.verify(receiver).endEntity();		
	}
	
	private void verifySuffixedEntityStripped(final InOrder ordered) {
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX_STRIPPED);
		ordered.verify(receiver).literal(LITERAL, VALUE);
		ordered.verify(receiver).endEntity();		
	}
	
}
