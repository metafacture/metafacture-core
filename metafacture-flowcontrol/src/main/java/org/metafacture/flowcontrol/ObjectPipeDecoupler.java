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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a new thread in which subsequent flow elements run.
 *
 * @param <T> Object type
 *
 * @author Markus Micheal Geipel
 */
@In(Object.class)
@Out(Object.class)
@Description("creates a new thread in which subsequent flow elements run.")
@FluxCommand("decouple")
public final class ObjectPipeDecoupler<T> implements ObjectPipe<T, ObjectReceiver<T>> {

    public static final int DEFAULT_CAPACITY = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(ObjectPipeDecoupler.class);

    private final BlockingQueue<Object> queue;
    private Thread thread;
    private ObjectReceiver<T> receiver;
    private boolean debug;

    public ObjectPipeDecoupler() {
        queue = new LinkedBlockingQueue<Object>(DEFAULT_CAPACITY);
    }

    public ObjectPipeDecoupler(final int capacity) {
        queue = new LinkedBlockingQueue<Object>(capacity);
    }

    public ObjectPipeDecoupler(final String capacity) {
        queue = new LinkedBlockingQueue<Object>(Integer.parseInt(capacity));
    }

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
                LOG.info("Current buffer size: " + queue.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void start() {
        thread = new Thread(new Feeder<T>(receiver, queue));
        thread.start();
    }

    @Override
    public <R extends ObjectReceiver<T>> R setReceiver(final R receiver) {
        if (null != thread) {
            throw new IllegalStateException("Receiver cannot be changed while processing thread is running.");
        }

        this.receiver = receiver;
        return receiver;
    }

    @Override
    public void resetStream() {
        try {
            queue.put(Feeder.BLUE_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void closeStream() {
        try {
            queue.put(Feeder.RED_PILL);
            thread.join();
        } catch (InterruptedException e) {
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

        public Feeder(final ObjectReceiver<T> receiver, final BlockingQueue<Object> queue) {
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
