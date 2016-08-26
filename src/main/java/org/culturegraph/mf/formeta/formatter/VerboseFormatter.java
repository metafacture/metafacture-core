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

import org.culturegraph.mf.formeta.Formeta;

/**
 * A formatter for verbose output.
 *
 * @author Christoph Böhme
 *
 */
public final class VerboseFormatter extends AbstractFormatter {

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
		final String triggerChars = Formeta.WHITESPACE + CHARS_TO_ESCAPE;
		for (int i = 0; i < len; ++i) {
			if (triggerChars.indexOf(buffer[i]) > -1) {
				return true;
			}
		}
		return len == 0;
	}

}
