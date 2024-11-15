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

 package org.metafacture.flowcontrol;

 import static org.mockito.ArgumentMatchers.anyString;
 import static org.mockito.Mockito.doThrow;

 import org.junit.Before;
 import org.junit.Test;
 import org.metafacture.framework.MetafactureException;
 import org.metafacture.framework.ObjectReceiver;
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
 public final class ObjectExceptionSleeperTest {

    @Mock
    private ObjectReceiver<String> sleepTimer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void cleanup() {
        bulk.closeStream();
    }


     @Test
     public void shouldTestIfClockedTimeExceedsDuration() {
        long sleepTime = 10;

        objectSleeper = new ObjectSleeper();
        objectSleeper.setSleepTime(sleepTime);
        Instant start = Instant.now();
        sleepTimer.objectSleeper();
        Instant end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);

        if (timeElampse > sleepTime) {
            exception.expect(MetafactureException.class);
            exception.expectMessage("Process did not sleep enough.");
        }

     }


 }
