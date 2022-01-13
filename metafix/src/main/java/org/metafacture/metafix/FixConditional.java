/*
 * Copyright 2021 Fabian Steeg, hbz
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

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

enum FixConditional {

    all_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return testConditional(record, params, ALL, CONTAINS);
        }
    },
    any_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return testConditional(record, params, ANY, CONTAINS);
        }
    },
    none_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return !any_contain.test(metafix, record, params, options);
        }
    },

    all_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return testConditional(record, params, ALL, EQUALS);
        }
    },
    any_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return testConditional(record, params, ANY, EQUALS);
        }
    },
    none_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return !any_equal.test(metafix, record, params, options);
        }
    },

    exists {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return record.containsField(params.get(0));
        }
    },

    all_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return testConditional(record, params, ALL, MATCHES);
        }
    },
    any_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return testConditional(record, params, ANY, MATCHES);
        }
    },
    none_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return !any_match.test(metafix, record, params, options);
        }
    };

    public static final BiPredicate<Stream<Value>, Predicate<Value>> ALL = (s, p) -> s.allMatch(p);
    public static final BiPredicate<Stream<Value>, Predicate<Value>> ANY = (s, p) -> s.anyMatch(p);

    public static final BiPredicate<Value, String> CONTAINS = (v, s) -> v.toString().contains(s);
    public static final BiPredicate<Value, String> EQUALS = (v, s) -> v.toString().equals(s);
    public static final BiPredicate<Value, String> MATCHES = (v, s) -> v.toString().matches(s);

    private static boolean testConditional(final Record record, final List<String> params, final BiPredicate<Stream<Value>, Predicate<Value>> qualifier, final BiPredicate<Value, String> conditional) {
        final String field = params.get(0);
        final String string = params.get(1);

        final Value value = record.find(field);
        return value != null && qualifier.test(value.asList(null).asArray().stream(), v -> conditional.test(v, string));
    }

    abstract boolean test(Metafix metafix, Record record, List<String> params, Map<String, String> options);

}
