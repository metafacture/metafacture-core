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

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

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

    public FixPath(final String[] path) {
        this.path = path;
    }

    public FixPath(final String path) {
        this(Value.split(path));
    }

    public Value findInArray(final Value.Array array) {
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
                .ifArray(a -> c.accept(new FixPath(p).findInArray(a)))
                .ifHash(h -> c.accept(new FixPath(p).findInHash(h)))
                .orElse(c)
        );
    }

    private String[] tail(final String[] fields) {
        return Arrays.copyOfRange(fields, 1, fields.length);
    }

    public void transformInArray(final Array array, final UnaryOperator<String> operator) {
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
                transformValueAt(array, i, tail(path), operator);
            }
        }

        array.getList().removeIf(v -> Value.isNull(v));
    }

    private void transformValueAt(final Array array, final int index, final String[] p, final UnaryOperator<String> operator) {
        final Value value = array.get(index);
        if (value != null) {
            value.matchType()
                .ifString(s -> array.set(index, operator != null ? new Value(operator.apply(s)) : null))
                .orElse(v -> new Value.TypeMatcher(v)
                        .ifArray(a -> new FixPath(p).transformInArray(a, operator))
                        .ifHash(h -> new FixPath(p).transformInHash(h, operator))
                        .orElseThrow());
        }
    }

    public Value findInHash(final Hash hash) {
        final String field = path[0];
        if (field.equals(ASTERISK)) {
            // TODO: search in all elements of value.asHash()?
            return new FixPath(tail(path)).findInHash(hash);
        }
        return path.length == 1 || !hash.containsField(field) ? hash.get(field) :
            findNested(hash, field, tail(path));
    }

    private Value findNested(final Hash hash, final String field, final String[] remainingFields) {
        final Value value = hash.get(field);
        return value == null ? null : value.extractType((m, c) -> m
                .ifArray(a -> c.accept(new FixPath(remainingFields).findInArray(a)))
                .ifHash(h -> c.accept(new FixPath(remainingFields).findInHash(h)))
                .orElseThrow()
        );
    }

    public void transformInHash(final Hash hash, final BiConsumer<TypeMatcher, Consumer<Value>> consumer) {
        final Value oldValue = findInHash(hash);

        if (oldValue != null) {
            final Value newValue = oldValue.extractType(consumer);

            if (newValue != null) {
                new FixPath(path).insertIntoHash(hash, InsertMode.REPLACE, newValue);
            }
        }
    }

    public void transformInHash(final Hash hash, final UnaryOperator<String> operator) {
        final String currentSegment = path[0];
        final String[] remainingPath = tail(path);

        if (currentSegment.equals(ASTERISK)) {
            // TODO: search in all elements of value.asHash()?
            new FixPath(remainingPath).transformInHash(hash, operator);
            return;
        }

        hash.modifyFields(currentSegment, f -> {
            final Value value = hash.getMap().get(f);

            if (value != null) {
                if (remainingPath.length == 0) {
                    hash.getMap().remove(f);

                    if (operator != null) {
                        value.matchType()
                            .ifString(s -> new FixPath(f).appendIn(hash, operator.apply(s)))
                            .orElseThrow();
                    }
                }
                else {
                    new TypeMatcher(value)
                        .ifArray(a -> new FixPath(remainingPath).transformInArray(a, operator))
                        .ifHash(h -> new FixPath(remainingPath).transformInHash(h, operator))
                        .orElseThrow();
                }
            }
        });
    }

    /* package-protected */ void insertIntoArray(final Array a, final InsertMode mode, final Value newValue) {
        if (path[0].equals(ASTERISK)) {
            return; // TODO: WDCD? descend into the array?
        }
        if (ReservedField.fromString(path[0]) == null) {
            processDefault(a, mode, newValue);
        }
        else {
            insertIntoReferencedObject(a, mode, newValue);
        }
    }

    private void insertIntoReferencedObject(final Array a, final InsertMode mode, final Value newValue) {
        // TODO replace switch, extract to enum behavior like reservedField.insertIntoReferencedObject(this)?
        switch (ReservedField.fromString(path[0])) {
            case $append:
                if (path.length == 1) {
                    a.add(newValue);
                }
                else {
                    a.add(Value.newHash(h -> new FixPath(tail(path)).insertIntoHash(h, mode, newValue)));
                }
                break;
            case $last:
                if (a.size() > 0) {
                    a.get(a.size() - 1).matchType().ifHash(h -> new FixPath(tail(path)).insertIntoHash(h, mode, newValue));
                }
                break;
            case $first:
                if (a.size() > 0) {
                    final Value first = a.get(0);
                    if (first.isHash()) {
                        new FixPath(tail(path)).insertIntoHash(first.asHash(), mode, newValue);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void processDefault(final Array a, final InsertMode mode, final Value newValue) {
        if (Value.isNumber(path[0])) {
            // TODO: WDCD? insert at the given index? also descend into the array?
            if (path.length == 1) {
                a.add(newValue);
            }
            else if (path.length > 1) {
                final Value newHash;
                final int index = Integer.parseInt(path[0]);
                if (index <= a.size()) {
                    newHash = a.get(index - 1);
                }
                else {
                    newHash = Value.newHash();
                    a.add(newHash);
                }
                mode.apply(newHash.asHash(), path[1], newValue);
            }
        }
        else {
            a.add(Value.newHash(h -> new FixPath(path).insertIntoHash(h, mode, newValue)));
        }
    }

    /*package-private*/ Value insertIntoHash(final Hash hash, final InsertMode mode, final Value newValue) {
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
                    .ifArray(a -> new FixPath(tail).insertIntoArray(a, mode, newValue))
                    .ifHash(h -> new FixPath(tail).insertIntoHash(h, mode, newValue))
                    .orElseThrow();
            }
        }

        return new Value(hash);
    }

    private Value processRef(final Hash hash, final InsertMode mode, final Value newValue, final String field, final String[] tail) {
        final Value referencedValue = getReferencedValue(hash, field);
        if (referencedValue != null) {
            return new FixPath(tail).insertIntoHash(referencedValue.asHash(), mode, newValue);
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

    @Override
    public String toString() {
        return Arrays.asList(path).toString();
    }

    protected enum InsertMode {

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

    public Value replaceIn(final Hash hash, final String newValue) {
        return new FixPath(path).insertIntoHash(hash, InsertMode.REPLACE, new Value(newValue));
    }

    public Value appendIn(final Hash hash, final String newValue) {
        return new FixPath(path).insertIntoHash(hash, InsertMode.APPEND, new Value(newValue));
    }

    /*package-protected*/ void removeNestedFromArray(final Array array) {
        if (path.length >= 1 && path[0].equals(ASTERISK)) {
            array.getList().clear();
        }
        else if (path.length >= 1 && Value.isNumber(path[0])) {
            final int index = Integer.parseInt(path[0]) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
            if (index >= 0 && index < array.size()) {
                array.remove(index);
            }
        }
    }

    /*package-protected*/ void removeNestedFromHash(final Hash hash) {
        final String field = path[0];

        if (path.length == 1) {
            hash.remove(field);
        }
        else if (hash.containsField(field)) {
            final Value value = hash.get(field);
            // TODO: impl and call just value.remove
            if (value != null) {
                value.matchType()
                    .ifArray(a -> new FixPath(tail(path)).removeNestedFromArray(a))
                    .ifHash(h -> new FixPath(tail(path)).removeNestedFromHash(h))
                    .orElseThrow();
            }
        }
    }

}
