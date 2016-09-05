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
package org.culturegraph.mf.formeta;

/**
 * Constant definitions for the Formeta format.
 *
 * @author Christoph Böhme
 *
 */
public final class Formeta {

	public static final char QUOT_CHAR = '\'';
	public static final char ESCAPE_CHAR = '\\';
	public static final char NEWLINE_ESC_SEQ = 'n';
	public static final char CARRIAGE_RETURN_ESC_SEQ = 'r';

	public static final String WHITESPACE = "\t\n\r ";

	public static final char GROUP_START = '{';
	public static final char GROUP_END = '}';
	public static final char ITEM_SEPARATOR = ',';
	public static final char NAME_VALUE_SEPARATOR = ':';

	private Formeta() {
		// No instances allowed
	}

	public static boolean isWhitespace(final char ch) {
		return WHITESPACE.indexOf(ch) > -1;
	}

}
