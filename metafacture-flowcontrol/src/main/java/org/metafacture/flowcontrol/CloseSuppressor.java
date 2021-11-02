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

package org.metafacture.flowcontrol;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

/**
 * Blocks {@link #closeStream()} events until {@code n} <i>close-stream</i>
 * events have been received.
 *
 * @param <T>
 *            object type
 *
 * @author Markus Geipel
 *
 */
@Description("")
@In(Object.class)
@Out(Object.class)
@FluxCommand("wait-for-inputs")
public final class CloseSuppressor<T>
        implements ObjectPipe<T, ObjectReceiver<T>> {

    private ObjectReceiver<T> receiver;
    private final int numCloses;
    private int count;

    /**
     * Creates an instance of {@link CloseSuppressor} with a number defining the
     * number of events to block before closing the stream.
     *
     * @param numCloses the number of events to block before closing the stream. The
     *                  String is parsed to an integer.
     */
    public CloseSuppressor(final String numCloses) {
        this(Integer.parseInt(numCloses));
    }

    /**
     * Creates an instance of {@link CloseSuppressor} with a number defining the
     * number of events to block before closing the stream.
     *
     * @param numCloses the number of events to block before closing the stream
     */
    public CloseSuppressor(final int numCloses) {
        this.numCloses = numCloses;
    }

    @Override
    public void process(final T obj) {
        if (receiver != null) {
            receiver.process(obj);
        }
    }

    @Override
    public <R extends ObjectReceiver<T>> R setReceiver(final R newReceiver) {
        receiver = newReceiver;
        return newReceiver;
    }

    @Override
    public void resetStream() {
        count = 0;
        if (receiver != null) {
            receiver.resetStream();
        }
    }

    @Override
    public void closeStream() {
        ++count;
        if (count == numCloses && receiver != null) {
            receiver.closeStream();
        }
    }

}
