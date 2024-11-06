/*
* Copyright 2024 hbz
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
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;


/**
 * Lets the process between objects sleep for a specific ms.
 *
 * @param <T> object type
 * @author Tobias Bülte
 */
@Description("Lets the process between objects sleep for a specific ms.")
@In(Object.class)
@Out(Object.class)
@FluxCommand("sleep")
public final class ObjectSleeper<T> extends DefaultObjectPipe<T, ObjectReceiver<T>> {

    public static final long DEFAULT_SLEEP_TIME = 1000;

    private long sleepTime = DEFAULT_SLEEP_TIME;

    /**
     * Creates an instance of {@link ObjectSleeper}.
    */
    public ObjectSleeper() {
    }

    /**
     * Sets the time in ms for the sleep phase.
     *
     * @param sleepTime the time to sleep
     */
    public void setSleepTime(final int sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Gets the time in ms for the sleep phase.
     *
     * @return the time to sleep
     */
    public long getSleepTime() {
        return sleepTime;
    }

    /**
     * Sleeps for the specified amount of time.
     */
    public void sleep() {
        try {
            Thread.sleep(sleepTime);
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MetafactureException(e.getMessage(), e);
        }
    }

    @Override
    public void process(final T obj) {
        sleep();
        getReceiver().process(obj);
    }

}
