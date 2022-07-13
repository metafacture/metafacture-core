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

import org.metafacture.commons.tries.SimpleRegexTrie;
import org.metafacture.commons.tries.WildcardTrie;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a record value, i.e., either an {@link Array}, a {@link Hash},
 * or a {@link String}.
 */
public class Value { // checkstyle-disable-line ClassDataAbstractionCoupling

    private static final String FIELD_PATH_SEPARATOR = "\\.";

    private final Array array;
    private final Hash hash;
    private final String string;

    private final Type type;

    private String path;

    private Value(final Type type, final Array array, final Hash hash, final String string) {
        final boolean hasValue = array != null || hash != null || string != null;

        if (type == null) {
            if (hasValue) {
                throw new IllegalArgumentException("Value without type");
            }
        }
        else {
            if (!hasValue) {
                throw new IllegalArgumentException("Type without value");
            }
        }

        this.type = type;
        this.array = array;
        this.hash = hash;
        this.string = string;
    }

    public Value(final Array array) {
        this(array != null ? Type.Array : null, array, null, null);
    }

    public Value(final List<Value> array) {
        this(array != null ? new Array() : null);

        if (array != null) {
            array.forEach(this.array::add);
        }
    }

    public Value(final Hash hash) {
        this(hash != null ? Type.Hash : null, null, hash, null);
    }

    public Value(final Map<String, Value> hash) {
        this(hash != null ? new Hash() : null);

        if (hash != null) {
            hash.forEach(this.hash::put);
        }
    }

    public Value(final String string) {
        this(string != null ? Type.String : null, null, null, string);
    }

    public Value(final int integer) {
        this(String.valueOf(integer));
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
        return isType(Type.Array);
    }

    public boolean isHash() {
        return isType(Type.Hash);
    }

    public boolean isString() {
        return isType(Type.String);
    }

    private boolean isType(final Type targetType) {
        return type == targetType;
    }

    public boolean isNull() {
        return isType(null);
    }

    public static boolean isNull(final Value value) {
        return value == null || value.isNull();
    }

    /*package-private*/ static boolean isNumber(final String s) {
        return s.matches("\\d+");
    }

    public Array asArray() {
        return extractType((m, c) -> m.ifArray(c).orElseThrow());
    }

    public Hash asHash() {
        return extractType((m, c) -> m.ifHash(c).orElseThrow());
    }

    public String asString() {
        return extractType((m, c) -> m.ifString(c).orElseThrow());
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

    public TypeMatcher matchType() {
        return new TypeMatcher(this);
    }

    public <T> T extractType(final BiConsumer<TypeMatcher, Consumer<T>> consumer) {
        final AtomicReference<T> result = new AtomicReference<>();
        consumer.accept(matchType(), result::set);
        return result.get();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof Value)) {
            return false;
        }

        final Value other = (Value) object;
        return Objects.equals(type, other.type) &&
            Objects.equals(array, other.array) &&
            Objects.equals(hash, other.hash) &&
            Objects.equals(string, other.string);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(type) +
            Objects.hashCode(array) +
            Objects.hashCode(hash) +
            Objects.hashCode(string);
    }

    @Override
    public String toString() {
        return isNull() ? null : extractType((m, c) -> m
                .ifArray(a -> c.accept(a.toString()))
                .ifHash(h -> c.accept(h.toString()))
                .ifString(c)
                .orElseThrow()
        );
    }

    private void toJson(final JsonGenerator jsonGenerator) {
        if (isNull()) {
            try {
                jsonGenerator.writeNull();
            }
            catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        else {
            matchType()
                .ifArray(a -> a.toJson(jsonGenerator))
                .ifHash(h -> h.toJson(jsonGenerator))
                .ifString(s -> {
                    try {
                        jsonGenerator.writeString(s);
                    }
                    catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .orElseThrow();
        }
    }

    /*package-private*/ static String[] split(final String fieldPath) {
        return fieldPath.split(FIELD_PATH_SEPARATOR);
    }

    public String getPath() {
        return path;
    }

    /*package-private*/ Value withPathSet(final String p) {
        this.path = p;
        return this;
    }

    private Value withPathAppend(final int i) {
        return withPathAppend(String.valueOf(i));
    }

    private Value withPathAppend(final String field) {
        return withPathSet(path == null || path.isEmpty() ? field : path + "." + field);
    }

    /*package-private*/ Value copy() {
        return extractType((m, c) -> m
                .ifArray(oldArray -> c.accept(Value.newArray(newArray -> oldArray.forEach(v -> newArray.add(v)))))
                .ifHash(oldHash -> c.accept(Value.newHash(newHash -> oldHash.forEach((k, v) -> newHash.put(k, v)))))
                .ifString(s -> c.accept(new Value(s)))
                .orElseThrow());
    }

    enum Type {
        Array,
        Hash,
        String
    }

    public static class TypeMatcher {

        private final Set<Type> expected = EnumSet.noneOf(Type.class);
        private final Value value;

        private TypeMatcher(final Value value) {
            this.value = value;
        }

        public TypeMatcher ifArray(final Consumer<Array> consumer) {
            return match(Type.Array, consumer, value.array);
        }

        public TypeMatcher ifHash(final Consumer<Hash> consumer) {
            return match(Type.Hash, consumer, value.hash);
        }

        public TypeMatcher ifString(final Consumer<String> consumer) {
            return match(Type.String, consumer, value.string);
        }

        public void orElse(final Consumer<Value> consumer) {
            if (!expected.contains(value.type)) {
                consumer.accept(value);
            }
        }

        public void orElseThrow() {
            orElse(v -> {
                final String types = expected.stream().map(Type::name).collect(Collectors.joining(" or "));
                throw new IllegalStateException("Expected " + types + ", got " + value.type);
            });
        }

        private <T> TypeMatcher match(final Type type, final Consumer<T> consumer, final T rawValue) {
            if (expected.add(type)) {
                if (value.isType(type)) {
                    consumer.accept(rawValue);
                }

                return this;
            }
            else {
                throw new IllegalStateException("Already expecting " + type);
            }
        }

    }

    private abstract static class AbstractValueType {

        protected static final Predicate<Value> REMOVE_EMPTY_VALUES = v ->
            v.extractType((m, c) -> m
                .ifArray(a -> {
                    a.removeEmptyValues();
                    c.accept(a.isEmpty());
                })
                .ifHash(h -> {
                    h.removeEmptyValues();
                    c.accept(h.isEmpty());
                })
                // TODO: Catmandu considers whitespace-only strings empty (`$v !~ /\S/`)
                .ifString(s -> c.accept(s.isEmpty()))
                .orElseThrow()
        );

        @Override
        public abstract boolean equals(Object object);

        @Override
        public abstract int hashCode();

        @Override
        public abstract String toString();

        protected abstract void toJson(JsonGenerator jsonGenerator);

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
            add(value, true);
        }

        /* package-private */ void add(final Value value, final boolean appendToPath) {
            if (!isNull(value)) {
                list.add(appendToPath ? value.withPathAppend(list.size() + 1) : value);
            }
        }

        public boolean isEmpty() {
            return list.isEmpty();
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

        private void removeEmptyValues() {
            list.removeIf(REMOVE_EMPTY_VALUES);
        }

        public void forEach(final Consumer<Value> consumer) {
            list.forEach(consumer);
        }

        @Override
        public final boolean equals(final Object object) {
            if (object == this) {
                return true;
            }

            if (!(object instanceof Array)) {
                return false;
            }

            final Array other = (Array) object;
            return Objects.equals(list, other.list);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(list);
        }

        @Override
        public String toString() {
            return list.toString();
        }

        @Override
        protected void toJson(final JsonGenerator jsonGenerator) {
            try {
                jsonGenerator.writeStartArray();
                forEach(v -> v.toJson(jsonGenerator));
                jsonGenerator.writeEndArray();
            }
            catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public void remove(final int index) {
            list.remove(index);
        }

        /*package-private*/ void set(final int index, final Value value) {
            list.set(index, value.withPathAppend(index + 1));
        }

        /*package-private*/ void removeIf(final Predicate<Value> predicate) {
            list.removeIf(predicate);
        }

        /*package-private*/ void removeAll() {
            list.clear();
        }

    }

    /**
     * Represents a hash of metadata fields and values.
     */
    public static class Hash extends AbstractValueType {

        // NOTE: Keep in sync with `WildcardTrie`/`SimpleRegexTrie` implementation in metafacture-core.
        private static final Pattern ALTERNATION_PATTERN = Pattern.compile(WildcardTrie.OR_STRING, Pattern.LITERAL);
        private static final Matcher PATTERN_MATCHER = Pattern.compile("[*?]|\\[[^\\]]").matcher("");

        private static final Map<String, String> PREFIX_CACHE = new HashMap<>();

        private static final Map<String, Map<String, Boolean>> TRIE_CACHE = new HashMap<>();
        private static final SimpleRegexTrie<String> TRIE = new SimpleRegexTrie<>();

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
            return !findFields(field).isEmpty();
        }

        public boolean containsPath(final String fieldPath) {
            final String[] path = split(fieldPath);
            final String field = path[0];

            final boolean containsField = containsField(field);
            final boolean containsPath;

            if (containsField && path.length > 1) {
                final Value value;

                try {
                    value = new FixPath(fieldPath).findIn(this);
                }
                catch (final IllegalStateException e) {
                    return false;
                }

                containsPath = !isNull(value);
            }
            else {
                containsPath = containsField;
            }

            return containsPath;
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
            put(field, value, true);
        }

        /*package-private*/ void put(final String field, final Value value, final boolean appendToPath) {
            if (!isNull(value)) {
                map.put(field, appendToPath ? value.withPathAppend(field) : value);
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
            return get(field, false);
        }

        /*package-private*/ Value get(final String field, final boolean enforceStringValue) { // TODO use Type.String etc.?
            // TODO: special treatment (only) for exact matches?
            final Set<String> set = findFields(field);

            return set.isEmpty() ? null : set.size() == 1 ? getField(set.iterator().next(), enforceStringValue) :
                newArray(a -> set.forEach(f -> getField(f, enforceStringValue).matchType()
                            .ifArray(b -> b.forEach(t -> a.add(t, false)))
                            .orElse(t -> a.add(t, false))
                ));
        }

        public Value getField(final String field) {
            return map.get(field);
        }

        private Value getField(final String field, final boolean enforceStringValue) {
            final Value value = getField(field);

            if (enforceStringValue) {
                value.asString();
            }

            return value;
        }

        public Value getList(final String field, final Consumer<Array> consumer) {
            return asList(get(field), consumer);
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
            final Value oldValue = new FixPath(field).findIn(this);

            if (oldValue == null) {
                put(field, newValue);
            }
            else {
                final String basePath = oldValue.getPath();
                if (!oldValue.isArray()) { // repeated field: convert single val to first in array
                    oldValue.withPathAppend(1);
                }

                put(field, oldValue.asList(oldVals -> newValue.asList(newVals ->
                                newVals.forEach(newVal -> oldVals.add(newVal.withPathSet(basePath))))));
            }
        }

        /**
         * Removes the given field/value pair from this hash.
         *
         * @param field the field name
         */
        public void remove(final String field) {
            final FixPath fixPath = new FixPath(field);

            if (fixPath.size() > 1) {
                fixPath.removeNestedFrom(this);
            }
            else {
                modifyFields(field, this::removeField);
            }
        }

        public void removeField(final String field) {
            map.remove(field);
        }

        /**
         * Retains only the given field/value pairs in this hash.
         *
         * @param fields the field names
         */
        public void retainFields(final Collection<String> fields) {
            final Set<String> retainFields = new HashSet<>();
            fields.forEach(f -> retainFields.addAll(findFields(f)));

            map.keySet().retainAll(retainFields);
        }

        /**
         * Recursively removes all field/value pairs from this hash whose value is empty.
         */
        public void removeEmptyValues() {
            map.values().removeIf(REMOVE_EMPTY_VALUES);
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
        public final boolean equals(final Object object) {
            if (object == this) {
                return true;
            }

            if (!(object instanceof Hash)) {
                return false;
            }

            final Hash other = (Hash) object;
            return Objects.equals(map, other.map);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(map);
        }

        @Override
        public String toString() {
            return map.toString();
        }

        @Override
        protected void toJson(final JsonGenerator jsonGenerator) {
            try {
                jsonGenerator.writeStartObject();

                forEach((f, v) -> {
                    try {
                        jsonGenerator.writeFieldName(f);
                    }
                    catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }

                    v.toJson(jsonGenerator);
                });

                jsonGenerator.writeEndObject();
            }
            catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Avoids {@link ConcurrentModificationException} when modifying the hash based on matched fields.
         *
         * @param pattern the field name pattern
         * @param consumer the action to be performed for each value
         */
        /*package-private*/ void modifyFields(final String pattern, final Consumer<String> consumer) {
            findFields(pattern).forEach(consumer);
        }

        private Set<String> findFields(final String pattern) {
            final Set<String> fieldSet = new LinkedHashSet<>();

            for (final String term : ALTERNATION_PATTERN.split(pattern)) {
                findFields(term, fieldSet);
            }

            return fieldSet;
        }

        private void findFields(final String pattern, final Set<String> fieldSet) {
            if (!PREFIX_CACHE.containsKey(pattern)) {
                final Matcher patternMatcher = PATTERN_MATCHER.reset(pattern);

                if (patternMatcher.find()) {
                    TRIE.put(pattern, pattern);
                    TRIE_CACHE.put(pattern, new HashMap<>());

                    PREFIX_CACHE.put(pattern, pattern.substring(0, patternMatcher.start()));
                }
                else {
                    PREFIX_CACHE.put(pattern, null);
                }
            }

            final String prefix = PREFIX_CACHE.get(pattern);

            if (prefix != null) {
                final Map<String, Boolean> fieldCache = TRIE_CACHE.get(pattern);

                for (final String field : map.keySet()) {
                    if (!fieldCache.containsKey(field)) {
                        final boolean matches = field.startsWith(prefix) && TRIE.get(field).contains(pattern);
                        fieldCache.put(field, matches);

                        if (matches) {
                            fieldSet.add(field);
                        }
                    }
                    else if (fieldCache.get(field)) {
                        fieldSet.add(field);
                    }
                }
            }
            else if (map.containsKey(pattern)) {
                fieldSet.add(pattern);
            }
        }

    }

}
