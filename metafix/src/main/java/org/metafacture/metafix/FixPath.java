/*
 * Copyright 2022 Fabian Steeg, hbz NRW
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

import org.metafacture.metafix.Value.Array;
import org.metafacture.metafix.Value.Hash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Our goal here is something like https://metacpan.org/pod/Catmandu::Path::simple
 *
 * With all get/set/update/create/delete logic collected here.
 *
 * @author Fabian Steeg (fsteeg)
 *
 */
/*package-private*/ class FixPath {

    private static final String ASTERISK = "*";
    private String[] path;

    /*package-private*/ FixPath(final String path) {
        this(Value.split(path));
    }

    private FixPath(final String[] path) {
        this.path = path;
    }

    /*package-private*/ Value findIn(final Hash hash) {
        return findIn(hash, false);
    }

    /*package-private*/ Value findIn(final Hash hash, final boolean enforceStringValue) {
        final String currentSegment = path[0];
        final FixPath remainingPath = new FixPath(tail(path));
        if (currentSegment.equals(ASTERISK) && remainingPath.size() > 0) {
            // TODO: search in all elements of hash?
            return remainingPath.findIn(hash, enforceStringValue);
        }
        final Value value = hash.get(currentSegment, enforceStringValue && path.length == 1);
        return value == null || path.length == 1 ? value : value.extractType((m, c) -> m
                .ifArray(a -> c.accept(remainingPath.findIn(a)))
                .ifHash(h -> c.accept(remainingPath.findIn(h, enforceStringValue)))
                .orElseThrow()
        );
    }

    /*package-private*/ Value findIn(final Array array) {

        final Value result;
        if (path.length == 0) {
            result = new Value(array);
        }
        else {
            final String currentSegment = path[0];
            if (currentSegment.equals(ASTERISK)) {
                result = Value.newArray(resultArray -> array.forEach(v -> {
                    final Value findInValue = findInValue(v, tail(path));
                    if (findInValue != null) {
                        findInValue.matchType()
                            // flatten result arrays (use Value#path for structure)
                            .ifArray(a -> a.forEach(resultArray::add))
                            .orElse(c -> resultArray.add(findInValue));
                    }
                }));
            }
            else if (isReference(currentSegment)) {
                final Value referencedValue = getReferencedValue(array, currentSegment);
                if (referencedValue != null) {
                    result = findInValue(referencedValue, tail(path));
                }
                else {
                    result = null;
                }
            }
            // TODO: WDCD? copy_field('your.name','author[].name'), where name is an array
            else {
                result = Value.newArray(a -> array.forEach(v -> a.add(findInValue(v, path))));
            }
        }
        return result;

    }

    private Value findInValue(final Value value, final String[] p) {
        // TODO: move impl into enum elements, here call only value.find
        return p.length == 0 ? value : value == null ? null : value.extractType((m, c) -> m
                .ifArray(a -> c.accept(new FixPath(p).findIn(a)))
                .ifHash(h -> c.accept(new FixPath(p).findIn(h)))
                .orElse(c)
        );
    }

    @Override
    public String toString() {
        return String.join(".", path);
    }

    /*package-private*/ int size() {
        return path.length;
    }

    // TODO: this is still very much work in progress, I think we should
    // try to replace this with consistent usage of Value#getPath
    // (e.g. take care of handling repeated fields and their paths)

    /*package-private*/ FixPath to(final Value value, final int i) {
        final FixPath result;
        // One *, no matching path: replace with index of current result
        if (countAsterisks() == 1 && !matches(value.getPath())) {
            result = new FixPath(replaceInPath(ASTERISK, i));
        }
        // Multiple * or wildcards, value has a path: use the value's path
        else if (value.getPath() != null && hasWildcard()) {
            result = new FixPath(value.getPath());
        }
        else {
            result = this;
        }
        return result;
    }

    private boolean matches(final String thatPath) {
        return thatPath != null && thatPath.replaceAll("\\.\\d+\\.", ".*.").equals(String.join(".", this.path));
    }

    private String[] replaceInPath(final String find, final int i) {
        return Arrays.asList(path).stream().map(s -> s.equals(find) ? String.valueOf(i + 1) : s).toArray(String[]::new);
    }

    private boolean hasWildcard() {
        return Arrays.asList(path).stream().filter(s -> s.contains("*") || s.contains("?") || s.contains("|") || s.matches(".*?\\[.+?\\].*?")).findAny().isPresent();
    }

    private long countAsterisks() {
        return Arrays.asList(path).stream().filter(s -> s.equals(ASTERISK)).count();
    }

    /* package-private */ enum InsertMode {

        REPLACE {
            @Override
            void apply(final Hash hash, final String field, final Value value) {
                hash.put(field, value);
            }

            @Override
            void apply(final Array array, final String field, final Value value) {
                try {
                    final ReservedField reservedField = ReservedField.fromString(field);
                    if (reservedField != null) {
                        switch (reservedField) {
                            case $append:
                                array.add(value);
                                break;
                            case $first:
                                array.set(0, value);
                                break;
                            case $last:
                                array.set(array.size() - 1, value);
                                break;
                            default:
                                break;
                        }
                    }
                    else {
                        array.set(Integer.valueOf(field) - 1, value);
                    }
                }
                catch (final NumberFormatException e) {
                    throw new IllegalStateException("Expected Hash, got Array", e);
                }
            }
        },
        APPEND {
            @Override
            void apply(final Hash hash, final String field, final Value value) {
                hash.add(field, value);
            }

            @Override
            void apply(final Array array, final String field, final Value value) {
                array.add(value);
            }
        };

        abstract void apply(Hash hash, String field, Value value);

        abstract void apply(Array array, String field, Value newValue);

    }

    /*package-private*/ void removeNestedFrom(final Array array) {
        if (path.length >= 1 && path[0].equals(ASTERISK)) {
            array.removeAll();
        }
        else if (path.length >= 1 && Value.isNumber(path[0])) {
            final int index = Integer.parseInt(path[0]) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
            if (index >= 0 && index < array.size()) {
                if (path.length == 1) {
                    array.remove(index);
                }
                else {
                    removeNestedFrom(array.get(index));
                }
            }
        }
    }

    /*package-private*/ void removeNestedFrom(final Hash hash) {
        final String field = path[0];

        if (path.length == 1) {
            hash.remove(field);
        }
        else if (hash.containsField(field)) {
            removeNestedFrom(hash.get(field));
        }
    }

    private void removeNestedFrom(final Value value) {
        // TODO: impl and call just value.remove
        if (value != null) {
            value.matchType()
                .ifArray(a -> new FixPath(tail(path)).removeNestedFrom(a))
                .ifHash(h -> new FixPath(tail(path)).removeNestedFrom(h))
                .orElseThrow();
        }
    }

    /*package-private*/ private Value insertInto(final Array array, final InsertMode mode, final Value newValue) {
        // basic idea: reuse findIn logic here? setIn(findIn(array), newValue)
        final String field = path[0];
        if (path.length == 1) {
            mode.apply(array, field, newValue);
        }
        else {
            if (ASTERISK.equals(field)) {
                array.forEach(value -> insertInto(value, mode, newValue, field, tail(path)));
            }
            else if (isReference(field)) {
                insertInto(getReferencedValue(array, field), mode, newValue, field, tail(path));
            }
        }
        return new Value(array);
    }

    /*package-private*/ Value insertInto(final Hash hash, final InsertMode mode, final Value newValue) {
        // basic idea: reuse findIn logic here? setIn(findIn(hash), newValue)
        final String field = path[0];
        if (path.length == 1) {
            mode.apply(hash, field, newValue);
        }
        else {
            if (!hash.containsField(field)) {
                hash.put(field, Value.newHash());
            }
            insertInto(hash.get(field), mode, newValue, field, tail(path));
        }

        return new Value(hash);
    }

    private Value insertInto(final Value value, final InsertMode mode, final Value newValue, final String field,
            final String[] tail) {
        if (value != null) {
            final FixPath fixPath = new FixPath(tail);
            newValue.updatePathAddBase(value, field);
            return value.extractType((m, c) -> m
                    .ifArray(a -> c.accept(fixPath.insertInto(a, mode, newValue)))
                    .ifHash(h -> c.accept(fixPath.insertInto(h, mode, newValue)))
                    .orElseThrow());
        }
        else {
            throw new IllegalArgumentException("Using ref, but can't find: " + field + " in: " + value);
        }
    }

    private String[] tail(final String[] fields) {
        return Arrays.copyOfRange(fields, 1, fields.length);
    }

    /*package-private*/ enum ReservedField {
        $append, $first, $last;

        private static final Map<String, ReservedField> STRING_TO_ENUM = new HashMap<>();
        static {
            for (final ReservedField f : values()) {
                STRING_TO_ENUM.put(f.toString(), f);
            }
        }

        static ReservedField fromString(final String string) {
            return STRING_TO_ENUM.get(string);
        }
    }

    private boolean isReference(final String field) {
        return ReservedField.fromString(field) != null || Value.isNumber(field);
    }

    // TODO replace switch, extract to method on array?
    private Value getReferencedValue(final Array array, final String field) {
        Value referencedValue = null;
        if (Value.isNumber(field)) {
            final int index = Integer.valueOf(field) - 1;
            return 0 <= index && index < array.size() ? array.get(index) : null;
        }
        final ReservedField reservedField = ReservedField.fromString(field);
        if (reservedField != null) {
            switch (reservedField) {
                case $first:
                    referencedValue = getReferencedValue(array, "1");
                    break;
                case $last:
                    referencedValue = getReferencedValue(array, String.valueOf(array.size()));
                    break;
                case $append:
                    referencedValue = Value.newHash(); // TODO: append non-hash?
                    array.add(referencedValue);
                    referencedValue.updatePathAppend(String.valueOf(array.size()), "");
                    break;
                default:
                    break;
            }
        }
        return referencedValue;
    }

}
