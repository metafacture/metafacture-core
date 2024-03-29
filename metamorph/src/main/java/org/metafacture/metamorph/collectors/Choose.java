/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.metamorph.collectors;

import org.metafacture.commons.StringUtil;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.helpers.AbstractFlushingCollect;

import java.util.HashMap;
import java.util.Map;

/**
 * Corresponds to the {@code <choose>} tag.
 *
 * @author Christoph Böhme
 * @author Markus Michael Geipel
 */
public final class Choose extends AbstractFlushingCollect {

    private String value;
    private String name;
    private int priority = Integer.MAX_VALUE;
    private final Map<NamedValueSource, Integer> priorities = new HashMap<>();
    private int nextPriority;

    /**
     * Creates an instance of {@link Choose}.
     */
    public Choose() {
    }

    @Override
    protected void emit() {
        if (!isEmpty()) {
            getNamedValueReceiver().receive(StringUtil.fallback(getName(), name),
                    StringUtil.fallback(getValue(), value), this, getRecordCount(),
                    getEntityCount());
        }
    }

    private boolean isEmpty() {
        return name == null;
    }

    @Override
    protected boolean isComplete() {
        return false;
    }

    @Override
    protected void clear() {
        value = null;
        name = null;
        priority = Integer.MAX_VALUE;
    }

    @Override
    protected void receive(final String newName, final String newValue, final NamedValueSource source) {
        final int sourcePriority = priorities.get(source).intValue();
        if (sourcePriority <= priority) {
            name = newName;
            value = newValue;
            priority = sourcePriority;
        }
    }

    @Override
    protected void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
        priorities.put(namedValueSource, Integer.valueOf(nextPriority));
        nextPriority += 1;
    }

}
