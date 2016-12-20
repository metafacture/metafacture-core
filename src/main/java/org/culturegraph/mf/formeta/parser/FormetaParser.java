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
package org.culturegraph.mf.formeta.parser;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.framework.FormatException;

/**
 * A parser for the formeta metadata serialisation format.
 *
 * @author Christoph Böhme
 *
 */
public final class FormetaParser {

	public static final int SNIPPET_SIZE = 20;
	public static final String SNIPPET_ELLIPSIS = "\u2026";
	public static final String POS_MARKER_LEFT = ">";
	public static final String POS_MARKER_RIGHT = "<";

	private static final int BUFFER_SIZE = 1024 * 1024;

	private char[] buffer = new char[BUFFER_SIZE];
	private final StructureParserContext structureParserContext = new StructureParserContext();

	public void setEmitter(final Emitter emitter) {
		structureParserContext.setEmitter(emitter);
	}

	public Emitter getEmitter() {
		return structureParserContext.getEmitter();
	}

	public void parse(final String data) {
		assert structureParserContext.getEmitter() != null: "No emitter set";

		// According to http://stackoverflow.com/a/11876086 it is faster to copy
		// a string into a char array then to use charAt():
		buffer = StringUtil.copyToBuffer(data, buffer);
		final int bufferLen = data.length();

		structureParserContext.reset();
		StructureParserState state = StructureParserState.ITEM_NAME;
		int i = 0;
		try {
			for (; i < bufferLen; ++i) {
				state = state.processChar(buffer[i], structureParserContext);
			}
		} catch (final FormatException e) {
			final String errorMsg = "Parsing error at position "
					+ (i + 1) + ": "
					+ getErrorSnippet(data, i) + ", "
					+ e.getMessage();
			throw new FormatException(errorMsg, e);
		}
		try {
			state.endOfInput(structureParserContext);
		} catch (final FormatException e) {
			throw new FormatException("Parsing error: " + e.getMessage(), e);
		}
	}

	/**
	 * Extracts a text snippet from the record for showing the position at
	 * which an error occurred. The exact position additionally highlighted
	 * with {@link POS_MARKER_LEFT} and {@link POS_MARKER_RIGHT}.
	 *
	 * @param record the record currently being parsed
	 * @param pos the position at which the error occurred
	 * @return a text snippet.
	 */
	private static String getErrorSnippet(final String record, final int pos) {
		final StringBuilder snippet = new StringBuilder();

		final int start = pos - SNIPPET_SIZE / 2;
		if (start < 0) {
			snippet.append(record.substring(0, pos));
		} else {
			snippet.append(SNIPPET_ELLIPSIS);
			snippet.append(record.substring(start, pos));
		}

		snippet.append(POS_MARKER_LEFT);
		snippet.append(record.charAt(pos));
		snippet.append(POS_MARKER_RIGHT);

		if (pos + 1 < record.length()) {
			final int end = pos + SNIPPET_SIZE / 2;
			if (end > record.length()) {
				snippet.append(record.substring(pos + 1));
			} else {
				snippet.append(record.substring(pos + 1, end));
				snippet.append(SNIPPET_ELLIPSIS);
			}
		}

		return snippet.toString();
	}

}
