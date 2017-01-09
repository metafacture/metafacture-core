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

import org.culturegraph.mf.commons.tries.SimpleRegexTrie;
import org.culturegraph.mf.commons.tries.WildcardTrie;

/**
 * Implements {@link Registry} with a {@link WildcardTrie}.
 *
 * @param <T> type of the values this registry stores
 * @author Markus Michael Geipel
 */
final class WildcardRegistry<T> implements Registry<T> {

	private final SimpleRegexTrie<T> trie = new SimpleRegexTrie<T>();

	@Override
	public void register(final String path, final T value) {
		trie.put(path, value);

	}

	@Override
	public List<T> get(final String path) {
		return trie.get(path);
	}

}
