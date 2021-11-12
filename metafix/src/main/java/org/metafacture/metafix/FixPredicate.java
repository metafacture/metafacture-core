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
import java.util.function.Predicate;
import java.util.stream.Stream;

enum FixPredicate {

    contain {
        public Predicate<Object> of(final Object value) {
            return v -> v.toString().contains(value.toString());
        }
    },
    equal {
        public Predicate<Object> of(final Object value) {
            return v ->  v.toString().equals(value.toString());
        }
    },
    match {
        public Predicate<Object> of(final Object value) {
            return v -> v.toString().matches(value.toString());
        }
    };

    abstract Predicate<Object> of(Object value);

    enum Quantifier {

        all {
            @Override
            public boolean test(final Record record, final FixPredicate p, final List<String> params) {
                return test(record, params.get(0), s -> s.allMatch(p.of(params.get(1))));
            }

        },
        any {
            @Override
            public boolean test(final Record record, final FixPredicate p, final List<String> params) {
                return test(record, params.get(0), s -> s.anyMatch(p.of(params.get(1))));
            }
        },
        none {
            @Override
            public boolean test(final Record record, final FixPredicate p, final List<String> params) {
                final Object fieldValue = FixMethod.find(record, FixMethod.split(params.get(0)));
                final String valueToTest = params.get(1);
                return fieldValue == null || Metafix.asList(fieldValue).stream().noneMatch(p.of(valueToTest));
            }
        };

        boolean test(final Record record, final String fieldName, final Predicate<Stream<Object>> f) {
            final Object value = FixMethod.find(record, FixMethod.split(fieldName));
            return value != null && f.test(Metafix.asList(value).stream());
        }

        abstract boolean test(Record record, FixPredicate p, List<String> params);
    }
}
