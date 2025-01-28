/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.commons;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for class {@link XmlUtil}.
 *
 * @author Christoph Böhme
 */
public class XmlUtilTest {

    public XmlUtilTest() {
    }

    @Test
    public void shouldEscapeXmlSpecialChars() {
        final String unescaped = "< > ' & \"";

        final String result = XmlUtil.escape(unescaped);

        Assert.assertEquals("&lt; &gt; &apos; &amp; &quot;", result);
    }

    @Test
    public void shouldNotEscapeAsciiChars() {
        final String unescaped = "Kafka";

        final String result = XmlUtil.escape(unescaped);

        Assert.assertEquals("Kafka", result);
    }

    @Test
    public void shouldEscapeAllNonAsciiChars() {
        final String unescaped = "K\u00f8benhavn";

        final String result = XmlUtil.escape(unescaped);

        Assert.assertEquals("K&#248;benhavn", result);
    }

    @Test
    public void issue267_shouldEscapeSurrogatePairsAsSingleEntity() {
        final String unescaped = "Smile: \ud83d\ude09";
        final String result = XmlUtil.escape(unescaped);

        Assert.assertEquals("Smile: &#128521;", result);
    }

}
