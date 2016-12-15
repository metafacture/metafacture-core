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
package org.culturegraph.mf.framework.helpers;

import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Default implementation of {@link StreamPipe} that forwards all received
 * events. It can be used as a base class for modules that only need to handle
 * some event types.
 *
 * @author Christoph Böhme
 * @see DefaultStreamPipe
 */
public class ForwardingStreamPipe extends DefaultStreamPipe<StreamReceiver> {

	@Override
	public void startRecord(final String identifier) {
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		getReceiver().endRecord();
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
