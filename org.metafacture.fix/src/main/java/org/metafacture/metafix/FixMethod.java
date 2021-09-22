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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

enum FixMethod {

    // RECORD-LEVEL METHODS:

    set_field {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.remove(params.get(0));
            insert(InsertMode.REPLACE, record, split(params.get(0)), params.get(1));
        }
    },
    set_array {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String key = params.get(0);
            final List<String> toAdd = params.subList(1, params.size());
            if (key.contains(APPEND)) {
                Metafix.addAll(record, key.replace(APPEND, EMPTY), toAdd);
            }
            else {
                record.put(key, toAdd);
            }
        }
    },
    set_hash {
        @SuppressWarnings("unchecked")
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String key = params.get(0);
            final Object val = record.get(key.replace(APPEND, EMPTY));
            if (key.contains(APPEND) && val instanceof List) {
                ((List<Object>) val).add(options);
            }
            else {
                record.put(key, options);
            }
        }
    },
    array { // array-from-hash
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String fieldName = params.get(0);
            Metafix.asList(record.get(fieldName)).forEach(recordEntry -> {
                if (recordEntry instanceof Map) {
                    record.remove(fieldName);
                    ((Map<?, ?>) recordEntry).entrySet().forEach(mapEntry -> {
                        Metafix.add(record, fieldName, mapEntry.getKey());
                        Metafix.add(record, fieldName, mapEntry.getValue());
                    });
                }
            });
        }
    },
    hash { // hash-from-array
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final List<Object> values = Metafix.asList(record.get(params.get(0)));
            final Map<String, Object> result = new HashMap<>();
            for (int i = 0; i < values.size(); i = i + 1) {
                if (i % 2 == 1) {
                    result.put(values.get(i - 1).toString(), values.get(i));
                }
            }
            record.put(params.get(0), result);
        }
    },
    add_field {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            insert(InsertMode.APPEND, record, split(params.get(0)), params.get(1));
        }
    },
    move_field {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            copy(record, params);
            remove(record, split(params.get(0)));
        }
    },
    copy_field {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            copy(record, params);
        }
    },
    remove_field {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            params.forEach(p -> {
                remove(record, split(p));
            });
        }
    },
    format {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final Collection<Object> oldVals = Metafix.asList(record.get(params.get(0)));
            final String newVal = String.format(params.get(1), oldVals.toArray(new Object[] {}));
            record.replace(params.get(0), Arrays.asList(newVal));
        }
    },
    parse_text {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            Metafix.asList(record.get(params.get(0))).forEach(v -> {
                final Pattern p = Pattern.compile(params.get(1));
                final Matcher m = p.matcher(v.toString());
                if (m.matches()) {
                    record.remove(params.get(0));
                    final Map<String, Integer> namedGroups = getNamedGroups(p);
                    if (!namedGroups.isEmpty()) {
                        final Map<String, String> result = new HashMap<>();
                        namedGroups.keySet().forEach(k -> {
                            result.put(k, m.group(namedGroups.get(k)));
                        });
                        Metafix.add(record, params.get(0), result);
                    }
                    else {
                        for (int i = 1; i <= m.groupCount(); i = i + 1) {
                            Metafix.add(record, params.get(0), m.group(i));
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
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            final String joinChar = options.get("join_char");
            insert(InsertMode.REPLACE, record, split(params.get(0)),
                    params.subList(1, params.size()).stream()
                            .filter(k -> literalString(k) || find(record, split(k)) != null)
                            .map(k -> literalString(k) ? k.substring(1) : Metafix.asList(find(record, split(k))).iterator().next())
                            .map(Object::toString).collect(Collectors.joining(joinChar != null ? joinChar : " ")));
        }

        private boolean literalString(final String s) {
            return s.startsWith("~");
        }
    },
    reject {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            record.put("__reject", null);
        }
    },
    retain {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            new HashSet<>(record.keySet()).forEach(key -> {
                if (!params.contains(key)) {
                    record.remove(key);
                }
            });
        }
    },
    vacuum {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            new HashSet<>(record.keySet()).forEach(key -> {
                if (EMPTY.equals(record.get(key))) {
                    record.remove(key, EMPTY);
                }
            });
        }
    },
    // FIELD-LEVEL METHODS:

    substring {
        @SuppressWarnings("checkstyle:MagicNumber") // TODO: switch to morph-style named params in general?
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params,
                s -> s.substring(Integer.parseInt(params.get(1)), Integer.parseInt(params.get(2)) - 1));
        }
    },
    trim {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.trim());
        }
    },
    upcase {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.toUpperCase());
        }
    },
    downcase {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.toLowerCase());
        }
    },
    capitalize {
        public void apply(final Map<String, Object> record, final List<String> params,
                final Map<String, String> options) {
            applyToFields(record, params, s -> s.substring(0, 1).toUpperCase() + s.substring(1));
        }
    },
    lookup {
        public void apply(final Map<String, Object> record, final List<String> params,
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

    private static final String NESTED_NON_MAP = "Nested non-map: ";
    private static final String EMPTY = "";
    private static final String APPEND = ".$append";

    private static void applyToFields(final Map<String, Object> record, final List<String> params,
            final Function<String, String> fun) {
        final String key = params.get(0);
        if (record.containsKey(key)) {
            new ArrayList<>(Metafix.asList(record.get(key))).forEach(old -> {
                record.remove(key, old);
                final Object object = record.get(key);
                if (object instanceof List) {
                    ((List<?>) object).remove(old);
                }
                final String val = fun.apply(old.toString());
                record.put(key, object == null ? val : Metafix.merged(object, val));
            });
        }
    }

    private static Object insert(final InsertMode mode, final Map<String, Object> map, final String[] keys, final String value) {
        final String currentKey = keys[0];
        if (keys.length == 1) {
            mode.apply(map, currentKey, value);
            return map;
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        final Object nested = insertNested(mode, map, value, currentKey, remainingKeys);
        map.put(currentKey, nested);
        return map;
    }

    private static Object insertNested(final InsertMode mode, final Map<String, Object> map, final String value, final String currentKey,
            final String[] remainingKeys) {
        if (!map.containsKey(currentKey)) {
            map.put(currentKey, new LinkedHashMap<String, Object>());
        }
        final Object nested = map.get(currentKey);
        if (!(nested instanceof Map)) {
            throw new IllegalStateException(NESTED_NON_MAP + nested);
        }
        @SuppressWarnings("unchecked")
        final Object result = insert(mode, (Map<String, Object>) nested, remainingKeys, value);
        return result;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    static Object find(final Map<String, Object> map, final String[] keys) {
        final String currentKey = keys[0];
        if (!map.containsKey(currentKey)) {
            return null;
        }
        if (keys.length == 1) {
            return map.get(currentKey);
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        return findNested(map, currentKey, remainingKeys);
    }

    private static Object findNested(final Map<String, Object> map, final String currentKey,
            final String[] remainingKeys) {
        final Object nested = map.get(currentKey);
        if (!(nested instanceof Map)) {
            throw new IllegalStateException(NESTED_NON_MAP + nested);
        }
        @SuppressWarnings("unchecked")
        final Object result = find((Map<String, Object>) nested, remainingKeys);
        return result;
    }

    private static Object remove(final Map<String, Object> map, final String[] keys) {
        final String currentKey = keys[0];
        if (keys.length == 1) {
            map.remove(currentKey);
        }
        if (!map.containsKey(currentKey)) {
            return map;
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        return removeNested(map, currentKey, remainingKeys);
    }

    private static Object removeNested(final Map<String, Object> map, final String currentKey,
            final String[] remainingKeys) {
        final Object nested = map.get(currentKey);
        if (!(nested instanceof Map)) {
            throw new IllegalStateException(NESTED_NON_MAP + nested);
        }
        @SuppressWarnings("unchecked")
        final Object result = remove((Map<String, Object>) nested, remainingKeys);
        return result;
    }

    private static void copy(final Map<String, Object> record, final List<String> params) {
        final String oldName = params.get(0);
        final String newName = params.get(1);
        final Object value = find(record, split(oldName));
        if (value != null) {
            Metafix.asList(value).forEach(v -> {
                insert(InsertMode.APPEND, record, split(newName), value.toString());
            });
        }
    }

    static String[] split(final String s) {
        return s.split("\\.");
    }

    private enum InsertMode {
        REPLACE {
            @Override
            void apply(final Map<String, Object> map, final String key, final String value) {
                map.put(key, value);
            }
        },
        APPEND {
            @Override
            void apply(final Map<String, Object> map, final String key, final String value) {
                final Object object = map.get(key);
                map.put(key, object == null ? value : Metafix.merged(object, value));
            }
        };
        abstract void apply(Map<String, Object> map, String key, String value);
    }

    abstract void apply(Map<String, Object> record, List<String> params, Map<String, String> options);
}
