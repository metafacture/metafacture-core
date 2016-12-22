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
import java.lang.reflect.Modifier;

import org.culturegraph.mf.framework.MetafactureException;

/**
 * Sets the value of a field. The field must be publicly accessible.
 *
 * @author Thomas Seidel
 */
class FieldValueSetter implements ValueSetter {

	private final Field field;

	FieldValueSetter(final Field field) {
		assert supportsField(field);
		this.field = field;
	}

	static boolean supportsField(final Field f) {
		return Modifier.isPublic(f.getModifiers());
	}

	@Override
	public void setValue(final Object object, final Object value) {
		try {
			field.set(object, value);
		} catch (final IllegalArgumentException e) {
			throw new MetafactureException(
					"The given object don't have a field named "
							+ field.getName(), e);
		} catch (final IllegalAccessException e) {
			throw new MetafactureException("Can't access the field named "
					+ field.getName(), e);
		}
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public ValueType getValueType() {
		return new ValueType(field.getType(), field.getGenericType());
	}

}
