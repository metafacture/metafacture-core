/*
 * Copyright 2013, 2014 Pascal Christoph, hbz
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
package org.culturegraph.mf.commons.tries;

import java.util.List;

/**
 * A wrapper for the {@link WildcardTrie} enabling the use of simple character
 * classes .
 *
 * @author Pascal Christoph
 *
 * @param <P>
 *            type of value stored
 */
public class SimpleRegexTrie<P> {

	private final WildcardTrie<P> trie;
	public static final String SIMPLE_CHARACTER_CLASS = "\\[.*\\]";

	public SimpleRegexTrie() {
		trie = new WildcardTrie<P>();
	}

	/**
	 * Enables the use of simple character classes like 'a[agt][ac]'. Calls the
	 * method of {@link WildcardTrie} for further treatment.
	 *
	 * @param keys
	 * @param value
	 */
	public void put(final String keys, final P value) {
		if (keys.matches(".*" + SIMPLE_CHARACTER_CLASS + ".*")) {
			int charClassStart = keys.indexOf('[', 0);
			final int charClassEnd = keys.indexOf(']', 1);
			String begin = keys.substring(0, charClassStart);
			for (; charClassStart < charClassEnd - 1; charClassStart++) {
				char middle = keys.charAt(charClassStart + 1);
				String end = keys.substring(charClassEnd + 1, keys.length());
				put(begin + middle + end, value);
			}
		} else
			trie.put(keys, value);
	}

	public List<P> get(final String key) {
		return trie.get(key);
	}

}
