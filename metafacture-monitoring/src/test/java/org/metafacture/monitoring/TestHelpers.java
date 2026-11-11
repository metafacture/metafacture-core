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

package org.metafacture.monitoring;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class TestHelpers { // checkstyle-disable-line HideUtilityClassConstructor

    private static final Object[] NO_ARGS = new Object[]{};

    /*package-private*/ TestHelpers() {
    }

    public static void assertLog(final Logger logger, final String level, final Consumer<BiConsumer<String, Object[]>> consumer) {
        final InOrder ordered = Mockito.inOrder(logger);

        switch (level) {
            case "DEBUG":
                consumer.accept((f, a) -> ordered.verify(logger).debug(f, a));
                break;
            case "ERROR":
                consumer.accept((f, a) -> ordered.verify(logger).error(f, a));
                break;
            case "INFO":
                consumer.accept((f, a) -> ordered.verify(logger).info(f, a));
                break;
            case "WARN":
                consumer.accept((f, a) -> ordered.verify(logger).warn(f, a));
                break;
            default:
                throw new IllegalArgumentException("Unsupported log level: " + level);
        }

        ordered.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(logger);
    }

    public static void assertLog(final BiConsumer<String, Object[]> consumer, final String format, final Object... arguments) {
        consumer.accept(format, arguments);
    }

    public static void assertLog(final Logger logger, final String prefix, final int executions) {
        final InOrder ordered = Mockito.inOrder(logger);

        IntStream.range(0, executions).forEach(i -> ordered.verify(logger).info(Mockito.matches(prefix + "Execution " + (i + 1) + ": \\d+.+s"), NO_ARGS));
        ordered.verify(logger).info(Mockito.matches(prefix + "Executions: " + executions + "; Cumulative duration: \\d+.*s; Average duration: \\d+.*s"), NO_ARGS);
        ordered.verify(logger).info(Mockito.matches(prefix + "Time to close stream:  \\d+.+s"), NO_ARGS);

        ordered.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(logger);
    }

}
