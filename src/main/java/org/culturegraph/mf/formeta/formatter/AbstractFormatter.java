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
package org.culturegraph.mf.formeta.formatter;

import org.culturegraph.mf.formeta.Formeta;
import org.culturegraph.mf.util.StringUtil;

/**
 * Base class for formatters.
 *
 * @author Christoph Böhme.
 *
 */
public abstract class AbstractFormatter implements Formatter {

	public static final String ESCAPED_CHARS_QUOTED = "\n\r"
			+ Formeta.QUOT_CHAR
			+ Formeta.ESCAPE_CHAR;

	public static final String ESCAPED_CHARS = ESCAPED_CHARS_QUOTED
			+ Formeta.GROUP_START
			+ Formeta.GROUP_END
			+ Formeta.ITEM_SEPARATOR
			+ Formeta.NAME_VALUE_SEPARATOR;

	protected static final int BUFFER_SIZE = 1024;

	private final StringBuilder builder = new StringBuilder();

	private char[] buffer = new char[BUFFER_SIZE];

	@Override
	public final void reset() {
		builder.delete(0, builder.length());
		onReset();
	}

	@Override
	public final String toString() {
		return builder.toString();
	}

	protected final void append(final char ch) {
		builder.append(ch);
	}

	protected final void append(final CharSequence charSeq) {
		builder.append(charSeq);
	}

	protected final void escapeAndAppend(final String str) {
		// According to http://stackoverflow.com/a/11876086 it is faster to copy
		// a string into a char array then to use charAt():
		buffer = StringUtil.copyToBuffer(str, buffer);
		final int bufferLen = str.length();

		final boolean addQuotes = shouldQuoteText(buffer, bufferLen);
		final String charsToEscape;
		if (addQuotes) {
			builder.append(Formeta.QUOT_CHAR);
			charsToEscape = ESCAPED_CHARS_QUOTED;
		} else {
			charsToEscape = ESCAPED_CHARS;
		}
		for (int i = 0; i < bufferLen; ++i) {
			final char ch = buffer[i];
			if (charsToEscape.indexOf(ch) > -1) {
				appendEscapedChar(ch);
			} else {
				builder.append(ch);
			}
		}
		if (addQuotes) {
			builder.append(Formeta.QUOT_CHAR);
		}
	}

	protected void onReset() {
		// Default implementation does nothing
	}

	protected abstract boolean shouldQuoteText(final char[] buffer, final int len);

	private void appendEscapedChar(final char ch) {
		builder.append(Formeta.ESCAPE_CHAR);
		switch (ch) {
		case '\n':
			builder.append(Formeta.NEWLINE_ESC_SEQ);
			break;
		case '\r':
			builder.append(Formeta.CARRIAGE_RETURN_ESC_SEQ);
			break;
		default:
			builder.append(ch);
		}
	}

}