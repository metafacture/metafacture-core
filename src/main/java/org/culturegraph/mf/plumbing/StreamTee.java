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
package org.culturegraph.mf.plumbing;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultTee;

/**
 * Replicates an event stream to an arbitrary number of {@link StreamReceiver}s.
 *
 * @author Christoph Böhme, Markus Michael Geipel
 *
 */
@Description("Replicates an event stream to an arbitrary number of stream receivers.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("stream-tee")
public final class StreamTee extends DefaultTee<StreamReceiver>
		implements StreamPipe<StreamReceiver> {

	@Override
	public void startRecord(final String identifier) {
		for (StreamReceiver receiver : getReceivers()) {
			receiver.startRecord(identifier);
		}
	}

	@Override
	public void endRecord() {
		for (StreamReceiver receiver : getReceivers()) {
			receiver.endRecord();
		}
	}

	@Override
	public void startEntity(final String name) {
		for (StreamReceiver receiver : getReceivers()) {
			receiver.startEntity(name);
		}
	}

	@Override
	public void endEntity() {
		for (StreamReceiver receiver : getReceivers()) {
			receiver.endEntity();
		}
	}

	@Override
	public void literal(final String name, final String value) {
		for (StreamReceiver receiver : getReceivers()) {
			receiver.literal(name, value);
		}
	}

}
