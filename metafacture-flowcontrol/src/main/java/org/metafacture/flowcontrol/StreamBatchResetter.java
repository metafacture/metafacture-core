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

package org.metafacture.flowcontrol;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.ForwardingStreamPipe;

/**
 * Resets Stream every {@code batchSize} records.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 */
@Description("Resets flow for every BATCHSIZE records.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("batch-reset")
public final class StreamBatchResetter extends ForwardingStreamPipe {

    public static final long DEFAULT_BATCH_SIZE = 1000;

    private long batchSize = DEFAULT_BATCH_SIZE;
    private long recordCount;
    private long batchCount;

    public StreamBatchResetter() {
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public long getBatchCount() {
        return batchCount;
    }

    public long getRecordCount() {
        return recordCount;
    }

    @Override
    public void endRecord() {
        getReceiver().endRecord();
        ++recordCount;
        recordCount %= batchSize;
        if (recordCount == 0) {
            ++batchCount;
            getReceiver().resetStream();
        }
    }

    @Override
    protected void onResetStream() {
        recordCount = 0;
        batchCount = 0;
    }

}
