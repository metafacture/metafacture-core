/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.lang.StringEscapeUtils;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Serialises an object as JSON. Records and entities are represented as objects
 * unless their name ends with []. If the name ends with [], an array is
 * created.
 * 
 * @author Markus Geipel
 * 
 */
@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(String.class)
public final class SimpleJsonEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	public static final String ARRAY_MARKER = "[]";

	private StringBuilder builder = new StringBuilder();
	private final Deque<Character> bracketStack = new LinkedList<Character>();
	private final Deque<Boolean> commaStack = new LinkedList<Boolean>();
	private boolean commaNeeded;


	@Override
	public void startRecord(final String id) {
		builder.append('{');
		bracketStack.push(Character.valueOf('}'));
	}

	@Override
	public void endRecord() {
		builder.append('}');
		bracketStack.clear();
		getReceiver().process(builder.toString());
		builder = new StringBuilder();
		commaStack.clear();
	}

	@Override
	public void startEntity(final String name) {

		if (commaNeeded) {
			builder.append(", ");
		}
		commaNeeded = true;

		if (name.endsWith(ARRAY_MARKER)) {
			builder.append("\"" + escape(name.substring(0, name.length() - ARRAY_MARKER.length())) + "\": [ ");
			bracketStack.push(Character.valueOf(']'));
		} else {
			if (inArray()) {
				builder.append(" { ");
			} else {
				builder.append("\"" + escape(name) + "\": { ");
			}

			bracketStack.push(Character.valueOf('}'));
		}
		commaStack.push(Boolean.valueOf(commaNeeded));
		commaNeeded = false;
	}

	private static String escape(final String string) {
		return StringEscapeUtils.escapeJavaScript(string);
	}

	@Override
	public void endEntity() {
		builder.append(bracketStack.pop() + " ");
		commaNeeded = commaStack.pop().booleanValue();
	}

	@Override
	public void literal(final String name, final String value) {

		if (commaNeeded) {
			builder.append(", ");
		}

		if (inArray()) {
			builder.append("\"" + escape(value) + "\"");
		} else {
			builder.append("\"" + escape(name) + "\":\"" + escape(value) + "\"");
		}

		commaNeeded = true;
	}

	private boolean inArray() {
		return bracketStack.peek().charValue() == ']';
	}

}
