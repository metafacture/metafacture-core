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

package org.metafacture.metamorph;

import org.metafacture.framework.StreamReceiver;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tests for class {@link Splitter}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class SplitterTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver1;

    @Mock
    private StreamReceiver receiver2;

    public SplitterTest() {
    }

    @Test
    public void shouldPassRecordToReceiverWithMatchingKey() {
        assertSplitter(
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("forward-to", "receiver-1");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("forward-to", "receiver-2");
                    i.endRecord();
                },
                (o1, o2) -> {
                    o1.get().startRecord("1");
                    o1.get().startEntity("data");
                    o1.get().literal("forward-to", "receiver-1");
                    o1.get().endEntity();
                    o1.get().endRecord();
                    o2.get().startRecord("2");
                    o2.get().literal("forward-to", "receiver-2");
                    o2.get().endRecord();
                }
        );
    }

    @Test
    public void shouldDiscardNonMatchingRecords() {
        assertSplitter(
                i -> {
                    i.startRecord("1");
                    i.literal("forward-to", "none");
                    i.endRecord();
                },
                (o1, o2) -> {
                }
        );
    }

    @Test
    public void shouldPassResetStreamToAllReceivers() {
        assertSplitter(
                i -> {
                    i.resetStream();
                },
                (o1, o2) -> {
                    o1.get().resetStream();
                    o2.get().resetStream();
                }
        );
    }

    @Test
    public void shouldPassCloseStreamToAllReceivers() {
        assertSplitter(
                i -> {
                    i.closeStream();
                },
                (o1, o2) -> {
                    o1.get().closeStream();
                    o2.get().closeStream();
                }
        );
    }

    private void assertSplitter(final Consumer<Splitter> in, final BiConsumer<Supplier<StreamReceiver>, Supplier<StreamReceiver>> out) {
        final InOrder ordered = Mockito.inOrder(receiver1, receiver2);

        final Splitter splitter = new Splitter("org/metafacture/metamorph/splitter-test.xml");
        splitter.setReceiver("receiver-1", receiver1);
        splitter.setReceiver("receiver-2", receiver2);

        in.accept(splitter);

        try {
            out.accept(() -> ordered.verify(receiver1), () -> ordered.verify(receiver2));

            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver1);
            Mockito.verifyNoMoreInteractions(receiver2);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver1).printInvocations());
            System.out.println(Mockito.mockingDetails(receiver2).printInvocations());
            throw e;
        }
    }

}
