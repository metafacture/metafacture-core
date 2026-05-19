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

import org.metafacture.framework.StreamReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Tests for class {@link StreamBatchLogger}.
 *
 * @author Jens Wille
 *
 */
@RunWith(MockitoJUnitRunner.class)
public final class StreamBatchLoggerTest extends TestHelpers {

    @Mock(name = "external.org.metafacture.monitoring.StreamBatchLogger")
    private Logger logger;

    @Mock
    private StreamReceiver receiver;

    private StreamBatchLogger streamBatchLogger;

    public StreamBatchLoggerTest() {
    }

    @Before
    public void setup() {
        streamBatchLogger = new StreamBatchLogger();
        streamBatchLogger.setReceiver(receiver);
        streamBatchLogger.setBatchSize(2);
    }

    @Test
    public void shouldForwardAllReceivedEvents() {
        streamBatchLogger.startRecord("1");
        streamBatchLogger.startEntity("entity");
        streamBatchLogger.literal("literal", "value");
        streamBatchLogger.endEntity();
        streamBatchLogger.endRecord();
        streamBatchLogger.startRecord("2");
        streamBatchLogger.endRecord();
        streamBatchLogger.startRecord("3");
        streamBatchLogger.endRecord();
        streamBatchLogger.startRecord("4");
        streamBatchLogger.endRecord();
        streamBatchLogger.startRecord("5");
        streamBatchLogger.endRecord();
        streamBatchLogger.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("2");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("3");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("4");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("5");
        ordered.verify(receiver).endRecord();
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
    public void shouldNotActAsSinkIfNoReceiverIsSet() {
        streamBatchLogger.setReceiver(null);

        Assert.assertThrows(NullPointerException.class, () -> streamBatchLogger.startRecord("1"));
        Assert.assertThrows(NullPointerException.class, streamBatchLogger::endRecord);
        streamBatchLogger.closeStream();

        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logger, "INFO", c -> {
            assertLog(c, "records processed: 0");
        });
    }

    @Test
    public void shouldLogWithCustomFormat() {
        streamBatchLogger = new StreamBatchLogger("records=${records}, totalRecords=${totalRecords}, batches=${batches}, batchSize=${batchSize}");
        streamBatchLogger.setReceiver(receiver);

        streamBatchLogger.startRecord("1");
        streamBatchLogger.startEntity("entity");
        streamBatchLogger.literal("literal", "value");
        streamBatchLogger.endEntity();
        streamBatchLogger.endRecord();
        streamBatchLogger.closeStream();

        assertLog(logger, "INFO", c -> {
            assertLog(c, "records=1, totalRecords=1, batches=0, batchSize=1000");
        });
    }

}
