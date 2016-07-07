/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.stream.pipe;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Writes log info every {@code batchSize} records.
 *
 * @author Markus Michael Geipel
 *
 */

@Description("Writes log info every BATCHSIZE records. ")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("batch-log")
public final class StreamBatchLogger extends AbstractBatcher {

	public static final String RECORD_COUNT_VAR = "records";
	public static final String TOTAL_RECORD_COUNT_VAR = "totalRecords";
	private static final String BATCH_COUNT_VAR = "batches";
	private static final String BATCH_SIZE_VAR = "batchSize";

	private static final Logger LOG = LoggerFactory.getLogger(StreamBatchLogger.class);
	private static final String DEFAULT_FORMAT = "records processed: ${totalRecords}";

	private final Map<String, String> vars = new HashMap<String, String>();
	private final String format;

	public StreamBatchLogger() {
		super();
		this.format = DEFAULT_FORMAT;

	}

	public StreamBatchLogger(final String format) {
		super();
		this.format = format;
	}

	public StreamBatchLogger(final String format, final Map<String, String> vars) {
		super();
		this.format = format;
		this.vars.putAll(vars);
	}

	@Override
	protected void onBatchComplete() {
		writeLog();
	}

	private void writeLog() {
		vars.put(RECORD_COUNT_VAR, Long.toString(getRecordCount()));
		vars.put(BATCH_COUNT_VAR, Long.toString(getBatchCount()));
		vars.put(BATCH_SIZE_VAR, Long.toString(getBatchSize()));
		vars.put(TOTAL_RECORD_COUNT_VAR,
				Long.toString((getBatchSize() * getBatchCount())+getRecordCount()));
		LOG.info(StringUtil.format(format, vars));
	}

	@Override
	protected void onCloseStream() {
		writeLog();
	}

}
