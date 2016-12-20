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

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.culturegraph.mf.metamorph.api.MorphBuildException;
import org.culturegraph.mf.metamorph.api.helpers.AbstractSimpleStatelessFunction;

/**
 * Changes the the received value from upper to lower case
 * or vice versa.
 *
 * @author Markus Michael Geipel
 */
public final class Case extends AbstractSimpleStatelessFunction {

	private static final String UPPER = "upper";
	private static final Set<String> LANGUAGES;
	private Locale locale = Locale.getDefault();
	private boolean toUpper;

	static {
		final Set<String> set = new HashSet<String>();
		Collections.addAll(set, Locale.getISOLanguages());
		LANGUAGES = Collections.unmodifiableSet(set);
	}

	@Override
	public String process(final String value) {
		if (toUpper) {
			return value.toUpperCase(locale);
		}
		return value.toLowerCase(locale);
	}

	public void setTo(final String string) {
		this.toUpper = UPPER.equals(string);
	}

	public void setLanguage(final String language) {
		if (!LANGUAGES.contains(language)) {
			throw new MorphBuildException("Language " + language
					+ " not supported.");
		}
		this.locale = new Locale(language);
	}

}
