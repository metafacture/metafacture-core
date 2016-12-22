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
 * Decodes a simple type into a <i>literal</i> event.
 *
 * @author Thomas Seidel
 */
class SimpleTypeDecoder implements TypeDecoder {

	static boolean supportsType(final Class<?> clazz) {
		return clazz.isPrimitive() || clazz.equals(String.class);
	}

	@Override
	public void decodeToStream(final StreamReceiver streamReceiver,
			final String name, final Object object) {
		streamReceiver.literal(name, object.toString());
	}

}
