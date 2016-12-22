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

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link StreamTee}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class StreamTeeTest {

	@Mock
	private StreamReceiver receiver1;

	@Mock
	private StreamReceiver receiver2;

	private StreamTee streamTee;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		streamTee = new StreamTee();
		streamTee
				.addReceiver(receiver1)
				.addReceiver(receiver2);
	}

	@Test
	public void shouldForwardAllEventsToAllDownStreamReceivers() {
		streamTee.startRecord("1");
		streamTee.literal("literal", "value");
		streamTee.startEntity("entity");
		streamTee.endEntity();
		streamTee.endRecord();
		streamTee.resetStream();
		streamTee.closeStream();

		final InOrder ordered = inOrder(receiver1, receiver2);
		ordered.verify(receiver1).startRecord("1");
		ordered.verify(receiver2).startRecord("1");
		ordered.verify(receiver1).literal("literal", "value");
		ordered.verify(receiver2).literal("literal", "value");
		ordered.verify(receiver1).startEntity("entity");
		ordered.verify(receiver2).startEntity("entity");
		ordered.verify(receiver1).endEntity();
		ordered.verify(receiver2).endEntity();
		ordered.verify(receiver1).endRecord();
		ordered.verify(receiver2).endRecord();
		ordered.verify(receiver1).resetStream();
		ordered.verify(receiver2).resetStream();
		ordered.verify(receiver1).closeStream();
		ordered.verify(receiver2).closeStream();
	}

}
