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

import org.metafacture.metafix.api.FixPredicate;

import java.util.List;
import java.util.Map;

public enum FixConditional implements FixPredicate {

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
            return record.containsPath(params.get(0));
        }
    },

    in {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final Value value1 = record.get(params.get(0));
            final Value value2 = record.get(params.get(1));

            return value1 != null && value2 != null && value1.<Boolean>extractType((m, c) -> m
                .ifArray(a1 -> value2.matchType()
                    .ifArray(a2 -> c.accept(a1.equals(a2)))
                )
                .ifHash(h1 -> value2.matchType()
                    .ifHash(h2 -> c.accept(h1.equals(h2)))
                )
                .ifString(s1 -> value2.matchType()
                    .ifArray(a2 -> c.accept(a2.stream().anyMatch(value1::equals)))
                    .ifHash(h2 -> c.accept(h2.containsField(s1)))
                    .ifString(s2 -> c.accept(s1.equals(s2)))
                )
            );
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
    }

}
