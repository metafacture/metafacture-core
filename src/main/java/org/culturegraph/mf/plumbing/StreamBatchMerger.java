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
package org.culturegraph.mf.plumbing;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Merges a sequence of {@link #setBatchSize(long)} records. On a
 * <i>close-stream</i> event, a record containing fewer source records may be
 * created.
 *
 * @author Christoph Böhme
 *
 */
@Description("Merges a sequence of batchSize records")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("merge-batch-stream")
public final class StreamBatchMerger extends DefaultStreamPipe<StreamReceiver> {

	/**
	 * The default value for {@link #setBatchSize(long)}.
	 */
	public static final long DEFAULT_BATCH_SIZE = 1;

	private long batchSize = DEFAULT_BATCH_SIZE;
	private long recordCount;

	/**
	 * Sets the number of records that should be merged into a batch.
	 * <p>
	 * The default batch size is 1, wich means that no records are merged.
	 * <p>
	 * This parameter must not be changed during processing.
	 *
	 * @param batchSize the number of records that should be merged.
	 */
	public void setBatchSize(final long batchSize) {
		this.batchSize = batchSize;
	}

	public long getBatchSize() {
		return batchSize;
	}

	@Override
	public void startRecord(final String identifier) {
		if (recordCount == 0) {
			getReceiver().startRecord(identifier);
		}
		recordCount += 1;
	}

	@Override
	public void endRecord() {
		if (recordCount >= batchSize) {
			getReceiver().endRecord();
			recordCount = 0;
		}
	}

	@Override
	public void startEntity(final String name) {
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		getReceiver().endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		getReceiver().literal(name, value);
	}

	@Override
	protected void onResetStream() {
		recordCount = 0;
	}

	@Override
	protected void onCloseStream() {
		if (recordCount > 0) {
			getReceiver().endRecord();
			recordCount = 0;
		}
	}

}
