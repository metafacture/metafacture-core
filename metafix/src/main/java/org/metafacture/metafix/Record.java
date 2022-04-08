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

import org.metafacture.metafix.FixPath.InsertMode;
import org.metafacture.metafix.Value.TypeMatcher;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Represents a metadata record, i.e., a {@link Value.Hash Hash} of fields
 * and values.
 */
public class Record extends Value.Hash {

    private final Map<String, Value> virtualFields = new LinkedHashMap<>();

    private boolean reject;

    /**
     * Creates an empty instance of {@link Record}.
     */
    public Record() {
    }

    /**
     * Returns a shallow clone of this record.
     *
     * @return a new record pre-populated with all entries from this record
     */
    public Record shallowClone() {
        final Record clone = new Record();

        clone.setReject(reject);
        forEach(clone::put);
        virtualFields.forEach(clone::putVirtualField);

        return clone;
    }

    /**
     * Flags whether this record should be rejected.
     *
     * @param reject true if this record should not be emitted, false otherwise
     */
    public void setReject(final boolean reject) {
        this.reject = reject;
    }

    /**
     * Checks whether this record should be rejected.
     *
     * @return true if this record should not be emitted, false otherwise
     */
    public boolean getReject() {
        return reject;
    }

    /**
     * Checks whether this record contains the <i>virtual</i> field.
     *
     * @param field the field name
     * @return true if this record contains the <i>virtual</i> field, false otherwise
     */
    public boolean containsVirtualField(final String field) {
        return virtualFields.containsKey(field);
    }

    /**
     * Adds a <i>virtual</i> field/value pair to this record, provided it's not
     * {@link Value#isNull(Value) null}. Virtual fields can be
     * {@link #get(String) accessed} like regular metadata fields, but aren't
     * {@link #forEach(BiConsumer) emitted} by default.
     *
     * @param field the field name
     * @param value the metadata value
     *
     * @see #retainFields(Collection)
     */
    public void putVirtualField(final String field, final Value value) {
        if (!Value.isNull(value)) {
            virtualFields.put(field, value);
        }
    }

    @Override
    public String toString() {
        // TODO: Improve string representation? Include reject status, virtual fields, etc.?
        return super.toString();
    }

    /**
     * Retrieves the field value from this record. Falls back to retrieving the
     * <i>virtual</i> field if the field name is not already
     * {@link #containsField(String) present}.
     *
     * @param field the field name
     * @return the metadata value
     */
    @Override
    public Value get(final String field) {
        final Value result;
        if (containsField(field)) {
            result = super.get(field);
        }
        else {
            final FixPath fixPath = new FixPath(field);
            if (fixPath.size() > 1) {
                result = fixPath.findIn(this);
            }
            else {
                result = virtualFields.get(field);
            }
        }
        return result;
    }

    /**
     * {@link #put(String, Value) Adds} a field/value pair to this record. Turns
     * <i>virtual</i> fields into regular metadata fields if they're not already
     * {@link #containsField(String) present}.
     *
     * @param field the field name
     * @param newValue the new metadata value
     */
    @Override
    public void add(final String field, final Value newValue) {
        if (containsField(field)) {
            super.add(field, newValue);
        }
        else {
            put(field, newValue);
        }
    }

    public void addNested(final String field, final Value newValue) {
        new FixPath(field).insertInto(this, InsertMode.APPEND, newValue);
    }

    /**
     * Sets a field/value pair to this record, replacing
     * any previous association of the field with a value.
     *
     * @param field the field name
     * @param newValue the new metadata value
     */
    public void set(final String field, final Value newValue) {
        final FixPath fixPath = new FixPath(field);
        fixPath.insertInto(this, InsertMode.REPLACE, newValue);
    }

    /**
     * Retains only the given field/value pairs in this record. Turns
     * <i>virtual</i> fields into regular metadata fields if they're not already
     * {@link #containsField(String) present}.
     *
     * @param fields the field names
     */
    @Override
    public void retainFields(final Collection<String> fields) {
        virtualFields.keySet().retainAll(fields);

        virtualFields.forEach((f, v) -> {
            if (!containsField(f)) {
                put(f, v);
            }
        });

        super.retainFields(fields);
    }

    /**
     * Transform this record by applying the given operator to all matching values for the given field.
     *
     * @param field The field
     * @param operator The operator
     */
    public void transform(final String field, final UnaryOperator<String> operator) {
        final FixPath findPath = new FixPath(field);
        final Value found = findPath.findIn(this, true);
        Value.asList(found, results -> {
            final Deque<FixPath> toDelete = new LinkedList<>();
            for (int i = 0; i < results.size(); ++i) {
                final Value oldValue = results.get(i);
                final FixPath insertPath = findPath.to(oldValue, i);
                final String newString = operator.apply(oldValue.asString());
                if (newString == null) {
                    toDelete.addFirst(insertPath);
                }
                else {
                    final Value newValue = new Value(newString);
                    insertPath.insertInto(this, InsertMode.REPLACE, newValue);
                    newValue.setPath(insertPath.toString());
                }
            }
            toDelete.forEach(path -> path.removeNestedFrom(this));
        });
    }

    /**
     * Transform this record by consuming all matching values for the given field with the given consumer.
     *
     * @param field The field
     * @param consumer The consumer
     */
    public void transform(final String field, final BiConsumer<TypeMatcher, Consumer<Value>> consumer) {
        final FixPath path = new FixPath(field);
        final Value oldValue = path.findIn(this);

        if (oldValue != null) {
            final Value newValue = oldValue.extractType(consumer);

            if (newValue != null) {
                path.insertInto(this, InsertMode.REPLACE, newValue);
            }
        }
    }

}
