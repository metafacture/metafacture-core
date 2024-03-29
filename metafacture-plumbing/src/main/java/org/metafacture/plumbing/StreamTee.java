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

package org.metafacture.plumbing;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultTee;

/**
 * Replicates an event stream to an arbitrary number of {@link StreamReceiver}s.
 *
 * @author Christoph Böhme, Markus Michael Geipel
 *
 */
@Description("Replicates an event stream to an arbitrary number of stream receivers.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("stream-tee")
public final class StreamTee extends DefaultTee<StreamReceiver> implements StreamPipe<StreamReceiver> {

    /**
     * Creates an instance of {@link StreamTee}.
     */
    public StreamTee() {
    }

    @Override
    public void startRecord(final String identifier) {
        for (final StreamReceiver receiver : getReceivers()) {
            receiver.startRecord(identifier);
        }
    }

    @Override
    public void endRecord() {
        for (final StreamReceiver receiver : getReceivers()) {
            receiver.endRecord();
        }
    }

    @Override
    public void startEntity(final String name) {
        for (final StreamReceiver receiver : getReceivers()) {
            receiver.startEntity(name);
        }
    }

    @Override
    public void endEntity() {
        for (final StreamReceiver receiver : getReceivers()) {
            receiver.endEntity();
        }
    }

    @Override
    public void literal(final String name, final String value) {
        for (final StreamReceiver receiver : getReceivers()) {
            receiver.literal(name, value);
        }
    }

}
