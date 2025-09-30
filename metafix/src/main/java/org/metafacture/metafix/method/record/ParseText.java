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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseText implements FixFunction {

    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(.+?)>");

    /**
     * Creates an instance of {@link ParseText}.
     */
    public ParseText() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final String field = params.get(0);

        record.getList(field, a -> a.forEach(v -> {
            final Pattern p = Pattern.compile(params.get(1));
            final Matcher m = p.matcher(v.asString());
            if (m.matches()) {
                record.remove(field);

                /**
                 * {@code Pattern.namedGroups()} not available as API,
                 * see https://stackoverflow.com/a/65012527.
                 *
                 * Assumptions: 1. Named groups are not escaped/quoted;
                 * 2. Named groups are not mixed with unnamed groups.
                 */
                final Matcher groupMatcher = NAMED_GROUP_PATTERN.matcher(p.pattern());
                final Value value = Value.newHash(h -> {
                    while (groupMatcher.find()) {
                        final String group = groupMatcher.group(1);
                        h.put(group, new Value(m.group(group)));
                    }
                });

                if (!value.asHash().isEmpty()) {
                    record.addNested(field, value);
                }
                else {
                    for (int i = 1; i <= m.groupCount(); i = i + 1) {
                        record.addNested(field, new Value(m.group(i)));
                    }
                }
            }
        }));
    }

}
