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

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.helpers.AbstractFlushingCollect;

/**
 * Implementation of the <code>&lt;group&gt;</code> tag.
 *
 * @author Markus Michael Geipel
 */
public final class Group extends AbstractFlushingCollect {

	@Override
	protected void receive(final String recName, final String recValue,
			final NamedValueSource source) {
		getNamedValueReceiver().receive(
				StringUtil.fallback(getName(), recName),
				StringUtil.fallback(getValue(), recValue), this,
				getRecordCount(), getEntityCount());
	}

	@Override
	protected boolean isComplete() {
		// Nothing to do
		return false;
	}

	@Override
	protected void clear() {
		// Nothing to do
	}

	@Override
	protected void emit() {
		// Nothing to do
	}

}
