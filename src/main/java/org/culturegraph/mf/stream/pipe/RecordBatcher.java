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

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * 
 * Count records and notifies a listener when a batch is complete.
 * 
 * @author Christoph Böhme, Markus Geipel
 * @deprecated use implementations of {@link AbstractBatcher} instead
 */
@Deprecated
public final class RecordBatcher 
		extends DefaultStreamPipe<StreamReceiver> {

	/**
	 * Classes interested in notification on batch completion
	 * need to implement this interface.
	 */
	public interface BatchListener {
		void batchComplete(final RecordBatcher modelBatcher);
	}
	
	private long count;
	private long batchSize;

	private final BatchListener listener;

	public RecordBatcher(final BatchListener listener, final int batchSize) {
		super();
		this.batchSize = batchSize;
		this.listener = listener;
	}

	public long getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	@Override
	public void startRecord(final String identifier) {
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		getReceiver().endRecord();

		++count;
		count %= batchSize;
		if (0 == count) {
			listener.batchComplete(this);
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
