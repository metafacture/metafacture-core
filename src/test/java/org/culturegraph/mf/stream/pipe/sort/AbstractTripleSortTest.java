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
package org.culturegraph.mf.stream.pipe.sort;

import org.culturegraph.mf.types.Triple;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link AbstractTripleSort}.
 *
 * @author Christoph Böhme
 *
 */
 public final class AbstractTripleSortTest {

	private static final Triple T1 = new Triple("s", "p", "o");

	// NO CHECKSTYLE IllegalType FOR 3 LINES:
	// AbstractFormatter is the system under test. To keep the test
	// case concise no named mock implementation is created.
	private AbstractTripleSort tripleSort;

	@Before
	public void setup() {
		tripleSort = new AbstractTripleSort() {
			@Override
			protected void sortedTriple(final Triple namedValue) {}
		};
	}

	@Test
	public void shouldNotFailIfFlushingBeforeFirstRecord() {
		tripleSort.memoryLow(0, 0);
		tripleSort.process(T1);
		tripleSort.closeStream();
	}

}
