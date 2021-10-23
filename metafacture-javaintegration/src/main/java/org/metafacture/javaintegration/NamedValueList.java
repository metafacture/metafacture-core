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

import org.metafacture.commons.types.NamedValue;
import org.metafacture.framework.helpers.DefaultStreamReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Collects the received results in a {@link List}.
 *
 * @author Markus Michael Geipel
 *
 */
public final class NamedValueList extends DefaultStreamReceiver implements List<NamedValue>, Collector<List<NamedValue>> {

    private Collection<List<NamedValue>> collection;
    private List<NamedValue> list = new ArrayList<>();

    public NamedValueList() {
    }

    /**
     * Constructs a NamedValueList with a given Collection.
     *
     * @param collection the Collection
     */
    public NamedValueList(final Collection<List<NamedValue>> collection) {
        this.collection = collection;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(final Object object) {
        return list.contains(object);
    }

    @Override
    public Iterator<NamedValue> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        return list.toArray(array);
    }

    @Override
    public boolean add(final NamedValue element) {
        return list.add(element);
    }

    @Override
    public void add(final int index, final NamedValue element) {
        list.add(index, element);
    }

    @Override
    public boolean remove(final Object object) {
        return list.remove(object);
    }

    @Override
    public NamedValue remove(final int index) {
        return list.remove(index);
    }

    @Override
    public boolean containsAll(final Collection<?> currentCollection) {
        return list.containsAll(currentCollection);
    }

    @Override
    public boolean addAll(final Collection<? extends NamedValue> currentCollection) {
        return list.addAll(currentCollection);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends NamedValue> currentCollection) {
        return list.addAll(index, currentCollection);
    }

    @Override
    public boolean removeAll(final Collection<?> currentCollection) {
        return list.removeAll(currentCollection);
    }

    @Override
    public boolean retainAll(final Collection<?> currentCollection) {
        return list.retainAll(currentCollection);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public NamedValue get(final int index) {
        return list.get(index);
    }

    @Override
    public NamedValue set(final int index, final NamedValue element) {
        return list.set(index, element);
    }

    @Override
    public int indexOf(final Object object) {
        return list.indexOf(object);
    }

    @Override
    public int lastIndexOf(final Object object) {
        return list.lastIndexOf(object);
    }

    @Override
    public ListIterator<NamedValue> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<NamedValue> listIterator(final int index) {
        return list.listIterator(index);
    }

    @Override
    public List<NamedValue> subList(final int fromIndex, final int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public void startRecord(final String identifier) {
        list.clear();
    }

    @Override
    public void endRecord() {
        if (collection != null) {
            collection.add(list);
            list = new ArrayList<NamedValue>();
        }
    }

    @Override
    public void literal(final String name, final String value) {
        list.add(new NamedValue(name, value));
    }

    @Override
    public Collection<List<NamedValue>> getCollection() {
        return collection;
    }

    @Override
    public void setCollection(final Collection<List<NamedValue>> collection) {
        this.collection = collection;
    }

}
