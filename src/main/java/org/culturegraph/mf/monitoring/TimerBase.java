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
package org.culturegraph.mf.monitoring;

import org.culturegraph.mf.commons.TimeUtil;
import org.culturegraph.mf.framework.Receiver;
import org.culturegraph.mf.framework.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Christoph Böhme
 *
 * @param <R>
 *            receiver type.
 */
public class TimerBase<R extends Receiver> implements Sender<R> {

	private static final Logger LOG = LoggerFactory.getLogger(TimerBase.class);

	private final String logPrefix;

	private long count;
	private long cumulativeDuration;
	private long startTime;

	private R receiver;

	protected TimerBase(final String logPrefix) {
		super();
		this.logPrefix = logPrefix;
	}

	@Override
	public final <S extends R> S setReceiver(final S receiver) {
		this.receiver = receiver;
		return receiver;
	}

	public final R getReceiver() {
		return receiver;
	}

	@Override
	public final void resetStream() {
		count = 0;
		cumulativeDuration = 0;
		if (receiver != null) {
			receiver.resetStream();
		}
	}

	@Override
	public final void closeStream() {
		final long averageDuration;
		if (count > 0) {
			averageDuration = cumulativeDuration / count;
		} else {
			averageDuration = 0;
		}
		LOG.info(logPrefix
				+ String.format("Executions: %d; Cumulative duration: %s; Average duration: %s", Long.valueOf(count),
						TimeUtil.formatDuration(cumulativeDuration), TimeUtil.formatDuration(averageDuration)));
		startMeasurement();
		if (receiver != null) {
			receiver.closeStream();
		}
		stopMeasurement("Time to close stream: ");
	}

	protected final void startMeasurement() {
		startTime = System.nanoTime();
	}

	protected final void stopMeasurement(){
		stopMeasurement("Execution %1$d:");
	}

	protected final void stopMeasurement(final String prefix) {
		final long duration = System.nanoTime() - startTime;

		count += 1;
		cumulativeDuration += duration;

		LOG.info(logPrefix + String.format(prefix + " %2$s", Long.valueOf(count), TimeUtil.formatDuration(duration)));
	}

}
