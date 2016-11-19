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

import org.culturegraph.mf.framework.helpers.DefaultObjectReceiver;

/**
 * Interface for objects which receive and process objects of type T.
 *
 * @param <T> object type
 *
 * @see DefaultObjectReceiver
 * @see ObjectPipe
 *
 * @author Christoph Böhme
 *
 */
public interface ObjectReceiver<T> extends Receiver {

	/**
	 * This method is called by upstream modules to trigger the
	 * processing of {@code obj}.
	 *
	 * @param obj the object to be processed
	 */
	void process(T obj);

}
