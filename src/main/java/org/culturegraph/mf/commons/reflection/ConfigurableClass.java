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
package org.culturegraph.mf.commons.reflection;

import static java.util.Arrays.asList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
 * @author Christoph Böhme
 */
public final class ConfigurableClass<T> {

	private static final String SETTER_PREFIX = "set";
	private static final Set<Class<?>> ELIGIBLE_TYPES = new HashSet<>(
			asList(boolean.class, int.class, String.class));

	private final Class<T> plainClass;

	private Map<String, Method> settersCache;

	public ConfigurableClass(Class<T> plainClass) {
		this.plainClass = plainClass;
	}

	public Class<T> getPlainClass() {
		return plainClass;
	}

	public Map<String, Method> getSetters() {
		if (settersCache == null) {
			initSettersCache();
		}
		return settersCache;
	}

	private void initSettersCache() {
		settersCache = new HashMap<>();
		for (Method method : plainClass.getMethods()) {
			if (isSetter(method)) {
				final String setterName = method.getName().substring(
						SETTER_PREFIX.length()).toLowerCase();
				settersCache.put(setterName, method);
			}
		}
	}

	private boolean isSetter(Method method) {
		if (method.getParameterTypes().length == 1) {
			final Class<?> type = method.getParameterTypes()[0];
			if (ELIGIBLE_TYPES.contains(type) || type.isEnum()) {
				return method.getName().startsWith(SETTER_PREFIX);
			}
		}
		return false;
	}

	public Map<String,Class<?>> getSetterTypes() {
		final Map<String, Class<?>> setterTypes = new HashMap<>();
		for(Map.Entry<String, Method> method : getSetters().entrySet()) {
			final Class<?> setterType = method.getValue().getParameterTypes()[0];
			setterTypes.put(method.getKey(), setterType);
		}
		return setterTypes;
	}

	public T newInstance() {
		return newInstance(Collections.emptyMap());
	}

	public T newInstance(Map<String, String> setterValues,
			Object... constructorArgs) {
		try {
			final Constructor<T> constructor = findConstructor(constructorArgs);
			final T instance = constructor.newInstance(constructorArgs);
			applySetters(instance, setterValues);
			return instance;
		} catch (ReflectiveOperationException e) {
			throw new ReflectionException("class could not be instantiated: " +
					plainClass, e);
		}
	}

	private Constructor<T> findConstructor(Object... arguments)
			throws NoSuchMethodException{
		@SuppressWarnings("unchecked")  // getConstructors() returns correct types
		final Constructor<T>[] constructors =
				(Constructor<T>[]) plainClass.getConstructors();
		for (Constructor<T> constructor : constructors) {
			if (checkArgumentTypes(constructor, arguments)) {
				return constructor;
			}
		}
		throw new NoSuchMethodException(
				"no appropriate constructor found for class " + plainClass);
	}

	private boolean checkArgumentTypes(Constructor<?> constructor,
			Object[] constructorArgs) {
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

	private void applySetters(T target, Map<String, String> setterValues) {
		for (Map.Entry<String, String> setterValue : setterValues.entrySet()) {
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
			} catch (ReflectiveOperationException e) {
				throw new ReflectionException("Cannot set " + setterName +
						" on class " + target.getClass().getSimpleName(), e);
			}
		}
	}

	private Object convertValue(String value, Class<?> type) {
		if (type == boolean.class) {
			return Boolean.valueOf(value);
		}
		if (type == int.class) {
			return Integer.valueOf(value);
		}
		if (type.isEnum()) {
			@SuppressWarnings("unchecked")  // protected by type.isEnum() check
			final Class<Enum> enumType = (Class<Enum>) type;
			return Enum.valueOf(enumType, value.toUpperCase());
		}
		return value;
	}

}
