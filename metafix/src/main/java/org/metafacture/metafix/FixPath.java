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
import org.metafacture.metafix.Value.ReservedField;
import org.metafacture.metafix.Value.TypeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Our goal here is something like https://metacpan.org/pod/Catmandu::Path::simple
 *
 * With all get/set/update/create/delete logic collected here.
 *
 * @author Fabian Steeg (fsteeg)
 *
 */
public class FixPath {

    private static final String ASTERISK = "*";
    private String[] path;

    public FixPath(final String path) {
        this(Value.split(path));
    }

    private FixPath(final String[] path) {
        this.path = path;
    }

    public Value findIn(final Hash hash) {
        final String currentSegment = path[0];
        final String[] remainingPath = tail(path);

        if (currentSegment.equals(ASTERISK)) {
            // TODO: search in all elements of value.asHash()?
            return new FixPath(remainingPath).findIn(hash);
        }
        final List<Value> result = new ArrayList<Value>();
        hash.findFields(currentSegment).collect(Collectors.toSet()).forEach(f -> {
            final Value value = hash.getField(f);

            if (value != null) {
                if (remainingPath.length == 0) {
                    result.add(value);
                }
                else {
                    value.matchType()
                        .ifArray(a -> result.add(new FixPath(remainingPath).findIn(a)))
                        .ifHash(h -> result.add(new FixPath(remainingPath).findIn(h)))
                        .orElseThrow();
                }
            }
        });
        return path.length == 1 ? hash.get(currentSegment) :
               result.size() == 1 ? result.get(0) :
               result.size() == 0 ? null : new Value(result);
    }

    /*package-private*/ Value findIn(final Array array) {
        final Value result;
        if (path.length > 0) {
            final String currentSegment = path[0];
            if (currentSegment.equals(ASTERISK)) {
                result = Value.newArray(a -> array.forEach(v -> a.add(findInValue(v, tail(path)))));
            }
            else if (Value.isNumber(currentSegment)) {
                final int index = Integer.parseInt(currentSegment) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
                if (index >= 0 && index < array.size()) {
                    result = findInValue(array.get(index), tail(path));
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
        else {
            result = new Value(array);
        }
        return result;
    }

    private Value findInValue(final Value value, final String[] p) {
        // TODO: move impl into enum elements, here call only value.find
        return value == null ? null : value.extractType((m, c) -> m
                .ifArray(a -> c.accept(new FixPath(p).findIn(a)))
                .ifHash(h -> c.accept(new FixPath(p).findIn(h)))
                .orElse(c)
        );
    }

    public Value replaceIn(final Hash hash, final String newValue) {
        return new FixPath(path).insertInto(hash, InsertMode.REPLACE, new Value(newValue));
    }

    public Value appendIn(final Hash hash, final String newValue) {
        return new FixPath(path).insertInto(hash, InsertMode.APPEND, new Value(newValue));
    }

    /*package-private*/ void appendIn(final Hash hash, final Value v) {
        // TODO: impl and call just value.append
        if (v != null) {
            v.matchType()
                .ifString(s -> appendIn(hash, s))
                //.ifArray(a -> /* TODO: see MetafixMethodTest.moveToNestedArray */)
                .ifHash(h -> {
                    if (path.length == 1) {
                        hash.add(path[0], v);
                    }
                    else {
                        appendIn(hash, new FixPath(tail(path)).findIn(h));
                    }
                })
                .orElseThrow();
        }
    }

    @Override
    public String toString() {
        return Arrays.asList(path).toString();
    }

    public void transformIn(final Hash hash, final UnaryOperator<String> operator) {
        final String currentSegment = path[0];
        final String[] remainingPath = tail(path);

        if (currentSegment.equals(ASTERISK)) {
            // TODO: search in all elements of value.asHash()?
            new FixPath(remainingPath).transformIn(hash, operator);
            return;
        }

        hash.findFields(currentSegment).collect(Collectors.toSet()).forEach(f -> {
            final Value value = hash.getField(f);

            if (value != null) {
                if (remainingPath.length == 0) {
                    hash.removeField(f);

                    if (operator != null) {
                        value.matchType()
                            .ifString(s -> new FixPath(f).appendIn(hash, operator.apply(s)))
                            .orElseThrow();
                    }
                }
                else {
                    value.matchType()
                        .ifArray(a -> new FixPath(remainingPath).transformIn(a, operator))
                        .ifHash(h -> new FixPath(remainingPath).transformIn(h, operator))
                        .orElseThrow();
                }
            }
        });
    }

    /*package-private*/ void transformIn(final Hash hash, final BiConsumer<TypeMatcher, Consumer<Value>> consumer) {
        final Value oldValue = findIn(hash);

        if (oldValue != null) {
            final Value newValue = oldValue.extractType(consumer);

            if (newValue != null) {
                new FixPath(path).insertInto(hash, InsertMode.REPLACE, newValue);
            }
        }
    }

    /*package-private*/ void transformIn(final Array array, final UnaryOperator<String> operator) {
        final String currentSegment = path[0];
        final int size = array.size();

        if (path.length == 0 || currentSegment.equals(ASTERISK)) {
            for (int i = 0; i < size; ++i) {
                transformValueAt(array, i, tail(path), operator);
            }
        }
        else if (Value.isNumber(currentSegment)) {
            final int index = Integer.parseInt(currentSegment) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
            if (index >= 0 && index < size) {
                transformValueAt(array, index, tail(path), operator);
            }
        }
        // TODO: WDCD? copy_field('your.name','author[].name'), where name is an array
        else {
            for (int i = 0; i < size; ++i) {
                transformValueAt(array, i, path, operator);
            }
        }

        array.removeIf(v -> Value.isNull(v));
    }

    private enum InsertMode {

        REPLACE {
            @Override
            void apply(final Hash hash, final String field, final Value value) {
                hash.put(field, value);
            }
        },
        APPEND {
            @Override
            void apply(final Hash hash, final String field, final Value value) {
                hash.add(field, value);
            }
        };

        abstract void apply(Hash hash, String field, Value value);

    }

    /*package-private*/ void removeNestedFrom(final Array array) {
        if (path.length >= 1 && path[0].equals(ASTERISK)) {
            array.removeAll();
        }
        else if (path.length >= 1 && Value.isNumber(path[0])) {
            final int index = Integer.parseInt(path[0]) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
            if (index >= 0 && index < array.size()) {
                array.remove(index);
            }
        }
    }

    /*package-private*/ void removeNestedFrom(final Hash hash) {
        final String field = path[0];

        if (path.length == 1) {
            hash.remove(field);
        }
        else if (hash.containsField(field)) {
            final Value value = hash.get(field);
            // TODO: impl and call just value.remove
            if (value != null) {
                value.matchType()
                    .ifArray(a -> new FixPath(tail(path)).removeNestedFrom(a))
                    .ifHash(h -> new FixPath(tail(path)).removeNestedFrom(h))
                    .orElseThrow();
            }
        }
    }

    /*package-private*/ void insertInto(final Array array, final InsertMode mode, final Value newValue) {
        if (path[0].equals(ASTERISK)) {
            return; // TODO: WDCD? descend into the array?
        }
        if (ReservedField.fromString(path[0]) == null) {
            processDefault(array, mode, newValue);
        }
        else {
            insertIntoReferencedObject(array, mode, newValue);
        }
    }

    /*package-private*/ Value insertInto(final Hash hash, final InsertMode mode, final Value newValue) {
        final String field = path[0];
        if (path.length == 1) {
            if (field.equals(ASTERISK)) {
                //TODO: WDCD? insert into each element?
            }
            else {
                mode.apply(hash, field, newValue);
            }
        }
        else {
            final String[] tail = tail(path);
            if (ReservedField.fromString(field) != null || Value.isNumber(field)) {
                return processRef(hash, mode, newValue, field, tail);
            }
            if (!hash.containsField(field)) {
                hash.put(field, Value.newHash());
            }
            final Value value = hash.get(field);
            if (value != null) {
                // TODO: move impl into enum elements, here call only value.insert
                value.matchType()
                    .ifArray(a -> new FixPath(tail).insertInto(a, mode, newValue))
                    .ifHash(h -> new FixPath(tail).insertInto(h, mode, newValue))
                    .orElseThrow();
            }
        }

        return new Value(hash);
    }

    private String[] tail(final String[] fields) {
        return Arrays.copyOfRange(fields, 1, fields.length);
    }

    private void transformValueAt(final Array array, final int index, final String[] p, final UnaryOperator<String> operator) {
        final Value value = array.get(index);
        if (value != null) {
            value.matchType()
                .ifString(s -> array.set(index, operator != null ? new Value(operator.apply(s)) : null))
                .orElse(v -> v.matchType()
                        .ifArray(a -> new FixPath(p).transformIn(a, operator))
                        .ifHash(h -> new FixPath(p).transformIn(h, operator))
                        .orElseThrow()
                );
        }
    }

    private void insertIntoReferencedObject(final Array array, final InsertMode mode, final Value newValue) {
        // TODO replace switch, extract to enum behavior like reservedField.insertIntoReferencedObject(this)?
        switch (ReservedField.fromString(path[0])) {
            case $append:
                if (path.length == 1) {
                    array.add(newValue);
                }
                else {
                    array.add(Value.newHash(h -> new FixPath(tail(path)).insertInto(h, mode, newValue)));
                }
                break;
            case $last:
                if (array.size() > 0) {
                    array.get(array.size() - 1).matchType().ifHash(h -> new FixPath(tail(path)).insertInto(h, mode, newValue));
                }
                break;
            case $first:
                if (array.size() > 0) {
                    final Value first = array.get(0);
                    if (first.isHash()) {
                        new FixPath(tail(path)).insertInto(first.asHash(), mode, newValue);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void processDefault(final Array array, final InsertMode mode, final Value newValue) {
        if (Value.isNumber(path[0])) {
            // TODO: WDCD? insert at the given index? also descend into the array?
            if (path.length == 1) {
                array.add(newValue);
            }
            else if (path.length > 1) {
                final Value newHash;
                final int index = Integer.parseInt(path[0]);
                if (index <= array.size()) {
                    newHash = array.get(index - 1);
                }
                else {
                    newHash = Value.newHash();
                    array.add(newHash);
                }
                mode.apply(newHash.asHash(), path[1], newValue);
            }
        }
        else {
            array.add(Value.newHash(h -> new FixPath(path).insertInto(h, mode, newValue)));
        }
    }

    private Value processRef(final Hash hash, final InsertMode mode, final Value newValue, final String field, final String[] tail) {
        final Value referencedValue = getReferencedValue(hash, field);
        if (referencedValue != null) {
            return new FixPath(tail).insertInto(referencedValue.asHash(), mode, newValue);
        }
        else {
            throw new IllegalArgumentException("Using ref, but can't find: " + field + " in: " + hash);
        }
    }

    private Value getReferencedValue(final Hash hash, final String field) {
        Value referencedValue = null;
        final ReservedField reservedField = ReservedField.fromString(field);
        if (reservedField == null) {
            return hash.get(field);
        }
        // TODO replace switch, extract to enum behavior like reservedField.getReferencedValueInHash(this)?
        switch (reservedField) {
            case $first:
                referencedValue = hash.get("1");
                break;
            case $last:
                referencedValue = hash.get(String.valueOf(hash.size()));
                break;
            case $append:
                referencedValue = new Value(hash);
                break;
            default:
                break;
        }
        return referencedValue;
    }
}
