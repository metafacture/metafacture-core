/*
 * Copyright 2026 hbz NRW
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

import org.metafacture.framework.ObjectReceiver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Tests for class {@link ObjectBatchLogger}.
 *
 * @author Jens Wille
 *
 */
@RunWith(MockitoJUnitRunner.class)
public final class ObjectBatchLoggerTest extends TestHelpers {

    @Mock(name = "external.org.metafacture.monitoring.ObjectBatchLogger")
    private Logger logger;

    @Mock
    private ObjectReceiver<String> receiver;

    private ObjectBatchLogger<String> objectBatchLogger;

    public ObjectBatchLoggerTest() {
    }

    @Before
    public void setup() {
        objectBatchLogger = new ObjectBatchLogger<>();
        objectBatchLogger.setReceiver(receiver);
        objectBatchLogger.setBatchSize(2);
    }

    @Test
    public void shouldForwardAllProcessedObjects() {
        objectBatchLogger.process("object1");
        objectBatchLogger.process("object2");
        objectBatchLogger.process("object3");
        objectBatchLogger.process("object4");
        objectBatchLogger.process("object5");
        objectBatchLogger.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process("object1");
        ordered.verify(receiver).process("object2");
        ordered.verify(receiver).process("object3");
        ordered.verify(receiver).process("object4");
        ordered.verify(receiver).process("object5");
        ordered.verify(receiver).closeStream();
        ordered.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logger, "INFO", c -> {
            assertLog(c, "records processed: 2");
            assertLog(c, "records processed: 4");
            assertLog(c, "records processed: 5");
        });
    }

    @Test
    public void shouldActAsSinkIfNoReceiverIsSet() {
        objectBatchLogger.setReceiver(null);

        objectBatchLogger.process("object");
        objectBatchLogger.closeStream();

        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logger, "INFO", c -> {
            assertLog(c, "records processed: 1");
        });
    }

    @Test
    public void shouldLogWithCustomFormat() {
        objectBatchLogger = new ObjectBatchLogger<>("records=${records}, totalRecords=${totalRecords}, batches=${batches}, batchSize=${batchSize}");

        objectBatchLogger.process("object");
        objectBatchLogger.closeStream();

        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logger, "INFO", c -> {
            assertLog(c, "records=1, totalRecords=1, batches=0, batchSize=1000");
        });
    }

}
