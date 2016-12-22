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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Represents the type of a value.
 *
 * @author Thomas Seidel
 */
class ValueType {

	private final Class<?> rawClass;
	private final Class<?> elementClass;

	ValueType(final Class<?> clazz) {
		rawClass = clazz;
		elementClass = null;
	}

	ValueType(final Class<?> clazz, final Type type) {
		rawClass = clazz;
		if (type instanceof ParameterizedType) {
			final Type[] actualTypeArguments = ((ParameterizedType) type)
					.getActualTypeArguments();
			if (Map.class.isAssignableFrom(clazz)) {
				elementClass = (Class<?>) actualTypeArguments[1];
			} else {
				elementClass = (Class<?>) actualTypeArguments[0];
			}
		} else {
			elementClass = null;
		}
	}

	Class<?> getRawClass() {
		return rawClass;
	}

	Class<?> getElementClass() {
		return elementClass;
	}

}
