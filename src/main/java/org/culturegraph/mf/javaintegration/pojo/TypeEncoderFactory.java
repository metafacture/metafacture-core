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

import java.util.List;
import java.util.Map;

import org.culturegraph.mf.framework.MetafactureException;

/**
 * Returns {@link TypeEncoder}s for POJOs, {@link Map}s and {@link List}s.
 *
 * @author Thomas Seidel
 */
class TypeEncoderFactory {

	TypeEncoder create(final ValueType valueType) {
		final TypeEncoder typeEncoder;
		final Class<?> rawClass = valueType.getRawClass();
		if (MapTypeEncoder.supportsType(rawClass)) {
			typeEncoder = new MapTypeEncoder(valueType);
		} else if (ListTypeEncoder.supportsType(rawClass)) {
			typeEncoder = new ListTypeEncoder(valueType);
		} else if (ComplexTypeEncoder.supportsType(rawClass)) {
			typeEncoder = new ComplexTypeEncoder(rawClass);
		} else {
			throw new MetafactureException("Can't encode type " + rawClass);
		}
		return typeEncoder;
	}

}
