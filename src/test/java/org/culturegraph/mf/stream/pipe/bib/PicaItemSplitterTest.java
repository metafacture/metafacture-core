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
	private static final String ENTITY = "001@";
	private static final String LITERAL1 = "a";
	private static final String LITERAL2 = "b";
	private static final String VALUE = "val";
	private static final String ENTITY_WITH_SUFFIX1 = "002/01";
	private static final String ENTITY_WITH_SUFFIX2 = "002/02";
	private static final String ENTITY_WITH_SUFFIX_STRIPPED = "002";
	
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
	public void testShouldSplitAtFirstEntityWithSuffix() {
		picaItemSplitter.startRecord(RECORD_ID);
		picaItemSplitter.startEntity(ENTITY);
		picaItemSplitter.literal(LITERAL1, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX1);
		picaItemSplitter.literal(LITERAL2, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).literal(LITERAL1, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX_STRIPPED);
		ordered.verify(receiver).literal(LITERAL2, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldNotSplitIfTheFirstEntityHasASuffix() {
		picaItemSplitter.startRecord(RECORD_ID);
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX1);
		picaItemSplitter.literal(LITERAL1, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.endRecord();		

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX_STRIPPED);
		ordered.verify(receiver).literal(LITERAL1, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldSplitAtFirstEntityWithoutSuffix() {
		picaItemSplitter.startRecord(RECORD_ID);
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX1);
		picaItemSplitter.literal(LITERAL2, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.startEntity(ENTITY);
		picaItemSplitter.literal(LITERAL1, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX_STRIPPED);
		ordered.verify(receiver).literal(LITERAL2, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();		
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY);
		ordered.verify(receiver).literal(LITERAL1, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}
	
	@Test
	public void testShouldSplitWhenSuffixChanges() {
		picaItemSplitter.startRecord(RECORD_ID);
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX1);
		picaItemSplitter.literal(LITERAL1, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.startEntity(ENTITY_WITH_SUFFIX2);
		picaItemSplitter.literal(LITERAL2, VALUE);
		picaItemSplitter.endEntity();
		picaItemSplitter.endRecord();
		
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX_STRIPPED);
		ordered.verify(receiver).literal(LITERAL1, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();		
		ordered.verify(receiver).startRecord(RECORD_ID);
		ordered.verify(receiver).startEntity(ENTITY_WITH_SUFFIX_STRIPPED);
		ordered.verify(receiver).literal(LITERAL2, VALUE);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();		
	}
	
}
