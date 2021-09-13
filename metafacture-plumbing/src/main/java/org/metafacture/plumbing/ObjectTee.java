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

package org.metafacture.plumbing;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultTee;

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
public final class ObjectTee<T> extends DefaultTee<ObjectReceiver<T>> implements ObjectPipe<T, ObjectReceiver<T>> {

    public ObjectTee() {
    }

    @Override
    public void process(final T obj) {
        for (final ObjectReceiver<T> receiver : getReceivers()) {
            receiver.process(obj);
        }
    }

}
