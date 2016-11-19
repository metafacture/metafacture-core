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
package org.culturegraph.mf.util.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.culturegraph.mf.framework.MetafactureException;


/**
 * Provides instances of preregistered classes. New classes can be registered
 * during runtime. This class is not thread-safe.
 *
 * @param <O> the type of objects created
 * @author Markus Michael Geipel
 */
public class ObjectFactory<O> {

	private static final String INSTANTIATION_PROBLEM =
			" could not be instantiated";
	private static final String SETTER_PREFIX = "set";
	private static final Set<Class<?>> ELIGIBLE_TYPES = new HashSet<>();
	static {
		ELIGIBLE_TYPES.add(boolean.class);
		ELIGIBLE_TYPES.add(int.class);
		ELIGIBLE_TYPES.add(String.class);
	}

	private final Map<String, Class<? extends O>> classes = new HashMap<>();
	private final Map<Class<? extends O>, Map<String, Method>> classMethodMaps =
			new HashMap<>();
	private final Set<String> availableClasses = Collections.unmodifiableSet(
			classes.keySet());


	public final void registerClass(final String key,
			final Class<? extends O> clazz) {
		classes.put(key, clazz);
		classMethodMaps.put(clazz, extractMethods(clazz));
	}

	private static Map<String, Method> extractMethods(final Class<?> clazz) {
		final Map<String, Method> methodMap = new HashMap<>();
		for (final Method method : clazz.getMethods()) {
			if (methodIsEligible(method)) {
				final String methodName = method.getName().substring(
						SETTER_PREFIX.length()).toLowerCase();
				methodMap.put(methodName, method);
			}
		}
		return methodMap;
	}

	private static boolean methodIsEligible(final Method method) {
		if (method.getParameterTypes().length == 1) {
			final Class<?> type = method.getParameterTypes()[0];
			if (ELIGIBLE_TYPES.contains(type) || type.isEnum()) {
				return method.getName().startsWith(SETTER_PREFIX);
			}
		}
		return false;
	}

	public final Class<? extends O> getClass(final String name) {
		return classes.get(name);
	}

	public final Map<String, Class<?>> getAttributes(final String classKey) {
		final Class<? extends O> clazz = classes.get(classKey);
		if (clazz != null) {
			final Map<String, Class<?>> attributes = new HashMap<>();
			final Set<Entry<String, Method>> entrySet = classMethodMaps.get(clazz)
					.entrySet();
			for(final Entry<String, Method> entry: entrySet) {
				attributes.put(entry.getKey(), entry.getValue().getParameterTypes()[0]);
			}
			return attributes;
		}
		return Collections.emptyMap();
	}


	public final Set<String> keySet() {
		return availableClasses;
	}

	public final boolean containsKey(final String name) {
		return availableClasses.contains(name);
	}

	public final O newInstance(final String name,
			final Object... constructorArgs) {
		return newInstance(name, Collections.emptyMap(), constructorArgs);
	}

	public final O newInstance(final String name,
			final Map<String, String> attributes, final Object... contructorArgs) {
		final Class<? extends O> clazz = classes.get(name);
		if (clazz == null) {
			throw new MetafactureException("no registered class for '" + name + "'");
		}
		final O instance = newInstance(clazz, contructorArgs);
		applySetters(instance, classMethodMaps.get(clazz), attributes);
		return instance;

	}

	private static Constructor<?> findConstructor(final Class<?> clazz,
			final Object... contructorArgs) throws NoSuchMethodException{
		for (final Constructor<?> constructor : clazz.getConstructors()) {
			final Class<?>[] argTypes = constructor.getParameterTypes();
			if (argTypes.length == contructorArgs.length) {
				boolean correct = true;
				for (int i = 0; i < argTypes.length; ++i) {
					final Class<?> argType = argTypes[i];
					final Class<?> inputArgType = contructorArgs[i].getClass();
					if(!argType.isAssignableFrom(inputArgType)) {
						correct = false;
						break;
					}
				}
				if(correct) {
					return constructor;
				}
			}
		}
		throw new NoSuchMethodException(
				"no appropriate constructor found for class " + clazz);
	}

	@SuppressWarnings("unchecked")
	public static <O> O newInstance(final Class<O> clazz,
			final Object... contructorArgs) {
		try {
			final Constructor<?> constructor = findConstructor(clazz, contructorArgs);
			return (O)constructor.newInstance(contructorArgs);
		} catch (final InstantiationException | SecurityException |
				IllegalArgumentException | IllegalAccessException |
				InvocationTargetException | NoSuchMethodException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		}
	}

	public static <O> void applySetters(final O instance,
			final Map<String, String> attributes) {
		applySetters(instance, extractMethods(instance.getClass()), attributes);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" }) // OK, because type.isEnum() is checked before casting to Enum
	private static <O> void applySetters(final O instance,
			final Map<String, Method> methodMap, final Map<String, String> attributes) {

		for (final Map.Entry<String, String> attribute : attributes.entrySet()) {
			final String methodName = attribute.getKey().toLowerCase();
			final Method method = methodMap.get(methodName);
			if (null == method) {
				throw new MetafactureException("Method '" + methodName +
						"' does not exist in '" + instance.getClass().getSimpleName() +
						"'!");
			}
			final Class<?> type = method.getParameterTypes()[0];

			try {
				if (type == boolean.class) {
					method.invoke(instance, Boolean.valueOf(attribute.getValue()));
				} else if (type == int.class) {
					method.invoke(instance, Integer.valueOf(attribute.getValue()));
				} else if (type.isEnum()) {
					method.invoke(instance, Enum.valueOf((Class<Enum>)type,
							attribute.getValue().toUpperCase()));
				}else {
					method.invoke(instance, attribute.getValue());
				}
			} catch (final IllegalArgumentException | IllegalAccessException |
					InvocationTargetException e) {
				setMethodError(methodName, instance.getClass().getSimpleName(), e);
			}
		}
	}

	private static void setMethodError(final String methodName,
			final String simpleName, final Exception exc) {
		throw new MetafactureException("Cannot set '" + methodName +
				"' for class '" + simpleName + "'", exc);
	}

	@SuppressWarnings("unchecked")
	// protected by 'if (type.isAssignableFrom(clazz)) {'
	public static <O> Class<? extends O> loadClass(final String className,
			final Class<O> baseType) {
		final Class<?> clazz;
		try {
			final ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader == null) {
        throw new MetafactureException("Class loader could not be found.");
      }
			clazz = loader.loadClass(className);
		} catch (final ClassNotFoundException e) {
			throw new MetafactureException(className + " not found.", e);
		}

		if (baseType.isAssignableFrom(clazz)) {
			return (Class<? extends O>) clazz;
		}
		throw new MetafactureException(className + " must extend or implement " +
				baseType.getName());

	}

	@SuppressWarnings("unchecked")
	// protected by 'if (type.isAssignableFrom(clazz)) {'
	public final void loadClassesFromMap(final Map<?, ?> properties,
			final Class<O> type) {

		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			throw new MetafactureException("Class loader could not be found.");
		}
		for (final Entry<?, ?> entry : properties.entrySet()) {
			final String className = entry.getValue().toString();
			final String name = entry.getKey().toString();

			try {
				final Class<?> clazz = loader.loadClass(className);
				if (type.isAssignableFrom(clazz)) {

					registerClass(name, (Class<? extends O>) clazz);

				} else {
					throw new MetafactureException(className + " does not implement " +
							type.getName() + " registration with " +
							this.getClass().getSimpleName() + " failed.");
				}
			} catch (final ClassNotFoundException e) {
				throw new MetafactureException(className + " not found", e);
			}
		}
	}

}
