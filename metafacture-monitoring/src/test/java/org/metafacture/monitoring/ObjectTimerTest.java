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

package org.metafacture.monitoring;

import org.metafacture.framework.helpers.DefaultObjectReceiver;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link ObjectTimer}.
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectTimerTest {

    private ObjectTimer<String> objectTimer;
    private BenchmarkedModule benchmarkedModule;

    public ObjectTimerTest() {
    }

    @Before
    public void setup() {
        objectTimer = new ObjectTimer<String>();
        benchmarkedModule = new BenchmarkedModule();
        objectTimer.setReceiver(benchmarkedModule);
    }

    @Test
    public void testShouldMeasureExecutionTime() {

        objectTimer.process("");
        objectTimer.process("");
        objectTimer.process("");
        objectTimer.process("");
        objectTimer.closeStream();
    }

    @Test
    public void testShouldHandleImmediateCloseStreamWithNoProcessing() {

        objectTimer.closeStream();
    }

    /**
     * A module with a slow process method.
     */
    private static final class BenchmarkedModule extends DefaultObjectReceiver<String> {

        private static final long[] DURATIONS = {150L, 20L, 30L, 202L};

        private int i;

        private BenchmarkedModule() {
        }

        @Override
        public void process(final String obj) {
            try {
                Thread.sleep(getDuration());
            }
            catch (final InterruptedException e) {
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

}
