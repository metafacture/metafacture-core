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

import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.maps.FileMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum FixMethod {

    // SCRIPT-LEVEL METHODS:

    nothing {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            // do nothing
        }
    },
    put_filemap {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String fileName = params.get(0);
            final FileMap fileMap = new FileMap();

            fileMap.setSeparator(options.getOrDefault(FILEMAP_SEPARATOR_OPTION, FILEMAP_DEFAULT_SEPARATOR));
            fileMap.setFile(fileName);

            metafix.putMap(params.size() <= 1 ? fileName : params.get(1), fileMap);
        }
    },
    put_map {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.putMap(params.get(0), options);
        }
    },
    put_var {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.getVars().put(params.get(0), params.get(1));
        }
    },
    put_vars {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.getVars().putAll(options);
        }
    },

    // RECORD-LEVEL METHODS:

    add_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.append(params.get(0), params.get(1));
        }
    },
    array { // array-from-hash
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> a.forEach(v -> v.matchType().ifHash(h -> {
                record.remove(field);

                h.forEach((subField, value) -> {
                    record.add(field, new Value(subField));
                    record.add(field, value);
                });
            })));
        }
    },
    copy_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.copy(params);
        }
    },
    format {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, oldValues -> {
                final String newValue = String.format(params.get(1), oldValues.stream().toArray());
                record.replace(field, new Value(Arrays.asList(new Value(newValue))));
            });
        }
    },
    hash { // hash-from-array
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> record.put(field, Value.newHash(h -> {
                for (int i = 1; i < a.size(); i = i + 2) {
                    h.put(a.get(i - 1).toString(), a.get(i));
                }
            })));
        }
    },
    move_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.copy(params);
            record.removeNested(params.get(0));
        }
    },
    parse_text {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> a.forEach(v -> {
                final Pattern p = Pattern.compile(params.get(1));
                final Matcher m = p.matcher(v.toString());
                if (m.matches()) {
                    record.remove(field);

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
                        record.add(field, value);
                    }
                    else {
                        for (int i = 1; i <= m.groupCount(); i = i + 1) {
                            record.add(field, new Value(m.group(i)));
                        }
                    }
                }
            }));
        }
    },
    paste {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String joinChar = options.get("join_char");
            record.replace(params.get(0), params.subList(1, params.size()).stream()
                    .filter(f -> literalString(f) || record.find(f) != null)
                    .map(f -> literalString(f) ? new Value(f.substring(1)) : record.findList(f, null).asArray().get(0))
                    .map(Value::toString).collect(Collectors.joining(joinChar != null ? joinChar : " ")));
        }

        private boolean literalString(final String s) {
            return s.startsWith("~");
        }
    },
    random {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);
            final int max = getInteger(params, 1);

            record.append(field, String.valueOf(RANDOM.nextInt(max)));
        }
    },
    reject {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.setReject(true);
        }
    },
    remove_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            params.forEach(record::removeNested);
        }
    },
    rename {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v -> {
                final String search = params.get(1);
                final String replace = params.get(2);

                // TODO: recurse into arrays/values
                return v.isHash() ? Value.newHash(h ->
                        v.asHash().forEach((f, w) -> h.put(f.replaceAll(search, replace), w))) : null;
            });
        }
    },
    retain {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.retainFields(params);
        }
    },
    set_array {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);
            final List<String> toAdd = params.subList(1, params.size());
            if (field.endsWith(DOT_APPEND)) {
                record.addAll(field.replace(DOT_APPEND, EMPTY), toAdd);
            }
            else {
                record.put(field, newArray(toAdd.stream().map(Value::new)));
            }
        }
    },
    set_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.remove(field);
            record.replace(field, params.get(1));
        }
    },
    set_hash {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            final Value value = record.get(field.replace(DOT_APPEND, EMPTY));
            final Value newValue = Value.newHash(h -> options.forEach((f, v) -> h.put(f, new Value(v))));

            if (field.endsWith(DOT_APPEND) && value.isArray()) {
                value.asArray().add(newValue);
            }
            else {
                record.put(field, newValue);
            }
        }
    },
    vacuum {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.removeEmptyValues();
        }
    },

    // FIELD-LEVEL METHODS:

    // TODO SPEC: switch to morph-style named params in general?

    append {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String value = params.get(1);
            record.transformFields(params, s -> s + value);
        }
    },
    capitalize {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, s -> s.substring(0, 1).toUpperCase() + s.substring(1));
        }
    },
    count {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v ->
                    v.isArray() ? new Value(v.asArray().size()) : v.isHash() ? new Value(v.asHash().size()) : null);
        }
    },
    downcase {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, String::toLowerCase);
        }
    },
    filter {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v -> {
                final Pattern search = Pattern.compile(params.get(1));
                final boolean invert = getBoolean(options, "invert");

                final Predicate<Value> predicate = s -> search.matcher(s.asString()).find();
                return v.isArray() ? newArray(v.asArray().stream().filter(invert ? predicate.negate() : predicate)) : null;
            });
        }
    },
    index {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String search = params.get(1);
            record.transformFields(params, s -> String.valueOf(s.indexOf(search))); // TODO: multiple
        }
    },
    lookup {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final Map<String, String> map;

            if (params.size() <= 1) {
                map = options;
            }
            else {
                final String mapName = params.get(1);

                if (!metafix.getMapNames().contains(mapName)) {
                    put_filemap.apply(metafix, record, Arrays.asList(mapName), options);
                }

                map = metafix.getMap(mapName);
            }

            final String defaultValue = map.get(Maps.DEFAULT_MAP_KEY); // TODO: Catmandu uses 'default'
            record.transformFields(params, k -> map.getOrDefault(k, defaultValue));
        }
    },
    prepend {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String value = params.get(1);
            record.transformFields(params, s -> value + s);
        }
    },
    replace_all {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String search = params.get(1);
            final String replace = params.get(2);

            record.transformFields(params, s -> s.replaceAll(search, replace));
        }
    },
    reverse {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v -> {
                final Value result;

                if (v.isString()) {
                    result = new Value(new StringBuilder(v.asString()).reverse().toString());
                }
                else if (v.isArray()) {
                    final List<Value> list = v.asArray().stream().collect(Collectors.toList());
                    Collections.reverse(list);
                    result = new Value(list);
                }
                else {
                    result = null;
                }

                return result;
            });
        }
    },
    sort_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v -> {
                final boolean numeric = getBoolean(options, "numeric");
                final boolean reverse = getBoolean(options, "reverse");
                final boolean uniq = getBoolean(options, "uniq");

                final Stream<Value> stream = v.asArray().stream();
                final Function<Value, String> function = Value::asString;
                final Comparator<Value> comparator = numeric ?
                    Comparator.comparing(function.andThen(Integer::parseInt)) : Comparator.comparing(function);

                return v.isArray() ? new Value((uniq ? unique(stream) : stream)
                        .sorted(reverse ? comparator.reversed() : comparator).collect(Collectors.toList())) : null;
            });
        }
    },
    split_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v -> {
                final String splitChar = params.size() > 1 ? params.get(1) : "\\s+";
                final Pattern splitPattern = Pattern.compile(splitChar);

                final UnaryOperator<Value> splitOperator = s ->
                    newArray(Arrays.stream(splitPattern.split(s.asString())).map(Value::new));

                return v.isString() ? splitOperator.apply(v) : v.isArray() ?
                    newArray(v.asArray().stream().map(splitOperator)) : v.isHash() ?
                    Value.newHash(h -> v.asHash().forEach((f, s) -> h.put(f, splitOperator.apply(s)))) : null;
            });
        }
    },
    substring {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, s -> s.substring(getInteger(params, 1), getInteger(params, 2) - 1));
        }
    },
    sum {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v ->
                    v.isArray() ? new Value(v.asArray().stream().map(Value::asString).mapToInt(Integer::parseInt).sum()) : null);
        }
    },
    trim {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, String::trim);
        }
    },
    uniq {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformField(params.get(0), v -> v.isArray() ? newArray(unique(v.asArray().stream())) : null);
        }
    },
    upcase {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, String::toUpperCase);
        }
    };

    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(.+?)>");

    private static final String EMPTY = "";
    private static final String DOT_APPEND = "." + Value.ReservedField.$append.name();

    private static final String FILEMAP_SEPARATOR_OPTION = "sep_char";
    private static final String FILEMAP_DEFAULT_SEPARATOR = ",";

    private static final Random RANDOM = new Random();

    private static boolean getBoolean(final Map<String, String> options, final String key) {
        return Boolean.parseBoolean(options.get(key));
    }

    private static int getInteger(final List<String> params, final int index) {
        return Integer.parseInt(params.get(index));
    }

    private static Value newArray(final Stream<Value> stream) {
        return Value.newArray(a -> stream.forEach(a::add));
    }

    private static Stream<Value> unique(final Stream<Value> stream) {
        final Set<Value> set = new HashSet<>();
        return stream.filter(set::add);
    }

    abstract void apply(Metafix metafix, Record record, List<String> params, Map<String, String> options);

}
