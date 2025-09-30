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
import org.metafacture.metafix.Value;
import org.metafacture.metafix.api.FixFunction;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortField implements FixFunction {

    /**
     * Creates an instance of {@link SortField}.
     */
    public SortField() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final boolean numeric = getBoolean(options, "numeric");
        final boolean reverse = getBoolean(options, "reverse");
        final boolean uniq = getBoolean(options, "uniq");

        final Function<Value, String> function = Value::asString;
        final Comparator<Value> comparator = numeric ?
            Comparator.comparing(function.andThen(Integer::parseInt)) : Comparator.comparing(function);

        record.transform(params.get(0), (m, c) -> m
                .ifArray(a -> c.accept(new Value((uniq ? unique(a.stream()) : a.stream())
                            .sorted(reverse ? comparator.reversed() : comparator).collect(Collectors.toList()))))
        );
    }

}
