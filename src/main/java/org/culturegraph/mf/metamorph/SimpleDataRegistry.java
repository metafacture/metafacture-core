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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link Registry} with a {@link HashMap}.
 *
 * @author Markus Michael Geipel
 *
 * @param <T>
 */
final class SimpleDataRegistry<T> implements Registry<T> {

	private final Map<String, List<T>> map = new HashMap<String, List<T>>();

	@Override
	public void register(final String path, final T value) {
		List<T> matchingData = map.get(path);
		if (matchingData == null) {
			matchingData = new ArrayList<T>();
			map.put(path, matchingData);

		}
		matchingData.add(value);

	}

	@Override
	public List<T> get(final String path) {
		final List<T> matchingData = map.get(path);
		if (matchingData == null) {
			return Collections.emptyList();
		}
		return matchingData;
	}

}
