/*
 * Copyright 2016 Deutsche Nationalbibliothek
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

import org.metafacture.framework.StreamReceiver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StreamDeferrer}.
 *
 * @author Christoph Böhme
 *
 */
public class StreamDeferrerTest {

    @Mock
    private StreamReceiver receiver;

    private StreamDeferrer streamDeferrer;

    public StreamDeferrerTest() {
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        streamDeferrer = new StreamDeferrer();
        streamDeferrer.setReceiver(receiver);
    }

    @Test
    public void shouldDeferStreamEventsUntilEndRecordIsReceived() {
        streamDeferrer.startRecord("1");
        streamDeferrer.literal("l", "v");
        streamDeferrer.startEntity("e");
        streamDeferrer.endEntity();

        Mockito.verifyZeroInteractions(receiver);

        streamDeferrer.endRecord();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("l", "v");
        ordered.verify(receiver).startEntity("e");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void shouldDiscardDeferredEventsIfAnotherStartRecordIsReceived() {
        streamDeferrer.startRecord("1");
        streamDeferrer.literal("l1", "v1");
        streamDeferrer.startRecord("2");
        streamDeferrer.literal("l2", "v2");
        streamDeferrer.endRecord();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver, Mockito.never()).startRecord("1");
        ordered.verify(receiver, Mockito.never()).literal("l1", "v1");
        ordered.verify(receiver).startRecord("2");
        ordered.verify(receiver).literal("l2", "v2");
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void shouldDiscardDeferredEventsOnResetStream() {
        streamDeferrer.startRecord("1");
        streamDeferrer.literal("l", "v");
        streamDeferrer.resetStream();
        streamDeferrer.endRecord();

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver, Mockito.never()).startRecord("1");
        ordered.verify(receiver, Mockito.never()).literal("l", "v");
        ordered.verify(receiver).endRecord();
    }

}
