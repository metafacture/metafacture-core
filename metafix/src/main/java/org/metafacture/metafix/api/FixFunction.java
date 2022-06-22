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

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@FunctionalInterface
public interface FixFunction {

    void apply(Metafix metafix, Record record, List<String> params, Map<String, String> options);

    default void getOption(final Map<String, String> options, final String key, final Consumer<String> consumer) {
        if (options.containsKey(key)) {
            consumer.accept(options.get(key));
        }
    }

    default boolean getBoolean(final Map<String, String> options, final String key) {
        return Boolean.parseBoolean(options.get(key));
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

}
