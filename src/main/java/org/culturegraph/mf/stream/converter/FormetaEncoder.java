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

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.Formeta;

/**
 * Encodes streams in formeta format.
 *
 * @author Christoph Böhme
 *
 */
@Description("Encodes streams in formeta format.")
@In(StreamReceiver.class)
@Out(String.class)
public final class FormetaEncoder extends
		DefaultStreamPipe<ObjectReceiver<String>> {
	
	/**
	 * Output styles.
	 */
	public enum Style { 	
		/**
		 * Dense and concise output style with minimal quotation and 
		 * item separation and without any unnecessary whitespace. 
		 * Good for automatic processing. 
		 */
		CONCISE {
			public Formatter createFormatter() {
				return new ConciseFormatter();
			}
		},
		
		/**
		 * Output style which aims to be easy to read. Inserts
		 * additional whitespace and item separators. Uses 
		 * quotation marks extensively. 
		 */
		VERBOSE {
			public Formatter createFormatter() {
				return new VerboseFormatter();
			}			 
		},
		
		/**
		 * Similar to the {@code VERBOSE} style but additionally
		 * adds line breaks and indents to support readability.
		 */
		MULTILINE {
			public Formatter createFormatter() {
				return new MultilineFormatter();
			}			
		};
		
		public abstract Formatter createFormatter();
	}

	/**
	 * Interface for formatters
	 */
	private interface Formatter {
		
		void reset();
		
		void startGroup(final String name);
		
		void endGroup();
		
		void literal(final String name, final String value);
	}
	
	/**
	 * Base class for formatters.
	 */
	private abstract static class AbstractFormatter implements Formatter {
		
		public static final String ESCAPED_CHARS_QUOTED = "\n\r" 
				+ Formeta.QUOT_CHAR 
				+ Formeta.ESCAPE_CHAR;
		
		public static final String ESCAPED_CHARS = ESCAPED_CHARS_QUOTED 
				+ Formeta.GROUP_START 
				+ Formeta.GROUP_END 
				+ Formeta.ITEM_SEPARATOR 
				+ Formeta.NAME_VALUE_SEPARATOR;

		private static final int BUFFER_SIZE = 1024;

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
			// the string into a char array than to use charAt():		
			final int strLen = str.length();			
			if(strLen > buffer.length) {
				buffer = new char[buffer.length * 2];
			}
			str.getChars(0, strLen, buffer, 0);
			
			final boolean addQuotes = shouldQuoteText(buffer, strLen);
			final String charsToEscape;
			if (addQuotes) {
				builder.append(Formeta.QUOT_CHAR);
				charsToEscape = ESCAPED_CHARS_QUOTED;
			} else {
				charsToEscape = ESCAPED_CHARS;
			}
			for (int i = 0; i < strLen; ++i) {
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
	
	/**
	 * A formatter for concise output.
	 */
	private static final class ConciseFormatter extends AbstractFormatter {

		private boolean appendItemSeparator;

		@Override
		public void startGroup(final String name) {
			if (appendItemSeparator) {
				append(Formeta.ITEM_SEPARATOR);
			}
			escapeAndAppend(name);	
			append(Formeta.GROUP_START);
			appendItemSeparator = false;
		}

		@Override
		public void endGroup() {
			append(Formeta.GROUP_END);
			appendItemSeparator = false;
		}

		@Override
		public void literal(final String name, final String value) {
			if (appendItemSeparator) {
				append(Formeta.ITEM_SEPARATOR);
			}
			escapeAndAppend(name);
			append(Formeta.NAME_VALUE_SEPARATOR);
			escapeAndAppend(value);
			appendItemSeparator = true;
		}
		
		@Override
		protected void onReset() {
			appendItemSeparator = false;
		}
	
		@Override
		protected boolean shouldQuoteText(final char[] buffer, final int len) {
			return len != 0 && (Formeta.WHITESPACE.indexOf(buffer[0]) > -1 || 
					Formeta.WHITESPACE.indexOf(buffer[len - 1]) > -1);
		}
		
	}
	
	/**
	 * A formatter for verbose output.
	 */
	private static final class VerboseFormatter extends AbstractFormatter {

		private static final String GROUP_START = Formeta.GROUP_START + " ";
		private static final String GROUP_END = " " + Formeta.GROUP_END;
		private static final String ITEM_SEPARATOR = Formeta.ITEM_SEPARATOR + " ";
		private static final String NAME_VALUE_SEPARATOR = Formeta.NAME_VALUE_SEPARATOR + " ";
		
		private boolean appendItemSeparator;

		@Override
		public void startGroup(final String name) {
			if (appendItemSeparator) {
				append(ITEM_SEPARATOR);
			}
			escapeAndAppend(name);
			append(GROUP_START);
			appendItemSeparator = false;
		}

		@Override
		public void endGroup() {
			append(GROUP_END);
			appendItemSeparator = true;
		}

		@Override
		public void literal(final String name, final String value) {
			if (appendItemSeparator) {
				append(ITEM_SEPARATOR);
			}
			escapeAndAppend(name);
			append(NAME_VALUE_SEPARATOR);
			escapeAndAppend(value);
			appendItemSeparator = true;
		}
		
		@Override
		protected void onReset() {
			appendItemSeparator = false;
		}
		
		@Override
		protected boolean shouldQuoteText(final char[] buffer, final int len) {
			final String triggerChars = Formeta.WHITESPACE + ESCAPED_CHARS;
			for (int i = 0; i < len; ++i) {
				if (triggerChars.indexOf(buffer[i]) > -1) {
					return true;
				}
			}
			return len == 0;
		}

	}

	/**
	 * A formatter for multiline output.
	 */
	private static final class MultilineFormatter extends AbstractFormatter {

		public static final String INDENT = "\t";
		
		private static final String GROUP_START = " " + Formeta.GROUP_START;
		private static final String NAME_VALUE_SEPARATOR = Formeta.NAME_VALUE_SEPARATOR + " ";

		private final StringBuilder indent = new StringBuilder();
		
		private boolean appendItemSeparator;
		private boolean firstItem;

		@Override
		public void startGroup(final String name) {
			if (appendItemSeparator) {
				append(Formeta.ITEM_SEPARATOR);
			}
			if (!firstItem) {
				append(indent);
			}
			escapeAndAppend(name);
			append(GROUP_START);
			
			indent.append(INDENT);
			appendItemSeparator = false;
			firstItem = false;
		}

		@Override
		public void endGroup() {
			indent.delete(indent.length() - INDENT.length(), indent.length());
			
			append(indent);
			append(Formeta.GROUP_END);
			appendItemSeparator = true;
		}

		@Override
		public void literal(final String name, final String value) {
			if (appendItemSeparator) {
				append(Formeta.ITEM_SEPARATOR);
			}
			if (!firstItem) {
				append(indent);
			}
			escapeAndAppend(name);
			append(NAME_VALUE_SEPARATOR);
			escapeAndAppend(value);
			appendItemSeparator = true;
			firstItem = false;
		}
		
		@Override
		protected void onReset() {
			indent.delete(0, indent.length());
			indent.append('\n');
			appendItemSeparator = false;
			firstItem = true;
		}

		@Override
		protected boolean shouldQuoteText(final char[] buffer, final int len) {
			return true;
		}

	}

	private Style style = Style.CONCISE;
	private Formatter formatter = style.createFormatter();
	
	public Style getStyle() {
		return style;
	}

	public void setStyle(final Style style) {
		this.style = style;
		formatter = style.createFormatter();
	}
	
	public void setStyle(final String style) {
		setStyle(Style.valueOf(style.toUpperCase()));
	}
	
	@Override
	public void startRecord(final String identifier) {
		formatter.reset();
		formatter.startGroup(identifier);
	}

	@Override
	public void endRecord() {
		formatter.endGroup();
		getReceiver().process(formatter.toString());
	}

	@Override
	public void startEntity(final String name) {
		formatter.startGroup(name);
	}

	@Override
	public void endEntity() {
		formatter.endGroup();
	}

	@Override
	public void literal(final String name, final String value) {
		formatter.literal(name, value);
	}

}
