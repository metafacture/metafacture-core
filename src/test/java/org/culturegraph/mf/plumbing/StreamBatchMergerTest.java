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
package org.culturegraph.mf.plumbing;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamBatchMerger}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamBatchMergerTest {

	@Mock
	private StreamReceiver receiver;

	private StreamBatchMerger batchMerger;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		batchMerger = new StreamBatchMerger();
		batchMerger.setReceiver(receiver);
	}

	@Test
	public void testShouldMergeNConsecutiveRecords() {
		batchMerger.setBatchSize(2);

		batchMerger.startRecord("0");
		batchMerger.literal("lit0", "A");
		batchMerger.endRecord();

		batchMerger.startRecord("1");
		batchMerger.literal("lit1", "B");
		batchMerger.endRecord();

		batchMerger.startRecord("2");
		batchMerger.literal("lit2", "C");
		batchMerger.startEntity("ent2");
		batchMerger.literal("ent2lit1", "D");
		batchMerger.endEntity();
		batchMerger.endRecord();

		batchMerger.startRecord("3");
		batchMerger.literal("lit3", "E");
		batchMerger.endRecord();

		batchMerger.startRecord("4");
		batchMerger.literal("lit4", "F");
		batchMerger.endRecord();

		batchMerger.closeStream();

		final InOrder ordered = Mockito.inOrder(receiver);
		ordered.verify(receiver).startRecord("0");
		ordered.verify(receiver).literal("lit0", "A");
		ordered.verify(receiver).literal("lit1", "B");
		ordered.verify(receiver).endRecord();

		ordered.verify(receiver).startRecord("2");
		ordered.verify(receiver).literal("lit2", "C");
		ordered.verify(receiver).startEntity("ent2");
		ordered.verify(receiver).literal("ent2lit1", "D");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("lit3", "E");
		ordered.verify(receiver).endRecord();

		ordered.verify(receiver).startRecord("4");
		ordered.verify(receiver).literal("lit4", "F");
		ordered.verify(receiver).endRecord();
	}

}
