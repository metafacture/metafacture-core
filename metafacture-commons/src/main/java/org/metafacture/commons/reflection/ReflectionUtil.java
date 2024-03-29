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

package org.metafacture.commons.reflection;

/**
 * Utility functions for dynamically loading classes and calling setters on
 * them.
 *
 * @author Christoph Böhme
 */
public final class ReflectionUtil {

    private ReflectionUtil() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * @return the context ClassLoader for this thread, or null indicating the
     *         system class loader (or, failing that, the bootstrap class loader)
     */
    public static ClassLoader getContextClassLoader() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            throw new ReflectionException("Class loader could not be found.");
        }
        return loader;
    }

    /**
     * Loads a Class.
     *
     * @param <T>       the object type
     * @param className the name of the class
     * @param baseType  the object type of the class to be wrapped
     * @return the ConfigurableClass
     */
    public static <T> ConfigurableClass<? extends T> loadClass(final String className, final Class<T> baseType) {
        return loadClass(getContextClassLoader(), className, baseType);
    }

    /**
     * Wraps a Class in a ConfigurableClass.
     *
     * @param <T>       the object type of the ConfigurableClass
     * @param loader    the ClassLoader
     * @param className the name of the class
     * @param baseType  the object type of the class to be wrapped
     * @return the ConfigurableClass
     */
    public static <T> ConfigurableClass<? extends T> loadClass(final ClassLoader loader, final String className, final Class<T> baseType) {
        final Class<?> clazz;
        try {
            clazz = loader.loadClass(className);
        }
        catch (final ClassNotFoundException e) {
            throw new ReflectionException("Class not found: " + className, e);
        }
        if (!baseType.isAssignableFrom(clazz)) {
            throw new ReflectionException(className + " must extend or implement " + baseType.getName());
        }
        @SuppressWarnings("unchecked")  // protected by isAssignableFrom check
        final Class<? extends T> castedClass = (Class<? extends T>) clazz;
        return new ConfigurableClass<>(castedClass);
    }

}
