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

import org.culturegraph.mf.formeta.Formeta;
import org.culturegraph.mf.framework.FormatException;

/**
 * Context of the text parser. It stores the parsed text, maps escape sequences
 * to characters and handles the removal of trailing whitespace in unquoted
 * text.
 */
class TextParserContext {

	private static final String ESCAPABLE_CHARS = Formeta.WHITESPACE
			+ Formeta.QUOT_CHAR
			+ Formeta.ESCAPE_CHAR
			+ Formeta.GROUP_START
			+ Formeta.GROUP_END
			+ Formeta.ITEM_SEPARATOR
			+ Formeta.NAME_VALUE_SEPARATOR;

	private final StringBuilder text = new StringBuilder();

	private int lengthWithoutTrailingWs;
	private boolean quoted;

	public String getText() {
		return text.substring(0, lengthWithoutTrailingWs);
	}

	public void setQuoted(final boolean quoted) {
		this.quoted = quoted;
	}

	public void appendChar(final char ch) {
		text.append(ch);
		if (quoted || !Formeta.isWhitespace(ch)) {
			lengthWithoutTrailingWs = text.length();
		}
	}

	public void appendEscapedChar(final char ch) {
		if (Formeta.NEWLINE_ESC_SEQ == ch) {
			text.append('\n');
		} else if (Formeta.CARRIAGE_RETURN_ESC_SEQ == ch) {
			text.append('\r');
		} else if (ESCAPABLE_CHARS.indexOf(ch) > -1) {
			text.append(ch);
		} else {
			throw new FormatException("invalid escape sequence: " + ch);
		}
		lengthWithoutTrailingWs = text.length();
	}

	public void reset() {
		text.delete(0, text.length());
		lengthWithoutTrailingWs = 0;
		quoted = false;
	}

}
