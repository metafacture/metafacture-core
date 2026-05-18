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

package org.metafacture.monitoring;

import org.metafacture.framework.StreamReceiver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Tests for class {@link StreamLogger}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public final class StreamLoggerTest extends TestHelpers {

    @Mock(name = "external.org.metafacture.monitoring.StreamLogger")
    private Logger logLogger;

    @Mock
    private StreamReceiver receiver;

    private StreamLogger logger;

    public StreamLoggerTest() {
    }

    @Before
    public void setup() {
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

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).resetStream();
        ordered.verify(receiver).closeStream();
        ordered.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logLogger, "DEBUG", c -> {
            assertLog(c, "{}start record {}", "", "1");
            assertLog(c, "{}start entity {}", "", "entity");
            assertLog(c, "{}literal {}={}", "", "literal", "value");
            assertLog(c, "{}end entity", "");
            assertLog(c, "{}end record", "");
            assertLog(c, "{}resetStream", "");
            assertLog(c, "{}closeStream", "");
        });
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

        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logLogger, "DEBUG", c -> {
            assertLog(c, "{}start record {}", "", "1");
            assertLog(c, "{}start entity {}", "", "entity");
            assertLog(c, "{}literal {}={}", "", "literal", "value");
            assertLog(c, "{}end entity", "");
            assertLog(c, "{}end record", "");
            assertLog(c, "{}resetStream", "");
            assertLog(c, "{}closeStream", "");
        });
    }

    @Test
    public void shouldLogWithPrefix() {
        final String prefix = "prefix:";
        logger.setPrefix(prefix);

        logger.startRecord("1");
        logger.startEntity("entity");
        logger.literal("literal", "value");
        logger.endEntity();
        logger.endRecord();
        logger.resetStream();
        logger.closeStream();

        assertLog(logLogger, "DEBUG", c -> {
            assertLog(c, "{}start record {}", prefix, "1");
            assertLog(c, "{}start entity {}", prefix, "entity");
            assertLog(c, "{}literal {}={}", prefix, "literal", "value");
            assertLog(c, "{}end entity", prefix);
            assertLog(c, "{}end record", prefix);
            assertLog(c, "{}resetStream", prefix);
            assertLog(c, "{}closeStream", prefix);
        });
    }

}
