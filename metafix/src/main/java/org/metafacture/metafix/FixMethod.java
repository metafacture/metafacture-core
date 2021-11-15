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

import java.util.Arrays;
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
            insert(InsertMode.REPLACE, record, split(params.get(0)), params.get(1));
        }
    },
    set_array {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String key = params.get(0);
            final List<String> toAdd = params.subList(1, params.size());
            if (key.endsWith(DOT_APPEND)) {
                Metafix.addAll(record, key.replace(DOT_APPEND, EMPTY), toAdd);
            }
            else {
                record.put(key, Value.newArray(a -> toAdd.forEach(s -> a.add(new Value(s)))));
            }
        }
    },
    set_hash {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String key = params.get(0);
            final Value val = record.get(key.replace(DOT_APPEND, EMPTY));

            final Value value = Value.newHash(h -> options.forEach((k, v) -> h.put(k, new Value(v))));

            if (key.endsWith(DOT_APPEND) && val.isArray()) {
                val.asArray().add(value);
            }
            else {
                record.put(key, value);
            }
        }
    },
    array { // array-from-hash
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String fieldName = params.get(0);
            Metafix.asList(record.get(fieldName), a -> a.forEach(recordEntry -> {
                if (recordEntry.isHash()) {
                    record.remove(fieldName);
                    recordEntry.asHash().forEach((subFieldName, value) -> {
                        Metafix.add(record, fieldName, new Value(subFieldName));
                        Metafix.add(record, fieldName, value);
                    });
                }
            }));
        }
    },
    hash { // hash-from-array
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            Metafix.asList(record.get(params.get(0)), values -> record.put(params.get(0), Value.newHash(h -> {
                for (int i = 1; i < values.size(); i = i + 2) {
                    h.put(values.get(i - 1).toString(), values.get(i));
                }
            })));
        }
    },
    add_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            insert(InsertMode.APPEND, record, split(params.get(0)), params.get(1));
        }
    },
    move_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            copy(record, params);
            remove(record, split(params.get(0)));
        }
    },
    copy_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            copy(record, params);
        }
    },
    remove_field {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            params.forEach(p -> remove(record, split(p)));
        }
    },
    format {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            Metafix.asList(record.get(params.get(0)), oldVals -> {
                final String newVal = String.format(params.get(1), oldVals.stream().toArray());
                record.replace(params.get(0), new Value(Arrays.asList(new Value(newVal))));
            });
        }
    },
    parse_text {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            Metafix.asList(record.get(params.get(0)), a -> a.forEach(v -> {
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
                    final Value value = Value.newHash(h -> {
                        while (groupMatcher.find()) {
                            final String group = groupMatcher.group(1);
                            h.put(group, new Value(m.group(group)));
                        }
                    });

                    if (!value.asHash().isEmpty()) {
                        Metafix.add(record, params.get(0), value);
                    }
                    else {
                        for (int i = 1; i <= m.groupCount(); i = i + 1) {
                            Metafix.add(record, params.get(0), new Value(m.group(i)));
                        }
                    }
                }
            }));
        }
    },
    paste {
        public void apply(final Record record, final List<String> params, final Map<String, String> options) {
            final String joinChar = options.get("join_char");
            insert(InsertMode.REPLACE, record, split(params.get(0)), params.subList(1, params.size()).stream()
                    .filter(k -> literalString(k) || find(record, split(k)) != null)
                    .map(k -> literalString(k) ? new Value(k.substring(1)) : Metafix.asList(find(record, split(k)), null).asArray().get(0))
                    .map(Value::toString).collect(Collectors.joining(joinChar != null ? joinChar : " ")));
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
        final Value found = find(record, split(key));
        if (found != null) {
            remove(record, split(key));

            if (fun != null) {
                Metafix.asList(found, a -> a.forEach(old -> insert(InsertMode.APPEND, record, split(key), fun.apply(old.toString()))));
            }
        }
    }

    private static Value insert(final InsertMode mode, final Value.Hash record, final String[] keys, final String value) {
        final String currentKey = keys[0];

        if (keys.length == 1) {
            mode.apply(record, currentKey, value);
        }
        else {
            final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
            final Value nested = insertNested(mode, record, value, currentKey, remainingKeys);
            record.put(currentKey, nested);
        }

        return new Value(record);
    }

    private static Value insertNested(final InsertMode mode, final Value.Hash record, final String value, final String currentKey, final String[] remainingKeys) {
        if (!record.containsField(currentKey)) {
            record.put(currentKey, Value.newHash());
        }
        final Value nested = record.get(currentKey);
        final Value result;
        if (nested.isHash()) {
            result = insert(mode, nested.asHash(), remainingKeys, value);
        }
        else if (nested.isArray()) {
            processList(mode, value, remainingKeys, nested.asArray());
            result = record.get(currentKey);
        }
        else {
            throw new IllegalStateException(NESTED + nested);
        }
        return result;
    }

    private static void processList(final InsertMode mode, final String value, final String[] remainingKeys, final Value.Array nestedList) {
        final Value nestedMap;
        switch (remainingKeys[0]) {
            case APPEND:
                nestedList.add(Value.newHash(h -> insert(mode, h, Arrays.copyOfRange(remainingKeys, 1, remainingKeys.length), value)));
                break;
            case LAST:
                final Value last = nestedList.get(nestedList.size() - 1);
                if (last.isHash()) {
                    insert(mode, last.asHash(), Arrays.copyOfRange(remainingKeys, 1, remainingKeys.length), value);
                }
                break;
            default:
                nestedList.add(Value.newHash(h -> insert(mode, h, remainingKeys, value)));
                break;
        }
    }

    static Value find(final Value.Hash record, final String[] keys) {
        final String currentKey = keys[0];
        if (!record.containsField(currentKey) || keys.length == 1) {
            return record.get(currentKey);
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        return findNested(record, currentKey, remainingKeys);
    }

    private static Value findNested(final Value.Hash record, final String currentKey, final String[] remainingKeys) {
        final Value nested = record.get(currentKey);

        // TODO: array of maps, like in insertNested
        if (nested.isArray()) {
            return Value.newArray(a -> nested.asArray().forEach(v -> a.add(findNested(record, currentKey, remainingKeys))));
        }

        if (nested.isHash()) {
            return find(nested.asHash(), remainingKeys);
        }

        throw new IllegalStateException(NESTED + nested);
    }

    private static Value remove(final Value.Hash record, final String[] keys) {
        final String currentKey = keys[0];
        if (keys.length == 1) {
            record.remove(currentKey);
        }
        if (!record.containsField(currentKey)) {
            return new Value(record);
        }
        final String[] remainingKeys = Arrays.copyOfRange(keys, 1, keys.length);
        return removeNested(record, currentKey, remainingKeys);
    }

    private static Value removeNested(final Value.Hash record, final String currentKey, final String[] remainingKeys) {
        final Value nested = record.get(currentKey);
        if (nested.isHash()) {
            return remove(nested.asHash(), remainingKeys);
        }
        throw new IllegalStateException(NESTED + nested);
    }

    private static void copy(final Record record, final List<String> params) {
        final String oldName = params.get(0);
        final String newName = params.get(1);
        final Value value = find(record, split(oldName));
        Metafix.asList(value, vs -> vs.forEach(v -> insert(InsertMode.APPEND, record, split(newName), v.toString())));
    }

    static String[] split(final String s) {
        return s.split("\\.");
    }

    private enum InsertMode {
        REPLACE {
            @Override
            void apply(final Value.Hash record, final String key, final String value) {
                record.put(key, new Value(value));
            }
        },
        APPEND {
            @Override
            void apply(final Value.Hash record, final String key, final String value) {
                final Value oldValue = record.get(key);
                final Value newValue = new Value(value);
                record.put(key, oldValue == null ? newValue : Metafix.merged(oldValue, newValue));
            }
        };
        abstract void apply(Value.Hash record, String key, String value);
    }

    abstract void apply(Record record, List<String> params, Map<String, String> options);
}
