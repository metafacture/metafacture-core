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
 * Only forwards records which match (or do not match) a regular expression
 * given in the constructor.
 *
 * @author Christoph Böhme
 *
 */
@Description("Only forwards records which match (or do not match) a regular expression.")
@In(String.class)
@Out(String.class)
@FluxCommand("filter-strings")
public final class StringFilter extends
        DefaultObjectPipe<String, ObjectReceiver<String>> {

    private final Matcher matcher;
    private boolean passMatches = true;

    /**
     * Creates an instance of {@link StringFilter} by a given pattern.
     *
     * @param pattern the pattern
     */
    public StringFilter(final String pattern) {
        this.matcher = Pattern.compile(pattern).matcher("");
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
     * Checks whether matches are passed.
     *
     * @return true if matches should pass
     */
    public boolean isPassMatches() {
        return passMatches;
    }

    /**
     * Flags whether to pass matches or, inversely, pass everything but the matches.
     *
     * @param passMatches true if matches should pass, otherwise false
     */
    public void setPassMatches(final boolean passMatches) {
        this.passMatches = passMatches;
    }

    @Override
    public void process(final String obj) {
        assert !isClosed();
        assert null != obj;
        matcher.reset(obj);
        if (matcher.find() == passMatches) {
            getReceiver().process(obj);
        }
    }

}
