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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

enum FixMethod {

    // SCRIPT-LEVEL METHODS:

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
    put_map {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            metafix.putMap(params.get(0), options);
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

    // RECORD-LEVEL METHODS:

    set_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.remove(field);
            record.replace(field, params.get(1));
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
                record.put(field, Value.newArray(a -> toAdd.forEach(s -> a.add(new Value(s)))));
            }
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
    array { // array-from-hash
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            final String field = params.get(0);

            record.getList(field, a -> a.forEach(recordEntry -> {
                if (recordEntry.isHash()) {
                    record.remove(field);

                    recordEntry.asHash().forEach((subField, value) -> {
                        record.add(field, new Value(subField));
                        record.add(field, value);
                    });
                }
            }));
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
    add_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.append(params.get(0), params.get(1));
        }
    },
    move_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.copy(params);
            record.removeNested(params.get(0));
        }
    },
    copy_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.copy(params);
        }
    },
    remove_field {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            params.forEach(record::removeNested);
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
    reject {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.setReject(true);
        }
    },
    retain {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.retainFields(params);
        }
    },
    vacuum {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.removeEmptyValues();
        }
    },

    // FIELD-LEVEL METHODS:

    // TODO SPEC: switch to morph-style named params in general?

    substring {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, s -> s.substring(Integer.parseInt(params.get(1)), Integer.parseInt(params.get(2)) - 1));
        }
    },
    trim {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, String::trim);
        }
    },
    upcase {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, String::toUpperCase);
        }
    },
    downcase {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, String::toLowerCase);
        }
    },
    capitalize {
        public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
            record.transformFields(params, s -> s.substring(0, 1).toUpperCase() + s.substring(1));
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
    };

    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(.+?)>");

    private static final String EMPTY = "";
    private static final String DOT_APPEND = "." + Value.APPEND_FIELD;

    private static final String FILEMAP_SEPARATOR_OPTION = "sep_char";
    private static final String FILEMAP_DEFAULT_SEPARATOR = ",";

    abstract void apply(Metafix metafix, Record record, List<String> params, Map<String, String> options);

}
