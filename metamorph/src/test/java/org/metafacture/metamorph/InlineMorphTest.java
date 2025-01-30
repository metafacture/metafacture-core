/*
 * Copyright 2021 hbz NRW
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

package org.metafacture.metamorph;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public final class InlineMorphTest {

    private static final String PREFIX = "<?xml version='1.1' encoding='UTF-8'?>\n" +
        "<metamorph version='1'\n    xmlns='http://www.culturegraph.org/metamorph'>";

    private static final String SUFFIX = "\n</metamorph>\n";

    public InlineMorphTest() {
    }

    @Test
    public void shouldProvideEmptyStringRepresentation() {
        Assert.assertEquals("", InlineMorph.in(this).toString());
    }

    @Test
    public void shouldProvideScriptStringRepresentation() {
        final InlineMorph morph = InlineMorph.in(this);

        final String[] script = new String[]{
            "<rules>",
            "  <data source='litA'>",
            "    <lookup>",
            "      <entry name='cat' value='mammal' />",
            "      <entry name='dog' value='mammal' />",
            "    </lookup>",
            "  </data>",
            "</rules>"
        };

        Arrays.stream(script).forEach(morph::with);

        Assert.assertEquals(PREFIX + String.join("\n", script) + SUFFIX, morph.toString());
    }

}
