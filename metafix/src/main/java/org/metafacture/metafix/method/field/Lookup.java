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

package org.metafacture.metafix.method.field;

import org.metafacture.metafix.FixCommand;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metafix.method.script.PutFileMap;
import org.metafacture.metamorph.api.Maps;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

@FixCommand("lookup")
public class Lookup implements FixFunction {

    private static final Map<Metafix, LongAdder> SCOPED_COUNTER = new HashMap<>();

    /**
     * Creates an instance of {@link Lookup}.
     */
    public Lookup() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final Map<String, String> map;

        if (params.size() <= 1) {
            map = options;
        }
        else {
            final String mapName = params.get(1);

            if (!metafix.getMapNames().contains(mapName)) {
                if (mapName.contains(".") || mapName.contains(File.separator)) {
                    new PutFileMap().apply(metafix, record, Arrays.asList(mapName), options);
                }
                else {
                    // Probably an unknown internal map? Log a warning?
                }
            }

            map = metafix.getMap(mapName);
        }

        final String defaultValue = options.getOrDefault("default", map.get(Maps.DEFAULT_MAP_KEY));
        final boolean delete = getBoolean(options, "delete");
        final boolean printUnknown = getBoolean(options, "print_unknown");

        final Consumer<Consumer<String>> consumer = c -> record.transform(params.get(0), oldValue -> {
            final String newValue = map.get(oldValue);
            if (newValue != null) {
                return newValue;
            }
            else {
                if (c != null) {
                    c.accept(oldValue);
                }

                return defaultValue != null ? defaultValue : delete ? null : oldValue;
            }
        });

        if (printUnknown) {
            options.putIfAbsent("append", "true");
            withWriter(metafix, record, options, SCOPED_COUNTER, consumer);
        }
        else {
            consumer.accept(null);
        }
    }

}
