/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.commons.types;

/**
 * Stores an immutable name-value-pair. The hash code is
 * precomputed during instantiation.
 *
 * @author Markus Michael Geipel
 */
public final class NamedValue  implements Comparable<NamedValue> {

	private static final int MAGIC1 = 23;
	private static final int MAGIC2 = 31;
	private final String name;
	private final String value;
	private final int preCompHashCode;

	public NamedValue(final String name, final String value) {
		this.name = name;
		this.value = value;
		int result = MAGIC1;
		result = MAGIC2 * result + value.hashCode();
		result = MAGIC2 * result + name.hashCode();
		preCompHashCode = result;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return preCompHashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof NamedValue) {
			final NamedValue namedValue = (NamedValue) obj;
			return namedValue.preCompHashCode == preCompHashCode
					&& namedValue.name.equals(name)
					&& namedValue.value.equals(value);
		}
		return false;
	}

	@Override
	public int compareTo(final NamedValue namedValue) {
		final int first = name.compareTo(namedValue.name);
		if (first == 0) {
			return value.compareTo(namedValue.value);
		}
		return first;
	}

	@Override
	public String toString() {
		return name + ":" + value;
	}
}
