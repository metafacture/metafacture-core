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


/**
 * Context of the record parser. It manages the text parser and the generation of
 * the stream events emitted by the decoder.
 */
class StructureParserContext {

	private Emitter emitter;

	private final TextParserContext textParserContext = new TextParserContext();
	private TextParserState textParser = TextParserState.LEADING_WHITESPACE;
	private String parsedText = "";

	private String literalName;
	private int nestingLevel;

	public void setEmitter(final Emitter emitter) {
		this.emitter = emitter;
	}

	public Emitter getEmitter() {
		return emitter;
	}

	/**
	 * Passes {@code ch} to the embedded text parser for processing. If
	 * the text parser reaches {@code DELIMITER_REACHED} it is
	 * automatically reset.
	 *
	 * @param ch the character to process
	 * @return true if the text parser reached the
	 *         {@code DELIMITER_REACHED} state.
	 */
	public boolean processCharWithTextParser(final char ch) {
		textParser = textParser.processChar(ch, textParserContext);
		if (textParser == TextParserState.DELIMITER_REACHED) {
			parsedText = textParserContext.getText();
			textParser = TextParserState.LEADING_WHITESPACE;
			textParserContext.reset();
			return true;
		}
		return false;
	}

	public void processEOIWithTextParser() {
		textParser.endOfInput(textParserContext);
		parsedText = textParserContext.getText();
		textParser = TextParserState.LEADING_WHITESPACE;
		textParserContext.reset();
	}

	public void startGroup() {
		emitter.startGroup(parsedText, nestingLevel);
		nestingLevel += 1;
	}

	public void endGroup() {
		nestingLevel -= 1;
		emitter.endGroup(nestingLevel);
	}

	public void startLiteral() {
		literalName = parsedText;
	}

	public void endLiteral() {
		emitter.literal(literalName, parsedText, nestingLevel);
		literalName = null;
	}

	public boolean isTextEmpty() {
		return parsedText.isEmpty();
	}

	public boolean isNested() {
		return nestingLevel > 0;
	}

	public void reset() {
		textParser = TextParserState.LEADING_WHITESPACE;
		textParserContext.reset();
		parsedText = "";
		literalName = null;
		nestingLevel = 0;
	}

}
