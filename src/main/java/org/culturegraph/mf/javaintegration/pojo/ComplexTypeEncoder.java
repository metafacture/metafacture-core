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
package org.culturegraph.mf.javaintegration.pojo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.MetafactureException;

/**
 * Encodes a Metafacture event stream to a POJO.
 *
 * @author Thomas Seidel
 */
class ComplexTypeEncoder implements TypeEncoder {

	private final Object instance;
	private final Map<String, ValueSetter> valueSetters;

	ComplexTypeEncoder(final Class<?> clazz) {
		assert supportsType(clazz);
		instance = createInstance(clazz);
		valueSetters = new HashMap<>();
		addFieldValueSettersFor(clazz);
		addMethodValueSettersFor(clazz);
	}

	private static Object createInstance(final Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (final Exception e) {
			throw new MetafactureException(
					"Can't instantiate object of class: " + clazz, e);
		}
	}

	private void addFieldValueSettersFor(Class<?> clazz) {
		final Field[] fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			if (FieldValueSetter.supportsField(field)) {
				final FieldValueSetter fieldValueSetter = new FieldValueSetter(
						field);
				valueSetters.put(fieldValueSetter.getName(),
						fieldValueSetter);
			}
		}
	}

	private void addMethodValueSettersFor(Class<?> clazz) {
		final Method[] methods = clazz.getDeclaredMethods();
		for (final Method method : methods) {
			if (MethodValueSetter.supportsMethod(method)) {
				final MethodValueSetter methodValueSetter = new MethodValueSetter(
						method);
				valueSetters.put(methodValueSetter.getName(),
						methodValueSetter);
			}
		}
	}

	static boolean supportsType(final Class<?> clazz) {
		return !clazz.isPrimitive() && !clazz.equals(String.class)
				&& !MapTypeEncoder.supportsType(clazz)
				&& !ListTypeEncoder.supportsType(clazz);
	}

	@Override
	public void setValue(final String name, final Object value) {
		final ValueSetter valueSetter = valueSetters.get(name);
		valueSetter.setValue(instance, value);
	}

	@Override
	public ValueType getValueType(final String name) {
		final ValueSetter valueSetter = valueSetters.get(name);
		if (valueSetter == null) {
			throw new MetafactureException("There is no attribute with name " + name);
		}
		return valueSetter.getValueType();
	}

	@Override
	public Object getInstance() {
		return instance;
	}

}
