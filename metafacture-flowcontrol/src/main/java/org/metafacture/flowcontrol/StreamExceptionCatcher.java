/*
 * Copyright 2016 Christoph Böhme
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
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Passes streams events through and catches exceptions.
 *
 * @author schaeferd
 *
 */
@Description("passes streams events through and catches exceptions.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("catch-stream-exception")
public final class StreamExceptionCatcher extends
        DefaultStreamPipe<StreamReceiver> {

    private static final Logger LOG = LoggerFactory.getLogger(StreamExceptionCatcher.class);
    private static final String MSG_PATTERN = "{}{}";

    private final String logPrefix;

    /**
     * Creates an instance of {@link StreamExceptionCatcher}.
     */
    public StreamExceptionCatcher() {
        this("");
    }

    /**
     * Creates an instance of {@link StreamExceptionCatcher} setting a prefix for
     * the logs messages.
     *
     * @param logPrefix the prefix of the log messages
     */
    public StreamExceptionCatcher(final String logPrefix) {
        this.logPrefix = logPrefix;
    }

    @Override
    public void startRecord(final String identifier) {
        try {
            getReceiver().startRecord(identifier);
        }
        catch (final Exception e) { // checkstyle-disable-line IllegalCatch
            LOG.error(MSG_PATTERN, logPrefix, "StartRecord" + identifier);
            LOG.error(MSG_PATTERN, logPrefix, e);
        }
    }

    @Override
    public void endRecord() {
        try {
            getReceiver().endRecord();
        }
        catch (final Exception e) { // checkstyle-disable-line IllegalCatch
            LOG.error(MSG_PATTERN, logPrefix, "endRecord");
            LOG.error(MSG_PATTERN, logPrefix, e);
        }
    }

    @Override
    public void startEntity(final String name) {
        try {
            getReceiver().startEntity(name);
        }
        catch (final Exception e) { // checkstyle-disable-line IllegalCatch
            LOG.error(MSG_PATTERN, logPrefix, "startEntity" + name);
            LOG.error(MSG_PATTERN, logPrefix, e);
        }
    }

    @Override
    public void endEntity() {
        try {
            getReceiver().endEntity();
        }
        catch (final Exception e) { // checkstyle-disable-line IllegalCatch
            LOG.error(MSG_PATTERN, logPrefix, "endEntity");
            LOG.error(MSG_PATTERN, logPrefix, e);
        }
    }

    @Override
    public void literal(final String name, final String value) {
        try {
            getReceiver().literal(name, value);
        }
        catch (final Exception e) { // checkstyle-disable-line IllegalCatch
            LOG.error(MSG_PATTERN, logPrefix, "literal " + name + " " + value);
            LOG.error(MSG_PATTERN, logPrefix, e);
        }
    }

}
