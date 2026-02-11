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
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Logs the string representation of every object.
 *
 * @param <T> object type
 *
 * @author Christoph Böhme
 *
 */
@Description("logs objects with the toString method")
@In(Object.class)
@Out(Object.class)
@FluxCommand("log-object")
public final class ObjectLogger<T>
        extends DefaultObjectPipe<T, ObjectReceiver<T>> {

    private static final MetafactureLogger LOG = new MetafactureLogger(ObjectLogger.class);

    private final String logPrefix;

    /**
     * Creates an instance of {@link ObjectLogger}.
     */
    public ObjectLogger() {
        this("");
    }

    /**
     * Creates an instance of {@link ObjectLogger} by a given prefix of the log
     * messages.
     *
     * @param logPrefix the prefix of the log messages
     */
    public ObjectLogger(final String logPrefix) {
        this.logPrefix = logPrefix;
    }

    @Override
    public void process(final T obj) {
        LOG.externalDebug("{}{}", logPrefix, obj);
        if (getReceiver() != null) {
            getReceiver().process(obj);
        }
    }

    @Override
    protected void onResetStream() {
        LOG.externalDebug("{}resetStream", logPrefix);
    }

    @Override
    protected void onCloseStream() {
        LOG.externalDebug("{}closeStream", logPrefix);
    }

}
