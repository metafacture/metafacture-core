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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a record value, i.e., either an {@link Value.Array Array},
 * a {@link Value.Hash Hash}, or a {@link java.lang.String String}.
 */
public class Value {

    private final Value.Array array;
    private final Value.Hash hash;
    private final String string;

    private final Type type;

    public Value(final Value.Array array) {
        type = array != null ? Type.Array : null;

        this.array = array;
        this.hash = null;
        this.string = null;
    }

    public Value(final List<Value> array) {
        this(array != null ? new Value.Array() : null);

        if (array != null) {
            array.forEach(this.array::add);
        }
    }

    public Value(final Value.Hash hash) {
        type = hash != null ? Type.Hash : null;

        this.array = null;
        this.hash = hash;
        this.string = null;
    }

    public Value(final Map<String, Value> hash) {
        this(hash != null ? new Value.Hash() : null);

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

    public static Value newArray(final Consumer<Value.Array> consumer) {
        final Value.Array array = new Value.Array();

        if (consumer != null) {
            consumer.accept(array);
        }

        return new Value(array);
    }

    public static Value newHash() {
        return newHash(null);
    }

    public static Value newHash(final Consumer<Value.Hash> consumer) {
        final Value.Hash hash = new Value.Hash();

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

    public Value.Array asArray() {
        if (isArray()) {
            return array;
        }
        else {
            throw new IllegalStateException("expected array, got " + type);
        }
    }

    public Value.Hash asHash() {
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
         * Creates an empty instance of {@link Value.Array Array}.
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

    }

    /**
     * Represents a hash of metadata fields and values.
     */
    public static class Hash extends AbstractValueType {

        private final Map<String, Value> map = new LinkedHashMap<>();

        /**
         * Creates an empty instance of {@link Value.Hash Hash}.
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

        /**
         * Retrieves the field value from this hash.
         *
         * @param field the field name
         * @return the metadata value
         */
        public Value get(final String field) {
            return map.get(field);
        }

        /**
         * Removes the given field/value pair from this hash.
         *
         * @param field the field name
         */
        public void remove(final String field) {
            map.remove(field);
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
