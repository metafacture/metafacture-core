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

package org.metafacture.commons.tries;

import org.metafacture.commons.tries.SetMatcher.Match;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;

/**
 * Replaces Strings by other Strings.
 *
 * @author Markus Michael Geipel
 */
public final class SetReplacer {
    private final SetMatcher<String> matcher = new SetMatcher<String>();

    public SetReplacer() {
    }

    /**
     * Adds a replacement of a String by another String.
     *
     * @param toReplace   String to replace
     * @param replacement String of replacement
     */
    public void addReplacement(final String toReplace, final String replacement) {
        matcher.put(toReplace, replacement);
    }

    /**
     * Adds replacements given as a Map of Strings.
     *
     * @param replacements the Map of Strings to be replaced
     */
    public void addReplacements(final Map<String, String> replacements) {
        for (final Entry<String, String> entry : replacements.entrySet()) {
            addReplacement(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Replaces the Strings defined with {@link #addReplacement(String, String)} in
     * the text.
     *
     * @param text
     * @return the text with the replacements
     */
    public String replaceIn(final String text) {
        final List<SetMatcher.Match<String>> matches = matcher.match(text);
        final StringBuilder builder = new StringBuilder();
        int lastCut = 0;

        Collections.sort(matches, new Comparator<SetMatcher.Match<String>>() {
            @Override
            public int compare(final Match<String> o1, final Match<String> o2) {
                final int delta = o1.getStart() - o2.getStart();
                return delta < 0 ? -1 : delta > 0 ? 1 : o1.getLength() > o2.getLength() ? -1 : 1;
            }
        });

        for (final SetMatcher.Match<String> match : matches) {
            if (match.getStart() < lastCut) {
                continue;
            }
            builder.append(text.substring(lastCut, match.getStart()));
            builder.append(match.getValue());
            lastCut = match.getStart() + match.getLength();
        }
        builder.append(text.substring(lastCut, text.length()));
        return builder.toString();
    }

}
