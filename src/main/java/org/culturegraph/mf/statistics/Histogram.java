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
package org.culturegraph.mf.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.helpers.DefaultStreamReceiver;


/**
 * Counts entity names, literal names, or literal values.
 *
 * @author Christoph Böhme
 */
public final class Histogram extends DefaultStreamReceiver {

	private final Map<String, Integer> histogram = new HashMap<String, Integer>();

	private boolean countEntities;
	private boolean countLiterals;
	private String countField;

	public Histogram() {
		super();
	}

	/**
	 * Initialises the module with a countField.
	 *
	 * @param countField name of the field whose content is counted
	 */
	public Histogram(final String countField) {
		super();
		setCountField(countField);
	}

	public Map<String, Integer> getHistogram() {
		return Collections.unmodifiableMap(histogram);
	}

	public boolean isCountEntities() {
		return countEntities;
	}

	public void setCountEntities(final boolean countEntities) {
		this.countEntities = countEntities;
	}

	public boolean isCountLiterals() {
		return countLiterals;
	}

	public void setCountLiterals(final boolean countLiterals) {
		this.countLiterals = countLiterals;
	}

	public String getCountField() {
		return countField;
	}

	public void setCountField(final String countField) {
		this.countField = countField;
	}

	@Override
	public void startEntity(final String name) {
		if (countEntities) {
			count(name);
		}
	}

	@Override
	public void literal(final String name, final String value) {
		if (countLiterals) {
			count(name);
		}
		if (name.equals(countField)) {
			count(value);
		}
	}

	@Override
	public void resetStream() {
		histogram.clear();
	}

	private void count(final String value) {
		Integer c = histogram.get(value);
		if (c == null) {
			c = Integer.valueOf(0);
		}
		histogram.put(value, Integer.valueOf(c.intValue() + 1));
	}

}
