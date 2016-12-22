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
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes log info for every BATCHSIZE records.
 *
 * @param <T>
 *            object type
 *
 * @author Markus Geipel
 *
 */
@Description("Writes log info for every BATCHSIZE records.")
@In(Object.class)
@Out(Object.class)
@FluxCommand("object-batch-log")
public final class ObjectBatchLogger<T> extends DefaultObjectPipe<T, ObjectReceiver<T>> {

	public static final String RECORD_COUNT_VAR = "records";
	public static final String TOTAL_RECORD_COUNT_VAR = "totalRecords";
	public static final long DEFAULT_BATCH_SIZE = 1000;
	public static final String BATCH_COUNT_VAR = "batches";
	public static final String BATCH_SIZE_VAR = "batchSize";

	private static final Logger LOG = LoggerFactory.getLogger(ObjectBatchLogger.class);
	private static final String DEFAULT_FORMAT = "records processed: ${totalRecords}";

	private final Map<String, String> vars = new HashMap<String, String>();
	private final String format;

	private long batchSize = DEFAULT_BATCH_SIZE;
	private long recordCount;
	private long batchCount;

	public ObjectBatchLogger() {
		super();
		this.format = DEFAULT_FORMAT;

	}

	public ObjectBatchLogger(final String format) {
		super();
		this.format = format;
	}

	public ObjectBatchLogger(final String format, final Map<String, String> vars) {
		super();
		this.format = format;
		this.vars.putAll(vars);
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	private void writeLog() {
		vars.put(RECORD_COUNT_VAR, Long.toString(recordCount));
		vars.put(BATCH_COUNT_VAR, Long.toString(batchCount));
		vars.put(BATCH_SIZE_VAR, Long.toString(batchSize));
		vars.put(TOTAL_RECORD_COUNT_VAR, Long.toString((batchSize * batchCount) + recordCount));
		LOG.info(StringUtil.format(format, vars));
	}

	@Override
	protected void onCloseStream() {
		writeLog();
	}

	@Override
	public void process(final T obj) {

		if (getReceiver() != null) {
			getReceiver().process(obj);
		}

		++recordCount;
		recordCount %= batchSize;
		if (0 == recordCount) {
			++batchCount;
			writeLog();
		}
	}

}
