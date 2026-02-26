/*
 * Copyright 2013-2019 Deutsche Nationalbibliothek and others
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
import org.metafacture.framework.MetafactureLogger;
import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Creates a new thread in which subsequent flow elements run.
 *
 * @param <T> Object type
 *
 * @author Markus Micheal Geipel
 * @author Pascal Christoph (dr0i)
 */
@In(Object.class)
@Out(Object.class)
@Description("creates a new thread in which subsequent flow elements run.")
@FluxCommand("decouple")
public final class ObjectPipeDecoupler<T> implements ObjectPipe<T, ObjectReceiver<T>> {

    public static final int DEFAULT_CAPACITY = 10000;
    private static final MetafactureLogger LOG = new MetafactureLogger(ObjectPipeDecoupler.class);

    private final BlockingQueue<Object> queue;
    private Thread thread;
    private ObjectReceiver<T> receiver;
    private boolean debug;

    /**
     * Creates an instance of {@link ObjectPipeDecoupler} by setting a default
     * capacity of {@value #DEFAULT_CAPACITY}.
     */
    public ObjectPipeDecoupler() {
        queue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
    }

    /**
     * Creates an instance of {@link ObjectPipeDecoupler} by setting a capacity.
     *
     * @param capacity the capacity
     */
    public ObjectPipeDecoupler(final int capacity) {
        queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Creates an instance of {@link ObjectPipeDecoupler} by setting a capacity.
     *
     * @param capacity the capacity as String. Will be parsed as integer.
     */
    public ObjectPipeDecoupler(final String capacity) {
        queue = new LinkedBlockingQueue<>(Integer.parseInt(capacity));
    }

    /**
     * Sets the log messages to be more verbose.
     *
     * @param debug true if the log messages should be more verbose
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    @Override
    public void process(final T obj) {

        if (null == thread) {
            start();
        }
        try {
            queue.put(obj);
            if (debug) {
                LOG.info("Current buffer size: {}", queue.size());
            }
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void start() {
        thread = new Thread(new Feeder<T>(receiver, queue));
        thread.start();
    }

    @Override
    public <R extends ObjectReceiver<T>> R setReceiver(final R newReceiver) {
        if (null != thread) {
            throw new IllegalStateException("Receiver cannot be changed while processing thread is running.");
        }

        receiver = newReceiver;
        return newReceiver;
    }

    @Override
    public void resetStream() {
        try {
            queue.put(Feeder.BLUE_PILL);
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void closeStream() {
        try {
            queue.put(Feeder.RED_PILL);
            thread.join();
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        thread = null;
    }

    /**
     * Pushes the content in the {@link BlockingQueue} to the receiver.
     *
     * @param <T> the type of objects the {@link ObjectPipeDecoupler} works on
     */
    static final class Feeder<T> implements Runnable {
        public static final Object RED_PILL = new Object();
        public static final Object BLUE_PILL = new Object();

        private final ObjectReceiver<T> receiver;
        private final BlockingQueue<Object> queue;

        Feeder(final ObjectReceiver<T> receiver, final BlockingQueue<Object> queue) {
            this.receiver = receiver;
            this.queue = queue;
        }

        @SuppressWarnings("unchecked")
        // OK because queue is only filled with T by Decoupler<T>
        @Override
        public void run() {
            try {
                while (true) {
                    final Object object = queue.take();
                    if (RED_PILL == object) {
                        receiver.closeStream();
                        break;
                    }
                    if (BLUE_PILL == object) {
                        receiver.resetStream();
                        continue;
                    }
                    receiver.process((T) object);
                }
            }
            catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
