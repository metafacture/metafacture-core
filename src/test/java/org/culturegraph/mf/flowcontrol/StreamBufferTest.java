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
package org.culturegraph.mf.flowcontrol;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link StreamBuffer}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class StreamBufferTest {

	@Mock
	private StreamReceiver receiver;

	private StreamBuffer streamBuffer;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		streamBuffer = new StreamBuffer();
		streamBuffer.setReceiver(receiver);
	}

	@Test
	public void shouldReplayRecordEvents() {
		streamBuffer.startRecord("1");
		streamBuffer.literal("l", "v");
		streamBuffer.startEntity("e");
		streamBuffer.endEntity();
		streamBuffer.endRecord();

		verifyZeroInteractions(receiver);

		streamBuffer.replay();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("l", "v");
		ordered.verify(receiver).startEntity("e");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldReplayBufferMultipleTimes() {
		streamBuffer.startRecord("1");
		streamBuffer.endRecord();

		streamBuffer.replay();
		streamBuffer.replay();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldClearBufferIfClearIsCalled() {
		streamBuffer.startRecord("1");
		streamBuffer.endRecord();

		streamBuffer.clear();
		streamBuffer.replay();

		verifyZeroInteractions(receiver);
	}

	@Test
	public void shouldClearBufferIfStreamIsReset() {
		streamBuffer.startRecord("1");
		streamBuffer.endRecord();

		streamBuffer.resetStream();
		streamBuffer.replay();

		verify(receiver).resetStream();
		verifyNoMoreInteractions(receiver);
	}

}
