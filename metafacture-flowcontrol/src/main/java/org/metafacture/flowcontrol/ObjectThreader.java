/* Copyright 2019 Pascal Christoph (hbz), and others.
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
import org.metafacture.framework.Tee;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Divides incoming objects and distributes them to added receivers. These
 * receivers are coupled with an
 * {@link org.metafacture.flowcontrol.ObjectPipeDecoupler}, so each added
 * receiver runs in its own thread.
 *
 * @param <T> Object type
 *
 * @author Pascal Christoph (dr0i)
 * @author Fabian Steeg (fsteeg)
 *
 */
@In(Object.class)
@Out(Object.class)
@Description("Incoming objects are distributed to the added receivers, running in their own threads.")
@FluxCommand("thread-object-tee")
public class ObjectThreader<T> implements Tee<ObjectReceiver<T>>, ObjectPipe<T, ObjectReceiver<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectThreader.class);
    private final List<ObjectReceiver<T>> receivers = new ArrayList<ObjectReceiver<T>>();
    private int objectNumber;

    /**
     * Creates an instance of {@link ObjectThreader}.
     */
    public ObjectThreader() {
    }

    @Override
    public void process(final T obj) {
        receivers.get(objectNumber).process(obj);
        if (objectNumber == receivers.size() - 1) {
            objectNumber = 0;
        }
        else {
            ++objectNumber;
        }
    }

    @Override
    public Tee<ObjectReceiver<T>> addReceiver(final ObjectReceiver<T> receiver) {
        LOG.info("Adding thread {}", receivers.size() + 1);
        final ObjectPipeDecoupler<T> opd = new ObjectPipeDecoupler<>();
        opd.setReceiver(receiver);
        receivers.add(opd);
        return this;
    }

    @Override
    public <R extends ObjectReceiver<T>> R setReceiver(final R receiver) {
        receivers.clear();
        addReceiver(receiver);
        return receiver;
    }

    @Override
    public <R extends ObjectReceiver<T>> R setReceivers(final R receiver, final ObjectReceiver<T> lateralReceiver) {
        receivers.clear();
        addReceiver(receiver);
        addReceiver(lateralReceiver);
        return receiver;
    }

    @Override
    public void resetStream() {
        receivers.forEach(ObjectReceiver::resetStream);
    }

    @Override
    public void closeStream() {
        receivers.forEach(ObjectReceiver::closeStream);
    }

    @Override
    public Tee<ObjectReceiver<T>> removeReceiver(final ObjectReceiver<T> receiver) {
        receivers.remove(receiver);
        return this;
    }

    @Override
    public Tee<ObjectReceiver<T>> clearReceivers() {
        receivers.clear();
        return this;
    }
}
