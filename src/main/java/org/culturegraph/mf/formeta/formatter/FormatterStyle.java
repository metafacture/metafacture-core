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
package org.culturegraph.mf.formeta.formatter;

/**
 * Output styles for formeta.
 *
 * @author Christoph Böhme
 */
public enum FormatterStyle {
	/**
	 * Dense and concise output style with minimal quotation and
	 * item separation and without any unnecessary whitespace.
	 * Good for automatic processing.
	 */
	CONCISE {
		@Override
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
		@Override
		public Formatter createFormatter() {
			return new VerboseFormatter();
		}
	},

	/**
	 * Similar to the {@code VERBOSE} style but additionally
	 * adds line breaks and indents to support readability.
	 */
	MULTILINE {
		@Override
		public Formatter createFormatter() {
			return new MultilineFormatter();
		}
	};

	public abstract Formatter createFormatter();
}
