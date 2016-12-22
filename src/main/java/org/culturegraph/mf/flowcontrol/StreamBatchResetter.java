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
package org.culturegraph.mf.flowcontrol;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.ForwardingStreamPipe;

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

	public final void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public final long getBatchSize() {
		return batchSize;
	}

	public final long getBatchCount() {
		return batchCount;
	}

	public final long getRecordCount() {
		return recordCount;
	}

	@Override
	public final void endRecord() {
		getReceiver().endRecord();
		recordCount++;
		recordCount %= batchSize;
		if (recordCount == 0) {
			batchCount++;
			getReceiver().resetStream();
		}
	}

	@Override
	protected final void onResetStream() {
		recordCount = 0;
		batchCount = 0;
	}

}
