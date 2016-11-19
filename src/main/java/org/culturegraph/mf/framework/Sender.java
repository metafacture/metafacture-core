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

import org.culturegraph.mf.framework.helpers.DefaultSender;

/**
 * Interface for objects that can be connected to a downstream module (receiver).
 *
 * @param <T> receiver base type of the downstream module
 *
 * @see DefaultSender
 *
 * @author Christoph Böhme
 *
 */
public interface Sender<T extends Receiver> extends LifeCycle {

	/**
	 * Connect to a downstream module.
	 *
	 * @param reference to the downstream module
	 * @return reference to the downstream receiver to enable method chaining
	 */
	<R extends T> R setReceiver(R receiver);

}
