/*
 * Copyright 2022 hbz NRW
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

package org.metafacture.metafix;

import org.metafacture.metafix.api.FixContext;

import java.util.List;
import java.util.Map;

public enum FixBind implements FixContext {

    list {
        @Override
        public void execute(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options, final RecordTransformer recordTransformer) {
            final String scopeVariable = options.get("var");
            Value.asList(record.get(options.get("path")), a -> {
                for (int i = 0; i < a.size(); ++i) {
                    final Value value = a.get(i);

                    // with var -> keep full record in scope, add the var:
                    if (scopeVariable != null) {
                        record.put(scopeVariable, value);
                        recordTransformer.transform(record);
                        record.remove(scopeVariable);
                    }
                    // w/o var -> use the currently bound value as the record:
                    else {
                        final int index = i;

                        value.matchType()
                            .ifHash(h -> {
                                final Record scopeRecord = new Record();
                                scopeRecord.addAll(h);

                                recordTransformer.transform(scopeRecord);
                                a.set(index, new Value(scopeRecord));
                            })
                            // TODO: bind to arrays (if that makes sense) and strings (access with '.')
                            .orElseThrow();
                    }
                }
            });
        }
    }

}
