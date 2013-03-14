/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.pipe;

import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Merges a sequence of {@code batchSize} records. On a close-stream event,
 * a record containing less than {@code batchSize} source records may be 
 * created.
 * 
 * @author Christoph Böhme
 *
 */
@Description("Merges a sequence of batchSize records")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class StreamBatchMerger extends AbstractBatcher {

	private boolean inRecord;
	
	public StreamBatchMerger() {
		super();
		setInternalReceiver(new Merger());
	}
	
	@Override
	protected void onBatchComplete() {
		getReceiver().endRecord();
		inRecord = false;
	}
	
	@Override
	protected void onReset() {
		inRecord = false;
	}

	@Override
	protected void onCloseStream() {
		if (inRecord) {
			onBatchComplete();
		}
	}
	
	/**
	 * Helper class for merging.
	 */
	private final class Merger extends DefaultStreamReceiver {
		
		@Override
		public void startRecord(final String identifier) {
			if (!inRecord) {
				getReceiver().startRecord(identifier);
				inRecord = true;
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
		
	}
	
}
