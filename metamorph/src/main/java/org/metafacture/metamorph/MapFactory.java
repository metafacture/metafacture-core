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

package org.metafacture.metamorph;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.commons.reflection.ObjectFactory;
import org.metafacture.framework.MetafactureException;

import java.io.IOException;
import java.util.Map;

/**
 * Creates the maps available in Metamorph.
 *
 * @author Markus Michael Geipel
 *
 */
final class MapFactory extends ObjectFactory<Map> {

    MapFactory() {
        try {
            loadClassesFromMap(ResourceUtil.loadProperties("morph-maps.properties"), Map.class);
        }
        catch (final IOException e) {
            throw new MetafactureException("Failed to load maps list", e);
        }
    }

}
