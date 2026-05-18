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

package org.metafacture.monitoring;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureLogger;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Leaves the event stream untouched but logs it to the debug log.
 * The {@link StreamReceiver} may be {@code null}.
 * In this case {@link StreamLogger} behaves as a sink, just logging.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("logs events")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("log-stream")
public final class StreamLogger
        extends DefaultStreamPipe<StreamReceiver> {

    private static final MetafactureLogger LOG = new MetafactureLogger(StreamLogger.class);

    private String logPrefix = "";

    /**
     * Creates an instance of {@link StreamLogger}.
     */
    public StreamLogger() {
    }

    /**
     * Creates an instance of {@link StreamLogger} by a given prefix used when log
     * messages.
     *
     * @deprecated Use {@link #setPrefix} instead.
     *
     * @param logPrefix the prefix of the log messages
     */
    @Deprecated/*(since="9.0", forRemoval=true)*/
    public StreamLogger(final String logPrefix) {
        setPrefix(logPrefix);
    }

    /**
     * Sets the prefix used when logging messages.
     *
     * @param prefix the prefix of the log messages
     */
    public void setPrefix(final String prefix) {
        this.logPrefix = prefix;
    }

    @Override
    public void startRecord(final String identifier) {
        assert !isClosed();
        writeLog("start record {}", identifier);
        if (null != getReceiver()) {
            getReceiver().startRecord(identifier);
        }
    }

    @Override
    public void endRecord() {
        assert !isClosed();
        writeLog("end record");
        if (null != getReceiver()) {
            getReceiver().endRecord();
        }
    }

    @Override
    public void startEntity(final String name) {
        assert !isClosed();
        writeLog("start entity {}", name);
        if (null != getReceiver()) {
            getReceiver().startEntity(name);
        }
    }

    @Override
    public void endEntity() {
        assert !isClosed();
        writeLog("end entity");
        if (null != getReceiver()) {
            getReceiver().endEntity();
        }

    }

    @Override
    public void literal(final String name, final String value) {
        assert !isClosed();
        writeLog("literal {}={}", name, value);
        if (null != getReceiver()) {
            getReceiver().literal(name, value);
        }
    }

    @Override
    protected void onResetStream() {
        writeLog("resetStream");
    }

    @Override
    protected void onCloseStream() {
        writeLog("closeStream");
    }

    private void writeLog(final String message, final Object... arguments) {
        final List<Object> argumentList = new ArrayList<>(arguments.length + 1);

        argumentList.add(logPrefix);
        Arrays.stream(arguments).forEach(argumentList::add);

        LOG.externalDebug("{}" + message, argumentList.toArray());
    }

}
