/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.commons.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map containing a list of values for each key.
 *
 * @author Markus Michael Geipel
 *
 * @param <K>
 *            key
 * @param <V>
 *            value
 */
public class ListMap<K, V> implements Map<K, List<V>> {

    private String identifier;
    private final Map<K, List<V>> map;

    public ListMap() {
        map = new HashMap<K, List<V>>();
    }

    public ListMap(final Map<K, List<V>> map) {
        this.map = map;
    }

    protected final Map<K, List<V>> getMap() {
        return map;
    }

    @Override
    public final void clear() {
        map.clear();
        identifier = null;
    }

    /**
     * Removes a key of the map.
     *
     * @param key the key
     * @return the List of values or null if the map didn't contain the key
     */
    public final List<V> removeKey(final K key) {
        return map.remove(key);
    }

    /**
     * Clears all values of a key of the map.
     *
     * @param key the key of the map
     */
    public final void clearKey(final K key) {
        final List<V> values = map.get(key);
        if (values != null) {
            values.clear();
        }
    }

    /**
     * Clears all values of all keys of the map.
     */
    public final void clearAllKeys() {
        for (final Entry<K, List<V>> entry: map.entrySet()) {
            entry.getValue().clear();
        }
    }

    @Override
    public final Set<Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public final Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Adds a value to the List of values of the key of the map.
     *
     * @param name  the key
     * @param value the value
     */
    public final void add(final K name, final V value) {
        List<V> values = map.get(name);
        if (values == null) {
            values = new ArrayList<V>();
            map.put(name, values);
        }
        values.add(value);
    }

    @Override
    public final List<V> get(final Object name) {
        final List<V> values = map.get(name);
        if (values == null) {
            return Collections.emptyList();
        }
        return values;
    }

    public final boolean existsKey(final K name) {
        return getFirst(name) != null;
    }

    /**
     * Gets the first element of the List of values of the key of the map.
     *
     * @param name the key
     * @return first element of the values of the key
     */
    public final V getFirst(final K name) {
        final List<V> values = map.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public final String toString() {
        return map.toString();
    }

    public final void setId(final String newIdentifier) {
        identifier = newIdentifier;
    }

    public final String getId() {
        return identifier;
    }

    @Override
    public final int size() {
        return map.size();
    }

    @Override
    public final boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public final boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    @Override
    public final boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    @Override
    public final List<V> put(final K key, final List<V> value) {
        return map.put(key, value);
    }

    @Override
    public final List<V> remove(final Object key) {
        return map.remove(key);
    }

    /**
     * Adds a List of values to a key of the map.
     *
     * @param name      the key
     * @param addValues the List of values
     */
    public final void putAll(final K name, final Collection<V> addValues) {
        List<V> values = map.get(name);
        if (values == null) {
            values = new ArrayList<V>();
            map.put(name, values);
        }

        values.addAll(addValues);
    }

    @Override
    public final void putAll(final Map<? extends K, ? extends List<V>> putMap) {
        map.putAll(putMap);
    }

    @Override
    public final Collection<List<V>> values() {
        return map.values();
    }
}
