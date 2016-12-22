/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.biblio.pica;


/**
 * A parser for PICA+ records. Only single records can be parsed as the parser
 * ignores end of record markers. The initial parser state is FIELD_NAME. All
 * states are valid end states. The parser processes any input, there is no
 * error state.
 *
 * The parser ignores spaces in field names. They are not included in the
 * field name.
 *
 * Empty subfields are skipped. For instance, parsing the following input
 * would NOT produce an empty literal: 003@ \u001f\u001e. The parser also
 * skips unnamed fields without any subfields.
 *
 * @author Christoph Böhme
 *
 */
enum PicaParserState {

	FIELD_NAME {
		@Override
		protected PicaParserState parseChar(final char ch, final PicaParserContext ctx) {
			final PicaParserState next;
			switch (ch) {
			case PicaConstants.RECORD_MARKER:
			case PicaConstants.FIELD_MARKER:
			case PicaConstants.FIELD_END_MARKER:
				ctx.emitStartEntity();
				ctx.emitEndEntity();
				next = FIELD_NAME;
				break;
			case PicaConstants.SUBFIELD_MARKER:
				ctx.emitStartEntity();
				next = SUBFIELD_NAME;
				break;
			default:
				ctx.appendText(ch);
				next = this;
			}
			return next;
		}

		@Override
		protected void endOfInput(final PicaParserContext ctx) {
			ctx.emitStartEntity();
			ctx.emitEndEntity();
		}
	},
	SUBFIELD_NAME {
		@Override
		protected PicaParserState parseChar(final char ch, final PicaParserContext ctx) {
			final PicaParserState next;
			switch (ch) {
			case PicaConstants.RECORD_MARKER:
			case PicaConstants.FIELD_MARKER:
			case PicaConstants.FIELD_END_MARKER:
				ctx.emitEndEntity();
				next = FIELD_NAME;
				break;
			case PicaConstants.SUBFIELD_MARKER:
				next = this;
				break;
			default:
				ctx.setSubfieldName(ch);
				next = SUBFIELD_VALUE;
			}
			return next;
		}

		@Override
		protected void endOfInput(final PicaParserContext ctx) {
			ctx.emitEndEntity();
		}
	},
	SUBFIELD_VALUE {
		@Override
		protected PicaParserState parseChar(final char ch, final PicaParserContext ctx) {
			final PicaParserState next;
			switch (ch) {
			case PicaConstants.RECORD_MARKER:
			case PicaConstants.FIELD_MARKER:
			case PicaConstants.FIELD_END_MARKER:
				ctx.emitLiteral();
				ctx.emitEndEntity();
				next = FIELD_NAME;
				break;
			case PicaConstants.SUBFIELD_MARKER:
				ctx.emitLiteral();
				next = SUBFIELD_NAME;
				break;
			default:
				ctx.appendText(ch);
				next = this;
			}
			return next;
		}

		@Override
		protected void endOfInput(final PicaParserContext ctx) {
			ctx.emitLiteral();
			ctx.emitEndEntity();
		}
	};

	protected abstract PicaParserState parseChar(final char ch, final PicaParserContext ctx);

	protected abstract void endOfInput(final PicaParserContext ctx);

}
