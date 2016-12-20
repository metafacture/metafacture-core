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
 * used to access zero to n {@link Data} instances based on
 * a {@link String} path. Used in {@link Metamorph}
 *
 * @author Markus Michael Geipel
 *
 * @param <T>
 */
interface Registry<T> {

	/**
	 * add an instance of {@link Data} to a path.
	 *
	 * @param path
	 * @param data
	 */
	void register(String path, T value);

	/**
	 * @param path
	 * @return matching {@link Data} instances. Should
	 *         NEVER be <code>null</code>. If no matches
	 *         found, an empty {@link List} is to be returned.
	 */
	List<T> get(String path);

}
