/*
 * Copyright 2020, 2021 hbz NRW
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

package org.metafacture.metafix;

import org.metafacture.framework.StreamReceiver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Helper functions for Metafix tests.
 *
 * @author Jens Wille (metamorph.TestHelpers)
 * @author Fabian Steeg (MetafixTestHelpers)
 */
public final class MetafixTestHelpers {

    private MetafixTestHelpers() {
    }

    public static void assertExecutionException(final Class<?> expectedClass, final String expectedMessage, final Executable executable) {
        assertThrows(FixExecutionException.class, expectedClass, expectedMessage, executable, UnaryOperator.identity());
    }

    public static void assertProcessException(final Class<?> expectedClass, final String expectedMessage, final Executable executable) {
        assertThrows(FixProcessException.class, expectedClass, expectedMessage, executable, UnaryOperator.identity());
    }

    public static void assertThrows(final Class<? extends Throwable> expectedClass, final String expectedMessage, final Executable executable) {
        assertThrows(expectedClass, null, expectedMessage, executable, UnaryOperator.identity());
    }

    public static void assertThrows(final Class<? extends Throwable> expectedClass, final UnaryOperator<String> operator, final String expectedMessage, final Executable executable) {
        assertThrows(expectedClass, null, expectedMessage, executable, operator);
    }

    private static void assertThrows(final Class<? extends Throwable> exceptionClass, final Class<?> expectedClass, final String expectedMessage, final Executable executable, final UnaryOperator<String> operator) {
        final Throwable thrownException = Assertions.assertThrows(exceptionClass, executable);
        final Throwable actualException;

        if (expectedClass != null) {
            actualException = thrownException.getCause();
            Assertions.assertSame(expectedClass, actualException.getClass());
        }
        else {
            actualException = thrownException;
        }

        Assertions.assertEquals(expectedMessage, operator.apply(actualException.getMessage()));
    }

    public static void assertFix(final StreamReceiver receiver, final List<String> fixDef, final Consumer<Metafix> in,
            final Consumer<Supplier<StreamReceiver>> out) {
        assertFix(receiver, fixDef, in, (s, f) -> out.accept(s), Metafix.NO_VARS);
    }

    public static void assertFix(final StreamReceiver receiver, final List<String> fixDef, final Consumer<Metafix> in,
            final BiConsumer<Supplier<StreamReceiver>, IntFunction<StreamReceiver>> out) {
        assertFix(receiver, fixDef, in, out, Metafix.NO_VARS);
    }

    public static void assertFix(final StreamReceiver receiver, final List<String> fixDef, final Map<String, String> vars,
            final Consumer<Metafix> in, final Consumer<Supplier<StreamReceiver>> out) {
        assertFix(receiver, fixDef, in, (s, f) -> out.accept(s), vars);
    }

    public static void assertFix(final StreamReceiver receiver, final List<String> fixDef, final Map<String, String> vars,
            final Consumer<Metafix> in, final BiConsumer<Supplier<StreamReceiver>, IntFunction<StreamReceiver>> out) {
        assertFix(receiver, fixDef, in, out, vars);
    }

    private static void assertFix(final StreamReceiver receiver, final List<String> fixLines, final Consumer<Metafix> in,
            final BiConsumer<Supplier<StreamReceiver>, IntFunction<StreamReceiver>> out, final Map<String, String> vars) {
        final String fixString = String.join("\n", fixLines);
        final Metafix metafix = fix(receiver, fixString, vars);
        final InOrder ordered = Mockito.inOrder(receiver);
        try {
            in.accept(metafix);
            out.accept(() -> ordered.verify(receiver), i -> ordered.verify(receiver, Mockito.times(i)));
            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println("\nFix string: " + fixString);
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

    private static Metafix fix(final StreamReceiver receiver, final String fix, final Map<String, String> vars) {
        Metafix metafix = null;
        try {
            metafix = new Metafix(fix, vars);
            metafix.setReceiver(receiver);
        }
        catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        return metafix;
    }

    public static void assertEmittedFields(final Value.Hash hash, final List<String> expectedFields, final List<Value> expectedValues) {
        final List<String> actualFields = new ArrayList<>();
        final List<Value> actualValues = new ArrayList<>();

        hash.forEach((f, v) -> {
            actualFields.add(f);
            actualValues.add(v);
        });

        Assertions.assertEquals(expectedFields, actualFields);
        Assertions.assertEquals(expectedValues, actualValues);
    }

}
