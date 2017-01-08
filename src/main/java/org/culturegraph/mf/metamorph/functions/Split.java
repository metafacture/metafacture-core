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

import java.util.regex.Pattern;

import org.culturegraph.mf.metamorph.api.NamedValueSource;
import org.culturegraph.mf.metamorph.api.helpers.AbstractFunction;

/**
 * Splits the received value and output each part separately.
 *
 * @author Markus Michael Geipel
 */
public final class Split extends AbstractFunction {

	private Pattern delimiterPattern;

	@Override
	public void receive(final String name, final String value,
			final NamedValueSource source, final int recordCount,
			final int entityCount) {
		final String[] parts = delimiterPattern.split(value);
		for (final String part : parts) {
			getNamedValueReceiver().receive(name, part, this, recordCount,
					entityCount);
		}
	}

	public void setDelimiter(final String delimiter) {
		this.delimiterPattern = Pattern.compile(delimiter);
	}

}
