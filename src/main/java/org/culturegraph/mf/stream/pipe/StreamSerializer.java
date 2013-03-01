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
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.converter.CGEntityEncoder;
import org.culturegraph.mf.util.StreamConstants;

/**
 * Forwards the stream without modification expect for emiting an additional 
 * literal with a serialised representation of each record prior to the 
 * end-record event. 
 * 
 * @deprecated Use a StreamTee combined with a StreamBatchMerger instead.
 *
 * @author Christoph Böhme
 * 
 * @see StreamDeserializer
 */
@Deprecated
public final class StreamSerializer extends DefaultStreamPipe<StreamReceiver> {

	private final ObjectBuffer<String> buffer = new ObjectBuffer<String>(1);
	private final StreamPipe<ObjectReceiver<String>> encoder;
	
	/**
	 * Initialises {@code StreamSerializer} with a {@link CGEntityEncoder}.
	 */
	public StreamSerializer() {
		this(new CGEntityEncoder());
	}
	
	/**
	 * Initialises {@code StreamSerializer} with a custom encoder.
	 * 
	 * @param encoder the encoder to use for serialising the records
	 */
	public StreamSerializer(final StreamPipe<ObjectReceiver<String>> encoder) {
		super();
		this.encoder = encoder;
		
		this.encoder.setReceiver(buffer);
	}
	
	@Override
	public void startRecord(final String identifier) {
		encoder.startRecord(identifier);
		getReceiver().startRecord(identifier);
	}
	
	@Override
	public void endRecord() {
		encoder.endRecord();
		getReceiver().literal(StreamConstants.SERIALIZED, buffer.pop());
		getReceiver().endRecord();
	}
	
	@Override
	public void startEntity(final String name) {
		encoder.startEntity(name);
		getReceiver().startEntity(name);
	}
	
	@Override
	public void endEntity() {
		encoder.endEntity();
		getReceiver().endEntity();
	}
	
	@Override
	public void literal(final String name, final String value) {
		encoder.literal(name, value);
		getReceiver().literal(name, value);
	}
	
}
