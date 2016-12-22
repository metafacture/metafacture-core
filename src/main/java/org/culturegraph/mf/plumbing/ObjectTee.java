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
package org.culturegraph.mf.plumbing;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultTee;

/**
 * Sends an object to more than one receiver.
 *
 * @param <T> Object type
 *
 * @author Christoph Böhme
 *
 */
@Description("Sends an object to more than one receiver.")
@In(Object.class)
@Out(Object.class)
@FluxCommand("object-tee")
public final class ObjectTee<T> extends DefaultTee<ObjectReceiver<T>>
		implements ObjectPipe<T, ObjectReceiver<T>> {

	@Override
	public void process(final T obj) {
		for (ObjectReceiver<T> receiver : getReceivers()) {
			receiver.process(obj);
		}
	}

}
