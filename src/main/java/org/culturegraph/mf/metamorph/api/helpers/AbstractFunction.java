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
package org.culturegraph.mf.metamorph.api.helpers;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.metamorph.api.Function;
import org.culturegraph.mf.metamorph.api.Maps;

/**
 * Base class for functions.
 *
 * @author Markus Michael Geipel
 */
public abstract class AbstractFunction extends AbstractNamedValuePipe
		implements Function {

	private Maps maps;
	private Map<String, String> localMap;
	private String mapName;

	protected final String getValue(final String mapName, final String key) {
		return maps.getValue(mapName, key);
	}

	protected final String getLocalValue(final String key) {
		if (localMap != null) {
			return localMap.get(key);
		}
		return null;
	}

	public final String getMapName() {
		return mapName;
	}

	public final Map<String, String> getMap() {
		if (localMap == null) {
			return maps.getMap(mapName);
		}
		return localMap;
	}

	public final void setMap(final String mapName) {
		this.mapName = mapName;
	}

	@Override
	public final void putValue(final String key, final String value) {
		if (localMap == null) {
			localMap = new HashMap<String, String>();
		}
		localMap.put(key, value);
	}

	public final void setMaps(final Maps maps) {
		this.maps = maps;
	}

	@Override
	public void flush(final int recordCount, final int entityCount) {
		// Does nothing by default
	}

}
