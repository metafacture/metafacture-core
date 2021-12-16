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
import java.util.stream.Stream;

/**
 * Represents a record value, i.e., either an {@link Array}, a {@link Hash},
 * or a {@link java.lang.String String}.
 */
public class Value {

    /*package-private*/ static final String APPEND_FIELD = "$append";
    private static final String LAST_FIELD = "$last";
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

    private static String[] tail(final String[] fields) {
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

        protected enum InsertMode {

            REPLACE {
                @Override
                void apply(final Hash hash, final String field, final String value) {
                    hash.put(field, new Value(value));
                }
            },
            APPEND {
                @Override
                void apply(final Hash hash, final String field, final String value) {
                    hash.add(field, new Value(value));
                }
            },
            /* For an indexed representation of arrays as hashes with 1, 2, 3 etc. keys.
             * i.e. ["a", "b", "c"] as { "1":"a", "2":"b", "3": "c" }
             * This is what is produced by JsonDecoder and Metafix itself for arrays.
             * TODO? maybe this would be a good general internal representation, resulting
             * in every value being either a hash or a string, no more separate array type.*/
            INDEXED {
                @Override
                void apply(final Hash hash, final String field, final String value) {
                    final Value newValue = field.equals(APPEND_FIELD) ? new Value(value) :
                        newHash(h -> h.put(field, new Value(value)));
                    hash.add(nextIndex(hash), newValue);
                }

                private String nextIndex(final Hash hash) {
                    return "" + (hash.size() + 1) /* TODO? check if keys are actually all ints? */;
                }
            };

            abstract void apply(Hash hash, String field, String value);

        }

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
                list.clear();
            }
            else if (fields.length >= 1 && isNumber(fields[0])) {
                final int index = Integer.parseInt(fields[0]) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
                if (index >= 0 && index < size()) {
                    remove(index);
                }
            }
        }

        private Value find(final String[] path) {
            final Value result;
            if (path.length > 0) {
                if (path[0].equals(ASTERISK)) {
                    result = newArray(a -> forEach(v -> a.add(findInValue(tail(path), v))));
                }
                else if (isNumber(path[0])) {
                    final int index = Integer.parseInt(path[0]) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
                    if (index >= 0 && index < size()) {
                        result = findInValue(tail(path), get(index));
                    }
                    else {
                        result = null;
                    }
                }
                // TODO: WDCD? copy_field('your.name','author[].name'), where name is an array
                else {
                    result = newArray(a -> forEach(v -> a.add(findInValue(path, v))));
                }
            }
            else {
                result = new Value(this);
            }
            return result;
        }

        private Value findInValue(final String[] path, final Value value) {
            final Value result;
            // TODO: move impl into enum elements, here call only value.find
            if (value != null) {
                switch (value.type) {
                    case Hash:
                        result = value.asHash().find(path);
                        break;
                    case Array:
                        result = value.asArray().find(path);
                        break;
                    case String:
                        result = value;
                        break;
                    default:
                        result = null;
                        break;
                }
            }
            else {
                result = null;
            }
            return result;
        }

        private void insert(final InsertMode mode, final String[] fields, final String newValue) {
            switch (fields[0]) {
                case ASTERISK:
                    // TODO: WDCD? descend into the array?
                    break;
                case APPEND_FIELD:
                    if (fields.length == 1) {
                        add(new Value(newValue));
                        return;
                    }
                    add(newHash(h -> h.insert(mode, tail(fields), newValue)));
                    break;
                case LAST_FIELD:
                    if (size() > 0) {
                        final Value last = get(size() - 1);
                        if (last.isHash()) {
                            last.asHash().insert(mode, tail(fields), newValue);
                        }
                    }
                    break;
                default:
                    if (isNumber(fields[0])) {
                        // TODO: WDCD? insert at the given index? also descend into the array?
                        if (fields.length == 1) {
                            add(new Value(newValue));
                        }
                        if (fields.length > 1) {
                            final Value newHash = Value.newHash();
                            mode.apply(newHash.asHash(), fields[1], newValue);
                            add(newHash);
                        }
                    }
                    else {
                        add(newHash(h -> h.insert(mode, fields, newValue)));
                    }
                    break;
            }
        }

        /*package-private*/ void set(final int index, final Value value) {
            list.set(index, value);
        }

    }

    /**
     * Represents a hash of metadata fields and values.
     */
    public static class Hash extends AbstractValueType {

        private static final String FIELD_PATH_SEPARATOR = "\\.";

        private static final String UNEXPECTED = "expected array or hash, got ";

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
         * Adds a field/value pair to this hash, provided it's not {@link #isNull(Value) null}.
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
            if (field.equals(ASTERISK)) {
                // TODO: search in all elements of value.asHash()?
                return find(tail(fields));
            }
            return fields.length == 1 || !containsField(field) ? get(field) :
                findNested(field, tail(fields));
        }

        private Value findNested(final String field, final String[] remainingFields) {
            final Value value = get(field);
            Value result = null;
            if (value != null) {
                switch (value.type) {
                    case Array:
                        result = value.asArray().find(remainingFields);
                        break;
                    case Hash:
                        result = value.asHash().find(remainingFields);
                        break;
                    default:
                        throw new IllegalStateException(UNEXPECTED + value.type);
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

        /**
         * {@link #put(String, Value) Adds} a field/value pair to this hash,
         * potentially merging with an existing value.
         *
         * @param field the field name
         * @param newValue the new metadata value
         */
        public void add(final String field, final Value newValue) {
            final Value oldValue = get(field);
            put(field, oldValue == null ? newValue : oldValue.asList(a1 -> newValue.asList(a2 -> a2.forEach(a1::add))));
        }

        public Value insert(final InsertMode mode, final String fieldPath, final String newValue) {
            return insert(mode, split(fieldPath), newValue);
        }

        private Value insert(final InsertMode mode, final String[] fields, final String newValue) {
            final String field = fields[0];
            if (fields.length == 1) {
                if (field.equals(ASTERISK)) {
                    //TODO: WDCD? insert into each element?
                }
                else {
                    mode.apply(this, field, newValue);
                }
            }
            else {
                if (field.equals(APPEND_FIELD) || field.equals(LAST_FIELD)) {
                    // TODO: WDCD? $last, $append skipped for hashes here:
                    return insert(mode, tail(fields), newValue);
                }
                if (!containsField(field)) {
                    put(field, newHash());
                }
                final Value value = get(field);
                if (value != null) {
                    switch (value.type) {
                        // TODO: move impl into enum elements, here call only value.insert
                        case Hash:
                            // if the field is marked as array, this hash should be smth. like { 1=a, 2=b }
                            final boolean isIndexedArray = field.endsWith(Metafix.ARRAY_MARKER);
                            value.asHash().insert(isIndexedArray ? InsertMode.INDEXED : mode, tail(fields), newValue);
                            break;
                        case Array:
                            value.asArray().insert(mode, tail(fields), newValue);
                            break;
                        default:
                            throw new IllegalStateException(UNEXPECTED + value.type);
                    }
                }
            }

            return new Value(this);
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
                            throw new IllegalStateException(UNEXPECTED + value.type);
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
                        append(String.join(".", newName), v.asString());
                        break;
                    case Array:
                        // TODO: do something here?
                        break;
                    case Hash:
                        if (newName.length == 1) {
                            add(newName[0], v);
                        }
                        else {
                            appendValue(newName, v.asHash().find(tail(newName)));
                        }
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
                removeNested(field);
                if (operator != null) {
                    value.asList(a -> a.forEach(v -> append(field, operator.apply(v.toString()))));
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

    }

}
