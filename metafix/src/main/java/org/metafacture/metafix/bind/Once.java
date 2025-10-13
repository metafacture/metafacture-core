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

package org.metafacture.metafix.bind;

import org.metafacture.metafix.FixCommand;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.RecordTransformer;
import org.metafacture.metafix.api.FixContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FixCommand("once")
public class Once implements FixContext {

    private static final Map<Metafix, Set<String>> EXECUTED = new HashMap<>();

    /**
     * Creates an instance of {@link Once}.
     */
    public Once() {
    }

    @Override
    public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
        if (EXECUTED.computeIfAbsent(metafix, k -> new HashSet<>()).add(params.isEmpty() ? null : params.get(0))) {
            recordTransformer.transform(record);
        }
    }

}
