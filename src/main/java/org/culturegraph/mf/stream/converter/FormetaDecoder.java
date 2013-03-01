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

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.Formeta;

/**
 * Decodes a record in formeta format.
 * 
 * @author Christoph Böhme
 *
 */
@Description("Decodes a record in formeta format.")
@In(String.class)
@Out(StreamReceiver.class)
public final class FormetaDecoder extends
		DefaultObjectPipe<String, StreamReceiver> {

	private static final int SNIPPET_SIZE = 20;
	private static final String SNIPPET_ELLIPSIS = "\u2026";
	private static final String POS_MARKER_LEFT = ">";
	private static final String POS_MARKER_RIGHT = "<";
	
	private static final int BUFFER_SIZE = 1024 * 1024;
	
	/**
	 * The parser can either parse full records or
	 * partial records.
	 */
	public enum Mode {
		
		FULL_RECORDS{
			public Emitter createEmitter() {
				return new FullRecordEmitter();
			}
		},
		PARTIAL_RECORDS{
			public Emitter createEmitter() {
				return new PartialRecordEmitter();
			}			
		};

		public abstract Emitter createEmitter();
		
	}
	
	/**
	 * Interface for event emitters
	 */
	private interface Emitter {
		
		void setReceiver(final StreamReceiver receiver);
		
		void startGroup(final String name, final int nestingLevel);
		
		void endGroup(final int nestingLevel);
		
		void literal(final String name, final String value, final int nestingLevel);
		
	}
	
	/**
	 * Emits full records
	 */
	private static final class FullRecordEmitter implements Emitter {

		private StreamReceiver receiver;
		
		@Override
		public void setReceiver(final StreamReceiver receiver) {
			this.receiver = receiver;
		}
		
		@Override
		public void startGroup(final String name, final int nestingLevel) {
			if (nestingLevel == 0) {
				receiver.startRecord(name);
			} else {
				receiver.startEntity(name);
			}
		}

		@Override
		public void endGroup(final int nestingLevel) {
			if (nestingLevel == 0) {
				receiver.endRecord();
			} else {
				receiver.endEntity();
			}
		}

		@Override
		public void literal(final String name, final String value, final int nestingLevel) {
			if (nestingLevel == 0) {
				throw new FormatException("literals may only appear in records");
			}
			receiver.literal(name, value);
		}
		
	}
	
	/**
	 * Emits partial records
	 */
	private static final class PartialRecordEmitter implements Emitter {

		private StreamReceiver receiver;
		private String defaultName;
		
		public PartialRecordEmitter() {
			this(null);
		}
		
		public PartialRecordEmitter(final String defaultName) {
			this.defaultName = defaultName;
		}
		
		@Override
		public void setReceiver(final StreamReceiver receiver) {
			this.receiver = receiver;
		}

		@Override
		public void startGroup(final String name, final int nestingLevel) {
			if (defaultName != null && name.isEmpty()) {
				receiver.startEntity(defaultName);
			} else {
				receiver.startEntity(name);
			}
		}

		@Override
		public void endGroup(final int nestingLevel) {
			receiver.endEntity();
		}

		@Override
		public void literal(final String name, final String value, final int nestingLevel) {
			if (defaultName != null && name.isEmpty()) {
				receiver.literal(defaultName, value);
			} else {
				receiver.literal(name, value);
			}
		}

	}
	
	/**
	 * Context of the text parser. It stores the parsed text, maps escape 
	 * sequences to characters and handles the removal of trailing 
	 * whitespace in unquoted text.
	 */
	private static class TextParserContext {
		
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
			if (quoted || !isWhitespace(ch)) {
				lengthWithoutTrailingWs = text.length();
			}
		}
		
		public void appendEscapedChar(final char ch) {
			if (Formeta.NEWLINE_ESC_SEQ == ch) {
				text.append('\n');
			} else if (Formeta.CARRIAGE_RETURN_ESC_SEQ == ch) {
				text.append('\r');
			} else {
				text.append(ch);
			}
			lengthWithoutTrailingWs = text.length();
		}
		
		public void reset() {
			text.delete(0, text.length());
			lengthWithoutTrailingWs = 0;
			quoted = false;
		}
		
	}
	
	/**
	 * FSA for parsing identifiers, names and values. The initial parser 
	 * state is {@code LEADING_WHITESPACE}, the final (accepting) state
	 * is {@code DELIMITER_REACHED}.
	 */
	private enum TextParserState {
		
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
					if (isWhitespace(ch)) {
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
					if (isWhitespace(ch)) {
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
		
	}
	
	/**
	 * Context of the record parser. It manages the text parser and the generation of
	 * the stream events emitted by the decoder.
	 */
	private static class RecordParserContext {
		
		private final Emitter emitter;
		
		private final TextParserContext textParserContext = new TextParserContext();
		private TextParserState textParser = TextParserState.LEADING_WHITESPACE;
		private String parsedText = "";
			
		private String literalName;		
		private int nestingLevel;
		
		public RecordParserContext(final Emitter emitter) {
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
		public boolean processWithTextParser(final char ch) {
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
	
	/**
	 * FSA (which additionally keeps tracks) for parsing records. The initial parser
	 * state is {@code ITEM_NAME}. The parser has no final state as the sequence of 
	 * records or literals/entities is theoretically unlimited.
	 */
	private enum RecordParserState {
		
		ITEM_NAME {
			protected RecordParserState delimiterReached(final char ch, final RecordParserContext ctx) {
				final RecordParserState newState;
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
			
			public void endOfInput(final RecordParserContext ctx) {
				ctx.processEOIWithTextParser();
				if (!ctx.isTextEmpty() || ctx.isNested()) {
					throw new FormatException(UNEXPECTED_EOI);
				}
			}
		},
		LITERAL_VALUE {
			protected RecordParserState delimiterReached(final char ch, final RecordParserContext ctx) {
				final RecordParserState newState;
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
			
			public void endOfInput(final RecordParserContext ctx) {
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

		protected abstract RecordParserState delimiterReached(final char ch, final RecordParserContext ctx);
		
		public RecordParserState processChar(final char ch, final RecordParserContext ctx) {
			if (ctx.processWithTextParser(ch)) {
				return delimiterReached(ch, ctx);
			}
			return this;
		}
		
		public abstract void endOfInput(final RecordParserContext ctx);
		
	}
	
	
	private char[] buffer = new char[BUFFER_SIZE];
	
	private final RecordParserContext recordParserContext;
	
	public FormetaDecoder() {
		this(Mode.FULL_RECORDS);
	}
	
	public FormetaDecoder(final Mode mode) {
		super();
		recordParserContext = new RecordParserContext(mode.createEmitter());
	}
	
	@Override
	public void process(final String record) {
		// According to http://stackoverflow.com/a/11876086 it is faster to copy 
		// the string into a char array than to use charAt():
		final int recordLen = record.length();
		if(recordLen > buffer.length) { 
			buffer = new char[buffer.length * 2];
		}
		record.getChars(0, recordLen, buffer, 0);
		
		recordParserContext.reset();
		RecordParserState state = RecordParserState.ITEM_NAME;
		int i = 0;
		try {
			for (; i < recordLen; ++i) {
				state = state.processChar(buffer[i], recordParserContext);
			}
		} catch (FormatException e) {
			final String errorMsg = "Parsing error at position " 
					+ (i + 1) + ": "
					+ getErrorSnippet(record, i) + ", " 
					+ e.getMessage();
			throw new FormatException(errorMsg, e);
		}
		try {
			state.endOfInput(recordParserContext);
		} catch (FormatException e) {
			throw new FormatException("Parsing error: " + e.getMessage(), e);
		}
	}
	
	@Override
	protected void onSetReceiver() {
		recordParserContext.getEmitter().setReceiver(getReceiver());
	}
	
	private static boolean isWhitespace(final char ch) {
		return Formeta.WHITESPACE.indexOf(ch) > -1;
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
	
	private static String getUnexpectedCharMsg(final String expected, final char actual) {
		return expected + " expected but got '" + actual + "'";
	}
	
}
