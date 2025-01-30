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

package org.metafacture.mangling;

import org.metafacture.framework.StreamReceiver;
import org.metafacture.mangling.StreamEventDiscarder.EventType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.EnumSet;

/**
 * Tests for class {@link StreamEventDiscarder}.
 *
 * @author Christoph Böhme
 *
 */
public class StreamEventDiscarderTest {

    @Mock
    private StreamReceiver receiver;

    private StreamEventDiscarder discarder;

    public StreamEventDiscarderTest() {
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        discarder = new StreamEventDiscarder();
        discarder.setReceiver(receiver);
    }

    @Test
    public void shouldPassOnAllEventsByDefault() {
        discarder.startRecord("1");
        discarder.startEntity("entity");
        discarder.endEntity();
        discarder.literal("literal", "value");
        discarder.endRecord();
        discarder.resetStream();
        discarder.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).resetStream();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void setDiscardedEventsShouldDiscardAllEventsIfAllFlagsAreSet() {
        discarder.setDiscardedEvents(EnumSet.allOf(EventType.class));

        discarder.startRecord("1");
        discarder.startEntity("entity");
        discarder.endEntity();
        discarder.literal("literal", "value");
        discarder.endRecord();
        discarder.resetStream();
        discarder.closeStream();

        Mockito.verifyZeroInteractions(receiver);
    }

    @Test
    public void setDiscardedEventsShouldWorkOnCopyOfThePassedEnumSet() {
        final EnumSet<EventType> eventTypes = EnumSet.noneOf(EventType.class);
        discarder.setDiscardedEvents(eventTypes);
        eventTypes.add(EventType.RECORD);

        discarder.startRecord("1");

        Mockito.verify(receiver).startRecord("1");
    }

    @Test
    public void setDiscardRecordEventsShouldDiscardStartRecordAndEndRecordIfTrue() {
        discarder.setDiscardRecordEvents(true);

        discarder.startRecord("1");
        discarder.startEntity("entity");
        discarder.endEntity();
        discarder.literal("literal", "value");
        discarder.endRecord();
        discarder.resetStream();
        discarder.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).resetStream();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void setDiscardEntityEventsShouldDiscardStartEntityAndEndEntityIfTrue() {
        discarder.setDiscardEntityEvents(true);

        discarder.startRecord("1");
        discarder.startEntity("entity");
        discarder.endEntity();
        discarder.literal("literal", "value");
        discarder.endRecord();
        discarder.resetStream();
        discarder.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).resetStream();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void setDiscardLiteralEventsShouldDiscardLiteralIfTrue() {
        discarder.setDiscardLiteralEvents(true);

        discarder.startRecord("1");
        discarder.startEntity("entity");
        discarder.endEntity();
        discarder.literal("literal", "value");
        discarder.endRecord();
        discarder.resetStream();
        discarder.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).resetStream();
        ordered.verify(receiver).closeStream();
    }

    @Test
    public void setDiscardLifecycleEventsShouldDiscardResetStreamAndCloseStreamIfTrue() {
        discarder.setDiscardLifecycleEvents(true);

        discarder.startRecord("1");
        discarder.startEntity("entity");
        discarder.endEntity();
        discarder.literal("literal", "value");
        discarder.endRecord();
        discarder.resetStream();
        discarder.closeStream();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("entity");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).literal("literal", "value");
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void getDiscardedEventsShouldReturnCopyOfTheInternalEnumSet() {
        final EnumSet<EventType> discardedEvents = discarder.getDiscardedEvents();
        discardedEvents.add(EventType.RECORD);

        discarder.startRecord("1");

        Mockito.verify(receiver).startRecord("1");
    }

}
