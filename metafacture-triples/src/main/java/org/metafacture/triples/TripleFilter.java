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

package org.metafacture.triples;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.framework.objects.Triple;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters triples. The patterns for subject, predicate and object are disjunctive.
 *
 * @author Christoph Böhme
 *
 */
@Description("Filters triple. The  patterns for subject, predicate and object are disjunctive.")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("filter-triples")
public final class TripleFilter extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> {

    // A regexp that is guaranteed to never match ( an `a` after the end
    // of the string, see http://stackoverflow.com/a/1723225 for details):
    private static final Matcher MATCH_NOTHING = Pattern.compile("$a").matcher("");

    private Matcher subjectMatcher = MATCH_NOTHING;
    private Matcher predicateMatcher = MATCH_NOTHING;
    private Matcher objectMatcher = MATCH_NOTHING;
    private boolean passMatches = true;

    /**
     * Creates an instance of {@link TripleFilter}.
     */
    public TripleFilter() {
    }

    /**
     * Gets the pattern of the subject filter.
     *
     * @return the pattern of the subject filter
     */
    public String getSubjectPattern() {
        return subjectMatcher.pattern().pattern();
    }

    /**
     * Gets the pattern of the subject filter.
     *
     * @param pattern the pattern of the subject filter
     */
    public void setSubjectPattern(final String pattern) {
        subjectMatcher = Pattern.compile(pattern).matcher("");
    }

    /**
     * Gets the pattern of the predicate filter.
     *
     * @return the pattern of the predicate filter
     */
    public String getPredicatePattern() {
        return predicateMatcher.pattern().pattern();
    }

    /**
     * Gets the pattern of the predicate filter.
     *
     * @param pattern the pattern of the predicate filter
     */
    public void setPredicatePattern(final String pattern) {
        predicateMatcher = Pattern.compile(pattern).matcher("");
    }

    /**
     * Gets the pattern of the object filter.
     *
     * @return the pattern of the object filter
     */
    public String getObjectPattern() {
        return objectMatcher.pattern().pattern();
    }

    /**
     * Sets a pattern to filter objects.
     *
     * @param pattern the pattern
     */
    public void setObjectPattern(final String pattern) {
        objectMatcher = Pattern.compile(pattern).matcher("");
    }

    /**
     * Checks whether matches passes.
     *
     * @return true if matches should pass
     */
    public boolean isPassMatches() {
        return passMatches;
    }

    /**
     * Flags whether matches should pass.
     *
     * @param passMatches true if matches should pass, otherwise matches don't pass
     */
    public void setPassMatches(final boolean passMatches) {
        this.passMatches = passMatches;
    }

    @Override
    public void process(final Triple obj) {
        subjectMatcher.reset(obj.getSubject());
        predicateMatcher.reset(obj.getPredicate());
        objectMatcher.reset(obj.getObject());

        final boolean matches = subjectMatcher.matches() || predicateMatcher.matches() || objectMatcher.matches();

        if (matches == passMatches) {
            getReceiver().process(obj);
        }
    }

}
