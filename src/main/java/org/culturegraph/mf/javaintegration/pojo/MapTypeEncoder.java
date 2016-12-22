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

import java.util.HashMap;
import java.util.Map;

/**
 * Encodes a Metafacture event stream to a {@link Map}.
 *
 * @author Thomas Seidel
 */
class MapTypeEncoder implements TypeEncoder {

	private final ValueType valueType;
	private final Map<String, Object> objectMap;

	MapTypeEncoder(final ValueType valueType) {
		this.valueType = valueType;
		objectMap = new HashMap<>();
	}

	static boolean supportsType(final Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}

	@Override
	public void setValue(final String name, final Object value) {
		objectMap.put(name, value);
	}

	@Override
	public ValueType getValueType(final String name) {
		return new ValueType(valueType.getElementClass());
	}

	@Override
	public Object getInstance() {
		return objectMap;
	}

}
