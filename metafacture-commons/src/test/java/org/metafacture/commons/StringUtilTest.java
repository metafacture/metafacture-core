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
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the static methods in {@link StringUtil}.
 *
 * @author Christoph Böhme
 * @author Markus Michael Geipel
 *
 */
public final class StringUtilTest {

    private static final String STRING_WITH_4_CHARS = "1234";
    private static final String STRING_WITH_10_CHARS = "1234567890";

    private static final String ALOHA_HAWAII = "Aloha Hawaii";
    private static final String ALOHAHAWAII = "AlohaHawaii";
    private static final String XHULAXHULAX = "XHulaXHulaX";

    private final Map<String, String> vars = new HashMap<String, String>();

    public StringUtilTest() {
    }

    @Before
    public void initvars() {
        vars.put("a", "Aloha");
        vars.put("b", "Hawaii");
        vars.put("bb", "Hula");
    }

    @Test
    public void copyToBufferShouldResizeBufferIfNecessary() {
        final char[] buffer = new char[2];
        final String str = STRING_WITH_10_CHARS;

        final char[] newBuffer = StringUtil.copyToBuffer(str, buffer);

        Assert.assertTrue(newBuffer.length >= str.length());
    }

    @Test
    public void copyToBufferShouldReturnBufferContainingTheStringData() {
        final int bufferLen = STRING_WITH_4_CHARS.length();
        char[] buffer = new char[bufferLen];
        final String str = STRING_WITH_4_CHARS;

        buffer = StringUtil.copyToBuffer(str, buffer);

        Assert.assertEquals(STRING_WITH_4_CHARS, String.valueOf(buffer, 0, bufferLen));
    }

    @Test
    public void testFormat() {

        Assert.assertEquals(ALOHA_HAWAII, StringUtil.format("${a} ${b}", vars));
        Assert.assertEquals(ALOHAHAWAII, StringUtil.format("${a}${b}", vars));
        Assert.assertEquals("Aloha${b", StringUtil.format("${a}${b", vars));
        Assert.assertEquals("XAloha${b", StringUtil.format("X${a}${b", vars));
        Assert.assertEquals("XX", StringUtil.format("X${ab}X", vars));
        Assert.assertEquals(XHULAXHULAX, StringUtil.format("X${bb}X${bb}X", vars));
        Assert.assertEquals("{a}Hawaii", StringUtil.format("{a}${b}", vars));
        Assert.assertEquals("Hula$Hula", StringUtil.format("${bb}$${bb}", vars));

    }

    @Test
    public void testCustomVarIndicators() {

        final String varStart = "VAR_START";
        final String varEnd = "VAR_END";
        Assert.assertEquals(ALOHA_HAWAII,
                StringUtil.format("VAR_STARTaVAR_END VAR_STARTbVAR_END", varStart, varEnd, vars));
        Assert.assertEquals(ALOHAHAWAII, StringUtil.format("VAR_STARTaVAR_ENDVAR_STARTbVAR_END", varStart, varEnd, vars));
        Assert.assertEquals("AlohaVAR_STARTb", StringUtil.format("VAR_STARTaVAR_ENDVAR_STARTb", varStart, varEnd, vars));
        Assert.assertEquals("XAlohaVAR_STARTb", StringUtil.format("XVAR_STARTaVAR_ENDVAR_STARTb", varStart, varEnd, vars));
        Assert.assertEquals("XX", StringUtil.format("XVAR_STARTabVAR_ENDX", varStart, varEnd, vars));
        Assert.assertEquals(XHULAXHULAX,
                StringUtil.format("XVAR_STARTbbVAR_ENDXVAR_STARTbbVAR_ENDX", varStart, varEnd, vars));
        Assert.assertEquals("{aVAR_ENDHawaii", StringUtil.format("{aVAR_ENDVAR_STARTbVAR_END", varStart, varEnd, vars));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingVars() {
        StringUtil.format("${a}${x}", false, vars);
    }

}
