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
package org.culturegraph.mf.metamorph;

import java.util.List;

/**
 * Used to access zero to n {@link Data} instances based on
 * a {@link String} path. Used in {@link Metamorph}
 *
 * @param <T> type of the values this registry holds.
 * @author Markus Michael Geipel
 */
interface Registry<T> {

	/**
	 * Binds a value to a path.
	 *
	 * @param path the path to which the {@code value} is bound
	 * @param value the value which is bound to {@code path}
	 */
	void register(String path, T value);

	/**
	 * Returns values registered on a path.
	 *
	 * @param path for which the registered values will be returned
	 * @return matching values. Should <strong>never</strong> be null. If no
	 * matches found, an empty {@link List} is to be returned.
	 */
	List<T> get(String path);

}
