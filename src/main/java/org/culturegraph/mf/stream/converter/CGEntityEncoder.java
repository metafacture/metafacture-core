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
package org.culturegraph.mf.stream.converter;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.CGEntity;

/**
 * Encodes an event stream in CGEntity format.
 * 
 * @see CGEntityDecoder
 * 
 * @author Markus Michael Geipel, Christoph Böhme
 *
 * @deprecated Use FormetaEncoder instead
 */
@Description("Encodes a stream in CGE Format")
@In(StreamReceiver.class)
@Out(String.class)
@Deprecated
public final class CGEntityEncoder 
 extends DefaultStreamPipe<ObjectReceiver<String>>  {

	private StringBuilder builder = new StringBuilder();
	
	@Override
	public void startRecord(final String identifier) {
		builder = new StringBuilder();
		builder.append(identifier);
		builder.append(CGEntity.FIELD_DELIMITER);
	}

	@Override
	public void endRecord() {
		getReceiver().process(builder.toString());
	}

	@Override
	public void startEntity(final String name) {
		builder.append(CGEntity.ENTITY_START_MARKER);
		builder.append(name);
		builder.append(CGEntity.FIELD_DELIMITER);
	}

	@Override
	public void endEntity() {
		builder.append(CGEntity.ENTITY_END_MARKER);
		builder.append(CGEntity.FIELD_DELIMITER);
	}

	@Override
	public void literal(final String name, final String value) {
		builder.append(CGEntity.LITERAL_MARKER);
		builder.append(name);
		builder.append(CGEntity.SUB_DELIMITER);
		builder.append(value.replace(CGEntity.NEWLINE, CGEntity.NEWLINE_ESC));
		builder.append(CGEntity.FIELD_DELIMITER);
	}

	@Override
	protected void onResetStream() {
		builder = new StringBuilder();
	}
	
	public String getCurrentSerialization(){
		return builder.toString();
	}
	
}