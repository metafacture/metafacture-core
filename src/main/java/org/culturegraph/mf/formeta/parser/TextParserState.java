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
 * FSA for parsing identifiers, names and values. The initial parser
 * state is {@code LEADING_WHITESPACE}, the final (accepting) state
 * is {@code DELIMITER_REACHED}.
 */
enum TextParserState {

	LEADING_WHITESPACE {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			final TextParserState newState;
			switch(ch) {
			case Formeta.ESCAPE_CHAR:
				ctx.setQuoted(false);
				newState = ESCAPE_SEQ;
				break;
			case Formeta.QUOT_CHAR:
				ctx.setQuoted(true);
				newState = QUOTED_TEXT;
				break;
			case Formeta.GROUP_START:
			case Formeta.GROUP_END:
			case Formeta.ITEM_SEPARATOR:
			case Formeta.NAME_VALUE_SEPARATOR:
				ctx.setQuoted(false);
				newState = DELIMITER_REACHED;
				break;
			default:
				if (Formeta.isWhitespace(ch)) {
					newState = LEADING_WHITESPACE;
				} else {
					ctx.setQuoted(false);
					ctx.appendChar(ch);
					newState = TEXT;
				}
			}
			return newState;
		}
	},
	TEXT {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			final TextParserState newState;
			switch(ch) {
			case Formeta.ESCAPE_CHAR:
				newState = ESCAPE_SEQ;
				break;
			case Formeta.GROUP_START:
			case Formeta.GROUP_END:
			case Formeta.ITEM_SEPARATOR:
			case Formeta.NAME_VALUE_SEPARATOR:
				newState = DELIMITER_REACHED;
				break;
			default:
				ctx.appendChar(ch);
				newState = TEXT;
			}
			return newState;
		}
	},
	ESCAPE_SEQ {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			ctx.appendEscapedChar(ch);
			return TEXT;
		}

		public void endOfInput(final TextParserContext ctx) {
			throw new FormatException("incomplete escape sequence");
		}
	},
	QUOTED_TEXT {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			final TextParserState newState;
			switch(ch) {
			case Formeta.ESCAPE_CHAR:
				newState = QUOTED_ESCAPE_SEQ;
				break;
			case Formeta.QUOT_CHAR:
				newState = TRAILING_WHITESPACE;
				break;
			default:
				ctx.appendChar(ch);
				newState = QUOTED_TEXT;
			}
			return newState;
		}

		public void endOfInput(final TextParserContext ctx) {
			throw new FormatException("quoted string is not terminated");
		}
	},
	QUOTED_ESCAPE_SEQ {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			ctx.appendEscapedChar(ch);
			return QUOTED_TEXT;
		}

		public void endOfInput(final TextParserContext ctx) {
			throw new FormatException("incomplete escape sequence and quoted string is not terminated");
		}
	},
	TRAILING_WHITESPACE {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			final TextParserState newState;
			switch(ch) {
			case Formeta.GROUP_START:
			case Formeta.GROUP_END:
			case Formeta.ITEM_SEPARATOR:
			case Formeta.NAME_VALUE_SEPARATOR:
				newState = DELIMITER_REACHED;
				break;
			default:
				if (Formeta.isWhitespace(ch)) {
					newState = TRAILING_WHITESPACE;
				} else {
					final String sep = "', '";
					final String expected = "whitespace or one of '"
							+ Formeta.GROUP_START + sep
							+ Formeta.GROUP_END + sep
							+ Formeta.ITEM_SEPARATOR + sep
							+ Formeta.NAME_VALUE_SEPARATOR + "'";
					throw new FormatException(getUnexpectedCharMsg(expected, ch));
				}
			}
			return newState;
		}
	},
	DELIMITER_REACHED {
		public TextParserState processChar(final char ch, final TextParserContext ctx) {
			throw new UnsupportedOperationException("Cannot process characters in state DELIMITER_REACHED");
		}
	};

	public abstract TextParserState processChar(final char ch, final TextParserContext ctx);

	public void endOfInput(final TextParserContext ctx) {
		// Default implementation does nothing
	}

	private static String getUnexpectedCharMsg(final String expected, final char actual) {
		return expected + " expected but got '" + actual + "'";
	}

}
