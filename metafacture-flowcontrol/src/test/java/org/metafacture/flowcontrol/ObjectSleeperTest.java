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

import static org.junit.Assert.assertTrue;

import org.metafacture.framework.ObjectReceiver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.Instant;

/**
 * Tests for class {@link ObjectSleeper}.
*
* @author Tobias Bülte
*
*/
public final class ObjectSleeperTest {

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldTestIfClockedTimeExceedsDuration() {
    long sleepTime = 100;

    ObjectSleeper<String> objectSleeper = new ObjectSleeper<>();
    objectSleeper.setReceiver(receiver);
    objectSleeper.setSleepTime(sleepTime);
    Instant start = Instant.now();
    objectSleeper.process(null);
    Instant end = Instant.now();

    Duration timeElapsed = Duration.between(start, end);

    assertTrue(timeElapsed.toMillis() >= sleepTime);

    }


}
