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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link Map} that delegates to another map if it does not
 * contain a value for key.
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 * @author Markus Michael Geipel
 */
public final class ScopedHashMap<K, V> extends HashMap<K, V> { // checkstyle-disable-line IllegalType

    private static final long serialVersionUID = -7184066609960144713L;
    private final ScopedHashMap<K, V> outerScope;

    /**
     * Creates an instance of {@link ScopedHashMap} and sets the outer scope.
     *
     * @param outerScope outer scope
     */
    public ScopedHashMap(final ScopedHashMap<K, V> outerScope) {
        this.outerScope = outerScope;
    }

    /**
     * Creates an empty instance of {@link ScopedHashMap}.
     */
    public ScopedHashMap() {
        outerScope = null;
    }

    @Override
    public boolean containsKey(final Object key) {
        if (super.containsKey(key)) {
            return true;
        }
        return outerScope != null && outerScope.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        if (super.containsValue(value)) {
            return true;
        }
        return outerScope != null && outerScope.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        final V ret = super.get(key);
        if (null == ret && outerScope != null) {
            return outerScope.get(key);
        }
        return ret;
    }

    /**
     * Gets the outer scope.
     *
     * @return the outer scope
     */
    public ScopedHashMap<K, V> getOuterScope() {
        return outerScope;
    }

    @Override
    public String toString() {
        return super.toString() + (getOuterScope() == null ? "" : "\n" + getOuterScope().toString());
    }
}
