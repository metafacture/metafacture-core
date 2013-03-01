/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.pipe;

import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.culturegraph.mf.stream.pipe.ObjectTimer;
import org.culturegraph.mf.stream.pipe.StreamTimer;
import org.junit.Test;


/**
 * Tests {@link ObjectTimer}.
 * 
 * @author Christoph Böhme
 */
public final class StreamTimerTest {

	private static final long[] DURATIONS = { 150L, 20L, 30L, 202L };
	
	/**
	 * A module with a slow process method.
	 */
	private static final class BenchmarkedModule extends DefaultStreamReceiver {
		
		@Override
		public void literal(final String name, final String value) {
			final long duration = Long.parseLong(value);
			try {
				Thread.sleep(duration);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	@Test
	public void testObjectTimer() {
		final StreamTimer timer = new StreamTimer();
		final BenchmarkedModule benchmarkedModule = new BenchmarkedModule();
		
		timer.setReceiver(benchmarkedModule);
		
		for (int i=0; i < DURATIONS.length; ++i) {
			timer.startRecord(Integer.toString(i));
			timer.literal("duration", Long.toString(DURATIONS[i]));
			timer.endRecord();
		}
		
		timer.closeStream();
	}

}
