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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides method for creating and initialising classes. The
 * {@link #newInstance(Map, Object...)} method creates an instance of the class
 * using the first constructor that matches the varargs argument of the methods.
 * <p>
 * Instances of this class wrap {@link Class}. The wrapped instance is available
 * via {@link #getPlainClass()}.
 *
 * @param <T> object type
 * @author Christoph Böhme
 */
public final class ConfigurableClass<T> {

    private static final String SETTER_PREFIX = "set";
    private static final Set<Class<?>> ELIGIBLE_TYPES = new HashSet<>(
            Arrays.asList(boolean.class, int.class, String.class));

    private final Class<T> plainClass;

    private Map<String, Method> settersCache;

    /**
     *
     * Creates an instance of {@link ConfigurableClass} defined by a Class.
     *
     * @param plainClass the plain class of object type T
     */
    public ConfigurableClass(final Class<T> plainClass) {
        this.plainClass = plainClass;
    }

    /**
     * Gets the plain class of the ConfigurableClass.
     *
     * @return the Class
     */
    public Class<T> getPlainClass() {
        return plainClass;
    }

    /**
     * Gets all public "set" methods of this class.
     *
     * @return the Map of the setter methods of this class
     */
    public Map<String, Method> getSetters() {
        if (settersCache == null) {
            initSettersCache();
        }
        return settersCache;
    }

    private void initSettersCache() {
        settersCache = new HashMap<>();
        for (final Method method : plainClass.getMethods()) {
            if (isSetter(method)) {
                final String setterName = method.getName().substring(
                        SETTER_PREFIX.length()).toLowerCase();
                settersCache.put(setterName, method);
            }
        }
    }

    private boolean isSetter(final Method method) {
        if (method.getParameterTypes().length == 1) {
            final Class<?> type = method.getParameterTypes()[0];
            if (ELIGIBLE_TYPES.contains(type) || type.isEnum()) {
                return method.getName().startsWith(SETTER_PREFIX);
            }
        }
        return false;
    }

    /**
     * Gets the parameter types of the setter methods.
     *
     * @return a Map of the setter method names and their types
     */
    public Map<String, Class<?>> getSetterTypes() {
        final Map<String, Class<?>> setterTypes = new HashMap<>();
        for (final Map.Entry<String, Method> method : getSetters().entrySet()) {
            final Class<?> setterType = method.getValue().getParameterTypes()[0];
            setterTypes.put(method.getKey(), setterType);
        }
        return setterTypes;
    }

    /**
     * Creates an empty instance of the class.
     *
     * @return a new instance
     */
    public T newInstance() {
        return newInstance(Collections.emptyMap());
    }

    /**
     * Creates an instance of the class using the first constructor that matches the
     * varargs argument of the methods.
     *
     * @param setterValues    the Map of setter values
     * @param constructorArgs the Object of args of the constructor
     * @return the new instance
     */
    public T newInstance(final Map<String, String> setterValues, final Object... constructorArgs) {
        try {
            final Constructor<T> constructor = findConstructor(constructorArgs);
            final T instance = constructor.newInstance(constructorArgs);
            applySetters(instance, setterValues);
            return instance;
        }
        catch (final ReflectiveOperationException e) {
            throw new ReflectionException("class could not be instantiated: " +
                    plainClass, e);
        }
    }

    private Constructor<T> findConstructor(final Object... arguments) throws NoSuchMethodException {
        @SuppressWarnings("unchecked")  // getConstructors() returns correct types
        final Constructor<T>[] constructors =
                (Constructor<T>[]) plainClass.getConstructors();
        for (final Constructor<T> constructor : constructors) {
            if (checkArgumentTypes(constructor, arguments)) {
                return constructor;
            }
        }
        throw new NoSuchMethodException(
                "no appropriate constructor found for class " + plainClass);
    }

    private boolean checkArgumentTypes(final Constructor<?> constructor, final Object[] constructorArgs) { // checkstyle-disable-line ReturnCount
        final Class<?>[] argTypes = constructor.getParameterTypes();
        if (argTypes.length != constructorArgs.length) {
            return false;
        }
        for (int i = 0; i < argTypes.length; ++i) {
            if (!argTypes[i].isAssignableFrom(constructorArgs[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private void applySetters(final T target, final Map<String, String> setterValues) {
        for (final Map.Entry<String, String> setterValue : setterValues.entrySet()) {
            final String setterName = setterValue.getKey().toLowerCase();
            final Method setter = getSetters().get(setterName);
            if (setter == null) {
                throw new ReflectionException("Method " + target.getClass()
                        .getSimpleName() + "." + setterName + " does not exist");
            }
            final Class<?> valueType = setter.getParameterTypes()[0];
            final Object value = convertValue(setterValue.getValue(), valueType);
            try {
                setter.invoke(target, value);
            }
            catch (final ReflectiveOperationException e) {
                throw new ReflectionException("Cannot set " + setterName +
                        " on class " + target.getClass().getSimpleName(), e);
            }
        }
    }

    private <T extends Enum<T>> Object convertValue(final String value, final Class<?> type) {
        final Object result;

        if (type == boolean.class) {
            result = Boolean.valueOf(value);
        }
        else if (type == int.class) {
            result = Integer.valueOf(value);
        }
        else if (type.isEnum()) {
            @SuppressWarnings("unchecked")  // protected by type.isEnum() check
            final Class<T> enumType = (Class<T>) type;
            result = Enum.valueOf(enumType, value.toUpperCase());
        }
        else {
            result = value;
        }

        return result;
    }

}
