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

/**
 * Serialise a record in CG-Text format.
 * 
 * @see CGTextDecoder
 * 
 * @author Christoph Böhme
 * 
 */
@Description("Serialise a record in CG-Text format.")
@In(StreamReceiver.class)
@Out(String.class)
public final class CGTextEncoder extends DefaultStreamPipe<ObjectReceiver<String>>  {

	private static final String START_GROUP = "={";
	private static final String END_GROUP = "}";
	private static final String SET_LITERAL = "=";
	private static final String LIST_SEP = ", ";

	private final StringBuilder builder = new StringBuilder();

	private String listSep;

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		builder.delete(0, builder.length());
		builder.append(escape(identifier));
		builder.append(START_GROUP);
		listSep = "";
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		builder.append(END_GROUP);
		getReceiver().process(builder.toString());
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		builder.append(listSep);
		builder.append(escape(name));
		builder.append(START_GROUP);
		listSep = "";
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		builder.append(END_GROUP);
		listSep = LIST_SEP;
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		builder.append(listSep);
		builder.append(escape(name));
		builder.append(SET_LITERAL);
		builder.append(escape(value));
		listSep = LIST_SEP;
	}

	@Override
	protected void onResetStream() {
		builder.delete(0, builder.length());
		listSep = "";
	}

	private String escape(final String str) {
		if (str.matches("^(?:[A-Za-z0-9-_.:]+)$")) {
			return str;
		}
		return "'" + str.replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

}
