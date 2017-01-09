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
/*
 *  Code based on http://www.javaspecialists.eu/archive/Issue092.html
 */
package org.culturegraph.mf.triples;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

/**
 * This memory warning system will call the listener when we exceed the
 * percentage of available memory specified. The class is static, since the
 * usage threshold can only be set to one number.
 */
final class MemoryWarningSystem {
	private static final MemoryPoolMXBean TENURED_GEN_POOL = findTenuredGenPool();
	private static final double DEFAULT_THRESHOLD = 0.8;
	private static final Collection<Listener> LISTENERS = new CopyOnWriteArrayList<Listener>();

	private MemoryWarningSystem() {
		// no instances
	}

	static {
		setUsageThreshold(DEFAULT_THRESHOLD);
		final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		final NotificationEmitter emitter = (NotificationEmitter) mbean;
		emitter.addNotificationListener(new NotificationListener() {

			@Override
			public void handleNotification(final Notification notification, final Object handback) {
				if (notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
					for (final Listener listener : getListeners()) {
						listener.memoryLow(getUsedMemory(), getMaxMemory());
					}
				}
			}
		}, null, null);
	}

	private static Collection<Listener> getListeners(){
		return LISTENERS;
	}

	private static long getMaxMemory() {
		return TENURED_GEN_POOL.getUsage().getMax();
	}

	private static long getUsedMemory() {
		return TENURED_GEN_POOL.getUsage().getUsed();
	}

	static boolean addListener(final Listener listener) {
		return LISTENERS.add(listener);
	}

	static boolean removeListener(final Listener listener) {
		return LISTENERS.remove(listener);
	}

	private static void setUsageThreshold(final double threshold) {
		if (threshold <= 0.0 || threshold > 1.0) {
			throw new IllegalArgumentException("'threshold' must be in [0.0, 1.0]");
		}
		TENURED_GEN_POOL.setUsageThreshold((long) (getMaxMemory() * threshold));
	}

	/**
	 * Tenured Space Pool can be determined by it being of type HEAP and by it
	 * being possible to set the usage threshold.
	 *
	 * @return MXBean for the Tenured Space Pool
	 */
	private static MemoryPoolMXBean findTenuredGenPool() {
		// I don't know whether this approach is better, or whether
		// we should rather check for the pool name "Tenured Gen"?
		for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans())
			if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
				return pool;
			}
		throw new AssertionError("Could not find tenured space");
	}

	/**
	 * Interface for low memory listeners
	 */
	interface Listener {
		void memoryLow(long usedMemory, long maxMemory);
	}

}
