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

import static java.util.stream.Collectors.joining;
import static org.culturegraph.mf.commons.ResourceUtil.BUFFER_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Tests for class {@link ResourceUtil}.
 *
 * @author Christoph Böhme
 */
public class ResourceUtilTest {

	@Test
	public void readAll_shouldReturnEmptyStringIfStreamIsEmpty()
			throws IOException {

		final String result = ResourceUtil.readAll(new StringReader(""));

		assertTrue(result.isEmpty());
	}

	@Test
	public void readAll_shouldReadStreamThatFitsIntoOneBuffer()
			throws IOException {
		final String input = repeat("a", BUFFER_SIZE - 1);

		final String result = ResourceUtil.readAll(new StringReader(input));

		assertEquals(input, result);
	}

	@Test
	public void readAll_shouldReadStreamThatFitsExactlyIntoOneBuffer()
			throws IOException {
		final String input = repeat("b", BUFFER_SIZE);

		final String result = ResourceUtil.readAll(new StringReader(input));

		assertEquals(input, result);
	}

	@Test
	public void readAll_shouldReadStreamThatSpansMultipleBuffers()
			throws IOException {
		final String input = repeat("c", BUFFER_SIZE * 2 + 1);

		final String result = ResourceUtil.readAll(new StringReader(input));

		assertEquals(input, result);
	}

	private String repeat(String string, int times) {
		return IntStream.range(0, times)
				.mapToObj(i -> string)
				.collect(joining());
	}

}
