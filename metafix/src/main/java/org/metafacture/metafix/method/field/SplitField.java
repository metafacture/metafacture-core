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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SplitField implements FixFunction {

    /**
     * Creates an instance of {@link SplitField}.
     */
    public SplitField() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final String splitChar = params.size() > 1 ? params.get(1) : "\\s+";
        final Pattern splitPattern = Pattern.compile(splitChar);

        final Function<String, Value> splitFunction = s ->
            newArray(Arrays.stream(splitPattern.split(s)).map(Value::new));

        record.transform(params.get(0), (m, c) -> m
                .ifArray(a -> c.accept(newArray(a.stream().map(Value::asString).map(splitFunction))))
                .ifHash(h -> c.accept(Value.newHash(n -> h.forEach((f, w) -> n.put(f, splitFunction.apply(w.asString()))))))
                .ifString(s -> c.accept(splitFunction.apply(s)))
        );
    }

}
