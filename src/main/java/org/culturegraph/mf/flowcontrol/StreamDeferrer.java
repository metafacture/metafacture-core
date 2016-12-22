/*
 * Copyright 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.flowcontrol;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Defers all stream events until an <i>end-record</i> is received. Once the
 * event is received {@code StreamDeferrer} forwards all events it has received
 * before the <i>end-record</i> event to the next downstream module. The
 * <i>end-record</i> event is forwarded immediately.
 * <p>
 * The {@code StreamDeferrer} is useful when merging two or more streams of
 * events into one. The module is intended to be added at the end of each flow.
 * There it ensures that the events for each record are forwarded consecutively
 * to the merged flow without them being mixed with events belonging to a record
 * from another stream.
 *
 * @author Christoph Böhme
 */
@Description("Defers all stream events until an end-record event is received")
@FluxCommand("defer-stream")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public class StreamDeferrer extends DefaultStreamPipe<StreamReceiver> {

    private final StreamBuffer buffer = new StreamBuffer();

    @Override
    public void startRecord(final String identifier) {
        buffer.clear();
        buffer.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        buffer.endRecord();
        buffer.replay();
    }

    @Override
    public void startEntity(final String name) {
        buffer.startEntity(name);
    }

    @Override
    public void endEntity() {
        buffer.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        buffer.literal(name, value);
    }

    @Override
    protected void onSetReceiver() {
        buffer.setReceiver(getReceiver());
    }

    @Override
    protected void onResetStream() {
        buffer.clear();
    }

    @Override
    protected void onCloseStream() {
        buffer.clear();
    }

}
