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

package org.metafacture.strings;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches the incoming strings against a regular expression and replaces
 * the matching parts.
 *
 * @author Christoph Böhme
 */
@Description("Matches the incoming strings against a regular expression and replaces the matching parts.")
@In(String.class)
@Out(String.class)
@FluxCommand("match")
public final class StringMatcher extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    private Matcher matcher;
    private String replacement;

    /**
     * Creates an instance of {@link StringMatcher}.
     */
    public StringMatcher() {
    }

    /**
     * Gets the pattern.
     *
     * @return the pattern
     */
    public String getPattern() {
        return matcher.pattern().pattern();
    }

    /**
     * Sets the pattern.
     *
     * @param pattern the pattern
     */
    public void setPattern(final String pattern) {
        this.matcher = Pattern.compile(pattern).matcher("");
    }

    /**
     * Gets the replacement.
     *
     * @return the replacement
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * Sets the replacement.
     *
     * @param replacement the replacement
     */
    public void setReplacement(final String replacement) {
        this.replacement = replacement;
    }

    @Override
    public void process(final String obj) {
        assert !isClosed();
        assert null != obj;
        matcher.reset(obj);
        getReceiver().process(matcher.replaceAll(replacement));
    }

}
