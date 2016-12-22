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

import java.util.ArrayList;
import java.util.List;

import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;


/**
 * {@link StreamPipe} which buffers incoming records and replays them upon
 * request.
 *
 * @author Markus Michael Geipel
 *
 */
public final class StreamBuffer
		extends DefaultStreamPipe<StreamReceiver> {

	/**
	 * Defines entity and literal message types.
	 */
	private enum MessageType {
		RECORD_START, RECORD_END, ENTITY_START, ENTITY_END, LITERAL
	}

	private final List<MessageType> typeBuffer = new ArrayList<StreamBuffer.MessageType>();
	private final List<String> valueBuffer = new ArrayList<String>();


	public boolean isEmpty(){
		return typeBuffer.isEmpty();
	}

	/**
	 * Replays the buffered event.
	 */
	public void replay() {

		int index = 0;
		for (MessageType type : typeBuffer) {
			switch (type) {
			case RECORD_START:
				getReceiver().startRecord(valueBuffer.get(index));
				++index;
				break;
			case RECORD_END:
				getReceiver().endRecord();
				break;

			case ENTITY_START:
				getReceiver().startEntity(valueBuffer.get(index));
				++index;
				break;
			case ENTITY_END:
				getReceiver().endEntity();
				break;
			default:
				getReceiver().literal(valueBuffer.get(index), valueBuffer.get(index+1));
				index +=2;
				break;
			}
		}
	}

	public void clear() {
		typeBuffer.clear();
		valueBuffer.clear();
	}

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		typeBuffer.add(MessageType.RECORD_START);
		valueBuffer.add(identifier);
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		typeBuffer.add(MessageType.RECORD_END);
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		typeBuffer.add(MessageType.ENTITY_START);
		valueBuffer.add(name);
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		typeBuffer.add(MessageType.ENTITY_END);
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		typeBuffer.add(MessageType.LITERAL);
		valueBuffer.add(name);
		valueBuffer.add(value);
	}

	@Override
	protected void onResetStream() {
		clear();
	}

}
