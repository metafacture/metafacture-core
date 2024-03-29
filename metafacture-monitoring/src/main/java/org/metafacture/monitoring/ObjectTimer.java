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
import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

/**
 * Benchmarks the execution time of the downstream modules.
 *
 * @param <T>
 *            object type.
 *
 * @author Christoph Böhme
 */
@In(Object.class)
@Out(Object.class)
@Description("Benchmarks the execution time of the downstream modules.")
@FluxCommand("log-time")
public final class ObjectTimer<T> extends TimerBase<ObjectReceiver<T>>
        implements ObjectPipe<T, ObjectReceiver<T>> {

    /**
     * Creates an instance of {@link ObjectTimer}.
     */
    public ObjectTimer() {
        this("");
    }

    /**
     * Creates an instance of {@link ObjectTimer} by a given log messages prefix.
     *
     * @param logPrefix the prefix of the log messages
     */
    public ObjectTimer(final String logPrefix) {
        super(logPrefix);
    }

    @Override
    public void process(final T obj) {
        startMeasurement();
        getReceiver().process(obj);
        stopMeasurement();
    }

}
