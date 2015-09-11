/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.morph.collectors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.NamedValueSource;
import org.culturegraph.mf.types.HierarchicalMultiMap;
import org.culturegraph.mf.util.StringUtil;

/**
 * Corresponds to the <code>&lt;collect&gt;</code> tag.
 *
 * @author Markus Michael Geipel
 */
public final class Combine extends AbstractFlushingCollect {

	private final Map<String, String>   variables   = new HashMap<>();
	private final Set<NamedValueSource> sources     = new HashSet<>();
	private final Set<NamedValueSource> sourcesLeft = new HashSet<>();

	public Combine(final Metamorph metamorph) {
		super(metamorph);
	}

	@Override
	protected void emit() {
		final String name = StringUtil.format(getName(), variables);
		final String value = StringUtil.format(getValue(), variables);

		if (getIncludeSubEntities()) {

			HierarchicalMultiMap<Integer, String, String> entityBuffer = getHierarchicalEntityBuffer();
			entityBuffer.addToEmit(getEntityCount(), name, value);

			return;
		}

		emit(name, value);
	}

	private void emit(String name, String value) {

		getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
	}

	protected void emitHierarchicalEntityBuffer() {

		for (Map.Entry<String, String> emitEntry : getHierarchicalEntityBuffer()) {
			emit(emitEntry.getKey(), emitEntry.getValue());
		}
	}

	protected void emitHierarchicalEntityValueBuffer() {

		if (!variables.isEmpty()) {

			final String name = StringUtil.format(getName(), variables);
			final String value = StringUtil.format(getValue(), variables);

			HierarchicalMultiMap<Integer, String, String> entityBuffer = getHierarchicalEntityBuffer();
			entityBuffer.addToValue(getEntityCount(), name, value);
		}
	}

	@Override
	protected boolean isComplete() {
		return sourcesLeft.isEmpty();
	}

	@Override
	protected void receive(final String name, final String value,
			final NamedValueSource source) {
		variables.put(name, value);
		sourcesLeft.remove(source);
	}

	@Override
	public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
		sources.add(namedValueSource);
		sourcesLeft.add(namedValueSource);
	}

	@Override
	protected void clear() {
		sourcesLeft.addAll(sources);
		variables.clear();

		if (getIncludeSubEntities()) {
			getHierarchicalEntityBuffer().clear();
		}
	}

}
