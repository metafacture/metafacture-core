/*
 * Copyright 2021 hbz NRW
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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class TestHelpers {

    public static void assertProcess(final StreamReceiver receiver, final Runnable process, final Consumer<Supplier<StreamReceiver>> out) {
        assertProcess(receiver, process, (s, f) -> out.accept(s));
    }

    public static void assertProcess(final StreamReceiver receiver, final Runnable process, final BiConsumer<Supplier<StreamReceiver>, IntFunction<StreamReceiver>> out) {
        final InOrder ordered = Mockito.inOrder(receiver);

        process.run();

        try {
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
