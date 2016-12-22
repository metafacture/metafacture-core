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

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.culturegraph.mf.framework.MetafactureException;

/**
 * Invokes a setter method on an object. The method must be public, have exactly
 * one argument and its name must begin with &quot;set&quto;.
 *
 * @author Thomas Seidel
 */
class MethodValueSetter implements ValueSetter {

	private static final String METHOD_PREFIX = "set";

	private final String name;
	private final Method method;

	MethodValueSetter(final Method method) {
		assert supportsMethod(method);
		this.method = method;
		// remove prefix then lower case first character
		name = Introspector.decapitalize(method.getName().substring(
				METHOD_PREFIX.length()));
	}

	static boolean supportsMethod(final Method m) {
		return Modifier.isPublic(m.getModifiers())
				&& m.getName().length() > METHOD_PREFIX.length()
				&& m.getName().startsWith(METHOD_PREFIX)
				&& m.getParameterTypes().length == 1;
	}

	@Override
	public void setValue(final Object object, final Object value) {
		try {
			method.invoke(object, value);
		} catch (final IllegalArgumentException e) {
			throw new MetafactureException(
					"The given object don't have a method named "
							+ method.getName(), e);
		} catch (final IllegalAccessException e) {
			throw new MetafactureException("Can't access the method named "
					+ method.getName(), e);
		} catch (final InvocationTargetException e) {
			throw new MetafactureException("Invoking the method named "
					+ method.getName() + " throws an excpetion", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ValueType getValueType() {
		return new ValueType(method.getParameterTypes()[0],
				method.getGenericParameterTypes()[0]);
	}

}
