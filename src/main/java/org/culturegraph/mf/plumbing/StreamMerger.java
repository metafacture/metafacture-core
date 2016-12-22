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
package org.culturegraph.mf.plumbing;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Merges records based on their id. The module compares the id
 * of each record it receives with the one of previous record; if
 * both records have got the same id, then the end-record and
 * start-record events that would normally separate the records
 * are suppressed in the output stream of {@code StreamMerger}.
 * This effectively merges the two records. Of course, this only
 * works if the records which are to be merged follow each other
 * directly.
 *
 * @author Christoph Böhme
 *
 */
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("merge-same-ids")
public final class StreamMerger
		extends DefaultStreamPipe<StreamReceiver> {

	private boolean hasRecordsReceived;
	private String currentId = "";

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		if (!currentId.equals(identifier)) {
			if (hasRecordsReceived) {
				getReceiver().endRecord();
			}
			getReceiver().startRecord(identifier);
			currentId = identifier;
		}

		hasRecordsReceived = true;
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		getReceiver().endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		getReceiver().literal(name, value);
	}

	@Override
	protected void onResetStream() {
		hasRecordsReceived = false;
		currentId = "";
	}

	@Override
	protected void onCloseStream() {
		if (hasRecordsReceived) {
			getReceiver().endRecord();
		}
		onResetStream();
	}

}
