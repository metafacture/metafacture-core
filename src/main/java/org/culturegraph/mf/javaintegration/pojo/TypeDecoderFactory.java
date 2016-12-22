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

import org.culturegraph.mf.framework.MetafactureException;

/**
 * Returns a decoder for a given class.
 *
 * @author Thomas Seidel
 */
class TypeDecoderFactory {

	private final Map<Class<?>, TypeDecoder> typeDecoders = new HashMap<>();

	TypeDecoder create(final Class<?> clazz) {
		if (typeDecoders.containsKey(clazz)) {
			return typeDecoders.get(clazz);
		}
		final TypeDecoder typeDecoder;
		if (SimpleTypeDecoder.supportsType(clazz)) {
			typeDecoder = new SimpleTypeDecoder();
		} else if (MetafactureSourceTypeDecoder.supportsType(clazz)) {
			typeDecoder = new MetafactureSourceTypeDecoder();
		} else if (CollectionTypeDecoder.supportsType(clazz)) {
			typeDecoder = new CollectionTypeDecoder(this);
		} else if (ArrayTypeDecoder.supportsType(clazz)) {
			typeDecoder = new ArrayTypeDecoder(this);
		} else if (ComplexTypeDecoder.supportsType(clazz)) {
			typeDecoder = new ComplexTypeDecoder(clazz, this);
		} else if (MapTypeDecoder.supportsType(clazz)) {
			typeDecoder = new MapTypeDecoder(this);
		} else {
			throw new MetafactureException("Can't decode type " + clazz);
		}
		typeDecoders.put(clazz, typeDecoder);
		return typeDecoder;
	}

}
