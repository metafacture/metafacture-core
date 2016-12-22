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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;


/**
 * Collects the received results in a {@link Map}. Duplicate names are thus lost.
 *
 * @author Markus Michael Geipel
 *
 */
public final class StringMap extends DefaultStreamReceiver
		implements Map<String, String> , Collector<Map<String, String>>{

	private boolean closed;
	private Collection<Map<String, String>> collection;
	private Map<String, String> map;

	public StringMap() {
		super();
		map = new HashMap<String, String>();
		collection=null;
	}

	public StringMap(final Collection<Map<String, String>> collection) {
		super();
		map = new HashMap<String, String>();
		this.collection=collection;
	}

	/**
	 * @param map is filled with the received results.
	 */
	public StringMap(final Map<String, String> map) {
		super();
		this.map = map;
	}

	protected void setMap(final Map<String, String> map) {
		this.map = map;
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return map.containsValue(value);
	}

	@Override
	public String get(final Object key) {
		return map.get(key);
	}

	@Override
	public String put(final String key, final String value) {
		return map.put(key, value);
	}

	@Override
	public String remove(final Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(final Map<? extends String, ? extends String> otherMap) {
		map.putAll(otherMap);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<String> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return map.entrySet();
	}

	@Override
	public boolean equals(final Object obj) {
		return map.equals(obj);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public void startRecord(final String identifier) {
		assert !closed;
		map.clear();
	}

	@Override
	public void endRecord() {
		assert !closed;
		if (collection != null) {
			collection.add(map);
			map = new HashMap<String, String>();
		}

	}

	@Override
	public void literal(final String name, final String value) {
		assert !closed;
		map.put(name, value);
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
	public Collection<Map<String, String>> getCollection() {
		return collection;
	}

	@Override
	public void setCollection(final Collection<Map<String, String>> collection) {
		this.collection = collection;
	}

}
