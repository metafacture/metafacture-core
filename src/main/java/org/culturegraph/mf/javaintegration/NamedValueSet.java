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
package org.culturegraph.mf.javaintegration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.culturegraph.mf.commons.types.NamedValue;
import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;


/**
 * Collects {@link NamedValue}s in a {@link Set}. So there will not be
 * duplicates.
 *
 * @author Markus Michael Geipel
 */
public final class NamedValueSet extends DefaultStreamReceiver
		implements Set<NamedValue>, Collector<Set<NamedValue>> {

	private Collection<Set<NamedValue>> collection;
	private Set<NamedValue> set;

	public NamedValueSet() {
		super();
		set = new HashSet<>();
		this.collection = null;

	}

	/**
	 * Creates a {@code NamedValueSet} receiver which stores the received values
	 * in the passed set.
	 *
	 * @param collection is filled with the received results.
	 */
	public NamedValueSet(final Collection<Set<NamedValue>> collection) {
		super();
		set = new HashSet<>();
		this.collection = collection;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(final Object obj) {
		return set.contains(obj);
	}

	@Override
	public Iterator<NamedValue> iterator() {
		return set.iterator();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arr) {
		return set.toArray(arr);
	}

	@Override
	public boolean add(final NamedValue ele) {
		return set.add(ele);
	}

	@Override
	public boolean remove(final Object obj) {
		return set.remove(obj);
	}

	@Override
	public boolean containsAll(final Collection<?> col) {
		return set.containsAll(col);
	}

	@Override
	public boolean addAll(final Collection<? extends NamedValue> col) {
		return set.addAll(col);
	}

	@Override
	public boolean retainAll(final Collection<?> col) {
		return set.retainAll(col);
	}

	@Override
	public boolean removeAll(final Collection<?> col) {
		return set.removeAll(col);
	}

	@Override
	public void clear() {
		set.clear();
	}

	@Override
	public boolean equals(final Object obj) {
		return set.equals(obj);
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

	@Override
	public String toString() {
		return set.toString();
	}

	@Override
	public void startRecord(final String identifier) {
		set.clear();
	}

	@Override
	public void endRecord() {
		if (collection != null) {
			collection.add(set);
			set = new HashSet<NamedValue>();
		}
	}

	@Override
	public void literal(final String name, final String value) {
		set.add(new NamedValue(name, value));
	}

	@Override
	public Collection<Set<NamedValue>> getCollection() {
		return collection;
	}

	@Override
	public void setCollection(final Collection<Set<NamedValue>> collection) {
		this.collection = collection;
	}

}
