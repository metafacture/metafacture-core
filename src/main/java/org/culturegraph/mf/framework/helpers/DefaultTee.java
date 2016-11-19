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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.culturegraph.mf.framework.Receiver;
import org.culturegraph.mf.framework.Tee;


/**
 * Default implementation for tee modules.
 *
 * @param <T> receiver type
 *
 * @author Christoph Böhme
 */
public class DefaultTee<T extends Receiver> implements Tee<T> {

	private final List<T> receivers = new ArrayList<T>();

	@Override
	public final <R extends T> R setReceiver(final R receiver) {
		receivers.clear();
		receivers.add(receiver);
		onChangeReceivers();
		return receiver;
	}

	@Override
	public final <R extends T> R setReceivers(final R receiver, final T lateralReceiver) {
		receivers.clear();
		receivers.add(receiver);
		receivers.add(lateralReceiver);
		onChangeReceivers();
		return receiver;
	}

	@Override
	public final Tee<T> addReceiver(final T receiver) {
		receivers.add(receiver);
		onChangeReceivers();
		return this;
	}

	@Override
	public final Tee<T> removeReceiver(final T receiver) {
		receivers.remove(receiver);
		onChangeReceivers();
		return this;
	}

	@Override
	public final Tee<T> clearReceivers() {
		receivers.clear();
		onChangeReceivers();
		return this;
	}

	@Override
	public final void resetStream() {
		onResetStream();
		for (final T receiver : receivers) {
			receiver.resetStream();
		}
	}

	@Override
	public final void closeStream() {
		onCloseStream();
		for (final T receiver : receivers) {
			receiver.closeStream();
		}
	}

	/**
	 * Invoked when the list of receivers connected to this tee is changed.
	 * This method is called after the receiver has been updated. Hence,
	 * {@code getReceivers} will return a reference to the new list of
	 * receivers.
	 */
	protected void onChangeReceivers() {
		// Default implementation does nothing
	}

	/**
	 * Invoked when the {@code resetStream()} method is called.
	 * Override this method to perform a reset of the module.
	 *
	 * Do not call the {@code resetStream()} method of the next modules downstream.
	 * This is handled by the implementation of {@code resetStream()} in
	 * {@code DefaultTee}.
	 *
	 * {@code onResetStream()} is called before {@code DefaultTee} calls the
	 * {@code resetStream()} method of the downstream modules.
	 */
	protected void onResetStream() {
		// Default implementation does nothing
	}

	/**
	 * Invoked when the {@code closeStream()} method is called. Override
	 * this method to close any resources used by the module.
	 *
	 * Do not call the {@code closeStream()} method of the next modules
	 * downstream. This is handled by the implementation of
	 * {@code closeStream()} in {@code DefaultTee}.
	 *
	 * {@code onCloseStream()} is called before {@code DefaultTee} calls
	 * the {@code closeStream()} method of the downstream modules.
	 */
	protected void onCloseStream() {
		// Default implementation does nothing
	}

	/**
	 * Returns a reference to the downstream module.
	 *
	 * @return reference to the downstream module
	 */
	protected final List<T> getReceivers() {
		return Collections.unmodifiableList(receivers);
	}

}
