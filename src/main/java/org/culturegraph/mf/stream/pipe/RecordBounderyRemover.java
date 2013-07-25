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
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.util.StreamConstants;


/**
 * Removes record boundaries
 * 
 * @author Markus Michael Geipel
 *
 */
@Description("Removes record boundaries")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class RecordBounderyRemover 
		extends DefaultStreamPipe<StreamReceiver> {
	
	private final String entityName;
	
	public RecordBounderyRemover(final String entityName) {
		super();
		this.entityName = entityName;
	}
	
	public RecordBounderyRemover(){
		super();
		this.entityName = null;
		
	}
	
	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();

		if (null != entityName) {
			getReceiver().startEntity(entityName);
			getReceiver().literal(StreamConstants.ID, identifier);
		}
	}

	@Override
	public void endRecord() {
		assert !isClosed();

		if (null != entityName) {
			getReceiver().endEntity();
		}
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
}
