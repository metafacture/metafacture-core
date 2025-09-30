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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Log implements FixFunction {

    private static final Logger LOG = LoggerFactory.getLogger(Log.class);

    /**
     * Creates an instance of {@link Log}.
     */
    public Log() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        // does not support Catmandu log level option FATAL

        final String level = options.getOrDefault("level", "INFO");
        final Consumer<String> consumer;

        switch (level) {
            case "DEBUG":
                consumer = LOG::debug;
                break;
            case "ERROR":
                consumer = LOG::error;
                break;
            case "INFO":
                consumer = LOG::info;
                break;
            case "WARN":
                consumer = LOG::warn;
                break;
            default:
                throw new IllegalArgumentException("Unsupported log level: " + level);
        }

        consumer.accept(params.get(0));
    }

}
