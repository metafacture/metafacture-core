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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Represents a metadata record, i.e., a mapping of fields and values.
 */
public class Record {

    private static final String EMPTY = "";

    private final Map<String, Object> map = new LinkedHashMap<>();

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
     * Checks whether this record contains the metadata field.
     *
     * @param field the field name
     * @return true if this record contains the metadata field, false otherwise
     */
    public boolean containsField(final String field) {
        return map.containsKey(field);
    }

    /**
     * Checks whether this record is empty.
     *
     * @return true if this record is empty, false otherwise
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Gets the number of field/value pairs in this record.
     *
     * @return the number of field/value pairs in this record
     */
    public int size() {
        return map.size();
    }

    /**
     * Adds a field/value pair to this record, provided it's not {@code null}.
     *
     * @param field the field name
     * @param value the metadata value
     */
    public void put(final String field, final Object value) {
        if (value != null) {
            map.put(field, value);
        }
    }

    /**
     * {@link #put(String, Object) Replaces} a field/value pair in this record,
     * provided the field name is already {@link #containsField(String) present}.
     *
     * @param field the field name
     * @param value the metadata value
     */
    public void replace(final String field, final Object value) {
        if (containsField(field)) {
            put(field, value);
        }
    }

    /**
     * Retrieves the field value from this record.
     *
     * @param field the field name
     * @return the metadata value
     */
    public Object get(final String field) {
        return map.get(field);
    }

    /**
     * Removes the given field/value pair from this record.
     *
     * @param field the field name
     */
    public void remove(final String field) {
        map.remove(field);
    }

    /**
     * Retains only the given field/value pairs in this record.
     *
     * @param fields the field names
     */
    public void retainFields(final Collection<String> fields) {
        map.keySet().retainAll(fields);
    }

    /**
     * Removes all field/value pairs from this record whose value is empty.
     */
    public void removeEmptyValues() {
        map.values().removeIf(EMPTY::equals);
    }

    /**
     * Iterates over all field/value pairs in this record.
     *
     * @param consumer the action to be performed for each field/value pair
     */
    public void forEach(final BiConsumer<String, Object> consumer) {
        map.forEach(consumer);
    }

    @Override
    public String toString() {
        // TODO: Improve string representation? Include reject status, etc.?
        return map.toString();
    }

    // TODO: Replace map accesses with record operations!
    public Map<String, Object> temporarilyGetMap() {
        return map;
    }

}
