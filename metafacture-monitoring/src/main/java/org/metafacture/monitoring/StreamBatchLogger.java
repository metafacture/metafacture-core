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

package org.metafacture.monitoring;

import org.metafacture.commons.StringUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureLogger;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.ForwardingStreamPipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Writes log info every {@code batchSize} records.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 */
@Description("Writes log info every BATCHSIZE records. ")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("batch-log")
public final class StreamBatchLogger extends ForwardingStreamPipe {

    public static final String RECORD_COUNT_VAR = "records";
    public static final String BATCH_COUNT_VAR = "batches";
    public static final String BATCH_SIZE_VAR = "batchSize";
    public static final String TOTAL_RECORD_COUNT_VAR = "totalRecords";
    public static final String DEFAULT_FORMAT = "records processed: ${totalRecords}";

    public static final long DEFAULT_BATCH_SIZE = 1000;

    private static final MetafactureLogger LOG = new MetafactureLogger(StreamBatchLogger.class);

    private final Map<String, String> vars = new HashMap<>();
    private final String format;

    private long batchSize = DEFAULT_BATCH_SIZE;
    private long recordCount;
    private long batchCount;

    /**
     * Creates an instance of {@link StreamBatchLogger} by a given format. The
     * default format: {@value #DEFAULT_FORMAT}
     */
    public StreamBatchLogger() {
        this.format = DEFAULT_FORMAT;
    }

    /**
     * Creates an instance of {@link StreamBatchLogger} by a given format.
     *
     * @param format the format
     */
    public StreamBatchLogger(final String format) {
        this.format = format;
    }

    /**
     * Constructs a StreamBatchLogger with a format and a map of variables.
     *
     * @param format a format
     * @param vars   a map of variables
     */
    public StreamBatchLogger(final String format, final Map<String, String> vars) {
        this.format = format;
        this.vars.putAll(vars);
    }

    /**
     * Sets the batch size.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Gets the batch size.
     *
     * @return the batch size
     */
    public long getBatchSize() {
        return batchSize;
    }

    /**
     * Gets the batch count.
     *
     * @return the batch count
     */
    public long getBatchCount() {
        return batchCount;
    }

    /**
     * Gets the record count.
     *
     * @return the record count
     */
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
            writeLog();
        }
    }

    @Override
    protected void onCloseStream() {
        writeLog();
    }

    @Override
    protected void onResetStream() {
        recordCount = 0;
        batchCount = 0;
    }

    private void writeLog() {
        vars.put(RECORD_COUNT_VAR, Long.toString(recordCount));
        vars.put(BATCH_COUNT_VAR, Long.toString(batchCount));
        vars.put(BATCH_SIZE_VAR, Long.toString(batchSize));
        vars.put(TOTAL_RECORD_COUNT_VAR,
                Long.toString(batchSize * batchCount + recordCount));
        LOG.externalInfo(StringUtil.format(format, vars));
    }

}
