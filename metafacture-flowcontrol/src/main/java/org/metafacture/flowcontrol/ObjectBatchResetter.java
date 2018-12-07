/*
 * Copyright 2018 Deutsche Nationalbibliothek
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Resets the downstream modules every {@link #setBatchSize(int) batch-size} objects.
 *
 * @param <T> object type
 * @author Christoph Böhme
 */
@Description("Resets the downstream modules every batch-size objects")
@FluxCommand("reset-object-batch")
@In(Object.class)
@Out(Object.class)
public class ObjectBatchResetter<T> extends DefaultObjectPipe<T, ObjectReceiver<T>> {

    public static final int DEFAULT_BATCH_SIZE = 1000;

    private int batchSize = DEFAULT_BATCH_SIZE;

    private long batchCount;
    private int objectCount;

    /**
     * Number of objects after which a <i>reset-stream</i> event is triggered.
     * <p>
     * The default value is {@value DEFAULT_BATCH_SIZE}.
     * <p>
     * This parameter can be changed anytime during processing. If the new value
     * is less than the number of received objects a <i>reset-stream</i> event is
     * emitted when the next object is received.
     *
     * @param batchSize number of objects before a <i>reset-stream</i> event is
     *                  triggered
     */
    public void setBatchSize(int batchSize) {

        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Returns the number of batches that were processed.
     * <p>
     * This counter is reset when this module receives a <i>reset-stream</i> event.
     *
     * @return number of batches
     */
    public long getBatchCount() {
        return batchCount;
    }

    /**
     * Returns the number of objects in the current batch.
     * <p>
     * This counter is reset after each batch and also when the module receives a <i>reset-stream</i> event.
     *
     * @return number of objects in the current batch
     */
    public int getObjectCount() {
        return objectCount;
    }

    @Override
    public void process(final T obj) {

        getReceiver().process(obj);

        objectCount += 1;
        if (objectCount >= batchSize) {
            getReceiver().resetStream();
            batchCount += 1;
            objectCount = 0;
        }
    }

    @Override
    protected void onResetStream() {

        batchCount = 0;
        objectCount = 0;
    }

}
