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

package org.metafacture.metamorph.api.helpers;

import org.metafacture.metamorph.api.Function;
import org.metafacture.metamorph.api.Maps;

import java.util.HashMap;
import java.util.Map;

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

    protected final String getValue(final String currentMapName, final String key) {
        return maps.getValue(currentMapName, key);
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

    /**
     * Gets the {@link #localMap} or a Map with the name of {@link #mapName}.
     *
     * @return the Map
     */
    public final Map<String, String> getMap() {
        if (localMap == null) {
            return maps.getMap(mapName);
        }
        return localMap;
    }

    public final void setMap(final String newMapName) {
        mapName = newMapName;
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
