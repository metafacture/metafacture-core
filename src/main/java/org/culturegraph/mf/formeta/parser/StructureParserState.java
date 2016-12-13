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
 * FSA (which additionally keeps track of its nesting level) for parsing records
 * and literals/entities on a structural level. The initial parser state is
 * {@code ITEM_NAME}. The parser has no final state
 * as the sequence of records or literals/entities is theoretically unlimited.
 */
enum StructureParserState {

	ITEM_NAME {
		protected StructureParserState delimiterReached(final char ch, final StructureParserContext ctx) {
			final StructureParserState newState;
			switch (ch) {
			case Formeta.GROUP_START:
				ctx.startGroup();
				newState = ITEM_NAME;
				break;
			case Formeta.NAME_VALUE_SEPARATOR:
				ctx.startLiteral();
				newState =  LITERAL_VALUE;
				break;
			case Formeta.ITEM_SEPARATOR:
				if (!ctx.isTextEmpty()) {
					throw new FormatException(getUnexpectedCharMsg(NAME_DELIMITER_EXPECTED, ch));
				}
				newState = ITEM_NAME;
				break;
			case Formeta.GROUP_END:
				if (!ctx.isTextEmpty() || !ctx.isNested()) {
					throw new FormatException(getUnexpectedCharMsg(NAME_DELIMITER_EXPECTED, ch));
				}
				ctx.endGroup();
				newState = ITEM_NAME;
				break;
			default:
				throw new FormatException(getUnexpectedCharMsg(NAME_DELIMITER_EXPECTED, ch));
			}
			return newState;
		}

		public void endOfInput(final StructureParserContext ctx) {
			ctx.processEOIWithTextParser();
			if (!ctx.isTextEmpty() || ctx.isNested()) {
				throw new FormatException(UNEXPECTED_EOI);
			}
		}
	},
	LITERAL_VALUE {
		protected StructureParserState delimiterReached(final char ch, final StructureParserContext ctx) {
			final StructureParserState newState;
			switch (ch) {
			case Formeta.ITEM_SEPARATOR:
				ctx.endLiteral();
				newState = ITEM_NAME;
				break;
			case Formeta.GROUP_END:
				if (!ctx.isNested()) {
					throw new FormatException(getUnexpectedCharMsg(ITEM_SEPARATOR_EXPECTED, ch));
				}
				ctx.endLiteral();
				ctx.endGroup();
				newState = ITEM_NAME;
				break;
			default:
				throw new FormatException(getUnexpectedCharMsg(VALUE_DELIMITER_EXPECTED, ch));
			}
			return newState;
		}

		public void endOfInput(final StructureParserContext ctx) {
			ctx.processEOIWithTextParser();
			if (ctx.isNested()) {
				throw new FormatException(UNEXPECTED_EOI);
			}
			ctx.endLiteral();
		}
	};

	private static final String NAME_DELIMITER_EXPECTED = "'{' or ':'";
	private static final String ITEM_SEPARATOR_EXPECTED = "','";
	private static final String VALUE_DELIMITER_EXPECTED = "'}' or ','";
	private static final String UNEXPECTED_EOI = "Unexpected end of input";

	public StructureParserState processChar(final char ch, final StructureParserContext ctx) {
		if (ctx.processCharWithTextParser(ch)) {
			return delimiterReached(ch, ctx);
		}
		return this;
	}

	public abstract void endOfInput(final StructureParserContext ctx);

	protected abstract StructureParserState delimiterReached(final char ch, final StructureParserContext ctx);

	private static String getUnexpectedCharMsg(final String expected, final char actual) {
		return expected + " expected but got '" + actual + "'";
	}

}
