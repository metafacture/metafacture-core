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

import org.metafacture.metafix.JsonValue;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.Value;
import org.metafacture.metafix.api.FixFunction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FromJson implements FixFunction {

    /**
     * Creates an instance of {@link FromJson}.
     */
    public FromJson() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final String errorString = options.get(ERROR_STRING_OPTION);
        final JsonValue.Parser parser = new JsonValue.Parser();

        record.transform(params.get(0), (m, c) -> m
                .ifString(s -> {
                    try {
                        c.accept(parser.parse(s));
                    }
                    catch (final IOException e) {
                        c.accept(errorString != null ? new Value(errorString) : null);
                    }
                })
        );
    }

}
