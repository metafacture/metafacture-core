/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.commons.reflection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Provides instances of preregistered classes. New classes can be registered
 * during runtime. This class is not thread-safe.
 *
 * @param <T> the type of objects created
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 */
public class ObjectFactory<T> {

    private final Map<String, ConfigurableClass<? extends T>> classes =
            new HashMap<>();

    /**
     * Creates an instance of {@link ObjectFactory}.
     */
    public ObjectFactory() {
    }

    /**
     * Loads classes from a map.
     *
     * @param classMap the map of classes
     * @param baseType the object type of the classes
     */
    public final void loadClassesFromMap(final Map<?, ?> classMap, final Class<T> baseType) {
        final ClassLoader loader = ReflectionUtil.getContextClassLoader();
        for (final Entry<?, ?> entry : classMap.entrySet()) {
            final String key = entry.getKey().toString();
            final String className = entry.getValue().toString();
            registerClass(key, ReflectionUtil.loadClass(loader, className, baseType));
        }
    }

    /**
     * Registers a Class as a ConfigurableClass.
     *
     * @param key         the key associcated with the Class
     * @param objectClass the Class
     */
    public final void registerClass(final String key, final Class<? extends T> objectClass) {
        registerClass(key, new ConfigurableClass<>(objectClass));
    }

    /**
     * Registers a ConfigurableClass.
     *
     * @param key         the key associcated with the ConfigurableClass
     * @param objectClass the ConfigurableClass
     */
    public final void registerClass(final String key, final ConfigurableClass<? extends T> objectClass) {
        classes.put(key, objectClass);
    }

    /**
     * Returns a new instance of a ConfigurableClass with no setters.
     *
     * @param key             the name of the class
     * @param constructorArgs the args of the constructor
     * @return a new instance
     */
    public final T newInstance(final String key, final Object... constructorArgs) {
        return newInstance(key, Collections.emptyMap(), constructorArgs);
    }

    /**
     * Returns a new instance of a ConfigurableClass.
     *
     * @param key             the name of the class
     * @param values          the Map of Strings of the setters
     * @param constructorArgs the args of the constructor
     * @return a new instance
     */
    public final T newInstance(final String key, final Map<String, String> values, final Object... constructorArgs) {
        if (!classes.containsKey(key)) {
            throw new NoSuchElementException("no registered class for: " + key);
        }
        final ConfigurableClass<? extends T> instanceClass = classes.get(key);
        return instanceClass.newInstance(values, constructorArgs);
    }

    /**
     * Checks whether a ConfigurableClass is asscociated with a key.
     *
     * @param key the key
     * @return true if the key is associcated with a ConfigurableClass
     */
    public final boolean containsKey(final String key) {
        return classes.containsKey(key);
    }

    /**
     * Gets the key set of all {ConfigurableClass}es.
     *
     * @return all keys that identify the {ConfigurableClass}es
     */
    public final Set<String> keySet() {
        return Collections.unmodifiableSet(classes.keySet());
    }

    /**
     * Gets a ConfigurableClass.
     *
     * @param key the key that identifies the ConfigurableClass
     * @return the ConfigurableClass
     */
    public final ConfigurableClass<? extends T> get(final String key) {
        return classes.get(key);
    }

}
