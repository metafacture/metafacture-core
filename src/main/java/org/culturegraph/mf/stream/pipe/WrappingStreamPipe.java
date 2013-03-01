/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.pipe;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.LifeCycle;

/**
 * Base class for modules which may rely on internal sub-flows. 
 * 
 *  
 * @param <T> receiver type
 * 
 * @author Christoph Böhme
 */
public class WrappingStreamPipe<T extends LifeCycle> extends
		DefaultStreamPipe<T> {

	private T internalReceiver;
	private boolean autoConnect = true;
	
	protected final void setAutoConnect(final boolean autoConnect) {
		this.autoConnect = autoConnect;
		onSetReceiver();
	}
	
	protected final boolean isAutoConnect() {
		return autoConnect;
	}
	
	protected final <R extends T> R setInternalReceiver(final R internalReceiver) {
		this.internalReceiver = internalReceiver;
		autoConnect = false;
		return internalReceiver;
	}
	
	protected final T getInternalReceiver() {
		return internalReceiver;
	}
	
	@Override
	protected final void onSetReceiver() {
		if (autoConnect) {
			internalReceiver = getReceiver();
		}
	}
	
	// FIXME: resetStream and closeStream events are not propagated to the internal flow.
	
}
