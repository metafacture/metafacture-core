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

import org.metafacture.framework.ObjectReceiver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link StringMatcher}.
 *
 * @author Christoph Böhme
 *
 */
public final class StringMatcherTest {

    private static final String INPUT_STRING =
            "The pattern is not part of the input";

    private StringMatcher matcher;

    @Mock
    private ObjectReceiver<String> receiver;

    public StringMatcherTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        matcher = new StringMatcher();
        matcher.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        matcher.closeStream();
    }

    @Test
    public void testShouldReplaceAllMatchesWithReplacementString() {
        matcher.setPattern("PLACEHOLDER");
        matcher.setReplacement("Karl");

        matcher.process("Hi PLACEHOLDER! -- Goodbye PLACEHOLDER!");

        Mockito.verify(receiver).process("Hi Karl! -- Goodbye Karl!");
    }

    @Test
    public void testShouldHandleCaptureGroupReferencesInReplacementString() {
        matcher.setPattern("^([^ ]+) .*$");
        matcher.setReplacement("$1");

        matcher.process("important-bit but this can be ignored");

        Mockito.verify(receiver).process("important-bit");
    }

    @Test
    public void testShouldRelayNonMatchingInput() {
        matcher.setPattern("Non-matching pattern");

        matcher.process(INPUT_STRING);

        Mockito.verify(receiver).process(INPUT_STRING);
    }

}
