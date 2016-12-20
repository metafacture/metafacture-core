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
package org.culturegraph.mf.metamorph;

import org.culturegraph.mf.commons.StringUtil;
import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.helpers.AbstractNamedValuePipe;

/**
 * Implementation of the <code>&lt;data&gt;</code> tag.
 *
 * @author Markus Michael Geipel
 */
final class Data extends AbstractNamedValuePipe {

	private String name;

	@Override
	public void receive(final String recName, final String recValue,
			final NamedValueSource source, final int recordCount,
			final int entityCount) {

		getNamedValueReceiver().receive(StringUtil.fallback(name, recName),
				recValue, this, recordCount, entityCount);
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
