/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of {@link MultiMap}
 * 
 * @author Markus Michael Geipel
 * 
 */

public class MultiHashMap implements MultiMap{

	private final Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
	


	@Override
	public final Map<String, String> getMap(final String mapName) {
		final Map<String, String> map = data.get(mapName);
		if (map == null) {
			return Collections.emptyMap();
		}
		return map;
	}


	@Override
	public final String getValue(final String mapName, final String key) {
		final Map<String, String> map = getMap(mapName);
		final String value = map.get(key);
		if (value == null) {
			return map.get(MultiMap.DEFAULT_MAP_KEY);
		}
		return value;
	}
	
	@Override
	public final String toString() {
		return data.toString();
	}


	@Override
	public final Map<String, String> putMap(final String mapName, final Map<String, String> map) {
		return data.put(mapName, map);
	}


	@Override
	public final String putValue(final String mapName, final String key, final String value) {
		Map<String, String> map = data.get(mapName);
		if (map == null) {
			map = new HashMap<String, String>();
			data.put(mapName, map);
		}
		return map.put(key, value);
	}


	@Override
	public final Collection<String> getMapNames() {
		return Collections.unmodifiableSet(data.keySet());
	}
	

}
