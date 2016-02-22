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
import org.culturegraph.mf.types.HierarchicalMultiMap;
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

		if (getIncludeSubEntities() && !(this.getNamedValueReceiver() != null && Combine.class.isInstance(this.getNamedValueReceiver()) && ((Combine) this.getNamedValueReceiver()).getIncludeSubEntities())) {

			final HierarchicalMultiMap<Integer, String, String> entityBuffer = getHierarchicalEntityBuffer();
			entityBuffer.addToEmit(getEntityCount(), name, value);
		}

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

		if (getIncludeSubEntities()) {
			getHierarchicalEntityBuffer().clear();
		}
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
	protected void emit(String originalName, String orignalValue) {

		final String name;

		if(originalName != null) {

			name = StringUtil.format(originalName, variables);
		} else {

			name = StringUtil.fallback(originalName, DEFAULT_NAME);
		}

		final String value = determineValue(DEFAULT_VALUE);

		getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
	}

	@Override
	protected void emit() {

		if (sourcesLeft.isEmpty()) {

			if (getIncludeSubEntities() && !(this.getNamedValueReceiver() != null && Combine.class.isInstance(this.getNamedValueReceiver()) && ((Combine) this.getNamedValueReceiver()).getIncludeSubEntities())) {

				emitHierarchicalEntityBuffer();

				return;
			}

			emit(getName(), getValue());
		} else if (getIncludeSubEntities()) {

			forcedNonMatchedEmit();
		}
	}

	protected void forcedNonMatchedEmit() {

		final String name = StringUtil.fallback(getName(), DEFAULT_NAME);
		final String value = determineValue(FALSE);

		// if replaced value is empty and the original value is not one of the default values for 'all' collector
		if(value.isEmpty() && (!getValue().equals(DEFAULT_VALUE) || !getValue().equals(FALSE))) {

			// force non-matched-emit for "default" all collector (i.e. that one that is not utilised in the d:swarm filter)

			return;
		}

		getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
	}

	@Override
	public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {

		sources.add(namedValueSource);
		sourcesLeft.add(namedValueSource);
	}

	@Override
	protected void emitHierarchicalEntityBuffer() {

		final HierarchicalMultiMap<Integer, String, String> hierarchicalEntityBuffer = getHierarchicalEntityBuffer();

		final Map<String, List<String>> variablesMap = new LinkedHashMap<>();

		for (final Map.Entry<String, String> emitEntry : hierarchicalEntityBuffer) {

			final String variableName = emitEntry.getKey();
			final String variableValue = emitEntry.getValue();

			if(!variablesMap.containsKey(variableName)) {

				variablesMap.put(variableName, new ArrayList<String>());
			}

			variablesMap.get(variableName).add(variableValue);
		}

		// determine longest variable value list

		String variableNameOfLongestVariableValueList = null;
		int longestVariableValueListSize = 0;

		for (final Map.Entry<String, List<String>> variablesEntry : variablesMap.entrySet()) {

			final String variableName = variablesEntry.getKey();
			final List<String> variableValues = variablesEntry.getValue();
			final int variableValuesSize = variableValues.size();

			if(variableValuesSize > longestVariableValueListSize) {

				longestVariableValueListSize = variableValuesSize;
				variableNameOfLongestVariableValueList = variableName;
			}
		}

		// emit all values for all variables as vector
		boolean createdVector = false;

		while(!createdVector) {

			for (final Map.Entry<String, List<String>> variablesEntry : variablesMap.entrySet()) {

				final String variableName = variablesEntry.getKey();
				final List<String> variableValues = variablesEntry.getValue();

				if (!variableValues.isEmpty()) {

					String variableValue = variableValues.get(0);
					variableValues.remove(0);

					variables.put(variableName, variableValue);

					createdVector = false;
				} else if(variableName.equals(variableNameOfLongestVariableValueList)) {

					// longest variable value list is empty

					createdVector = true;
				}
			}

			if(!createdVector) {

				emit(getName(), getValue());
			}
		}

		clear();
	}

	protected void emitHierarchicalEntityValueBuffer() {

		if (!variables.isEmpty()) {

			final String name = StringUtil.format(getName(), variables);
			final String value = StringUtil.format(getValue(), variables);

			final HierarchicalMultiMap<Integer, String, String> entityBuffer = getHierarchicalEntityBuffer();
			entityBuffer.addToValue(getEntityCount(), name, value);
		}
	}

	private String determineValue(final String defaultValue) {

		final String originalValue = getValue();

		final String value;

		if(originalValue != null) {

			value = StringUtil.format(originalValue, variables);
		} else {

			value = StringUtil.fallback(originalValue, defaultValue);
		}
		return value;
	}
}
