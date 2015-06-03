/*
 *  Copyright 2014 Christoph Böhme
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the static methods in {@link StringUtil}.
 *
 * @author Christoph Böhme
 *
 */
public final class StringUtilTest {

	private static final String STRING_WITH_4_CHARS = "1234";
	private static final String STRING_WITH_10_CHARS = "1234567890";

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

}
