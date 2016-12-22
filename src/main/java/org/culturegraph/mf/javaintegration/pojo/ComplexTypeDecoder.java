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
import java.util.ArrayList;
import java.util.List;

import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Decodes a POJO to a Metafacture event stream.
 *
 * @author Thomas Seidel
 */
class ComplexTypeDecoder implements TypeDecoder {

	private final TypeDecoderFactory typeDecoderFactory;
	private final List<ValueGetter> valueGetters;

	ComplexTypeDecoder(final Class<?> clazz,
			final TypeDecoderFactory typeDecoderFactory) {
		this.typeDecoderFactory = typeDecoderFactory;
		valueGetters = new ArrayList<>();
		addFieldValueGettersFor(clazz);
		addMethodValueGettersFor(clazz);
	}

	private void addFieldValueGettersFor(Class<?> clazz) {
		final Field[] fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			if (FieldValueGetter.supportsField(field)) {
				valueGetters.add(new FieldValueGetter(field));
			}
		}
	}

	private void addMethodValueGettersFor(Class<?> clazz) {
		final Method[] methods = clazz.getDeclaredMethods();
		for (final Method method : methods) {
			if (MethodValueGetter.supportsMethod(method)) {
				valueGetters.add(new MethodValueGetter(method));
			}
		}
	}

	static boolean supportsType(final Class<?> clazz) {
		return !SimpleTypeDecoder.supportsType(clazz)
				&& !MetafactureSourceTypeDecoder.supportsType(clazz)
				&& !CollectionTypeDecoder.supportsType(clazz)
				&& !ArrayTypeDecoder.supportsType(clazz)
				&& !MapTypeDecoder.supportsType(clazz);
	}

	@Override
	public void decodeToStream(final StreamReceiver streamReceiver,
			final String name, final Object object) {

		if (name != null) {
			streamReceiver.startEntity(name);
		}
		for (final ValueGetter valueGetter : valueGetters) {
			final Object value = valueGetter.getValue(object);
			final Class<?> valueType = valueGetter.getValueType();
			final String valueName = valueGetter.getName();
			final TypeDecoder typeDecoder = typeDecoderFactory.create(valueType);
			typeDecoder.decodeToStream(streamReceiver, valueName, value);
		}
		if (name != null) {
			streamReceiver.endEntity();
		}
	}

}
