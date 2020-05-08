/*
 * Copyright 2013, 2020 Deutsche Nationalbibliothek and others
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
package org.metafacture.metamorph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.metafacture.commons.StringUtil;
import org.metafacture.flowcontrol.StreamBuffer;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.api.NamedValueReceiver;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.helpers.AbstractFlushingCollect;

/**
 * Corresponds to the <code>&lt;entity&gt;</code> tag.
 *
 * @author Markus Michael Geipel
 */
public final class Entity extends AbstractFlushingCollect {

    private final List<NamedValueSource> sourceList = new ArrayList<NamedValueSource>();
    private final Set<NamedValueSource> sourcesLeft = new HashSet<NamedValueSource>();
    private final StreamBuffer buffer = new StreamBuffer();

    private final Supplier<StreamReceiver> receiver;

    private NamedValueSource nameSource;
    private String currentName;

    public Entity(final Metamorph metamorph) {
        this.receiver = () -> metamorph.getStreamReceiver();
    }

    public Entity(final StreamReceiver receiver) {
        this.receiver = () -> receiver;
    }

    public void setNameSource(final NamedValueSource source) {
        nameSource = source;
        nameSource.setNamedValueReceiver(this);
        onNamedValueSourceAdded(nameSource);
    }

    @Override
    protected void emit() {
        final NamedValueReceiver namedValueReceiver = getNamedValueReceiver();
        if (namedValueReceiver instanceof Entity) {
            final Entity parent = (Entity) namedValueReceiver;
            parent.receive(null, null, this, getRecordCount(), getEntityCount());
        } else {
            write(receiver.get());
        }
    }

    private void write(final StreamReceiver receiver) {
        if (!buffer.isEmpty()) {
            receiver.startEntity(StringUtil.fallback(currentName, getName()));
            buffer.setReceiver(receiver);
            buffer.replay();
            receiver.endEntity();
        }
    }

    @Override
    protected void receive(final String name, final String value,
            final NamedValueSource source) {
        if (source == nameSource) {
            currentName = value;
        } else if (source instanceof Entity) {
            final Entity child = (Entity) source;
            child.write(buffer);
        } else {
            buffer.literal(name, value);
        }
        sourcesLeft.remove(source);
    }

    @Override
    protected boolean isComplete() {
        return sourcesLeft.isEmpty();
    }

    @Override
    protected void clear() {
        sourcesLeft.addAll(sourceList);
        buffer.clear();
        currentName = null;
    }

    @Override
    public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
        sourceList.add(namedValueSource);
        sourcesLeft.add(namedValueSource);
    }

}
