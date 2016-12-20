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
package org.culturegraph.mf.metamorph;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link Splitter}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class SplitterTest {

	@Mock
	private StreamReceiver receiver1;

	@Mock
	private StreamReceiver receiver2;

	private Splitter splitter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		splitter = new Splitter("org/culturegraph/mf/metamorph/splitter-test.xml");
		splitter.setReceiver("receiver-1", receiver1);
		splitter.setReceiver("receiver-2", receiver2);
	}

	@Test
	public void shouldPassRecordToReceiverWithMatchingKey() {
		splitter.startRecord("1");
		splitter.startEntity("data");
		splitter.literal("forward-to", "receiver-1");
		splitter.endEntity();
		splitter.endRecord();
		splitter.startRecord("2");
		splitter.literal("forward-to", "receiver-2");
		splitter.endRecord();

		final InOrder ordered = inOrder(receiver1, receiver2);
		ordered.verify(receiver1).startRecord("1");
		ordered.verify(receiver1).startEntity("data");
		ordered.verify(receiver1).literal("forward-to", "receiver-1");
		ordered.verify(receiver1).endEntity();
		ordered.verify(receiver1).endRecord();
		ordered.verify(receiver2).startRecord("2");
		ordered.verify(receiver2).literal("forward-to", "receiver-2");
		ordered.verify(receiver2).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldDiscardNonMatchingRecords() {
		splitter.startRecord("1");
		splitter.literal("forward-to", "none");
		splitter.endRecord();

		verifyZeroInteractions(receiver1, receiver2);
	}

	@Test
	public void shouldPassResetStreamToAllReceivers() {
		splitter.resetStream();

		verify(receiver1).resetStream();
		verify(receiver2).resetStream();
	}

	@Test
	public void shouldPassCloseStreamToAllReceivers() {
		splitter.closeStream();

		verify(receiver1).closeStream();
		verify(receiver2).closeStream();
	}

}
