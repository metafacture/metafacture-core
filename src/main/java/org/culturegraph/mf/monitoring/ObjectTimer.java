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
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

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

	public ObjectTimer() {
		this("");
	}

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
