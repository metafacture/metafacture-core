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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Corresponds to the <code>&lt;collect&gt;</code> tag.
 *
 * @author Markus Michael Geipel
 */
public final class Combine extends AbstractFlushingCollect {

    private final Map<String, String> variables = new HashMap<String, String>();
    private final Set<NamedValueSource> sources = new HashSet<NamedValueSource>();
    private final Set<NamedValueSource> sourcesLeft = new HashSet<NamedValueSource>();

    /**
     * Creates an instance of {@link Combine}.
     */
    public Combine() {
    }

    @Override
    protected void emit() {
        final String name = StringUtil.format(getName(), variables);
        final String value = StringUtil.format(getValue(), variables);
        getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
    }

    @Override
    protected boolean isComplete() {
        return sourcesLeft.isEmpty();
    }

    @Override
    protected void receive(final String name, final String value, final NamedValueSource source) {
        variables.put(name, value);
        sourcesLeft.remove(source);
    }

    @Override
    protected void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
        sources.add(namedValueSource);
        sourcesLeft.add(namedValueSource);
    }

    @Override
    protected void clear() {
        sourcesLeft.addAll(sources);
        variables.clear();
    }

}
