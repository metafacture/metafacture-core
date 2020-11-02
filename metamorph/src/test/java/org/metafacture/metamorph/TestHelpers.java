/*
 * Copyright 2020 hbz NRW
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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Helper functions for Metamorph tests.
 *
 * @author Jens Wille
 */
public final class TestHelpers {

    public static void assertMorph(final StreamReceiver receiver, final String morphDef, final Consumer<Metamorph> in, final Consumer<Supplier<StreamReceiver>> out) {
        assertMorph(receiver, morphDef, in, (s, f) -> out.accept(s));
    }

    public static void assertMorph(final StreamReceiver receiver, final String morphDef, final Consumer<Metamorph> in, final BiConsumer<Supplier<StreamReceiver>, IntFunction<StreamReceiver>> out) {
        final Metamorph metamorph = InlineMorph.in(TestHelpers.class).with(morphDef).createConnectedTo(receiver);
        final InOrder ordered = Mockito.inOrder(receiver);

        try {
            in.accept(metamorph);
            out.accept(() -> ordered.verify(receiver), i -> ordered.verify(receiver, Mockito.times(i)));

            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
