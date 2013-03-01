/**
 * 
 */
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
package org.culturegraph.mf.stream.sink;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;

/**
 * @param <T> object type
 * 
 * @author Christoph Böhme
 *
 */

@Description("Writes objects to stdout")
@In(Object.class)
public final class ObjectStdoutWriter<T> implements ObjectReceiver<T> {

		
	@Override
	public void process(final T obj) {
		System.out.println(obj);
	}

	@Override
	public void resetStream() {
		//nothing
	}
	
	@Override
	public void closeStream() {
		//nothing
	}

}
