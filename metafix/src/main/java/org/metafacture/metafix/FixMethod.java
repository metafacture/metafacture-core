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

import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metafix.maps.RdfMap;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.functions.ISBN;
import org.metafacture.metamorph.functions.Timestamp;
import org.metafacture.metamorph.maps.FileMap;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum FixMethod implements FixFunction { // checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity

    // SCRIPT-LEVEL METHODS:

    include {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String includeFile = params.get(0);

            if (!Metafix.isFixFile(includeFile)) {
                throw new IllegalArgumentException("Not a Fix file: " + includeFile);
            }

            // TODO: Catmandu load path
            final String includePath = metafix.resolvePath(includeFile);

            metafix.getRecordTransformer(includePath).transform(record, options);
        }
    },
    nothing {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            // do nothing
        }
    },
    put_filemap {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String fileName = params.get(0);
            final FileMap fileMap = new FileMap();

            fileMap.setSeparator(options.getOrDefault(FILEMAP_SEPARATOR_OPTION, FILEMAP_DEFAULT_SEPARATOR));
            fileMap.setFile(metafix.resolvePath(fileName));

            withOption(options, "allow_empty_values", fileMap::setAllowEmptyValues, this::getBoolean);
            withOption(options, "compression", fileMap::setCompression);
            withOption(options, "decompress_concatenated", fileMap::setDecompressConcatenated, this::getBoolean);
            withOption(options, "encoding", fileMap::setEncoding);
            withOption(options, "expected_columns", fileMap::setExpectedColumns, this::getInteger);
            withOption(options, "ignore_pattern", fileMap::setIgnorePattern);
            withOption(options, "key_column", fileMap::setKeyColumn, this::getInteger);
            withOption(options, "value_column", fileMap::setValueColumn, this::getInteger);

            metafix.putMap(params.size() > 1 ? params.get(1) : fileName, fileMap);
        }
    },
    put_map {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.putMap(params.get(0), options);
        }
    },
    put_rdfmap {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String rdfMapName = params.size() == 1 ? params.get(0) + options.get(RdfMap.TARGET) + options.getOrDefault(RdfMap.TARGET_LANGUAGE, "") : params.get(1);
            final String replaceTargets = options.get(RdfMap.TARGET) + options.getOrDefault(RdfMap.TARGET_LANGUAGE, "");
            final String resourceName = Optional.ofNullable(params.get(0))
                .map(str -> str.replaceAll(replaceTargets + "$", ""))
                .orElse(params.get(0));
            final RdfMap rdfMap = new RdfMap();
            if (resourceName.startsWith("http")) {
                rdfMap.setResource(resourceName);
            }
            else {
                rdfMap.setResource(metafix.resolvePath(resourceName));
            }
            withOption(options, RdfMap.TARGET, rdfMap::setTarget);
            withOption(options, RdfMap.TARGET_LANGUAGE, rdfMap::setTargetLanguage);
            withOption(options, RdfMap.SELECT, rdfMap::setSelect);
            withOption(options, Maps.DEFAULT_MAP_KEY, rdfMap::setDefault);

            metafix.putMap(rdfMapName, rdfMap);
        }
    },
    put_var {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.getVars().put(params.get(0), params.get(1));
        }
    },
    put_vars {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.getVars().putAll(options);
        }
    },

    // RECORD-LEVEL METHODS:

    add_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.addNested(params.get(0), new Value(params.get(1)));
        }
    },
    array { // array-from-hash
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> a.forEach(v -> v.matchType().ifHash(h -> {
                record.remove(field);

                h.forEach((subField, value) -> {
                    record.addNested(field, new Value(subField));
                    record.addNested(field, value);
                });
            })));
        }
    },
    call_macro {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String macroName = params.get(0);
            final RecordTransformer recordTransformer = metafix.getMacro(macroName);

            if (recordTransformer != null) {
                recordTransformer.transform(record, options);
            }
            else {
                throw new IllegalArgumentException("Macro '" + macroName + "' undefined!");
            }
        }
    },
    copy_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String oldName = params.get(0);
            final String newName = params.get(1);
            Value.asList(record.get(oldName), a -> a.forEach(oldValue -> {
                record.addNested(newName, oldValue); // we're actually aliasing
            }));
        }
    },
    format {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, oldValues -> {
                final String newValue = String.format(params.get(1), oldValues.stream().toArray());
                record.replace(field, new Value(Arrays.asList(new Value(newValue))));
            });
        }
    },
    hash { // hash-from-array
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> record.put(field, Value.newHash(h -> {
                for (int i = 1; i < a.size(); i = i + 2) {
                    h.put(a.get(i - 1).asString(), a.get(i));
                }
            })));
        }
    },
    move_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            FixMethod.copy_field.apply(metafix, record, params, options);
            record.remove(params.get(0));
        }
    },
    parse_text {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> a.forEach(v -> {
                final Pattern p = Pattern.compile(params.get(1));
                final Matcher m = p.matcher(v.asString());
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
                        record.addNested(field, value);
                    }
                    else {
                        for (int i = 1; i <= m.groupCount(); i = i + 1) {
                            record.addNested(field, new Value(m.group(i)));
                        }
                    }
                }
            }));
        }
    },
    paste {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String joinChar = options.get("join_char");
            final Value newValue = new Value(params.subList(1, params.size()).stream()
                    .filter(f -> literalString(f) || record.get(f) != null)
                    .map(f -> literalString(f) ? new Value(f.substring(1)) : Value.asList(record.get(f), null).asArray().get(0))
                    .map(Value::asString).collect(Collectors.joining(joinChar != null ? joinChar : " ")));
            record.set(params.get(0), newValue);
        }

        private boolean literalString(final String s) {
            return s.startsWith("~");
        }
    },
    print_record {
        private final Map<Metafix, LongAdder> scopedCounter = new HashMap<>();

        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final boolean internal = getBoolean(options, "internal");
            final boolean pretty = getBoolean(options, "pretty");

            if (!params.isEmpty()) {
                options.put("prefix", params.get(0));
            }

            withWriter(metafix, record, options, scopedCounter, c -> {
                if (internal) {
                    if (pretty) {
                        record.forEach((f, v) -> c.accept(f + "=" + v));
                    }
                    else {
                        c.accept(record.toString());
                    }
                }
                else {
                    try {
                        c.accept(record.toJson(pretty));
                    }
                    catch (final IOException e) {
                        // Log a warning? Print string representation instead?
                    }
                }
            });
        }
    },
    random {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);
            final int max = getInteger(params, 1);
            record.set(field, new Value(String.valueOf(RANDOM.nextInt(max))));
        }
    },
    reject {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.setReject(true);
        }
    },
    remove_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            params.forEach(p -> record.remove(p));
        }
    },
    rename {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String search = params.get(1);
            final String replace = params.get(2);

            final UnaryOperator<String> operator = s -> s.replaceAll(search, replace);

            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(renameArray(a, operator)))
                    .ifHash(h -> c.accept(renameHash(h, operator)))
                    .orElseThrow()
            );
        }

        private Value renameArray(final Value.Array array, final UnaryOperator<String> operator) {
            return Value.newArray(a -> array.forEach(v -> a.add(renameValue(v, operator))));
        }

        private Value renameHash(final Value.Hash hash, final UnaryOperator<String> operator) {
            return Value.newHash(h -> hash.forEach((f, v) -> h.put(operator.apply(f), renameValue(v, operator))));
        }

        private Value renameValue(final Value value, final UnaryOperator<String> operator) {
            return value.extractType((m, c) -> m
                    .ifArray(a -> c.accept(renameArray(a, operator)))
                    .ifHash(h -> c.accept(renameHash(h, operator)))
                    .orElse(c)
            );
        }
    },
    retain {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.retainFields(params);
        }
    },
    set_array {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);
            final Value newValue = newArray(params.subList(1, params.size()).stream().map(Value::new));
            record.set(field, newValue);
        }
    },
    set_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.set(params.get(0), new Value(params.get(1)));
        }
    },
    set_hash {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);
            final Value newValue = Value.newHash(h -> options.forEach((f, v) -> h.put(f, new Value(v))));
            record.set(field, newValue);
        }
    },
    timestamp {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);
            final Timestamp timestamp = new Timestamp();

            withOption(options, "format", timestamp::setFormat);
            withOption(options, "language", timestamp::setLanguage);
            withOption(options, "timezone", timestamp::setTimezone);

            record.set(field, new Value(timestamp.process(null)));
        }
    },
    vacuum {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.removeEmptyValues();
        }
    },

    // FIELD-LEVEL METHODS:

    // TODO SPEC: switch to morph-style named params in general?

    append {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String value = params.get(1);
            record.transform(params.get(0), s -> s + value);
        }
    },
    capitalize {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), s -> s.substring(0, 1).toUpperCase() + s.substring(1));
        }
    },
    count {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(new Value(a.size())))
                    .ifHash(h -> c.accept(new Value(h.size())))
            );
        }
    },
    downcase {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), s -> s.toLowerCase());
        }
    },
    filter {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final Pattern search = Pattern.compile(params.get(1));
            final boolean invert = getBoolean(options, "invert");

            final Predicate<Value> predicate = s -> search.matcher(s.asString()).find();

            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(newArray(a.stream().filter(invert ? predicate.negate() : predicate))))
            );
        }
    },
    flatten {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(newArray(flatten(a.stream()))))
            );
        }
    },
    from_json {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String errorString = options.get(ERROR_STRING_OPTION);
            final JsonValue.Parser parser = new JsonValue.Parser();

            record.transform(params.get(0), (m, c) -> m
                    .ifString(s -> {
                        try {
                            c.accept(parser.parse(s));
                        }
                        catch (final IOException e) {
                            c.accept(errorString != null ? new Value(errorString) : null);
                        }
                    })
            );
        }
    },
    index {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String search = params.get(1);
            record.transform(params.get(0), s -> String.valueOf(s.indexOf(search))); // TODO: multiple
        }
    },
    isbn {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final ISBN isbn = new ISBN();

            withOption(options, ERROR_STRING_OPTION, isbn::setErrorString);
            withOption(options, "to", isbn::setTo);
            withOption(options, "verify_check_digit", isbn::setVerifyCheckDigit);

            record.transform(params.get(0), isbn::process);
        }
    },
    join_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String joinChar = params.size() > 1 ? params.get(1) : "";
            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(new Value(a.stream().map(Value::asString).collect(Collectors.joining(joinChar)))))
            );
        }
    },
    lookup {
        private final Map<Metafix, LongAdder> scopedCounter = new HashMap<>();

        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final Map<String, String> map;

            if (params.size() <= 1) {
                map = options;
            }
            else {
                String mapName = params.get(1);

                if (!metafix.getMapNames().contains(mapName)) {
                    mapName = putMapAndGetMapName(metafix, record, params, options, mapName);
                }

                map = metafix.getMap(mapName);
            }

            final String defaultValue = map.get(Maps.DEFAULT_MAP_KEY); // TODO: Catmandu uses 'default'
            final boolean delete = getBoolean(options, "delete");
            final boolean printUnknown = getBoolean(options, "print_unknown");

            final Consumer<Consumer<String>> consumer = c -> record.transform(params.get(0), oldValue -> {
                final String newValue = map.get(oldValue);
                if (newValue != null) {
                    return newValue;
                }
                else {
                    if (c != null) {
                        c.accept(oldValue);
                    }

                    return defaultValue != null ? defaultValue : delete ? null : oldValue;
                }
            });

            if (printUnknown) {
                options.putIfAbsent("append", "true");
                withWriter(metafix, record, options, scopedCounter, consumer);
            }
            else {
                consumer.accept(null);
            }
        }

        private String putMapAndGetMapName(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final String mapName) {
            String newMapName = mapName;
            if (options.containsKey(RdfMap.TARGET)) {
                put_rdfmap.apply(metafix, record, Arrays.asList(params.get(1)), options);
                newMapName = params.get(1) + options.get(RdfMap.TARGET) + options.getOrDefault(RdfMap.TARGET_LANGUAGE, "");
            }
            else {
                if (mapName.contains(".") || mapName.contains(File.separator)) {
                    put_filemap.apply(metafix, record, Arrays.asList(mapName), options);
                }
                else {
                    // Probably an unknown internal map? Log a warning?
                }
            }
            return newMapName;
        }
    },
    prepend {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String value = params.get(1);
            record.transform(params.get(0), s -> value + s);
        }
    },
    replace_all {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String search = params.get(1);
            final String replace = params.get(2);

            record.transform(params.get(0), s -> s.replaceAll(search, replace));
        }
    },
    reverse {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> {
                        final List<Value> list = a.stream().collect(Collectors.toList());
                        Collections.reverse(list);
                        c.accept(new Value(list));
                    })
                    .ifString(s -> c.accept(new Value(new StringBuilder(s).reverse().toString())))
            );
        }
    },
    sort_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final boolean numeric = getBoolean(options, "numeric");
            final boolean reverse = getBoolean(options, "reverse");
            final boolean uniq = getBoolean(options, "uniq");

            final Function<Value, String> function = Value::asString;
            final Comparator<Value> comparator = numeric ?
                Comparator.comparing(function.andThen(Integer::parseInt)) : Comparator.comparing(function);

            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(new Value((uniq ? unique(a.stream()) : a.stream())
                                .sorted(reverse ? comparator.reversed() : comparator).collect(Collectors.toList()))))
            );
        }
    },
    split_field {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String splitChar = params.size() > 1 ? params.get(1) : "\\s+";
            final Pattern splitPattern = Pattern.compile(splitChar);

            final Function<String, Value> splitFunction = s ->
                newArray(Arrays.stream(splitPattern.split(s)).map(Value::new));

            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(newArray(a.stream().map(Value::asString).map(splitFunction))))
                    .ifHash(h -> c.accept(Value.newHash(n -> h.forEach((f, w) -> n.put(f, splitFunction.apply(w.asString()))))))
                    .ifString(s -> c.accept(splitFunction.apply(s)))
            );
        }
    },
    substring {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final int offset = getInteger(params, 1);
            final Integer end = params.size() > 2 ? offset + getInteger(params, 2) : null;
            // TODO: final String replacement = params.size() > 3 ? params.get(3) : null;

            record.transform(params.get(0), s -> {
                final int length = s.length();
                return offset > length ? s : end == null || end > length ? s.substring(offset) : s.substring(offset, end);
            });
        }
    },
    sum {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(new Value(a.stream().map(Value::asString).mapToInt(Integer::parseInt).sum())))
            );
        }
    },
    to_json {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String errorString = options.get(ERROR_STRING_OPTION);
            final boolean pretty = getBoolean(options, "pretty");

            record.transform(params.get(0), (m, c) -> m
                    .orElse(v -> {
                        try {
                            c.accept(new Value(v.toJson(pretty)));
                        }
                        catch (final IOException e) {
                            c.accept(errorString != null ? new Value(errorString) : null);
                        }
                    })
            );
        }
    },
    trim {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), String::trim);
        }
    },
    uniq {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), (m, c) -> m
                    .ifArray(a -> c.accept(newArray(unique(a.stream()))))
            );
        }
    },
    upcase {
        @Override
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transform(params.get(0), s -> s.toUpperCase());
        }
    };

    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(.+?)>");

    private static final String FILEMAP_SEPARATOR_OPTION = "sep_char";
    private static final String FILEMAP_DEFAULT_SEPARATOR = ",";

    private static final String ERROR_STRING_OPTION = "error_string";

    private static final Random RANDOM = new Random();

}
