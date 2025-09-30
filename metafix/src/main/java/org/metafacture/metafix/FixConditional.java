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
import org.metafacture.metafix.conditional.*; // checkstyle-disable-line AvoidStarImport

import java.util.List;
import java.util.Map;

public enum FixConditional implements FixPredicate { // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity

    all_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new AllContain().test(metafix, record, params, options);
        }
    },
    any_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new AnyContain().test(metafix, record, params, options);
        }
    },
    none_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new NoneContain().test(metafix, record, params, options);
        }
    },
    str_contain {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new StrContain().test(metafix, record, params, options);
        }
    },

    all_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new AllEqual().test(metafix, record, params, options);
        }
    },
    any_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new AnyEqual().test(metafix, record, params, options);
        }
    },
    none_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new NoneEqual().test(metafix, record, params, options);
        }
    },
    str_equal {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new StrEqual().test(metafix, record, params, options);
        }
    },

    exists {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new Exists().test(metafix, record, params, options);
        }
    },

    in {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new In().test(metafix, record, params, options);
        }
    },
    is_contained_in {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsContainedIn().test(metafix, record, params, options);
        }
    },

    is_array {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsArray().test(metafix, record, params, options);
        }
    },
    is_empty {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsEmpty().test(metafix, record, params, options);
        }
    },
    is_false {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsFalse().test(metafix, record, params, options);
        }
    },
    is_hash {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsHash().test(metafix, record, params, options);
        }
    },
    is_number {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsNumber().test(metafix, record, params, options);
        }
    },
    is_object {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsObject().test(metafix, record, params, options);
        }
    },
    is_string {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsString().test(metafix, record, params, options);
        }
    },
    is_true {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new IsTrue().test(metafix, record, params, options);
        }
    },

    all_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new AllMatch().test(metafix, record, params, options);
        }
    },
    any_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new AnyMatch().test(metafix, record, params, options);
        }
    },
    none_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new NoneMatch().test(metafix, record, params, options);
        }
    },
    str_match {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new StrMatch().test(metafix, record, params, options);
        }
    },

    greater_than {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new GreaterThan().test(metafix, record, params, options);
        }
    },
    less_than {
        @Override
        public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            return new LessThan().test(metafix, record, params, options);
        }
    }

}
