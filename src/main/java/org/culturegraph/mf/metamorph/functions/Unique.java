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
package org.culturegraph.mf.metamorph.functions;

import java.util.HashSet;
import java.util.Set;

import org.culturegraph.mf.metamorph.api.helpers.AbstractStatefulFunction;

/**
 * Checks whether the received value was not received before.
 *
 * @author Markus Michael Geipel
 *
 */
public final class Unique extends AbstractStatefulFunction {

	private static final String ENTITY = "entity";
	private static final String NAME = "name";
	private static final String VALUE = "value";

	private final Set<String> set = new HashSet<String>();

	private boolean uniqueInEntity;

	private KeyGenerator keyGenerator = new KeyGenerator() {
		@Override
		public String createKey(final String name, final String value) {
			return name + "\0" + value;
		}
	};

	@Override
	public String process(final String value) {
		final String key = keyGenerator.createKey(getLastName(), value);
		if (set.contains(key)) {
			return null;
		}
		set.add(key);
		return value;
	}

	@Override
	protected void reset() {
		set.clear();
	}

	@Override
	protected boolean doResetOnEntityChange() {
		return uniqueInEntity;
	}

	public void setIn(final String scope) {
		uniqueInEntity = ENTITY.equals(scope);
	}

	public void setPart(final String part) {
		if (NAME.equals(part)) {
			keyGenerator = new KeyGenerator() {
				@Override
				public String createKey(final String name, final String value) {
					return name;
				}
			};
		} else if (VALUE.equals(part)) {
			keyGenerator = new KeyGenerator() {
				@Override
				public String createKey(final String name, final String value) {
					return value;
				}
			};
		}
	}

	/**
	 * To implement different uniqueness strategies
	 */
	private interface KeyGenerator {
		String createKey(String name, String value);
	}

}
