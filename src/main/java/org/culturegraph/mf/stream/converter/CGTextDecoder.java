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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * Decodes a record stored in CG-Text format.
 * 
 * @see CGTextEncoder
 * 
 * @author Christoph Böhme
 * 
 */
@Description("Decodes a record stored in CG-Text format.")
@In(String.class)
@Out(StreamReceiver.class)
public final class CGTextDecoder 
		extends DefaultObjectPipe<String, StreamReceiver> {

	private static final String UNQUOTED_NAME = "(?:[A-Za-z0-9-_.:]+)";
	private static final String QUOTED_NAME = "(?:'(?:\\\\'|[^'])*')"; 
	private static final String NAME = "(" + UNQUOTED_NAME + "|" + QUOTED_NAME + ")";
	private static final String GROUP_START = "(?:\\{)";
	private static final String GROUP_END = "(?:\\})";	
	private static final String CONTENT = "(?:(.*))";
	private static final String LEADING_WS = "(?:(?:\\A|\\G)\\s*)";
	private static final String TRAILING_WS = "(?:\\s*$)";
	private static final String ASSIGNMENT = "(?:\\s*=\\s*)";
	private static final String LIST_SEP = "(?:(?:\\s*,\\s*)|(?=\\s*\\})|" + TRAILING_WS + ")";
	
	private static final Pattern RECORD = Pattern.compile( 
			LEADING_WS + NAME + ASSIGNMENT + GROUP_START + CONTENT + GROUP_END + TRAILING_WS);
	private static final Pattern ENTITY_START = Pattern.compile(
			LEADING_WS + NAME + ASSIGNMENT + GROUP_START);
	private static final Pattern ENTITY_END = Pattern.compile(
			LEADING_WS + GROUP_END + LIST_SEP);
	private static final Pattern LITERAL = Pattern.compile( 
			LEADING_WS + NAME + ASSIGNMENT + NAME + LIST_SEP);
		
	@Override
	public void process(final String str) {
		assert !isClosed();
		final Matcher record = RECORD.matcher(str);
		if (!record.matches()) {
			throw new FormatException("expecting only a single record");
		}
		final String id = unescape(record.group(1));
		final String contents = record.group(2); 
		getReceiver().startRecord(id);
		processList(contents);
		getReceiver().endRecord();
	}

	private void processList(final String str) {
		final Matcher literal = LITERAL.matcher(str);
		final Matcher entityStart = ENTITY_START.matcher(str);
		final Matcher entityEnd = ENTITY_END.matcher(str);
		int pos = 0;
		while (pos < str.length()) {
			if (literal.find(pos)) {
				final String name = unescape(literal.group(1));
				final String value = unescape(literal.group(2));
				getReceiver().literal(name, value);
				pos = literal.end();
			} else if (entityStart.find(pos)) {
				final String name = unescape(entityStart.group(1));
				getReceiver().startEntity(name);
				pos = entityStart.end();
			} else if (entityEnd.find(pos)) {
					getReceiver().endEntity();
					pos = entityEnd.end();
			} else {
				throw new FormatException("unexpected format at position: " + pos);
			}
		}
	}
	
	private String unescape(final String str) {
		return str.replaceAll("(^')|('$)", "").replace("\\'", "'").replace("\\\\", "\\");
	}
	
}
