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

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes a Metafacture event stream to a {@link List}.
 *
 * @author Thomas Seidel
 */
class ListTypeEncoder implements TypeEncoder {

	private final ValueType valueType;
	private final List<Object> objects;

	ListTypeEncoder(final ValueType valueType) {
		this.valueType = valueType;
		objects = new ArrayList<>();
	}

	static boolean supportsType(final Class<?> clazz) {
		return List.class.isAssignableFrom(clazz);
	}

	@Override
	public void setValue(final String name, final Object value) {
		objects.add(value);
	}

	@Override
	public ValueType getValueType(final String name) {
		return new ValueType(valueType.getElementClass());
	}

	@Override
	public Object getInstance() {
		return objects;
	}

}
