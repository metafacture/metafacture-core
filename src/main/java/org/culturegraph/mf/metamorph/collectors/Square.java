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
package org.culturegraph.mf.metamorph.collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.helpers.AbstractFlushingCollect;

/**
 * Corresponds to the <code>&lt;collect-literal&gt;</code> tag.
 *
 * @author Markus Michael Geipel
 */
public final class Square extends AbstractFlushingCollect {

	private final List<String> values = new ArrayList<String>();
	private String prefix = "";
	private String postfix = "";
	private String delimiter = "";

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public void setPostfix(final String postfix) {
		this.postfix = postfix;
	}

	public void setDelimiter(final String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	protected void emit() {
		Collections.sort(values);
		final int size = values.size();
		for (int i = 0; i < size; i++) {
			final String last = values.remove(values.size() - 1);
			for (final String value : values) {
				getNamedValueReceiver().receive(getName(),
						prefix + value + delimiter + last + postfix, this,
						getRecordCount(), getEntityCount());
			}
		}
	}

	@Override
	protected boolean isComplete() {
		return false;
	}

	@Override
	protected void receive(final String name, final String value,
			final NamedValueSource source) {
		values.add(value);
	}

	@Override
	protected void clear() {
		values.clear();
	}

}
