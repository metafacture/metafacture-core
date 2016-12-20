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
package org.culturegraph.mf.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

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

		assertTrue(newBuffer.length >= str.length());
	}

	@Test
	public void copyToBufferShouldReturnBufferContainingTheStringData() {
		final int bufferLen = STRING_WITH_4_CHARS.length();
		char[] buffer = new char[bufferLen];
		final String str = STRING_WITH_4_CHARS;

		buffer = StringUtil.copyToBuffer(str, buffer);

		assertEquals(STRING_WITH_4_CHARS, String.valueOf(buffer, 0, bufferLen));
	}

	@Test
	public void testFormat() {

		assertEquals(ALOHA_HAWAII, StringUtil.format("${a} ${b}", vars));
		assertEquals(ALOHAHAWAII, StringUtil.format("${a}${b}", vars));
		assertEquals("Aloha${b", StringUtil.format("${a}${b", vars));
		assertEquals("XAloha${b", StringUtil.format("X${a}${b", vars));
		assertEquals("XX", StringUtil.format("X${ab}X", vars));
		assertEquals(XHULAXHULAX, StringUtil.format("X${bb}X${bb}X", vars));
		assertEquals("{a}Hawaii", StringUtil.format("{a}${b}", vars));
		assertEquals("Hula$Hula", StringUtil.format("${bb}$${bb}", vars));

	}

	@Test
	public void testCustomVarIndicators() {

		final String varStart = "VAR_START";
		final String varEnd = "VAR_END";
		assertEquals(ALOHA_HAWAII,
				StringUtil.format("VAR_STARTaVAR_END VAR_STARTbVAR_END", varStart, varEnd, vars));
		assertEquals(ALOHAHAWAII, StringUtil.format("VAR_STARTaVAR_ENDVAR_STARTbVAR_END", varStart, varEnd, vars));
		assertEquals("AlohaVAR_STARTb", StringUtil.format("VAR_STARTaVAR_ENDVAR_STARTb", varStart, varEnd, vars));
		assertEquals("XAlohaVAR_STARTb", StringUtil.format("XVAR_STARTaVAR_ENDVAR_STARTb", varStart, varEnd, vars));
		assertEquals("XX", StringUtil.format("XVAR_STARTabVAR_ENDX", varStart, varEnd, vars));
		assertEquals(XHULAXHULAX,
				StringUtil.format("XVAR_STARTbbVAR_ENDXVAR_STARTbbVAR_ENDX", varStart, varEnd, vars));
		assertEquals("{aVAR_ENDHawaii", StringUtil.format("{aVAR_ENDVAR_STARTbVAR_END", varStart, varEnd, vars));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMissingVars() {
		StringUtil.format("${a}${x}", false, vars);
	}

}
