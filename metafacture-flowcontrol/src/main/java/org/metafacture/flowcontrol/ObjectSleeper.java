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

import java.util.concurrent.TimeUnit;

/**
 * Lets the process sleep for a specific amount of time between objects.
 *
 * @param <T> object type
 * @author Tobias Bülte
 */
@Description("Lets the process sleep for a specific amount of time between objects.")
@In(Object.class)
@Out(Object.class)
@FluxCommand("sleep")
public final class ObjectSleeper<T> extends DefaultObjectPipe<T, ObjectReceiver<T>> {

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final long DEFAULT_SLEEP_TIME = 1000;

    private static final String TIME_UNIT_SUFFIX = "S";

    private TimeUnit timeUnit = DEFAULT_TIME_UNIT;
    private long sleepTime = DEFAULT_SLEEP_TIME;

    /**
     * Creates an instance of {@link ObjectSleeper}.
    */
    public ObjectSleeper() {
    }

    /**
     * Sets the amount of time for the sleep phase (measured in {@link
     * #setTimeUnit time unit}).
     *
     * @param sleepTime the time to sleep
     */
    public void setSleepTime(final int sleepTime) {
        // NOTE: ConfigurableClass.convertValue() doesn't support long.
        setSleepTime((long) sleepTime);
    }

    /**
     * Sets the amount of time for the sleep phase (measured in {@link
     * #setTimeUnit time unit}).
     *
     * @param sleepTime the time to sleep
     */
    public void setSleepTime(final long sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Gets the amount of time for the sleep phase (measured in {@link
     * #setTimeUnit time unit}).
     *
     * @return the time to sleep
     */
    public long getSleepTime() {
        return sleepTime;
    }

    /**
     * Sets the time unit for the sleep phase. See {@link TimeUnit available
     * time units}, case-insensitive, trailing "s" optional.
     *
     * @param timeUnit the time unit
     */
    public void setTimeUnit(final String timeUnit) {
        // NOTE: Adds NANOSECONDS and DAYS over Catmandu's supported time units.

        final String timeUnitName = timeUnit.toUpperCase();
        final String timeUnitSuffix = timeUnitName.endsWith(TIME_UNIT_SUFFIX) ? "" : TIME_UNIT_SUFFIX;

        setTimeUnit(TimeUnit.valueOf(timeUnitName + timeUnitSuffix));
    }

    /**
     * Sets the time unit for the sleep phase.
     *
     * @param timeUnit the time unit
     */
    public void setTimeUnit(final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Gets the time unit for the sleep phase.
     *
     * @return the time unit
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Sleeps for the specified amount of time.
     */
    public void sleep() {
        try {
            timeUnit.sleep(sleepTime);
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
