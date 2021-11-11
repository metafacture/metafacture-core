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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            record.remove(params.get(0));
            insert(InsertMode.REPLACE, record.temporarilyGetMap(), split(params.get(0)), params.get(1));
        }
    },
    set_array {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String key = params.get(0);
            final List<String> toAdd = params.subList(1, params.size());
            if (key.endsWith(DOT_APPEND)) {
                Metafix.addAll(record.temporarilyGetMap(), key.replace(DOT_APPEND, EMPTY), toAdd);
            }
            else {
                record.put(key, toAdd);
            }
        }
    },
    set_hash {
        @SuppressWarnings("unchecked")
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String key = params.get(0);
            final Object val = record.get(key.replace(DOT_APPEND, EMPTY));
            if (key.endsWith(DOT_APPEND) && val instanceof List) {
                ((List<Object>) val).add(options);
            }
            else {
                record.put(key, options);
            }
        }
    },
    array { // array-from-hash
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String fieldName = params.get(0);
            Metafix.asList(record.get(fieldName)).forEach(recordEntry -> {
                if (recordEntry instanceof Map) {
                    record.remove(fieldName);
                    ((Map<?, ?>) recordEntry).entrySet().forEach(mapEntry -> {
                        Metafix.add(record.temporarilyGetMap(), fieldName, mapEntry.getKey());
                        Metafix.add(record.temporarilyGetMap(), fieldName, mapEntry.getValue());
                    });
                }
            });
        }
    },
    hash { // hash-from-array
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
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
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            insert(InsertMode.APPEND, record.temporarilyGetMap(), split(params.get(0)), params.get(1));
        }
    },
    move_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            copy(record, params);
            remove(record.temporarilyGetMap(), split(params.get(0)));
        }
    },
    copy_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            copy(record, params);
        }
    },
    remove_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            params.forEach(p -> remove(record.temporarilyGetMap(), split(p)));
        }
    },
    format {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final Collection<Object> oldVals = Metafix.asList(record.get(params.get(0)));
            final String newVal = String.format(params.get(1), oldVals.toArray(new Object[] {}));
            record.replace(params.get(0), Arrays.asList(newVal));
        }
    },
    parse_text {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            Metafix.asList(record.get(params.get(0))).forEach(v -> {
                final Pattern p = Pattern.compile(params.get(1));
                final Matcher m = p.matcher(v.toString());
                if (m.matches()) {
                    record.remove(params.get(0));

                    /**
                     * {@code Pattern.namedGroups()} not available as API,
                     * see https://stackoverflow.com/a/65012527.
                     *
                     * Assumptions: 1. Named groups are not escaped/quoted;
                     * 2. Named groups are not mixed with unnamed groups.
                     */
                    final Matcher groupMatcher = NAMED_GROUP_PATTERN.matcher(p.pattern());
                    final Map<String, String> result = new LinkedHashMap<>();

                    while (groupMatcher.find()) {
                        final String group = groupMatcher.group(1);
                        result.put(group, m.group(group));
                    }

                    if (!result.isEmpty()) {
                        Metafix.add(record.temporarilyGetMap(), params.get(0), result);
                    }
                    else {
                        for (int i = 1; i <= m.groupCount(); i = i + 1) {
                            Metafix.add(record.temporarilyGetMap(), params.get(0), m.group(i));
                        }
                    }
                }
            });
        }
    },
    paste {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String joinChar = options.get("join_char");
            insert(InsertMode.REPLACE, record.temporarilyGetMap(), split(params.get(0)),
                    params.subList(1, params.size()).stream()
                            .filter(k -> literalString(k) || find(record.temporarilyGetMap(), split(k)) != null)
                            .map(k -> literalString(k) ? k.substring(1) : Metafix.asList(find(record.temporarilyGetMap(), split(k))).iterator().next())
                            .map(Object::toString).collect(Collectors.joining(joinChar != null ? joinChar : " ")));
        }

        private boolean literalString(final String s) {
            return s.startsWith("~");
        }
    },
    reject {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            record.setReject(true);
        }
    },
    retain {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            record.retainFields(params);
        }
    },
    vacuum {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            record.removeEmptyValues();
        }
    },
    // FIELD-LEVEL METHODS:

    substring {
        @SuppressWarnings("checkstyle:MagicNumber") // TODO: switch to morph-style named params in general?
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            applyToFields(record, params,
                s -> s.substring(Integer.parseInt(params.get(1)), Integer.parseInt(params.get(2)) - 1));
        }
    },
    trim {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            applyToFields(record, params, s -> s.trim());
        }
    },
    upcase {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            applyToFields(record, params, s -> s.toUpperCase());
        }
    },
    downcase {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            applyToFields(record, params, s -> s.toLowerCase());
        }
    },
    capitalize {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            applyToFields(record, params, s -> s.substring(0, 1).toUpperCase() + s.substring(1));
        }
    },
    lookup {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
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

    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(.+?)>");

    private static final String NESTED = "Nested non-map / non-list: ";
    private static final String EMPTY = "";
    private static final String APPEND = "$append";
    private static final String DOT_APPEND = "." + APPEND;
    private static final String LAST = "$last";

    private static void applyToFields(final Record record, final List<String> params, final Function<String, String> fun) {
        final String key = params.get(0);
        final Object found = find(record.temporarilyGetMap(), split(key));
        final boolean containsKey = found != null;
        if (containsKey) {
            remove(record.temporarilyGetMap(), split(key));
            new ArrayList<>(Metafix.asList(found)).forEach(old -> {
                if (fun != null && old != null) {
                    final String val = fun.apply(old.toString());
                    insert(InsertMode.APPEND, record.temporarilyGetMap(), split(key), val);
                }
            });
        }
    }

    private static Object insert(final InsertMode mode, final Map<String, Object> record, final String[] keys, final String value) {
        final String currentKey = keys[0];
        if (keys.length == 1) {
            mode.apply(record, currentKey, value);
            return record;
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        final Object nested = insertNested(mode, record, value, currentKey, remainingKeys);
        record.put(currentKey, nested);
        return record;
    }

    @SuppressWarnings("unchecked")
    private static Object insertNested(final InsertMode mode, final Map<String, Object> record, final String value, final String currentKey, final String[] remainingKeys) {
        if (!record.containsKey(currentKey)) {
            record.put(currentKey, new LinkedHashMap<String, Object>());
        }
        final Object nested = record.get(currentKey);
        final Object result;
        if (nested instanceof Map) {
            result = insert(mode, (Map<String, Object>) nested, remainingKeys, value);
        }
        else if (nested instanceof List) {
            processList(mode, value, remainingKeys, nested);
            result = record.get(currentKey);
        }
        else {
            throw new IllegalStateException(NESTED + nested);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void processList(final InsertMode mode, final String value, final String[] remainingKeys,
            final Object nested) {
        final List<Object> nestedList = (List<Object>) nested;
        final Map<String, Object> nestedMap;
        switch (remainingKeys[0]) {
            case APPEND:
                nestedMap = new LinkedHashMap<>();
                nestedList.add(nestedMap);
                insert(mode, nestedMap, Arrays.copyOfRange(remainingKeys, 1, remainingKeys.length), value);
                break;
            case LAST:
                final Object last = nestedList.get(nestedList.size() - 1);
                if (last instanceof Map) {
                    nestedMap = (Map<String, Object>) last;
                    insert(mode, nestedMap, Arrays.copyOfRange(remainingKeys, 1, remainingKeys.length), value);
                }
                break;
            default:
                nestedMap = new LinkedHashMap<>();
                nestedList.add(nestedMap);
                insert(mode, nestedMap, remainingKeys, value);
                break;
        }
    }

    static Object find(final Map<String, Object> record, final String[] keys) {
        final String currentKey = keys[0];
        if (!record.containsKey(currentKey) || keys.length == 1) {
            return record.get(currentKey);
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        return findNested(record, currentKey, remainingKeys);
    }

    private static Object findNested(final Map<String, Object> record, final String currentKey, final String[] remainingKeys) {
        final Object nested = record.get(currentKey);
        // TODO: array of maps, like in insertNested
        if (nested instanceof List) {
            return ((List<?>) nested).stream().map(o -> findNested(record, currentKey, remainingKeys))
                    .collect(Collectors.toList());
        }
        if (nested instanceof Map) {
            @SuppressWarnings("unchecked")
            final Object result = find((Map<String, Object>) nested, remainingKeys);
            return result;
        }
        throw new IllegalStateException(NESTED + nested);
    }

    private static Object remove(final Map<String, Object> record, final String[] keys) {
        final String currentKey = keys[0];
        if (keys.length == 1) {
            record.remove(currentKey);
        }
        if (!record.containsKey(currentKey)) {
            return record;
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        return removeNested(record, currentKey, remainingKeys);
    }

    private static Object removeNested(final Map<String, Object> record, final String currentKey, final String[] remainingKeys) {
        final Object nested = record.get(currentKey);
        if (!(nested instanceof Map)) {
            throw new IllegalStateException(NESTED + nested);
        }
        @SuppressWarnings("unchecked")
        final Object result = remove((Map<String, Object>) nested, remainingKeys);
        return result;
    }

    private static void copy(final Record record, final List<String> params) {
        final String oldName = params.get(0);
        final String newName = params.get(1);
        final Object value = find(record.temporarilyGetMap(), split(oldName));
        if (value != null) {
            final List<Object> vs = Metafix.asList(value);
            for (final Object v : vs.stream().filter(v -> v != null).collect(Collectors.toList())) {
                insert(InsertMode.APPEND, record.temporarilyGetMap(), split(newName), v.toString());
            }
        }
    }

    static String[] split(final String s) {
        return s.split("\\.");
    }

    private enum InsertMode {
        REPLACE {
            @Override
            void apply(final Map<String, Object> record, final String key, final String value) {
                record.put(key, value);
            }
        },
        APPEND {
            @Override
            void apply(final Map<String, Object> record, final String key, final String value) {
                final Object object = record.get(key);
                record.put(key, object == null ? value : Metafix.merged(object, value));
            }
        };
        abstract void apply(Map<String, Object> record, String key, String value);
    }

    abstract void apply(Record record, List<String> params, Map<String, String> options);
}
