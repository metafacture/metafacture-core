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

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.Value;
import org.metafacture.metafix.api.FixFunction;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class Rename implements FixFunction {

    /**
     * Creates an instance of {@link Rename}.
     */
    public Rename() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final String search = params.get(1);
        final String replace = params.get(2);

        final UnaryOperator<String> operator = s -> s.replaceAll(search, replace);

        record.transform(params.get(0), (m, c) -> m
                .ifArray(a -> c.accept(renameArray(a, operator)))
                .ifHash(h -> c.accept(renameHash(h, operator)))
                .orElseThrow()
        );
    }

    private Value renameArray(final Value.Array array, final UnaryOperator<String> operator) {
        return Value.newArray(a -> array.forEach(v -> a.add(renameValue(v, operator))));
    }

    private Value renameHash(final Value.Hash hash, final UnaryOperator<String> operator) {
        return Value.newHash(h -> hash.forEach((f, v) -> h.put(operator.apply(f), renameValue(v, operator))));
    }

    private Value renameValue(final Value value, final UnaryOperator<String> operator) {
        return value.extractType((m, c) -> m
                .ifArray(a -> c.accept(renameArray(a, operator)))
                .ifHash(h -> c.accept(renameHash(h, operator)))
                .orElse(c)
        );
    }

}
