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

import static org.junit.Assert.assertTrue;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.culturegraph.mf.framework.objects.Triple;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link AbstractTripleSort}.
 *
 * @author Christoph Böhme
 */
 public final class AbstractTripleSortTest {

	private static final Triple T1 = new Triple("s", "p", "o");

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

	/**
	 * This test case may throw fail unexpectedly as it relies on the
	 * garbage collector to run when calling {@code System.gc()}. This
	 * is not guaranteed by the JVM.
	 *
	 * @throws InterruptedException thrown the the waiting period for the garbage
	 * collector is interrupted.
	 */
	@Test
	public void issue192ShouldUnregisterFromTheJVMToNotCauseMemoryLeak() throws InterruptedException {

		// Get weak reference for checking whether the object was actually freed later:
		final ReferenceQueue<AbstractTripleSort> refQueue = new ReferenceQueue<AbstractTripleSort>();
		final WeakReference<AbstractTripleSort> weakRef = new WeakReference<AbstractTripleSort>(tripleSort, refQueue);

		tripleSort.closeStream();
		tripleSort = null;

		System.gc();

		// Wait a tiny bit since GC Thread is not immediately done in Java 8
		Thread.sleep(10);

		assertTrue(weakRef.isEnqueued());
	}

}
