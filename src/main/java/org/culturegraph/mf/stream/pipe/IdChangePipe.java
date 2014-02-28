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
import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.util.StreamConstants;

/**
 * Changes the record id to the value of the _id literal if present in the
 * record.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("By default changes the record id to the value of the '_id' literal (if present). Use the contructor to choose another literal as id source.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class IdChangePipe extends DefaultStreamPipe<StreamReceiver> {

	private String idName = StreamConstants.ID;
	private final StreamBuffer streamBuffer = new StreamBuffer();
	private final StreamFlattener streamFlattener = new StreamFlattener();
	private final DefaultStreamReceiver dummyReceiver = new DefaultStreamReceiver();
	private String currentIdentifier;
	private String originalIdentifier;
	private boolean keepIdless = true;
	private boolean keepIdLiteral;

	public IdChangePipe() {
		this(StreamConstants.ID);
	}

	public IdChangePipe(final String idName) {
		super();
		setIdName(idName);
		streamFlattener.setReceiver(dummyReceiver);
	}

	public void setIdName(final String idName) {
		this.idName = idName;
	}

	public void setKeepIdless(final boolean keepIdless) {
		this.keepIdless = keepIdless;
	}

	public void setKeepIdLiteral(final boolean keepIdLiteral) {
		this.keepIdLiteral = keepIdLiteral;
	}

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		currentIdentifier = null;
		originalIdentifier = identifier;
		streamFlattener.startRecord(identifier);
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		if (currentIdentifier != null || keepIdless) {
			if (currentIdentifier == null) {
				getReceiver().startRecord(originalIdentifier);
			} else {
				getReceiver().startRecord(currentIdentifier);
			}
			streamBuffer.replay();
			getReceiver().endRecord();
		}
		streamBuffer.clear();
		streamFlattener.endRecord();
	}

	@Override
	public void startEntity(final String name) {
		streamBuffer.startEntity(name);
		streamFlattener.startEntity(name);
	}

	@Override
	public void endEntity() {
		streamBuffer.endEntity();
		streamFlattener.endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		String path = streamFlattener.getCurrentPath();
		if (!path.isEmpty()) {
			path += ".";
		}
		path += name;
		if (idName.equals(path)) {
			currentIdentifier = value;
			if (!keepIdLiteral) {
				return;
			}
		}
		streamBuffer.literal(name, value);
	}

	@Override
	public void onSetReceiver() {
		streamBuffer.setReceiver(getReceiver());
	}

	@Override
	public void onResetStream() {
		streamBuffer.clear();
	}

	@Override
	public void onCloseStream() {
		streamBuffer.clear();
	}

}
