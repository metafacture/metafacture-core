/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.metamorph.api.helpers;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Base class for maps which are read only. Allows access to the key set. It's
 * up to the extending class when overriding {@link #keySet()} to return an
 * {@link java.util.Collections#unmodifiableSet(Set)}.
 *
 * @param <K> type of keys
 * @param <V> type of values
 * @author Markus Michael Geipel
 */
public abstract class AbstractReadOnlyMap<K, V> implements Map<K, V>, Closeable {

    @Override
    public final int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    @Override
    public final boolean containsValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();

    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * It's up to the extending class to return an
     * {@link java.util.Collections#unmodifiableSet(Set)} when overriding.
     */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
