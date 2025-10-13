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

package org.metafacture.metafix.method.record;

import org.metafacture.metafix.FixCommand;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixFunction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@FixCommand("print_record")
public class PrintRecord implements FixFunction {

    private static final Map<Metafix, LongAdder> SCOPED_COUNTER = new HashMap<>();

    /**
     * Creates an instance of {@link PrintRecord}.
     */
    public PrintRecord() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final boolean internal = getBoolean(options, "internal");
        final boolean pretty = getBoolean(options, "pretty");

        if (!params.isEmpty()) {
            options.put("prefix", params.get(0));
        }

        withWriter(metafix, record, options, SCOPED_COUNTER, c -> {
            if (internal) {
                if (pretty) {
                    record.forEach((f, v) -> c.accept(f + "=" + v));
                }
                else {
                    c.accept(record.toString());
                }
            }
            else {
                try {
                    c.accept(record.toJson(pretty));
                }
                catch (final IOException e) {
                    // Log a warning? Print string representation instead?
                }
            }
        });
    }

}
