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

import org.junit.Assert;
import org.junit.Test;

/**
 * tests {@link Regexp}, {@link Substring}, {@link Compose}, {@link Replace}
 *
 * @author Markus Michael Geipel
 */

public final class StringOperationsTest {

    private static final String VALUE1 = "Franz";
    private static final String VALUE2 = "Kafka";
    private static final String VALUE3 = "Josef";

    public StringOperationsTest() {
    }

    @Test
    public void testSubstring() {
        final Substring substring = new Substring();
        Assert.assertEquals(VALUE1, substring.process(VALUE1));

        final int position = 2;
        substring.setStart(String.valueOf(position));
        Assert.assertEquals(VALUE1.substring(position), substring.process(VALUE1));

        substring.setStart("0");
        substring.setEnd(String.valueOf(VALUE1.length() + 1));
        Assert.assertEquals(VALUE1, substring.process(VALUE1));

        substring.setStart(String.valueOf(VALUE1.length() + 1));
        substring.setEnd(String.valueOf(position));
        Assert.assertNull(substring.process(VALUE1));

    }

    // TODO: Can we remove this test?
    //    @Test(expected=Regexp.PatternNotFoundException.class)
    //    public void testRegexpExceptionOnFail() {
    //        final Regexp regexp = new Regexp();
    //        regexp.setMatch(VALUE2);
    //        regexp.setExceptionOnFail("true");
    //        regexp.process(VALUE3);
    //    }

    @Test
    public void testCompose() {
        final Compose compose = new Compose();
        compose.setPrefix(VALUE1);
        Assert.assertEquals(VALUE1 + VALUE2, compose.process(VALUE2));
        compose.setPrefix("");
        compose.setPostfix(VALUE1);
        Assert.assertEquals(VALUE2 + VALUE1, compose.process(VALUE2));
    }

    @Test
    public void testReplace() {
        final Replace replace = new Replace();
        replace.setPattern(VALUE2);
        replace.setWith(VALUE3);
        Assert.assertEquals(VALUE1 + VALUE3, replace.process(VALUE1 + VALUE2));
    }

}
