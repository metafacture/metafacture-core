/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import org.culturegraph.mf.exceptions.MetafactureException;


/**
 * Provides instances of preregistered classes. New classes can be registered
 * during runtime.
 * 
 * @author Markus Michael Geipel
 * @param <O>
 *            the type of objects created
 * 
 */

// TODO make Threadsafe
public class ObjectFactory<O> {

	private static final String INSTANTIATION_PROBLEM = " could not be instantiated";

	private static final String SET = "set";

	private static final Set<Class<?>> ELIGIBLE_TYPES = new HashSet<Class<?>>();

	private final Map<String, Class<? extends O>> classes = new HashMap<String, Class<? extends O>>();
	private final Map<Class<? extends O>, Map<String, Method>> classMethodMaps = new HashMap<Class<? extends O>, Map<String, Method>>();
	// private final SimpleMultiMap defaultAttributes = new MultiMap();

	private final Set<String> availableClasses = Collections.unmodifiableSet(classes.keySet());

	static {
		ELIGIBLE_TYPES.add(boolean.class);
		ELIGIBLE_TYPES.add(int.class);
		ELIGIBLE_TYPES.add(String.class);
	}

	public final void registerClass(final String key, final Class<? extends O> clazz) {
		classes.put(key, clazz);
		classMethodMaps.put(clazz, extractMethods(clazz));
	}

	private static Map<String, Method> extractMethods(final Class<?> clazz) {
		final Map<String, Method> methodMap = new HashMap<String, Method>();
		for (Method method : clazz.getMethods()) {
			if (methodIsEligible(method)) {
				final String methodName = method.getName().substring(SET.length()).toLowerCase();
				methodMap.put(methodName, method);
			}
		}
		return methodMap;
	}

	private static boolean methodIsEligible(final Method method) {
		if (method.getParameterTypes().length == 1) {
			final Class<?> type = method.getParameterTypes()[0];
			if (ELIGIBLE_TYPES.contains(type)) {
				return method.getName().startsWith(SET);
			}
		}
		return false;
	}

	public final Class<? extends O> getClass(final String name) {
		return classes.get(name);
	}

	public final Set<String> getAttributes(final String key) {
		if (classes.containsKey(key)) {
			return Collections.unmodifiableSet(classMethodMaps.get(classes.get(key)).keySet());
		}
		return Collections.emptySet();
	}

	public final Set<String> keySet() {
		return availableClasses;
	}

	public final boolean containsKey(final String name) {
		return availableClasses.contains(name);
	}

	public final O newInstance(final String name, final Object... contructorArgs) {
		return newInstance(name, Collections.<String, String> emptyMap(), contructorArgs);
	}

	public final O newInstance(final String name, final Map<String, String> attributes, final Object... contructorArgs) {
		if (!classes.containsKey(name)) {
			throw new MetafactureException("no registered class for '" + name + "'");
		}

		final Class<? extends O> clazz = classes.get(name);

		final O instance = newInstance(clazz, contructorArgs);
		applySetters(instance, classMethodMaps.get(clazz), attributes);
		return instance;

	}
	
	private static Constructor<?> findConstructor(final Class<?> clazz, final Object... contructorArgs) throws NoSuchMethodException{
		for (Constructor<?> constructor : clazz.getConstructors()) {
			final Class<?>[] argTypes = constructor.getParameterTypes();
			boolean correct = true;
			if (argTypes.length == contructorArgs.length) {
				for (int i = 0; i < argTypes.length; ++i) {
					final Class<?> argType = argTypes[i];
					final Class<?> inputArgType = contructorArgs[i].getClass();
					if(!argType.isAssignableFrom(inputArgType)){
						correct = false;
						break;
					}						
				}
				if(correct){
					return constructor;
				}
			}
		}
		throw new NoSuchMethodException("no appropriate constructor found for class " + clazz);
	}

	//public static <O> O newInstance(final Class<? extends O> clazz, final Object... contructorArgs) {
	@SuppressWarnings("unchecked")
	public static <O> O newInstance(final Class<O> clazz, final Object... contructorArgs) {
		try {
			final Class<?>[] contructorArgTypes = new Class[contructorArgs.length];
			for (int i = 0; i < contructorArgs.length; ++i) {
				contructorArgTypes[i] = contructorArgs[i].getClass();
			}

			final Constructor<?> constructor = findConstructor(clazz, contructorArgs); 
		
			return (O)constructor.newInstance(contructorArgs);

		} catch (InstantiationException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		} catch (SecurityException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		} catch (NoSuchMethodException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		} catch (IllegalArgumentException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		} catch (IllegalAccessException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		} catch (InvocationTargetException e) {
			throw new MetafactureException(clazz + INSTANTIATION_PROBLEM, e);
		}
	}

	public static <O> void applySetters(final O instance, final Map<String, String> attributes) {
		applySetters(instance, extractMethods(instance.getClass()), attributes);
	}

	private static <O> void applySetters(final O instance, final Map<String, Method> methodMap,
			final Map<String, String> attributes) {

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			final String methodName = attribute.getKey().toLowerCase();
			final Method method = methodMap.get(methodName);
			if (null == method) {
				throw new MetafactureException("Method '" + methodName + "' does not exist in '"
						+ instance.getClass().getSimpleName() + "'!");
			}
			final Class<?> type = method.getParameterTypes()[0];

			try {
				if (type == boolean.class) {
					method.invoke(instance, Boolean.valueOf(attribute.getValue()));
				} else if (type == int.class) {
					method.invoke(instance, Integer.valueOf(attribute.getValue()));
				} else {
					method.invoke(instance, attribute.getValue());
				}
			} catch (IllegalArgumentException e) {
				setMethodError(methodName, instance.getClass().getSimpleName(), e);
			} catch (IllegalAccessException e) {
				setMethodError(methodName, instance.getClass().getSimpleName(), e);
			} catch (InvocationTargetException e) {
				setMethodError(methodName, instance.getClass().getSimpleName(), e);
			}
		}
	}

	private static void setMethodError(final String methodName, final String simpleName, final Exception exc) {
		throw new MetafactureException("Cannot set '" + methodName + "' for class '" + simpleName + "'", exc);
	}

	@SuppressWarnings("unchecked")
	// protected by 'if (type.isAssignableFrom(clazz)) {'
	public static <O> Class<? extends O> loadClass(final String className, final Class<O> baseType) {
		Class<?> clazz;
		try {
			clazz = ReflectionUtil.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new MetafactureException(className + " not found.", e);
		}

		if (baseType.isAssignableFrom(clazz)) {
			return (Class<? extends O>) clazz;
		}
		throw new MetafactureException(className + " must extend or implement " + baseType.getName());

	}

	@SuppressWarnings("unchecked")
	// protected by 'if (type.isAssignableFrom(clazz)) {'
	public final void loadClassesFromMap(final Map<?, ?> properties, final Class<O> type) {

		final ClassLoader loader = ReflectionUtil.getClassLoader();
		for (Entry<?, ?> entry : properties.entrySet()) {
			final String className = entry.getValue().toString();
			final String name = entry.getKey().toString();

			try {
				final Class<?> clazz = loader.loadClass(className);
				if (type.isAssignableFrom(clazz)) {

					registerClass(name, (Class<? extends O>) clazz);

				} else {
					throw new MetafactureException(className + " does not implement " + type.getName()
							+ " registration with " + this.getClass().getSimpleName() + " failed.");
				}
			} catch (ClassNotFoundException e) {
				throw new MetafactureException(className + " not found", e);
			}
		}
	}
}
