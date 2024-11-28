/*
 * Copyright 2024 Tobias Bülte, hbz
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

package org.metafacture.flowcontrol;

import org.metafacture.framework.ObjectReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

/**
 * Tests for class {@link ObjectSleeper}.
 *
 * @author Tobias Bülte
 */
public final class ObjectSleeperTest {

    private static final int PROCESS_OVERHEAD_MILLISECONDS = 100;

    private static final int MILLISECONDS_PER_SECOND = 1_000;
    private static final int NANOSECONDS_PER_MILLISECOND = 1_000_000;

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldTestIfClockedTimeExceedsDuration() {
        final int sleepTime = 1234;
        assertSleep(sleepTime, s -> s.setSleepTime(sleepTime));
    }

    @Test
    public void shouldTestIfClockedTimeExceedsDurationInMilliseconds() {
        final int sleepTime = 567;
        assertSleep(sleepTime, s -> {
            s.setSleepTime(sleepTime);
            s.setTimeUnit("MILLISECONDS");
        });
    }

    @Test
    public void shouldTestIfClockedTimeExceedsDurationInSeconds() {
        final int sleepTime = 1;
        assertSleep(sleepTime * MILLISECONDS_PER_SECOND, s -> {
            s.setSleepTime(sleepTime);
            s.setTimeUnit("SECOND");
        });
    }

    private void assertSleep(final long expectedMillis, final Consumer<ObjectSleeper> consumer) {
        final ObjectSleeper<String> objectSleeper = new ObjectSleeper<>();
        objectSleeper.setReceiver(receiver);
        consumer.accept(objectSleeper);

        final long startTime = System.nanoTime();
        objectSleeper.process(null);
        final long actualMillis = (System.nanoTime() - startTime) / NANOSECONDS_PER_MILLISECOND;

        Assert.assertTrue("sleep time too short: " + actualMillis, actualMillis >= expectedMillis);
        Assert.assertTrue("sleep time too long: " + actualMillis, actualMillis < expectedMillis + PROCESS_OVERHEAD_MILLISECONDS);
    }

}
