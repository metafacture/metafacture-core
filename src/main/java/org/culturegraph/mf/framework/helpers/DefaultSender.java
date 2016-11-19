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

import org.culturegraph.mf.framework.Receiver;
import org.culturegraph.mf.framework.Sender;

/**
 * Default implementation for {@link Sender}s that simply stores a reference to
 * the receiver and implements the correct behaviour required by the LifeCycle
 * interface.
 *
 * @param <T>
 *            receiver base type of the downstream module
 *
 * @see DefaultStreamPipe
 * @see DefaultObjectPipe
 * @see DefaultXmlPipe
 *
 * @author Christoph Böhme
 *
 */
public class DefaultSender<T extends Receiver> implements Sender<T> {

	private T receiver;
	private boolean isClosed;

	public final boolean isClosed() {
		return isClosed;
	}

	@Override
	public final <R extends T> R setReceiver(final R receiver) {
		this.receiver = receiver;
		onSetReceiver();
		return receiver;
	}

	@Override
	public final void resetStream() {
		onResetStream();
		if (receiver != null) {
			receiver.resetStream();
		}
		isClosed = false;
	}

	@Override
	public final void closeStream() {
		if (!isClosed) {
			onCloseStream();
			if (receiver != null) {
				receiver.closeStream();
			}
		}
		isClosed = true;
	}

	/**
	 * Invoked when the sender is connected with a receiver. This method is
	 * called after the receiver has been updated. Hence, {@code getReceiver}
	 * will return a reference to the new receiver.
	 */
	protected void onSetReceiver() {
		// Default implementation does nothing
	}

	/**
	 * Invoked when the {@code resetStream()} method is called. Override this
	 * method to perform a reset of the module.
	 *
	 * Do not call the {@code resetStream()} method of the next module
	 * downstream. This is handled by the implementation of
	 * {@code resetStream()} in {@code DefaultSender}.
	 *
	 * {@code onResetStream()} is called before {@code DefaultSender} calls the
	 * {@code resetStream()} method of the downstream module.
	 */
	protected void onResetStream() {
		// Default implementation does nothing
	}

	/**
	 * Invoked when the {@code closeStream()} method is called. Override this
	 * method to close any resources used by the module.
	 *
	 * Do not call the {@code closeStream()} method of the next module
	 * downstream. This is handled by the implementation of
	 * {@code closeStream()} in {@code DefaultSender}.
	 *
	 * {@code onCloseStream()} is called before {@code DefaultSender} calls the
	 * {@code closeStream()} method of the downstream module.
	 */
	protected void onCloseStream() {
		// Default implementation does nothing
	}

	/**
	 * Returns a reference to the downstream module.
	 *
	 * @return reference to the downstream module
	 */
	protected final T getReceiver() {
		return receiver;
	}

}
