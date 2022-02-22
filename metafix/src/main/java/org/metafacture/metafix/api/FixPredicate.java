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

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@FunctionalInterface
public interface FixPredicate {

    BiPredicate<Stream<Value>, Predicate<Value>> ALL = Stream::allMatch;
    BiPredicate<Stream<Value>, Predicate<Value>> ANY = Stream::anyMatch;

    BiPredicate<String, String> CONTAINS = String::contains;
    BiPredicate<String, String> EQUALS = String::equals;
    BiPredicate<String, String> MATCHES = String::matches;

    boolean test(Metafix metafix, Record record, List<String> params, Map<String, String> options);

    default boolean testConditional(final Record record, final List<String> params, final BiPredicate<Stream<Value>, Predicate<Value>> qualifier, final BiPredicate<String, String> conditional) {
        final String field = params.get(0);
        final String string = params.get(1);

        final Value value = record.get(field);
        return value != null && qualifier.test(value.asList(null).asArray().stream(), v -> v.extractType((m, c) -> m
                    .ifString(s -> c.accept(conditional.test(s, string)))
                    .orElse(w -> c.accept(false))
        ));
    }

}
