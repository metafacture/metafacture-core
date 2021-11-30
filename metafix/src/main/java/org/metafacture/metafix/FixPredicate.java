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
        @Override
        public Predicate<Value> of(final String string) {
            return v -> v.toString().contains(string);
        }
    },
    equal {
        @Override
        public Predicate<Value> of(final String string) {
            return v -> v.toString().equals(string);
        }
    },
    match {
        @Override
        public Predicate<Value> of(final String string) {
            return v -> v.toString().matches(string);
        }
    };

    abstract Predicate<Value> of(String string);

    enum Quantifier {

        all {
            @Override
            protected boolean test(final Record record, final String fieldName, final Predicate<Value> p) {
                return testStream(record, fieldName, s -> s.allMatch(p));
            }

        },
        any {
            @Override
            protected boolean test(final Record record, final String fieldName, final Predicate<Value> p) {
                return testStream(record, fieldName, s -> s.anyMatch(p));
            }
        },
        none {
            @Override
            protected boolean test(final Record record, final String fieldName, final Predicate<Value> p) {
                return !any.test(record, fieldName, p);
            }
        };

        boolean testStream(final Record record, final String fieldName, final Predicate<Stream<Value>> p) {
            final Value value = record.find(fieldName);
            return value != null && p.test(value.asList(null).asArray().stream());
        }

        public boolean test(final Record record, final FixPredicate p, final List<String> params) {
            return test(record, params.get(0), p.of(params.get(1)));
        }

        protected abstract boolean test(Record record, String fieldName, Predicate<Value> p);
    }

}
