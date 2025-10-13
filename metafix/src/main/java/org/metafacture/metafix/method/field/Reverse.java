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
import org.metafacture.metafix.Value;
import org.metafacture.metafix.api.FixFunction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@FixCommand("reverse")
public class Reverse implements FixFunction {

    /**
     * Creates an instance of {@link Reverse}.
     */
    public Reverse() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        record.transform(params.get(0), (m, c) -> m
                .ifArray(a -> {
                    final List<Value> list = a.stream().collect(Collectors.toList());
                    Collections.reverse(list);
                    c.accept(new Value(list));
                })
                .ifString(s -> c.accept(new Value(new StringBuilder(s).reverse().toString())))
        );
    }

}
