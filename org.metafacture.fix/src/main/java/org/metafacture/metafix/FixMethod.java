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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

enum FixMethod {

    // RECORD-LEVEL METHODS:

    set_field {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.replaceValues(params.get(0), Arrays.asList(params.get(1)));
        }
    },
    set_array {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.replaceValues(params.get(0), params.subList(1, params.size()));
        }
    },
    set_hash {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.removeAll(params.get(0));
            record.put(params.get(0), options);
        }
    },
    array { // array-from-hash
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String fieldName = params.get(0);
            record.get(fieldName).forEach(recordEntry -> {
                if (recordEntry instanceof Map) {
                    record.removeAll(fieldName);
                    ((Map<?, ?>) recordEntry).entrySet().forEach(mapEntry -> {
                        record.put(fieldName, mapEntry.getKey());
                        record.put(fieldName, mapEntry.getValue());
                    });
                }
            });
        }
    },
    hash { // hash-from-array
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final List<Object> values = new ArrayList<>(record.get(params.get(0)));
            final Map<String, Object> result = new HashMap<>();
            for (int i = 0; i < values.size(); i = i + 1) {
                if (i % 2 == 1) {
                    result.put(values.get(i - 1).toString(), values.get(i));
                }
            }
            record.removeAll(params.get(0).toString());
            record.put(params.get(0), result);
        }
    },
    add_field {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.put(params.get(0), params.get(1));
        }
    },
    move_field {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String oldFieldName = params.get(0);
            final String newFieldName = params.get(1);
            record.putAll(newFieldName, record.get(oldFieldName));
            record.removeAll(oldFieldName);
        }
    },
    copy_field {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String oldName = params.get(0);
            final String newName = params.get(1);
            record.putAll(newName, record.get(oldName));
        }
    },
    remove_field {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            params.forEach(p -> {
                record.removeAll(p);
            });
        }
    },
    format {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final Collection<Object> oldVals = record.get(params.get(0));
            final String newVal = String.format(params.get(1), oldVals.toArray(new Object[] {}));
            record.replaceValues(params.get(0), Arrays.asList(newVal));
        }
    },
    parse_text {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.get(params.get(0)).forEach(v -> {
                final Pattern p = Pattern.compile(params.get(1));
                final Matcher m = p.matcher(v.toString());
                if (m.matches()) {
                    record.removeAll(params.get(0));
                    final Map<String, Integer> namedGroups = getNamedGroups(p);
                    if (!namedGroups.isEmpty()) {
                        final Map<String, String> result = new HashMap<>();
                        namedGroups.keySet().forEach(k -> {
                            result.put(k, m.group(namedGroups.get(k)));
                        });
                        record.put(params.get(0), result);
                    }
                    else {
                        for (int i = 1; i <= m.groupCount(); i = i + 1) {
                            record.put(params.get(0), m.group(i));
                        }
                    }
                }
            });
        }

        @SuppressWarnings("unchecked")
        private Map<String, Integer> getNamedGroups(final Pattern regex) {
            try {
                // Not available as API, see https://stackoverflow.com/a/15596145/18154:
                final Method namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
                namedGroupsMethod.setAccessible(true);
                Map<String, Integer> namedGroups = null;
                namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(regex);
                if (namedGroups == null) {
                    throw new InternalError();
                }
                return Collections.unmodifiableMap(namedGroups);
            }
            catch (final NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return Collections.emptyMap();
        }
    },
    paste {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String joinChar = options.get("join_char");
            record.put(params.get(0),
                    params.subList(1, params.size()).stream()
                            .map(k -> k.startsWith("~") ? k.substring(1) : record.get(k).iterator().next())
                            .map(Object::toString).collect(Collectors.joining(joinChar != null ? joinChar : " ")));
        }
    },
    reject {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.put("__reject", true);
        }
    },
    // FIELD-LEVEL METHODS:

    substring {
        @SuppressWarnings("checkstyle:MagicNumber") // TODO: switch to morph-style named params in general?
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params,
                s -> s.substring(Integer.parseInt(params.get(1)), Integer.parseInt(params.get(2)) - 1));
        }
    },
    trim {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.trim());
        }
    },
    upcase {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.toUpperCase());
        }
    },
    downcase {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.toLowerCase());
        }
    },
    capitalize {
        public void apply(final Multimap<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.substring(0, 1).toUpperCase() + s.substring(1));
        }
    },
    lookup {
        public void apply(final Multimap<String, Object> record, final List<String> params,
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

    private static void applyToFields(final Multimap<String, Object> record, final List<String> params,
            final Function<String, String> fun) {
        final String key = params.get(0);
        if (record.containsKey(key)) {
            final Collection<Object> olds = new ArrayList<Object>(record.get(key));
            olds.forEach(old -> {
                record.remove(key, old);
                record.put(key, fun.apply(old.toString()));
            });
        }
    }

    abstract void apply(Multimap<String, Object> record, List<String> params, Map<String, String> options);
}
