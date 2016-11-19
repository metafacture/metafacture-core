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

import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * An {@link ObjectReceiver} that also implements the {@link Sender} interface.
 * This interface should be implemented by all modules which receive objects
 * and invoke methods on a downstream receiver.
 *
 * @param <T> object type that this module processes
 * @param <R> receiver type of the downstream module
 *
 * @see DefaultObjectPipe
 *
 * @author Christoph Böhme
 *
 */
public interface ObjectPipe<T, R extends Receiver> extends ObjectReceiver<T>, Sender<R> {
	// Just a combination of sender and receiver
}
