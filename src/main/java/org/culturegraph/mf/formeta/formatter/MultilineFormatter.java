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
 * A formatter for multiline output.
 *
 * @author Christoph Böhme
 *
 */
public final class MultilineFormatter extends AbstractFormatter {

	public static final String INDENT = "\t";

	private static final String GROUP_START = " " + Formeta.GROUP_START;
	private static final String NAME_VALUE_SEPARATOR = Formeta.NAME_VALUE_SEPARATOR + " ";

	private final StringBuilder indent = new StringBuilder();

	private boolean appendItemSeparator;
	private boolean firstItem;

	public MultilineFormatter() {
		super();
		onReset();
	}

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
