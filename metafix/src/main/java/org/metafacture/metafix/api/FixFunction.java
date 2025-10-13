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
import org.metafacture.metafix.FixPath;
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

    String DEFAULT_OPTION = "default";
    String ERROR_STRING_OPTION = "error_string";

    /**
     * Applies the Fix function.
     *
     * @param metafix the Metafix instance
     * @param record  the record
     * @param params  the parameters
     * @param options the options
     */
    void apply(Metafix metafix, Record record, List<String> params, Map<String, String> options);

    /**
     * Performs the consumer on the specified option.
     *
     * @param options  the options
     * @param key      the option name
     * @param consumer the consumer to perform
     */
    default void withOption(final Map<String, String> options, final String key, final Consumer<String> consumer) {
        withOption(options, key, consumer, Map::get);
    }

    /**
     * Performs the consumer on the specified option.
     *
     * @param <T>      the type of the option
     * @param options  the options
     * @param key      the option name
     * @param consumer the consumer to perform
     * @param function the function to retrieve the option
     */
    default <T> void withOption(final Map<String, String> options, final String key, final Consumer<T> consumer, final BiFunction<Map<String, String>, String, T> function) {
        if (options.containsKey(key)) {
            consumer.accept(function.apply(options, key));
        }
    }

    /**
     * Performs the consumer on an object writer configured with the given
     * options.
     *
     * @param options  the options
     * @param operator the optional operator to transform the destination
     * @param consumer the consumer to perform
     */
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

    /**
     * Performs the consumer on an object writer configured with the given
     * options and format directives in the destination.
     *
     * @param metafix       the Metafix instance
     * @param record        the record
     * @param options       the options
     * @param scopedCounter the counter
     * @param consumer      the consumer to perform
     */
    default void withWriter(final Metafix metafix, final Record record, final Map<String, String> options, final Map<Metafix, LongAdder> scopedCounter, final Consumer<Consumer<String>> consumer) {
        final Value idValue = record.get(options.getOrDefault("id", StandardEventNames.ID));

        final LongAdder counter = scopedCounter.computeIfAbsent(metafix, k -> new LongAdder());
        counter.increment();

        final UnaryOperator<String> formatter = s -> String.format(s,
                counter.sum(), Value.isNull(idValue) ? "" : idValue.toString());

        final String prefix = formatter.apply(options.getOrDefault("prefix", ""));
        withWriter(options, formatter, w -> consumer.accept(s -> w.process(prefix + s)));
    }

    /**
     * Parses the specified option as a Boolean.
     *
     * @param options  the options
     * @param key      the option name
     *
     * @return the Boolean
     */
    default boolean getBoolean(final Map<String, String> options, final String key) {
        return Boolean.parseBoolean(options.get(key));
    }

    /**
     * Parses the specified option as an Integer.
     *
     * @param options  the options
     * @param key      the option name
     *
     * @return the Integer
     */
    default int getInteger(final Map<String, String> options, final String key) {
        return Integer.parseInt(options.get(key));
    }

    /**
     * Parses the specified parameter as an Integer.
     *
     * @param params the parameters
     * @param index  the parameter index
     *
     * @return the Integer
     */
    default int getInteger(final List<String> params, final int index) {
        return Integer.parseInt(params.get(index));
    }

    /**
     * Creates a new Array from the given stream.
     *
     * @param stream the Value stream
     *
     * @return the new Value
     */
    default Value newArray(final Stream<Value> stream) {
        return Value.newArray(a -> stream.forEach(a::add));
    }

    /**
     * Removes duplicates from the given stream.
     *
     * @param stream the Value stream
     *
     * @return the unique Value stream
     */
    default Stream<Value> unique(final Stream<Value> stream) {
        final Set<Value> set = new HashSet<>();
        return stream.filter(set::add);
    }

    /**
     * Flattens the given stream.
     *
     * @param stream the Value stream
     *
     * @return the flattened Value stream
     */
    default Stream<Value> flatten(final Stream<Value> stream) {
        return stream.flatMap(v -> v.extractType((m, c) -> m
                    .ifArray(a -> c.accept(flatten(a.stream())))
                    .orElse(w -> c.accept(Stream.of(w)))
        ));
    }

    /**
     * Checks whether the given field's parent field exists in the record.
     *
     * @param record the record
     * @param field the field
     *
     * @return true if the given field's parent field exists in the record
     */
    default boolean parentFieldExists(final Record record, final String field) {
        final FixPath parentPath = new FixPath(field).getParentPath();
        return parentPath == null || !parentPath.isAddingToArray() && record.containsPath(parentPath.toString());
    }

}
