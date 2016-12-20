/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.metamorph.api.helpers;

import org.culturegraph.mf.metamorph.api.Function;
import org.culturegraph.mf.metamorph.api.NamedValueSource;

/**
 * Baseclass for {@link Function}s returning only one or zero results and do not
 * maintain a state.
 *
 * @author Markus Michael Geipel
 *
 */
public abstract class AbstractSimpleStatelessFunction extends AbstractFunction {

	@Override
	public final void receive(final String name, final String value,
			final NamedValueSource source, final int recordCount,
			final int entityCount) {

		final String processedValue = process(value);
		if (processedValue == null) {
			return;
		}

		getNamedValueReceiver().receive(name, processedValue, this,
				recordCount, entityCount);
	}

	protected abstract String process(String value);

}
