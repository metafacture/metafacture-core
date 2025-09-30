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

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixFunction;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ToBase64 implements FixFunction {

    /**
     * Creates an instance of {@link ToBase64}.
     */
    public ToBase64() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final boolean urlSafe = getBoolean(options, "url_safe");
        final Base64.Encoder encoder = urlSafe ? Base64.getUrlEncoder() : Base64.getEncoder();

        record.transform(params.get(0), s -> encoder.encodeToString(s.getBytes()));
    }

}
