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
 * A formatter for concise output.
 *
 * @author Christoph Böhme
 *
 */
public final class ConciseFormatter extends AbstractFormatter {

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
