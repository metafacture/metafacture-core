/*
 * Copyright 2013, 2014 Christoph Böhme
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

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the {@code <all>} tag.
 *
 * @author Christoph Böhme
 */
public final class All extends AbstractFlushingCollect {

    private static final String DEFAULT_NAME = "";
    private static final String DEFAULT_VALUE = "true";

    private final Set<NamedValueSource> sources = new HashSet<NamedValueSource>();
    private final Set<NamedValueSource> sourcesLeft = new HashSet<NamedValueSource>();

    public All() {
    }

    @Override
    protected void receive(final String name, final String value, final NamedValueSource source) {
        sourcesLeft.remove(source);
    }

    @Override
    protected boolean isComplete() {
        return sourcesLeft.isEmpty();
    }

    @Override
    protected void clear() {
        sourcesLeft.addAll(sources);
    }

    @Override
    protected void emit() {
        if (sourcesLeft.isEmpty()) {
            final String name = StringUtil.fallback(getName(), DEFAULT_NAME);
            final String value = StringUtil.fallback(getValue(), DEFAULT_VALUE);
            getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
        }
    }

    @Override
    public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
        sources.add(namedValueSource);
        sourcesLeft.add(namedValueSource);
    }

}
