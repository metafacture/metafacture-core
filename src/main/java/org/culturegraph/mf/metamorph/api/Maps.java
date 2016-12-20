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
package org.culturegraph.mf.metamorph.api;

import java.util.Collection;
import java.util.Map;

/**
 * Provides access to the maps defined in a Metamorph script.
 *
 * @author Markus Michael Geipel
 *
 */
public interface Maps {

	String DEFAULT_MAP_KEY = "__default";

	Collection<String> getMapNames();

	Map<String, String> getMap(String mapName);

	String getValue(String mapName, String key);

	Map<String, String> putMap(String mapName, Map<String, String> map);

	String putValue(String mapName, String key, String value);

}
