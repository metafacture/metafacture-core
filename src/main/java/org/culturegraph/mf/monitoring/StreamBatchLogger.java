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
package org.culturegraph.mf.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.ForwardingStreamPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final long DEFAULT_BATCH_SIZE = 1000;

	private static final Logger LOG =
			LoggerFactory.getLogger(StreamBatchLogger.class);

	private static final String DEFAULT_FORMAT =
			"records processed: ${totalRecords}";

	private final Map<String, String> vars = new HashMap<>();
	private final String format;

	private long batchSize = DEFAULT_BATCH_SIZE;
	private long recordCount;
	private long batchCount;

	public StreamBatchLogger() {
		this.format = DEFAULT_FORMAT;
	}

	public StreamBatchLogger(final String format) {
		this.format = format;
	}

	public StreamBatchLogger(final String format, final Map<String, String> vars) {
		this.format = format;
		this.vars.putAll(vars);
	}

	public final void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public final long getBatchSize() {
		return batchSize;
	}

	public long getBatchCount() {
		return batchCount;
	}

	public long getRecordCount() {
		return recordCount;
	}

	@Override
	public final void endRecord() {
		getReceiver().endRecord();
		recordCount++;
		recordCount %= batchSize;
		if (recordCount == 0) {
			batchCount++;
			writeLog();
		}
	}

	@Override
	protected void onCloseStream() {
		writeLog();
	}

	@Override
	protected final void onResetStream() {
		recordCount = 0;
		batchCount = 0;
	}

	private void writeLog() {
		vars.put(RECORD_COUNT_VAR, Long.toString(recordCount));
		vars.put(BATCH_COUNT_VAR, Long.toString(batchCount));
		vars.put(BATCH_SIZE_VAR, Long.toString(batchSize));
		vars.put(TOTAL_RECORD_COUNT_VAR,
				Long.toString(batchSize * batchCount + recordCount));
		LOG.info(StringUtil.format(format, vars));
	}

}
