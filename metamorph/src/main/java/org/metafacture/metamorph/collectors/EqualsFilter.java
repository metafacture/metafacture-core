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
 * Corresponds to the <code>&lt;equalsFilter-literal&gt;</code> tag. Emits data
 * if values in variables are all equal.
 *
 * @author Thomas Haidlas
 */
public final class EqualsFilter extends AbstractFlushingCollect {

    private final Map<String, String> variables = new HashMap<String, String>();
    private final Set<NamedValueSource> sources = new HashSet<NamedValueSource>();
    private final Set<NamedValueSource> sourcesLeft = new HashSet<NamedValueSource>();
    private boolean isEqual = true;

    public EqualsFilter() {
    }

    @Override
    protected void emit() {
        final String name = StringUtil.format(getName(), variables);
        final String value = StringUtil.format(getValue(), variables);
        if (isEqual) {
            getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
        }
    }

    @Override
    protected boolean isComplete() {
        return sourcesLeft.isEmpty();
    }

    @Override
    protected void receive(final String name, final String value, final NamedValueSource source) {
        if (variables.size() > 0 && !variables.containsValue(value)) {
            isEqual = false;
        }
        variables.put(name, value);
        sourcesLeft.remove(source);
    }

    @Override
    public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
        sources.add(namedValueSource);
        sourcesLeft.add(namedValueSource);
    }

    @Override
    protected void clear() {
        sourcesLeft.addAll(sources);
        variables.clear();
        isEqual = true;
    }

}
