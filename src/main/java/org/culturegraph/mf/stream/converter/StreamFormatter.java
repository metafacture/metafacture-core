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
package org.culturegraph.mf.stream.converter;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * Simple {@link StreamReceiver} for debugging. Formats the received messages,
 * counts the records, and writes everything to a writer.
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
@Description("Formats a stream as strings")
@In(StreamReceiver.class)
@Out(String.class)
public final class StreamFormatter extends DefaultStreamPipe<ObjectReceiver<String>>{

	private static final String PREFIX = " ";
	private static final char INDENT_CHAR = '\t';

	private final StringBuilder indentBuilder = new StringBuilder(PREFIX);
	private String indent = PREFIX;
	private int count;
	//private boolean closed;



	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		count += 1;
		getReceiver().process("RECORD " + count + ": " + identifier);
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		getReceiver().process("END");
	}


	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		getReceiver().process(indent + "> " + name);
		indentBuilder.append(INDENT_CHAR);
		indent = indentBuilder.toString();
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		getReceiver().process(indent + "< ");
		indentBuilder.deleteCharAt(indentBuilder.length() - 1);
		indent = indentBuilder.toString();
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		getReceiver().process(indent + name + " = " + value);
	}
}
