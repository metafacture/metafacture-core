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
 * Tests for class {@link ObjectLogger}.
 *
 * @author Jens Wille
 *
 */
@RunWith(MockitoJUnitRunner.class)
public final class ObjectLoggerTest extends TestHelpers {

    @Mock(name = "external.org.metafacture.monitoring.ObjectLogger")
    private Logger logLogger;

    @Mock
    private ObjectReceiver<String> receiver;

    private ObjectLogger<String> logger;

    public ObjectLoggerTest() {
    }

    @Before
    public void setup() {
        logger = new ObjectLogger<>();
        logger.setReceiver(receiver);
    }

    @Test
    public void shouldForwardAllProcessedObjects() {
        logger.process("object");
        logger.resetStream();
        logger.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process("object");
        ordered.verify(receiver).resetStream();
        ordered.verify(receiver).closeStream();
        ordered.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logLogger, "INFO", c -> {
            assertLog(c, "{}{}", "", "object");
            assertLog(c, "{}resetStream", "");
            assertLog(c, "{}closeStream", "");
        });
    }

    @Test
    public void shouldActAsSinkIfNoReceiverIsSet() {
        logger.setReceiver(null);

        logger.process("object");
        logger.resetStream();
        logger.closeStream();

        Mockito.verifyNoMoreInteractions(receiver);

        assertLog(logLogger, "INFO", c -> {
            assertLog(c, "{}{}", "", "object");
            assertLog(c, "{}resetStream", "");
            assertLog(c, "{}closeStream", "");
        });
    }

    @Test
    public void shouldLogWithPrefix() {
        final String prefix = "prefix:";
        logger.setPrefix(prefix);

        logger.process("object");
        logger.resetStream();
        logger.closeStream();

        assertLog(logLogger, "INFO", c -> {
            assertLog(c, "{}{}", prefix, "object");
            assertLog(c, "{}resetStream", prefix);
            assertLog(c, "{}closeStream", prefix);
        });
    }

    @Test
    public void shouldLogAtCustomLevel() {
        logger.setLevel("WARN");

        logger.process("object");
        logger.resetStream();
        logger.closeStream();

        assertLog(logLogger, "WARN", c -> {
            assertLog(c, "{}{}", "", "object");
            assertLog(c, "{}resetStream", "");
            assertLog(c, "{}closeStream", "");
        });
    }

}
