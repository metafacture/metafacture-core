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

package org.metafacture.javaintegration;

import org.metafacture.commons.types.ListMap;
import org.metafacture.framework.helpers.DefaultStreamReceiver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collects the received results in a {@link ListMap}.
 *
 * @author Markus Michael Geipel
 *
 */
public final class StringListMap extends DefaultStreamReceiver implements Collector<ListMap<String, String>>, Map<String, List<String>> {

    private boolean closed;
    private Collection<ListMap<String, String>> collection;
    private ListMap<String, String> listMap = new ListMap<String, String>();

    /**
     * Creates an instance of {@link StringListMap}.
     */
    public StringListMap() {
    }

    /**
     * Creates an instance of {@link StringListMap} by a given Collection.
     *
     * @param collection the Collection
     */
    public StringListMap(final Collection<ListMap<String, String>> collection) {
        this.collection = collection;
    }

    /**
     * Checks if StringListMap is closed.
     *
     * @return true if it is closed
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void clear() {
        listMap.clear();
    }

    /**
     * Removes a key in the map.
     *
     * @param key the key
     */
    public void removeKey(final String key) {
        listMap.removeKey(key);
    }

    /**
     * Clears a key in the map.
     *
     * @param key the key
     */
    public void clearKey(final String key) {
        listMap.clearKey(key);
    }

    /**
     * Clears all keys in the map.
     */
    public void clearAllKeys() {
        listMap.clearAllKeys();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return listMap.entrySet();
    }

    @Override
    public Set<String> keySet() {
        return listMap.keySet();
    }

    /**
     * Adds a value to the List of values of the key in the map.
     *
     * @param name  the key
     * @param value the value
     */
    public void add(final String name, final String value) {
        listMap.add(name, value);
    }

    /**
     * Adds a List of values to a key in the map
     *
     * @param name      the key
     * @param addValues the {@link Collection} of values
     */
    public void putAll(final String name, final Collection<String> addValues) {
        listMap.putAll(name, addValues);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends List<String>> putMap) {
        listMap.putAll(putMap);
    }

    @Override
    public int hashCode() {
        return listMap.hashCode();
    }

    @Override
    public List<String> get(final Object name) {
        return listMap.get(name);
    }

    /**
     * Checks whether a key exists.
     *
     * @param name the key
     * @return true if the key exists, otherwise false
     */
    public boolean existsKey(final String name) {
        return listMap.existsKey(name);
    }

    /**
     * Gets the first element of the List of values of the key in the map or null
     *
     * @param name the name
     * @return the first element of the List of values of the key in the map or
     *         null.
     */
    public String getFirst(final String name) {
        return listMap.getFirst(name);
    }

    @Override
    public String toString() {
        return listMap.toString();
    }

    /**
     * Sets the ID.
     *
     * @param identifier the ID
     */
    public void setId(final String identifier) {
        listMap.setId(identifier);
    }

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public String getId() {
        return listMap.getId();
    }

    @Override
    public int size() {
        return listMap.size();
    }

    @Override
    public boolean isEmpty() {
        return listMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return listMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return listMap.containsValue(value);
    }

    @Override
    public List<String> put(final String key, final List<String> value) {
        return listMap.put(key, value);
    }

    @Override
    public List<String> remove(final Object key) {
        return listMap.remove(key);
    }

    @Override
    public Collection<List<String>> values() {
        return listMap.values();
    }

    @Override
    public boolean equals(final Object obj) {
        return listMap.equals(obj);
    }

    @Override
    public void startRecord(final String identifier) {
        listMap.clear();
        listMap.setId(identifier);
    }

    @Override
    public void endRecord() {
        if (collection != null) {
            collection.add(listMap);
            listMap = new ListMap<String, String>();
        }
    }

    @Override
    public void literal(final String name, final String value) {
        listMap.add(name, value);
    }

    @Override
    public void resetStream() {
        closed = false;
        clear();
    }

    @Override
    public void closeStream() {
        closed = true;
    }

    @Override
    public Collection<ListMap<String, String>> getCollection() {
        return collection;
    }

    @Override
    public void setCollection(final Collection<ListMap<String, String>> collection) {
        this.collection = collection;
    }

}
