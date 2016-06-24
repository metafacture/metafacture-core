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
package org.culturegraph.mf.stream.pipe;

import static org.junit.Assert.fail;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Test;


/**
 * Tests for class {@link StreamLogger}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public final class StreamLoggerTest {

	@Test
	public void testCorrectPipeFunction() {
		final EventList list = new EventList();
		execTestEvents(list);

		final StreamValidator validator = new StreamValidator(list.getEvents());
		final StreamLogger logger = new StreamLogger("test-logger");
		logger.setReceiver(validator);

		try {
			execTestEvents(logger);
			logger.closeStream();
		} catch (FormatException e) {
			fail("Logger did not forward data as expected: " + e);
		}

	}

	private void execTestEvents(final StreamReceiver receiver) {
		receiver.startRecord("1");
		receiver.literal("l1", "value1");
		receiver.literal("l1", "value2");
		receiver.startEntity("e1");
		receiver.literal("l2", "value3");
		receiver.endEntity();
		receiver.endRecord();
		receiver.startRecord("2");
		receiver.literal("l3", "value4");
		receiver.endRecord();
	}

}
