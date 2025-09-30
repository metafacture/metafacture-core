/*
 * Copyright 2025 hbz NRW
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

package org.metafacture.metafix.method.script;

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metafix.maps.RdfMap;
import org.metafacture.metamorph.api.Maps;

import java.util.List;
import java.util.Map;

public class PutRdfMap implements FixFunction {

    /**
     * Creates an instance of {@link PutRdfMap}.
     */
    public PutRdfMap() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final String fileName = params.get(0);
        final RdfMap rdfMap = new RdfMap();

        rdfMap.setResource(fileName, metafix::resolvePath);

        withOption(options, RdfMap.TARGET, rdfMap::setTarget);
        withOption(options, RdfMap.TARGET_LANGUAGE, rdfMap::setTargetLanguage);
        withOption(options, RdfMap.SELECT, rdfMap::setSelect);
        withOption(options, Maps.DEFAULT_MAP_KEY, rdfMap::setDefault);

        metafix.putMap(params.size() > 1 ? params.get(1) : fileName, rdfMap);
    }

}
