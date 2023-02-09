/*
 * Copyright 2022 hbz NRW
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

package org.metafacture.metafix.api;

import org.metafacture.framework.StandardEventNames;
import org.metafacture.io.ObjectWriter;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@FunctionalInterface
public interface FixFunction {

    void apply(Metafix metafix, Record record, List<String> params, Map<String, String> options);

    default void withOption(final Map<String, String> options, final String key, final Consumer<String> consumer) {
        withOption(options, key, consumer, Map::get);
    }

    default <T> void withOption(final Map<String, String> options, final String key, final Consumer<T> consumer, final BiFunction<Map<String, String>, String, T> function) {
        if (options.containsKey(key)) {
            consumer.accept(function.apply(options, key));
        }
    }

    default void withWriter(final Map<String, String> options, final UnaryOperator<String> operator, final Consumer<ObjectWriter<String>> consumer) {
        final String destination = options.getOrDefault("destination", ObjectWriter.STDOUT);
        final ObjectWriter<String> writer = new ObjectWriter<>(operator != null ? operator.apply(destination) : destination);

        withOption(options, "append", writer::setAppendIfFileExists, this::getBoolean);
        withOption(options, "compression", writer::setCompression);
        withOption(options, "encoding", writer::setEncoding);
        withOption(options, "footer", writer::setFooter);
        withOption(options, "header", writer::setHeader);
        withOption(options, "separator", writer::setSeparator);

        try {
            consumer.accept(writer);
        }
        finally {
            writer.closeStream();
        }
    }

    default void withWriter(final Metafix metafix, final Record record, final Map<String, String> options, final Map<Metafix, LongAdder> scopedCounter, final Consumer<Consumer<String>> consumer) {
        final Value idValue = record.get(options.getOrDefault("id", StandardEventNames.ID));

        final LongAdder counter = scopedCounter.computeIfAbsent(metafix, k -> new LongAdder());
        counter.increment();

        final UnaryOperator<String> formatter = s -> String.format(s,
                counter.sum(), Value.isNull(idValue) ? "" : idValue.toString());

        final String prefix = formatter.apply(options.getOrDefault("prefix", ""));
        withWriter(options, formatter, w -> consumer.accept(s -> w.process(prefix + s)));
    }

    default boolean getBoolean(final Map<String, String> options, final String key) {
        return Boolean.parseBoolean(options.get(key));
    }

    default int getInteger(final Map<String, String> options, final String key) {
        return Integer.parseInt(options.get(key));
    }

    default int getInteger(final List<String> params, final int index) {
        return Integer.parseInt(params.get(index));
    }

    default Value newArray(final Stream<Value> stream) {
        return Value.newArray(a -> stream.forEach(a::add));
    }

    default Stream<Value> unique(final Stream<Value> stream) {
        final Set<Value> set = new HashSet<>();
        return stream.filter(set::add);
    }

    default Stream<Value> flatten(final Stream<Value> stream) {
        return stream.flatMap(v -> v.extractType((m, c) -> m
                    .ifArray(a -> c.accept(flatten(a.stream())))
                    .orElse(w -> c.accept(Stream.of(w)))
        ));
    }

}
