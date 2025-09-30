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

package org.metafacture.metafix.conditional;

import org.metafacture.metafix.FixCommand;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.Value;
import org.metafacture.metafix.api.FixPredicate;

import java.util.List;
import java.util.Map;

@FixCommand("in")
public class In implements FixPredicate {

    /**
     * Creates an instance of {@link In}.
     */
    public In() {
    }

    @Override
    public boolean test(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final Value value1 = record.get(params.get(0));
        final Value value2 = record.get(params.get(1));

        return value1 != null && value2 != null && value1.<Boolean>extractType((m, c) -> m
            .ifArray(a1 -> value2.matchType()
                .ifArray(a2 -> c.accept(a1.equals(a2)))
                .orElse(v -> c.accept(false))
            )
            .ifHash(h1 -> value2.matchType()
                .ifHash(h2 -> c.accept(h1.equals(h2)))
                .orElse(v -> c.accept(false))
            )
            .ifString(s1 -> value2.matchType()
                .ifArray(a2 -> c.accept(a2.stream().anyMatch(value1::equals)))
                .ifHash(h2 -> c.accept(h2.containsField(s1)))
                .ifString(s2 -> c.accept(s1.equals(s2)))
            )
        );
    }

}
