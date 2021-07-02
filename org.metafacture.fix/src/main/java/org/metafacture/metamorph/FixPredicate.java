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

package org.metafacture.metamorph;

import com.google.common.collect.Multimap;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

enum FixPredicate {

    contain {
        public Predicate<String> of(final String value) {
            return v -> v.contains(value);
        }
    },
    equal {
        public Predicate<String> of(final String value) {
            return v -> v.equals(value);
        }
    },
    match {
        public Predicate<String> of(final String value) {
            return v -> v.matches(value);
        }
    };

    abstract Predicate<String> of(String value);

    enum Quantifier {

        all {
            @Override
            public boolean test(final Multimap<String, String> record, final FixPredicate p,
                    final List<String> params) {
                return test(record, params.get(0), s -> s.allMatch(p.of(params.get(1))));
            }

        },
        any {
            @Override
            public boolean test(final Multimap<String, String> record, final FixPredicate p,
                    final List<String> params) {
                return test(record, params.get(0), s -> s.anyMatch(p.of(params.get(1))));
            }
        },
        none {
            @Override
            public boolean test(final Multimap<String, String> record, final FixPredicate p,
                    final List<String> params) {
                final String fieldName = params.get(0);
                final String valueToTest = params.get(1);
                return !record.containsKey(fieldName) || record.get(fieldName).stream().noneMatch(p.of(valueToTest));
            }
        };

        boolean test(final Multimap<String, String> record, final String fieldName, final Predicate<Stream<String>> f) {
            return record.containsKey(fieldName) && f.test(record.get(fieldName).stream());
        }

        abstract boolean test(Multimap<String, String> record, FixPredicate p, List<String> params);
    }
}
