/*
 *  Copyright 2013, 2014 Christoph Böhme
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

import java.util.*;

import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.NamedValueSource;
import org.culturegraph.mf.util.StringUtil;

/**
 * Corresponds to the <code>&lt;all&gt;</code> tag.
 *
 * @author Christoph Böhme <c.boehme@dnb.de>
 *
 */
public final class All extends AbstractFlushingCollect {

	private static final String DEFAULT_NAME = "";
	private static final String DEFAULT_VALUE = "true";
	private static final String FALSE = "false";

	private final Map<String, String> variables   = new HashMap<>();
	private final Set<NamedValueSource>       sources             = new HashSet<>();
	private final Set<NamedValueSource>       sourcesLeft         = new HashSet<>();
	private final Stack<NamedValueSource>     matchedSourcesStack = new Stack<>();
	final         ArrayList<NamedValueSource> toBeMatchedSources  = new ArrayList<>();

	public All(final Metamorph metamorph) {
		super(metamorph);
	}

	@Override
	protected void receive(final String name, final String value, final NamedValueSource source) {

		variables.put(name, value);
		sourcesLeft.remove(source);
		matchedSourcesStack.push(source);
	}

	@Override
	protected boolean isComplete() {

		return sourcesLeft.isEmpty();
	}

	@Override
	protected void clear() {

		sourcesLeft.addAll(sources);
		variables.clear();
		toBeMatchedSources.clear();
	}

	protected void clearLastMatchedEntity() {

		if (!matchedSourcesStack.isEmpty()) {

			final NamedValueSource pop = matchedSourcesStack.pop();
			toBeMatchedSources.add(pop);

			sourcesLeft.addAll(toBeMatchedSources);
		} else {

			sourcesLeft.addAll(sources);
		}
	}

	@Override
	protected void emit() {
		if (sourcesLeft.isEmpty()) {

			final String originalName = getName();

			final String name;

			if(originalName != null) {

				name = StringUtil.format(originalName, variables);
			} else {

				name = StringUtil.fallback(originalName, DEFAULT_NAME);
			}

			final String originalValue = getValue();

			final String value;

			if(originalValue != null) {

				value = StringUtil.format(originalValue, variables);
			} else {

				value = StringUtil.fallback(originalValue, DEFAULT_VALUE);
			}
			getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
		} else if (getIncludeSubEntities()) {

			forcedNonMatchedEmit();
		}
	}

	protected void forcedNonMatchedEmit() {

		final String name = StringUtil.fallback(getName(), DEFAULT_NAME);
		final String value = FALSE;

		getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
	}

	@Override
	public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {

		sources.add(namedValueSource);
		sourcesLeft.add(namedValueSource);
	}

}
