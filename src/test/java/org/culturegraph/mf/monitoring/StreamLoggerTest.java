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
package org.culturegraph.mf.monitoring;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link StreamLogger}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class StreamLoggerTest {

	@Mock
	private StreamReceiver receiver;

	private StreamLogger logger;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		logger = new StreamLogger();
		logger.setReceiver(receiver);
	}

	@Test
	public void shouldForwardAllReceivedEvents() {
		logger.startRecord("1");
		logger.startEntity("entity");
		logger.literal("literal", "value");
		logger.endEntity();
		logger.endRecord();
		logger.resetStream();
		logger.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).resetStream();
		ordered.verify(receiver).closeStream();
	}

	@Test
	public void shouldActAsSinkIfNoReceiverIsSet() {
		logger.setReceiver(null);

		logger.startRecord("1");
		logger.startEntity("entity");
		logger.literal("literal", "value");
		logger.endEntity();
		logger.endRecord();
		logger.resetStream();
		logger.closeStream();

		// No exceptions expected
	}

}
