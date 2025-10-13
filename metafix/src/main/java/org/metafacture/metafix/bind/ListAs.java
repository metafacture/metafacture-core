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
import org.metafacture.metafix.Value;
import org.metafacture.metafix.api.FixContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FixCommand("list_as")
public class ListAs implements FixContext {

    /**
     * Creates an instance of {@link ListAs}.
     */
    public ListAs() {
    }

    @Override
    public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
        final Map<String, Value.Array> lists = new HashMap<>();
        options.forEach((k, v) -> Value.asList(record.get(v), a -> lists.put(k, a)));

        final int size = lists.values().stream().mapToInt(a -> a.size()).max().orElse(0);
        for (int i = 0; i < size; ++i) {
            final int index = i;

            lists.forEach((k, v) -> {
                final Value value = index < v.size() ? v.get(index) : null;

                if (value != null) {
                    record.put(k, value);
                }
                else {
                    record.remove(k);
                }
            });

            recordTransformer.transform(record);
        }

        lists.keySet().forEach(record::remove);
    }

}
