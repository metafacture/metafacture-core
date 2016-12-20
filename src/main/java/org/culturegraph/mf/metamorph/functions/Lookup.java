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
package org.culturegraph.mf.metamorph.functions;

/**
 * Uses the received value as a key for looking up
 * the output value in a map.
 *
 * @author Markus Michael Geipel
 */
public final class Lookup extends AbstractLookup {

	private String defaultValue;

	public void setDefault(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String process(final String key) {
		final String returnValue = lookup(key);

		if (returnValue == null) {
			return defaultValue;
		}
		return returnValue;
	}

	public void setIn(final String mapName) {
		setMap(mapName);
	}

}
