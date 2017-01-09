/*
 * Copyright 2013, 2014 Christoph Böhme
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
import org.culturegraph.mf.metamorph.api.helpers.AbstractCollect;

/**
 * Corresponds to the {@code <any>} tag.
 *
 * @author Christoph Böhme
 */
public final class Any extends AbstractCollect {

	private static final String DEFAULT_NAME = "";
	private static final String DEFAULT_VALUE = "true";

	private boolean receivedInput;
	private boolean emittedResult;

	@Override
	protected void receive(final String name, final String value, final NamedValueSource source) {
		receivedInput = true;
	}

	@Override
	protected boolean isComplete() {
		return receivedInput;
	}

	@Override
	protected void clear() {
		receivedInput = false;
		emittedResult = false;
	}

	@Override
	protected void emit() {
		if (receivedInput && !emittedResult) {
			final String name = StringUtil.fallback(getName(), DEFAULT_NAME);
			final String value = StringUtil.fallback(getValue(), DEFAULT_VALUE);
			getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
			emittedResult = true;
		}
	}

	@Override
	public void flush(final int recordCount, final int entityCount) {
		emit();
		clear();
	}

}
