/*
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
package org.culturegraph.mf.triples;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.objects.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link TripleFilter},
 *
 * @author Christoph Böhme
 *
 */
public final class TripleFilterTest {

	private static final Triple TRIPLE1 = new Triple("sA", "pA", "oA");
	private static final Triple TRIPLE2 = new Triple("sB", "pB", "oB");
	private static final Triple TRIPLE3 = new Triple("sC", "pC", "oC");

	private TripleFilter tripleFilter;

	@Mock
	private ObjectReceiver<Triple> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		tripleFilter = new TripleFilter();
		tripleFilter.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		tripleFilter.closeStream();
	}

	@Test
	public void testShouldPassMatchingTripleByDefault() {
		tripleFilter.setSubjectPattern("sA");

		tripleFilter.process(TRIPLE1);
		tripleFilter.process(TRIPLE2);

		verify(receiver).process(TRIPLE1);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldPassNonMatchingTripleIfPassMatchesIsFalse() {
		tripleFilter.setSubjectPattern("sA");
		tripleFilter.setPassMatches(false);

		tripleFilter.process(TRIPLE1);
		tripleFilter.process(TRIPLE2);

		verify(receiver).process(TRIPLE2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldUseDisjunctionForPatterns() {
		tripleFilter.setSubjectPattern("sA");
		tripleFilter.setObjectPattern("oC");

		tripleFilter.process(TRIPLE1);
		tripleFilter.process(TRIPLE2);
		tripleFilter.process(TRIPLE3);

		verify(receiver).process(TRIPLE1);
		verify(receiver).process(TRIPLE3);
		verifyNoMoreInteractions(receiver);
	}

}
