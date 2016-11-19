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

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.Receiver;

/**
 * Default implementation for {@link ObjectPipe}s which simply
 * does nothing.
 *
 * @param <T> object type that this module processes
 * @param <R> receiver type of the downstream module

 * @author Christoph Böhme
 *
 */
public class DefaultObjectPipe<T, R extends Receiver>
		extends DefaultSender<R> implements ObjectPipe<T, R> {

	@Override
	public void process(final T obj) {
		// Default implementation does nothing
	}

}
