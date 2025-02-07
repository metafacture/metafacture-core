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

import java.math.BigDecimal;
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

    Predicate<String> IS_TRUE = s -> "true".equals(s) || "1".equals(s);
    Predicate<String> IS_FALSE = s -> "false".equals(s) || "0".equals(s);

    Predicate<String> IS_NUMBER = s -> testNumberConditional(s, x -> true);

    BiPredicate<String, String> GREATER_THAN = (s, t) -> testNumberConditional(s, x -> testNumberConditional(t, y -> x.compareTo(y) > 0));
    BiPredicate<String, String> LESS_THAN = (s, t) -> testNumberConditional(s, x -> testNumberConditional(t, y -> x.compareTo(y) < 0));

    Predicate<Value> IS_EMPTY = v -> v.extractType((m, c) -> m
            .ifArray(a -> c.accept(a.isEmpty()))
            .ifHash(h -> c.accept(h.isEmpty()))
            // TODO: Catmandu considers whitespace-only strings empty (`$v !~ /\S/`)
            .ifString(s -> c.accept(s.isEmpty()))
    );

    /**
     * Tests the given record against the predicate.
     *
     * @param metafix the Metafix instance
     * @param record  the record
     * @param params  the parameters
     * @param options the options
     *
     * @return true if the record matches the predicate, otherwise false
     */
    boolean test(Metafix metafix, Record record, List<String> params, Map<String, String> options);

    /**
     * Tests the field value against the target string according to the qualified conditional.
     *
     * @param record      the record
     * @param params      the field name and the target string
     * @param qualifier   the qualifier
     * @param conditional the conditional
     *
     * @return true if the record matches the qualified conditional
     */
    default boolean testConditional(final Record record, final List<String> params, final BiPredicate<Stream<Value>, Predicate<Value>> qualifier, final BiPredicate<String, String> conditional) {
        final String field = params.get(0);
        final String string = params.get(1);

        final Value value = record.get(field);
        return value != null && qualifier.test(value.asList(null).asArray().stream(), v -> v.extractType((m, c) -> m
                    .ifString(s -> c.accept(conditional.test(s, string)))
                    .orElse(w -> c.accept(false))
        ));
    }

    /**
     * Tests the field value against the conditional.
     *
     * @param record      the record
     * @param params      the field name
     * @param conditional the conditional
     *
     * @return true if the record matches the conditional
     */
    default boolean testConditional(final Record record, final List<String> params, final Predicate<Value> conditional) {
        final String field = params.get(0);

        final Value value = record.get(field);
        return value != null && conditional.test(value);
    }

    /**
     * Tests the parameters against the conditional.
     *
     * @param params      the parameters
     * @param conditional the conditional
     *
     * @return true if the parameters match the conditional
     */
    default boolean testConditional(final List<String> params, final BiPredicate<String, String> conditional) {
        return conditional.test(params.get(0), params.get(1));
    }

    /**
     * Tests the field value against the conditional.
     *
     * @param record      the record
     * @param params      the field name
     * @param conditional the conditional
     *
     * @return true if the record matches the conditional
     */
    default boolean testStringConditional(final Record record, final List<String> params, final Predicate<String> conditional) {
        return testConditional(record, params, v -> v.extractType((m, c) -> m
                    .ifString(s -> c.accept(conditional.test(s)))
                    .orElse(w -> c.accept(false))
        ));
    }

    static boolean testNumberConditional(final String string, final Predicate<BigDecimal> conditional) {
        try {
            final BigDecimal number = new BigDecimal(string);
            return number != null && conditional.test(number);
        }
        catch (final NumberFormatException e) {
            return false;
        }
    }

}
