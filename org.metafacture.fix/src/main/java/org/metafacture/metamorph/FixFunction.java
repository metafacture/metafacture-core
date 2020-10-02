/*
 * Copyright 2020 Fabian Steeg, hbz
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

import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.MethodCall;
import org.metafacture.fix.fix.Options;
import org.metafacture.metamorph.api.NamedValuePipe;
import org.metafacture.metamorph.functions.Lookup;
import org.metafacture.metamorph.maps.FileMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum FixFunction {
    // fix-style functions, not (like) in morph
    MAP {
        public void apply(final FixBuilder builder, final Expression expression, final List<String> params,
                final String source) {
            final NamedValuePipe enterDataMap = builder.enterDataMap(params, false);
            builder.exitData();
            if (enterDataMap instanceof Entity) {
                builder.exitCollectorAndFlushWith(null);
            }
        }
    },
    ADD_FIELD {
        public void apply(final FixBuilder builder, final Expression expression, final List<String> params,
                final String source) {
            builder.enterDataAdd(params);
            builder.exitData();
        }
    },
    LOOKUP {
        public void apply(final FixBuilder builder, final Expression expression, final List<String> params,
                final String source) {
            final Lookup lookup = new Lookup();
            lookup.setMaps(builder.getMetafix());
            final Map<String, String> map = buildMap(expression);
            final String name = map.hashCode() + "";
            lookup.setMap(name);
            builder.getMetafix().putMap(name, map);
            builder.enterDataFunction(source, lookup, false);
        }

        private Map<String, String> buildMap(final Expression expression) {
            final Map<String, String> options = options((MethodCall) expression);
            final String file = options.get("in");
            final String sep = "separator";
            final boolean useFileMap = file != null &&
                    (options.size() == 1 || options.size() == 2 && options.containsKey(sep));
            final Map<String, String> map = useFileMap ? fileMap(file, options.get(sep)) : options;
            return map;
        }

        private Map<String, String> options(final MethodCall method) {
            final Options options = method.getOptions();
            final Map<String, String> map = new HashMap<>();
            if (options != null) {
                for (int i = 0; i < options.getKeys().size(); i += 1) {
                    map.put(options.getKeys().get(i), options.getValues().get(i));
                }
            }
            return map;
        }

        private Map<String, String> fileMap(final String secondParam, final String separator) {
            final FileMap fileMap = new FileMap();
            if (separator != null) {
                fileMap.setSeparator(separator);
            }
            fileMap.setFile(secondParam);
            return fileMap;
        }

    };
    public abstract void apply(FixBuilder builder, Expression expression, List<String> params, String source);
}
