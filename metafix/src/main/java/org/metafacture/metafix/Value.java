/*
 * Copyright 2021 hbz NRW
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a record value, i.e., either an {@link Array}, a {@link Hash},
 * or a {@link java.lang.String String}.
 */
public class Value {

    private static final String ASTERISK = "*";

    private final Array array;
    private final Hash hash;
    private final String string;

    private final Type type;

    public Value(final Array array) {
        type = array != null ? Type.Array : null;

        this.array = array;
        this.hash = null;
        this.string = null;
    }

    public Value(final List<Value> array) {
        this(array != null ? new Array() : null);

        if (array != null) {
            array.forEach(this.array::add);
        }
    }

    public Value(final Hash hash) {
        type = hash != null ? Type.Hash : null;

        this.array = null;
        this.hash = hash;
        this.string = null;
    }

    public Value(final Map<String, Value> hash) {
        this(hash != null ? new Hash() : null);

        if (hash != null) {
            hash.forEach(this.hash::put);
        }
    }

    public Value(final String string) {
        type = string != null ? Type.String : null;

        this.array = null;
        this.hash = null;
        this.string = string;
    }

    public static Value newArray() {
        return newArray(null);
    }

    public static Value newArray(final Consumer<Array> consumer) {
        final Array array = new Array();

        if (consumer != null) {
            consumer.accept(array);
        }

        return new Value(array);
    }

    public static Value newHash() {
        return newHash(null);
    }

    public static Value newHash(final Consumer<Hash> consumer) {
        final Hash hash = new Hash();

        if (consumer != null) {
            consumer.accept(hash);
        }

        return new Value(hash);
    }

    public boolean isArray() {
        return type == Type.Array;
    }

    public boolean isHash() {
        return type == Type.Hash;
    }

    public boolean isString() {
        return type == Type.String;
    }

    public boolean isNull() {
        final boolean result;

        if (type != null) {
            switch (type) {
                case Array:
                    result = array == null;
                    break;
                case Hash:
                    result = hash == null;
                    break;
                case String:
                    result = string == null;
                    break;
                default:
                    result = true;
            }
        }
        else {
            result = true;
        }

        return result;
    }

    public static boolean isNull(final Value value) {
        return value == null || value.isNull();
    }

    private static boolean isNumber(final String s) {
        return s.matches("\\d+");
    }

    public Array asArray() {
        if (isArray()) {
            return array;
        }
        else {
            throw new IllegalStateException("expected array, got " + type);
        }
    }

    public Hash asHash() {
        if (isHash()) {
            return hash;
        }
        else {
            throw new IllegalStateException("expected hash, got " + type);
        }
    }

    public String asString() {
        if (isString()) {
            return string;
        }
        else {
            throw new IllegalStateException("expected string, got " + type);
        }
    }

    public static Value asList(final Value value, final Consumer<Array> consumer) {
        return isNull(value) ? null : value.asList(consumer);
    }

    public Value asList(final Consumer<Array> consumer) {
        if (isArray()) {
            if (consumer != null) {
                consumer.accept(asArray());
            }

            return this;
        }
        else {
            return newArray(a -> {
                a.add(this);

                if (consumer != null) {
                    consumer.accept(a);
                }
            });
        }
    }

    public Value merge(final Value value) {
        return asList(a1 -> value.asList(a2 -> a2.forEach(a1::add)));
    }

    @Override
    public String toString() {
        final String result;

        if (!isNull()) {
            switch (type) {
                case Array:
                    result = array.asString();
                    break;
                case Hash:
                    result = hash.asString();
                    break;
                case String:
                    result = string;
                    break;
                default:
                    result = null;
            }
        }
        else {
            result = null;
        }

        return result;
    }

    static String[] tail(final String[] fields) {
        return Arrays.copyOfRange(fields, 1, fields.length);
    }

    enum Type {
        Array,
        Hash,
        String
    }

    private abstract static class AbstractValueType {

        @Override
        public String toString() {
            return asString();
        }

        public abstract String asString();

    }

    /**
     * Represents an array of metadata values.
     */
    public static class Array extends AbstractValueType {

        private final List<Value> list = new ArrayList<>();

        /**
         * Creates an empty instance of {@link Array}.
         */
        private Array() {
        }

        public void add(final Value value) {
            if (!isNull(value)) {
                list.add(value);
            }
        }

        public int size() {
            return list.size();
        }

        public Value get(final int index) {
            return list.get(index);
        }

        public Stream<Value> stream() {
            return list.stream();
        }

        public void forEach(final Consumer<Value> consumer) {
            list.forEach(consumer);
        }

        @Override
        public String asString() {
            return list.toString();
        }

        public void remove(final int index) {
            list.remove(index);
        }

        private void removeNested(final String[] fields) {
            if (fields.length >= 1 && fields[0].equals(ASTERISK)) {
                for (int i = 0; i < size(); ++i) {
                    remove(i);
                }
            }
            else if (fields.length >= 1 && isNumber(fields[0])) {
                final int index = Integer.parseInt(fields[0]) - 1;
                if (index >= 0 && index < size()) {
                    remove(index);
                }
            }
        }

        public Value find(final String[] fields) {
            Value result = null;
            if (fields.length > 0) {
                if (fields[0].equals(ASTERISK)) {
                    result = find(tail(fields));
                }
                else if (isNumber(fields[0])) {
                    final int index = Integer.parseInt(fields[0]) - 1;
                    if (index >= 0 && index < size()) {
                        final Value value = get(index);
                        // TODO: move impl into enum elements, here call only value.find
                        if (value != null) {
                            switch (value.type) {
                                case Hash:
                                    result = value.asHash().find(tail(fields));
                                    break;
                                case Array:
                                    result = find(tail(fields));
                                    break;
                                case String:
                                    result = value;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                else {
                    final Value newResult = newArray();
                    forEach(c -> {
                        newResult.asArray().add(c.asHash().find(fields[0])); /* TODO: non-hash */
                    });
                    result = newResult;
                }
            }
            else {
                result = new Value(this);
            }
            return result;
        }
    }

    /**
     * Represents a hash of metadata fields and values.
     */
    public static class Hash extends AbstractValueType {

        /*package-private*/ static final String APPEND_FIELD = "$append";
        private static final String LAST_FIELD = "$last";

        private static final String FIELD_PATH_SEPARATOR = "\\.";

        private final Map<String, Value> map = new LinkedHashMap<>();

        /**
         * Creates an empty instance of {@link Hash}.
         */
        protected Hash() {
        }

        /**
         * Checks whether this hash contains the metadata field.
         *
         * @param field the field name
         * @return true if this hash contains the metadata field, false otherwise
         */
        public boolean containsField(final String field) {
            return map.containsKey(field);
        }

        /**
         * Checks whether this hash is empty.
         *
         * @return true if this hash is empty, false otherwise
         */
        public boolean isEmpty() {
            return map.isEmpty();
        }

        /**
         * Gets the number of field/value pairs in this hash.
         *
         * @return the number of field/value pairs in this hash
         */
        public int size() {
            return map.size();
        }

        /**
         * Adds a field/value pair to this hash, provided it's not {@code null}.
         *
         * @param field the field name
         * @param value the metadata value
         */
        public void put(final String field, final Value value) {
            if (!isNull(value)) {
                map.put(field, value);
            }
        }

        /**
         * {@link #put(String, Value) Replaces} a field/value pair in this hash,
         * provided the field name is already {@link #containsField(String) present}.
         *
         * @param field the field name
         * @param value the metadata value
         */
        public void replace(final String field, final Value value) {
            if (containsField(field)) {
                put(field, value);
            }
        }

        public Value replace(final String fieldPath, final String newValue) {
            return insert(InsertMode.REPLACE, fieldPath, newValue);
        }

        public Value append(final String fieldPath, final String newValue) {
            return insert(InsertMode.APPEND, fieldPath, newValue);
        }

        /**
         * Retrieves the field value from this hash.
         *
         * @param field the field name
         * @return the metadata value
         */
        public Value get(final String field) {
            return map.get(field);
        }

        public Value find(final String fieldPath) {
            return find(split(fieldPath));
        }

        private Value find(final String[] fields) {
            final String field = fields[0];

            return fields.length == 1 || !containsField(field) ? get(field) :
                findNested(field, tail(fields));
        }

        private Value findNested(final String field, final String[] remainingFields) {
            final Value value = get(field);
            Value result = null;
            if (value != null) {
                switch (value.type) {
                    case Array:
                        if (remainingFields[0].equals(ASTERISK)) {
                            result = value.asArray().find(tail(remainingFields));
                        }
                        else {
                            result =  value.asArray().find(remainingFields);
                        }
                        break;
                    case Hash:
                        if (remainingFields[0].equals(ASTERISK)) {
                            result = value.asHash().find(tail(remainingFields));
                        }
                        else {
                            result = value.asHash().find(remainingFields);
                        }
                        break;
                    case String:
                        throw new IllegalStateException("expected string, got " + value.type);
                    default:
                        throw new IllegalStateException("unexpected, got " + value.type);
                }
            }
            return result;
        }

        public Value findList(final String fieldPath, final Consumer<Array> consumer) {
            return asList(find(fieldPath), consumer);
        }

        public Value getList(final String field, final Consumer<Array> consumer) {
            return asList(get(field), consumer);
        }

        private String[] split(final String fieldPath) {
            return fieldPath.split(FIELD_PATH_SEPARATOR);
        }

        public void addAll(final String field, final List<String> values) {
            values.forEach(value -> add(field, new Value(value)));
        }

        public void addAll(final Hash hash) {
            hash.forEach(this::add);
        }

        public void add(final String field, final Value newValue) {
            final Value oldValue = get(field);
            put(field, oldValue == null ? newValue : oldValue.merge(newValue));
        }

        public Value insert(final InsertMode mode, final String fieldPath, final String newValue) {
            return insert(mode, split(fieldPath), newValue);
        }

        private Value insert(final InsertMode mode, final String[] fields, final String newValue) {
            final String field = fields[0];

            if (fields.length == 1 && !fields[0].equals(ASTERISK)) {
                mode.apply(this, field, newValue);
            }
            else {
                if (!containsField(field)) {
                    put(field, newHash());
                }

                final Value value = get(field);
                if (value != null) {
                    switch (value.type) {
                        // TODO: move impl into enum elements, here call only value.insert
                        case Hash:
                            final String[] tail = tail(fields);
                            final String[] rest = tail[0].startsWith("$") ? tail(tail) : tail; // TODO: why?
                            value.asHash().insert(mode, rest, newValue);
                            break;
                        case Array:
                            insertArray(mode, newValue, tail(fields), value.asArray());
                            break;
                        case String:
                            throw new IllegalStateException("expected array or hash, got " + value.type);
                        default:
                            throw new IllegalStateException("expected array or hash, got " + value.type);
                    }
                }
            }

            return new Value(this);
        }

        private void insertArray(final InsertMode mode, final String newValue, final String[] fields,
                final Array array) {
            switch (fields[0]) {
                case ASTERISK:
                    break;
                case APPEND_FIELD:
                    array.add(newHash(h -> h.insert(mode, tail(fields), newValue)));
                    break;
                case LAST_FIELD:
                    if (array.size() > 0) {
                        final Value last = array.get(array.size() - 1);
                        if (last.isHash()) {
                            last.asHash().insert(mode, tail(fields), newValue);
                        }
                    }
                    break;
                default:
                    if (isNumber(fields[0])) {
                        if (fields.length == 1) {
                            array.add(new Value(newValue));
                        }
                        if (fields.length > 1) {
                            final Value newHash = Value.newHash();
                            newHash.asHash().put(fields[1], new Value(newValue));
                            array.add(newHash);
                        }
                    }
                    else {
                        final String[] rem = fields;
                        array.add(newHash(h -> h.insert(mode, rem, newValue)));
                    }
                    break;
            }
        }

        /**
         * Removes the given field/value pair from this hash.
         *
         * @param field the field name
         */
        public void remove(final String field) {
            map.remove(field);
        }

        public void removeNested(final String fieldPath) {
            removeNested(split(fieldPath));
        }

        private void removeNested(final String[] fields) {
            final String field = fields[0];

            if (fields.length == 1) {
                remove(field);
            }
            else if (containsField(field)) {
                final Value value = get(field);
                // TODO: impl and call just value.remove
                if (value != null) {
                    switch (value.type) {
                        case String:
                            break;
                        case Array:
                            value.asArray().removeNested(tail(fields));
                            break;
                        case Hash:
                            value.asHash().removeNested(tail(fields));
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        public void copy(final List<String> params) {
            final String oldName = params.get(0);
            final String newName = params.get(1);
            findList(oldName, a -> a.forEach(v -> appendValue(split(newName), v)));
        }

        private void appendValue(final String[] newName, final Value v) {
            // TODO: impl and call just value.append
            if (v != null) {
                switch (v.type) {
                    case String:
                        append(Arrays.asList(newName).stream().collect(Collectors.joining(".")), v.asString());
                        break;
                    case Array:
                        break;
                    case Hash:
                        appendValue(newName, v.asHash().find(tail(newName)));
                        break;
                    default:
                        break;
                }
            }
        }

        public void transformFields(final List<String> params, final UnaryOperator<String> operator) {
            final String field = params.get(0);
            final Value value = find(field);
            if (value != null) {
                removeNested(field.replace(".*", ""));
                if (operator != null) {
                    value.asList(a -> a.forEach(v -> append(field.replace(".*", ""), operator.apply(v.toString()))));
                }
            }
        }

        /**
         * Retains only the given field/value pairs in this hash.
         *
         * @param fields the field names
         */
        public void retainFields(final Collection<String> fields) {
            map.keySet().retainAll(fields);
        }

        /**
         * Removes all field/value pairs from this hash whose value is empty.
         */
        public void removeEmptyValues() {
            // TODO:
            //
            // - Remove empty arrays/hashes?
            // - Remove empty strings(/arrays/hashes) recursively?
            //
            // => Compare Catmandu behaviour
            map.values().removeIf(v -> v.isString() && v.asString().isEmpty());
        }

        /**
         * Iterates over all field/value pairs in this hash.
         *
         * @param consumer the action to be performed for each field/value pair
         */
        public void forEach(final BiConsumer<String, Value> consumer) {
            map.forEach(consumer);
        }

        @Override
        public String asString() {
            return map.toString();
        }

        private enum InsertMode {

            REPLACE {
                @Override
                void apply(final Hash hash, final String field, final String value) {
                    hash.put(field, new Value(value));
                }
            },
            APPEND {
                @Override
                void apply(final Hash hash, final String field, final String value) {
                    final Value oldValue = hash.get(field);
                    final Value newValue = new Value(value);
                    hash.put(field, oldValue == null ? newValue : oldValue.merge(newValue));
                }
            };

            abstract void apply(Hash hash, String field, String value);

        }

    }
}
