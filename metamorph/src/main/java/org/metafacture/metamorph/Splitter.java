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

import org.metafacture.flowcontrol.StreamBuffer;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.javaintegration.SingleValue;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Splits a stream based on a morph definition.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("Splits a stream based on a morph definition")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class Splitter implements StreamPipe<StreamReceiver> {

    private final StreamBuffer buffer = new StreamBuffer();
    private final SingleValue singleValue = new SingleValue();
    private final Map<String, StreamReceiver> receiverMap = new HashMap<String, StreamReceiver>();
    private final Metamorph metamorph;

    /**
     * Constructs a Splitter from the path to a Metamorph definition file.
     *
     * @param morphDef the name of the file of the Metamorph definition
     */
    public Splitter(final String morphDef) {
        metamorph = new Metamorph(morphDef);
        metamorph.setReceiver(singleValue);
    }

    /**
     * Constructs a Splitter from the Reader of a Metamorph definition.
     *
     * @param morphDef the Reader of the metamorph definition
     */
    public Splitter(final Reader morphDef) {
        metamorph = new Metamorph(morphDef);
        metamorph.setReceiver(singleValue);
    }

    /**
     * Constructs a Splitter from the Metamorph.
     *
     * @param metamorph the Metamoprh
     */
    public Splitter(final Metamorph metamorph) {
        this.metamorph = metamorph;
        metamorph.setReceiver(singleValue);
    }

    @Override
    public <R extends StreamReceiver> R setReceiver(final R receiver) {
        receiverMap.put("", receiver);
        return receiver;
    }

    /**
     * Sets the receiver.
     *
     * @param <R>      the type of the receiver
     * @param key      the name of the receiver
     * @param receiver the receiver
     * @return the receiver
     */
    public <R extends StreamReceiver> R setReceiver(final String key, final R receiver) {
        receiverMap.put(key, receiver);
        return receiver;
    }

    private void dispatch() {
        final String key = singleValue.getValue();
        final StreamReceiver receiver = receiverMap.get(key);

        if (null != receiver) {
            buffer.setReceiver(receiver);
            buffer.replay();
        }
        buffer.clear();
    }

    @Override
    public void startRecord(final String identifier) {
        buffer.startRecord(identifier);
        metamorph.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        buffer.endRecord();
        metamorph.endRecord();
        dispatch();
    }

    @Override
    public void startEntity(final String name) {
        buffer.startEntity(name);
        metamorph.startEntity(name);
    }

    @Override
    public void endEntity() {
        buffer.endEntity();
        metamorph.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        buffer.literal(name, value);
        metamorph.literal(name, value);
    }

    @Override
    public void resetStream() {
        buffer.clear();
        metamorph.resetStream();
        for (final StreamReceiver receiver: receiverMap.values()) {
            receiver.resetStream();
        }
    }

    @Override
    public void closeStream() {
        buffer.clear();
        metamorph.closeStream();
        for (final StreamReceiver receiver: receiverMap.values()) {
            receiver.closeStream();
        }
    }
}
