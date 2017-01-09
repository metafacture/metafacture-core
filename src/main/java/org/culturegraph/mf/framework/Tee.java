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
package org.culturegraph.mf.framework;


import org.culturegraph.mf.framework.helpers.DefaultTee;

/**
 * Interface for {@code Sender}s which are connected to more
 * than one downstream module.
 *
 * @param <T> receiver type
 *
 * @see DefaultTee
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
public interface Tee<T extends Receiver> extends Sender<T> {

	/**
	 * Replaces all receivers attached to the module with {@code receiver}.
	 *
	 * @param <R> type of the receiver of the downstream module
	 * @param receiver the downstream module to which the tee should connect
	 * @return reference to the downstream receiver for method chaining
	 */
	@Override
	<R extends T> R setReceiver(R receiver);

	/**
	 * Sets two receivers and returns the first. All other
	 * receivers attached to the module are removed.
	 *
	 * @param <R> type of the receiver of the downstream module
	 * @param receiver the main downstream module to which the tee should connect
	 * @param lateralReceiver the second downstream module to which the tee should
	 *                        connect
	 * @return reference to the main downstream receiver for method chaining
	 */
	<R extends T> R setReceivers(R receiver, T lateralReceiver);

	/**
	 * Adds receiver even if receiver is already added.
	 *
	 * @param receiver a downstream module to which the tee should connect
	 * @return reference to the tee for method chaining
	 */
	Tee<T> addReceiver(T receiver);

	/**
	 * Removes a receiver from the list of receivers.
	 *
	 * @param receiver the downstream receiver to remove from the list of
	 *                 connected receivers.
	 * @return reference to the tee for method chaining
	 */
	Tee<T> removeReceiver(T receiver);

	/**
	 * Clears the list of receivers.
	 *
	 * @return reference to the tee for method chaining
	 */
	 Tee<T> clearReceivers();

}
