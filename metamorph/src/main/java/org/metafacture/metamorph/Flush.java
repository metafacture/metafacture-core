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

package org.metafacture.metamorph;

import org.metafacture.metamorph.api.FlushListener;
import org.metafacture.metamorph.api.NamedValueReceiver;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.SourceLocation;

/**
 * Flushes a {@link FlushListener}.
 *
 * @author Markus Geipel
 * @author Christoph Böhme
 *
 */
public final class Flush implements NamedValueReceiver {

    private final FlushListener listener;

    private SourceLocation sourceLocation;

    /**
     * Creates an instance of {@link Flush} by a given {@link FlushListener}.
     *
     * @param listener the {@link FlushListener}
     */
    public Flush(final FlushListener listener) {
        this.listener = listener;
    }

    @Override
    public void receive(final String name, final String value, final NamedValueSource source, final int recordCount, final int entityCount) {
        listener.flush(recordCount, entityCount);
    }

    @Override
    public void addNamedValueSource(final NamedValueSource namedValueSource) {
        // Nothing to do
    }

    @Override
    public void setSourceLocation(final SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

}
