/*
 * Copyright 2023 hbz
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Tests {@link URLEncode}
 *
 * @author Pascal Christoph (dr0i)
 */

public final class URLEncodeTest {

    private static final String CAFE_UTF8 = "café";
    private static final String CAFE_ENCODED = "caf%C3%A9";
    private static final String SOME_CHARS = "/&%\\+";
    private static final String SOME_CHARS_ENCODED = "%2F%26%25%5C%2B";
    private static final String SPECIAL_CHARACTERS = ".-*_";
    private static final String URL =
            "http://lobid.org/resources/search?q=hasItem.hasItem.heldBy.id:\"http://lobid" +
                    ".org/organisations/DE-290#!\"&format=json";
    private static final String WHITESPACE = " ";
    private static final String WHITESPACE_AS_PLUS_ENCODED = "+";
    private static final String WHITESPACE_PERCENT_ENCODED = "%20";

    public URLEncodeTest() {
    }

    @Test
    public void testUtf8() {
        final URLEncode urlEncode = new URLEncode();
        Assert.assertEquals(CAFE_ENCODED, urlEncode.process(CAFE_UTF8));
    }

    @Test
    public void testSomeChars() {
        final URLEncode urlEncode = new URLEncode();
        Assert.assertEquals(SOME_CHARS_ENCODED, urlEncode.process(SOME_CHARS));
    }

    @Test
    public void testEscapeSpaceAsPlus() {
        final URLEncode urlEncode = new URLEncode();
        Assert.assertEquals(WHITESPACE_AS_PLUS_ENCODED, urlEncode.process(WHITESPACE));
    }

    @Test
    public void testEscapeSpaceAsPercentEncoded() {
        final URLEncode urlEncode = new URLEncode();
        urlEncode.setPlusForSpace(false);
        Assert.assertEquals(WHITESPACE_PERCENT_ENCODED, urlEncode.process(WHITESPACE));
    }

    @Test
    public void testSafeChars() {
        final URLEncode urlEncode = new URLEncode();
        urlEncode.setSafeChars(SOME_CHARS);
        Assert.assertEquals(SOME_CHARS, urlEncode.process(SOME_CHARS));
    }

    @Test
    public void testSpecialChars() {
        final URLEncode urlEncode = new URLEncode();
        Assert.assertEquals(SPECIAL_CHARACTERS, urlEncode.process(SPECIAL_CHARACTERS));
    }

    @Test
    public void testBackwardsCompatibility() throws UnsupportedEncodingException {
        final URLEncode urlEncode = new URLEncode();
        Assert.assertEquals(urlEncode.process(URL), URLEncoder.encode(URL, "UTF-8"));
    }

}
