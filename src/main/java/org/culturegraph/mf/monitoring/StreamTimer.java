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

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Benchmarks the execution time of the downstream modules.
 *
 * @author Christoph Böhme
 */
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@Description("Benchmarks the execution time of the downstream modules.")
@FluxCommand("log-stream-time")
public final class StreamTimer extends TimerBase<StreamReceiver> implements
		StreamPipe<StreamReceiver> {

	public StreamTimer() {
		this("");
	}

	public StreamTimer(final String logPrefix) {
		super(logPrefix);
	}

	@Override
	public void startRecord(final String identifier) {
		startMeasurement();
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		getReceiver().endRecord();
		stopMeasurement();
	}

	@Override
	public void startEntity(final String name) {
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		getReceiver().endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		getReceiver().literal(name, value);
	}

}
