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

package org.metafacture.metamorph.functions;

import org.metafacture.metamorph.api.MorphBuildException;
import org.metafacture.metamorph.api.helpers.AbstractSimpleStatelessFunction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Changes the the received value from upper to lower case
 * or vice versa.
 *
 * @author Markus Michael Geipel
 */
public final class Case extends AbstractSimpleStatelessFunction {

    public static final String UPPER = "upper";
    private static final Set<String> LANGUAGES;
    private Locale locale = Locale.getDefault();
    private boolean toUpper;

    static {
        final Set<String> set = new HashSet<String>();
        Collections.addAll(set, Locale.getISOLanguages());
        LANGUAGES = Collections.unmodifiableSet(set);
    }

    /**
     * Creates an instance of {@link Case}.
     */
    public Case() {
    }

    @Override
    public String process(final String value) {
        if (toUpper) {
            return value.toUpperCase(locale);
        }
        return value.toLowerCase(locale);
    }

    /**
     * Flags whether the case should be upper.
     *
     * @param string the case is set to upper when set to {@value #UPPER}
     */
    public void setTo(final String string) {
        this.toUpper = UPPER.equals(string);
    }

    /**
     * Sets the language if it's included in {@link #LANGUAGES}.
     *
     * @param language the language
     */
    public void setLanguage(final String language) {
        if (!LANGUAGES.contains(language)) {
            throw new MorphBuildException("Language " + language + " not supported.");
        }
        this.locale = new Locale(language);
    }

}
