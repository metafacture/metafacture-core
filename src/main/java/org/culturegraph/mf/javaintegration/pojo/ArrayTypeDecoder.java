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

import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Decodes a java array to a Metafacture event stream.
 *
 * @author Thomas Seidel
 */
class ArrayTypeDecoder implements TypeDecoder {

	private final TypeDecoderFactory typeDecoderFactory;

	ArrayTypeDecoder(final TypeDecoderFactory typeDecoderFactory) {
		this.typeDecoderFactory = typeDecoderFactory;
	}

	static boolean supportsType(final Class<?> clazz) {
		return clazz.isArray();
	}

	@Override
	public void decodeToStream(final StreamReceiver streamReceiver,
			final String name, final Object object) {
		final Object[] array = (Object[]) object;
		for (final Object element : array) {
			final TypeDecoder typeDecoder = typeDecoderFactory
					.create(element.getClass());
			typeDecoder.decodeToStream(streamReceiver, name, element);
		}
	}

}
