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
package org.culturegraph.mf.plumbing;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link StreamMerger}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamMergerTest {

	@Mock
	private StreamReceiver receiver;

	private StreamMerger streamMerger;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		streamMerger = new StreamMerger();
		streamMerger.setReceiver(receiver);
	}

	@Test
	public  void shouldMergeSequencesOfRecordsWithTheSameId() {
		streamMerger.startRecord("1");
		streamMerger.startEntity("entity-1");
		streamMerger.endEntity();
		streamMerger.literal("literal-1", "value-1");
		streamMerger.endRecord();
		streamMerger.startRecord("1");
		streamMerger.startEntity("entity-2");
		streamMerger.endEntity();
		streamMerger.literal("literal-2", "value-2");
		streamMerger.endRecord();
		streamMerger.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).startEntity("entity-1");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("literal-1", "value-1");
		ordered.verify(receiver).startEntity("entity-2");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("literal-2", "value-2");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public  void shouldNoMergeRecordsWithDifferentIds() {
		streamMerger.startRecord("1");
		streamMerger.literal("literal-1", "value-1");
		streamMerger.endRecord();
		streamMerger.startRecord("2");
		streamMerger.literal("literal-2", "value-2");
		streamMerger.endRecord();
		streamMerger.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("literal-1", "value-1");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("2");
		ordered.verify(receiver).literal("literal-2", "value-2");
		ordered.verify(receiver).endRecord();
	}

}
