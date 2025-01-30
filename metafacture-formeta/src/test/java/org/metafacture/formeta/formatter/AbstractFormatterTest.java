/*
 * Copyright 2014 Deutsche Nationalbibliothek
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

package org.metafacture.formeta.formatter;

import org.metafacture.commons.StringUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link AbstractFormatter}.
 *
 * @author Christoph Böhme
 *
 */
public final class AbstractFormatterTest { // checkstyle-disable-line AbstractClassName

    private AbstractFormatter abstractFormatter;

    public AbstractFormatterTest() {
    }

    @Before
    public void setup() {
        abstractFormatter = new AbstractFormatter() {
            @Override
            public void startGroup(final String name) {
            }

            @Override
            public void endGroup() {
            }

            @Override
            public void literal(final String name, final String value) {

            }

            @Override
            protected boolean shouldQuoteText(final char[] buffer, final int len) {
                return false;
            }
        };
    }

    @Test
    public void issue161() {
        final String longValue = StringUtil.repeatChars('a', AbstractFormatter.BUFFER_SIZE * 2 + 1);

        abstractFormatter.escapeAndAppend(longValue);

        Assert.assertEquals(longValue, abstractFormatter.toString());
    }

}
