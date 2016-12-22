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
 * Retrieves a property of an object by calling a getter method.
 *
 * @author Thomas Seidel
 */
class MethodValueGetter implements ValueGetter {

	private static final String METHOD_PREFIX = "get";

	private final Method method;
	private final String name;

	MethodValueGetter(final Method method) {
		assert supportsMethod(method);
		this.method = method;
		// remove prefix then lower case first character
		name = Introspector.decapitalize(method.getName().substring(
				METHOD_PREFIX.length()));
	}

	static boolean supportsMethod(final Method m) {
		return Modifier.isPublic(m.getModifiers())
				&& m.getName().length() > METHOD_PREFIX.length()
				&& m.getName().startsWith(METHOD_PREFIX);
	}

	@Override
	public Object getValue(final Object object) {
		try {
			return method.invoke(object);
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
	public Class<?> getValueType() {
		return method.getReturnType();
	}

}
