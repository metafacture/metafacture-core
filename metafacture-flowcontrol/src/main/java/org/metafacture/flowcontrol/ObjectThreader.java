/* Copyright 2019 hbz, Pascal Christoph. 
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

package org.metafacture.flowcontrol;

import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.Tee;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultTee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Divides incoming objects and distributes them to added receivers. These
 * receivers are coupled with an
 * {@link org.metafacture.flowcontrol.ObjectPipeDecoupler}, so each added
 * receiver runs in its own thread.
 * 
 * @param <T> Object type
 *
 * @author Pascal Christoph(dr0i)
 * 
 */
@In(Object.class)
@Out(Object.class)
public class ObjectThreader<T> extends DefaultTee<ObjectReceiver<T>> implements ObjectPipe<T, ObjectReceiver<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectThreader.class);
    private int objectNumber = 0;

    @Override
    public void process(final T obj) {
        getReceivers().get(objectNumber).process(obj);
        if (objectNumber == getReceivers().size() - 1)
            objectNumber = 0;
        else
            objectNumber++;
    }

    @Override
    public <R extends ObjectReceiver<T>> R setReceiver(final R receiver) {
        return super.setReceiver(new ObjectPipeDecoupler<T>().setReceiver(receiver));
    }

    @Override
    public Tee<ObjectReceiver<T>> addReceiver(final ObjectReceiver<T> receiver) {
        LOG.info("Adding thread {0}" + (getReceivers().size() + 1));
        ObjectPipeDecoupler<T> opd = new ObjectPipeDecoupler<>();
        opd.setReceiver(receiver);
        return super.addReceiver(opd);
    }
}
