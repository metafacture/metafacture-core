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
package org.culturegraph.mf.monitoring;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for class {@link ObjectTimer}.
 *
 * @author Christoph Böhme
 *
 */
public final class StreamTimerTest {

	/**
	 * A module with a slow process method.
	 */
	private static final class BenchmarkedModule extends DefaultStreamReceiver {

		private static final long[] DURATIONS = { 150L, 20L, 30L, 202L };

		private int i;

		@Override
		public void startRecord(final String id) {
			try {
				Thread.sleep(getDuration());
			} catch (final InterruptedException e) {
				return;
			}
		}

		private long getDuration() {
			final long duration = DURATIONS[i];
			i += 1;
			if (i == DURATIONS.length) {
				i = 0;
			}
			return duration;
		}

	}

	private StreamTimer streamTimer;
	private BenchmarkedModule benchmarkedModule;

	@Before
	public void setup() {
		streamTimer = new StreamTimer();
		benchmarkedModule = new BenchmarkedModule();
		streamTimer.setReceiver(benchmarkedModule);
	}

	@Test
	public void testShouldMeasureExecutionTime() {

		streamTimer.startRecord("");
		streamTimer.endRecord();
		streamTimer.startRecord("");
		streamTimer.endRecord();
		streamTimer.startRecord("");
		streamTimer.endRecord();
		streamTimer.startRecord("");
		streamTimer.endRecord();

		streamTimer.closeStream();
	}

	@Test
	public void testShouldHandleImmediateCloseStreamWithNoProcessing() {

		streamTimer.closeStream();
	}

}
