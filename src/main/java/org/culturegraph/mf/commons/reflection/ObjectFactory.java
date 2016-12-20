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
package org.culturegraph.mf.commons.reflection;

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

	public final void loadClassesFromMap(Map<?, ?> classMap, Class<T> baseType) {
		final ClassLoader loader = ReflectionUtil.getContextClassLoader();
		for (final Entry<?, ?> entry : classMap.entrySet()) {
			final String key = entry.getKey().toString();
			final String className = entry.getValue().toString();
			registerClass(key, ReflectionUtil.loadClass(loader, className, baseType));
		}
	}

	public final void registerClass(String key, Class<? extends T> objectClass) {
		registerClass(key, new ConfigurableClass<>(objectClass));
	}

	public final void registerClass(String key,
			ConfigurableClass<? extends T> objectClass) {
		classes.put(key, objectClass);
	}

	public final T newInstance(String key, Object... constructorArgs) {
		return newInstance(key, Collections.emptyMap(), constructorArgs);
	}

	public final T newInstance(String key, Map<String, String> values,
			Object... constructorArgs) {
		if (!classes.containsKey(key)) {
			throw new NoSuchElementException("no registered class for: " + key);
		}
		final ConfigurableClass<? extends T> instanceClass = classes.get(key);
		return instanceClass.newInstance(values, constructorArgs);
	}

	public final boolean containsKey(String key) {
		return classes.containsKey(key);
	}

	public final Set<String> keySet() {
		return Collections.unmodifiableSet(classes.keySet());
	}

	public final ConfigurableClass<? extends T> get(String key) {
		return classes.get(key);
	}

}
