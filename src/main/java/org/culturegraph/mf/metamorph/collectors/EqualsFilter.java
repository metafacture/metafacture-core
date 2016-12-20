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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.helpers.AbstractFlushingCollect;

/**
 * Corresponds to the <code>&lt;equalsFilter-literal&gt;</code> tag. Emits data
 * if values in variables are all equal.
 *
 * @author Thomas Haidlas
 */
public final class EqualsFilter extends AbstractFlushingCollect {

	private final Map<String, String> variables = new HashMap<String, String>();
	private final Set<NamedValueSource> sources = new HashSet<NamedValueSource>();
	private final Set<NamedValueSource> sourcesLeft = new HashSet<NamedValueSource>();
	private boolean isEqual = true;

	@Override
	protected void emit() {
		final String name = StringUtil.format(getName(), this.variables);
		final String value = StringUtil.format(getValue(), this.variables);
		if (this.isEqual) {
			getNamedValueReceiver().receive(name, value, this,
					getRecordCount(), getEntityCount());
		}
	}

	@Override
	protected boolean isComplete() {
		return this.sourcesLeft.isEmpty();
	}

	@Override
	protected void receive(final String name, final String value,
			final NamedValueSource source) {
		if (this.variables.size() > 0 && !this.variables.containsValue(value)) {
			this.isEqual = false;
		}
		this.variables.put(name, value);
		this.sourcesLeft.remove(source);
	}

	@Override
	public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
		this.sources.add(namedValueSource);
		this.sourcesLeft.add(namedValueSource);
	}

	@Override
	protected void clear() {
		this.sourcesLeft.addAll(this.sources);
		this.variables.clear();
		this.isEqual = true;
	}

}
