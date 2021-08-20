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

import org.metafacture.metamorph.maps.FileMap;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

enum FixMethod {

    // RECORD-LEVEL METHODS:

    set_field {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            record.replaceValues(params.get(0), Arrays.asList(params.get(1)));
        }
    },
    set_array {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            record.replaceValues(params.get(0), params.subList(1, params.size()));
        }
    },
    set_hash {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            options.keySet().forEach(k -> {
                record.replaceValues(params.get(0) + "." + k, Arrays.asList(options.get(k)));
            });
        }
    },
    array { // array-from-hash
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            //if record.get(params.get(0)) instanceof Map, etc. TODO: switch internal record to JSON-equiv
        }
    },
    hash { // hash-from-array
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            //if record.get(params.get(0)) instanceof List, etc. TODO: switch internal record to JSON-equiv
        }
    },
    add_field {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            record.put(params.get(0), params.get(1));
        }
    },
    move_field {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            final String oldFieldName = params.get(0);
            final String newFieldName = params.get(1);
            record.putAll(newFieldName, record.get(oldFieldName));
            record.removeAll(oldFieldName);
        }
    },
    copy_field {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            final String oldName = params.get(0);
            final String newName = params.get(1);
            record.putAll(newName, record.get(oldName));
        }
    },
    remove_field {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            record.removeAll(params.get(0));
        }
    },

    // FIELD-LEVEL METHODS:

    substring {
        @SuppressWarnings("checkstyle:MagicNumber") // TODO: switch to morph-style named params in general?
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params,
                s -> s.substring(Integer.parseInt(params.get(1)), Integer.parseInt(params.get(2)) - 1));
        }
    },
    trim {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.trim());
        }
    },
    upcase {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.toUpperCase());
        }
    },
    downcase {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.toLowerCase());
        }
    },
    capitalize {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.substring(0, 1).toUpperCase() + s.substring(1));
        }
    },
    lookup {
        public void apply(final Multimap<String, String> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> {
                final Map<String, String> map = buildMap(options, params.size() <= 1 ? null : params.get(1));
                return map.getOrDefault(s, map.get("__default")); // TODO Catmandu uses 'default'
            });
        }

        private Map<String, String> buildMap(final Map<String, String> options, final String fileLocation) {
            final String sep = "sep_char";
            final Map<String, String> map = fileLocation != null ? fileMap(fileLocation, options.get(sep)) : options;
            return map;
        }

        private Map<String, String> fileMap(final String location, final String separator) {
            final FileMap fileMap = new FileMap();
            fileMap.setSeparator(","); // CSV as default
            if (separator != null) { // override with option
                fileMap.setSeparator(separator);
            }
            fileMap.setFile(location);
            return fileMap;
        }
    };

    private static void applyToFields(final Multimap<String, String> record, final List<String> params,
            final Function<String, String> fun) {
        final String key = params.get(0);
        if (record.containsKey(key)) {
            final Collection<String> olds = new ArrayList<String>(record.get(key));
            olds.forEach(old -> {
                record.remove(key, old);
                record.put(key, fun.apply(old));
            });
        }
    }

    abstract void apply(Multimap<String, String> record, List<String> params, Map<String, String> options);
}
