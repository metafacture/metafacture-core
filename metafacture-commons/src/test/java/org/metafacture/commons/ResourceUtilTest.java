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

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for class {@link ResourceUtil}.
 *
 * @author Christoph Böhme
 */
public class ResourceUtilTest {

    public ResourceUtilTest() {
    }

    @Test
    public void readAllShouldReturnEmptyStringIfStreamIsEmpty()
            throws IOException {
        final String result = ResourceUtil.readAll(new StringReader(""));

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void readAllShouldReadStreamThatFitsIntoOneBuffer()
            throws IOException {
        final String input = repeat("a", ResourceUtil.BUFFER_SIZE - 1);

        final String result = ResourceUtil.readAll(new StringReader(input));

        Assert.assertEquals(input, result);
    }

    @Test
    public void readAllShouldReadStreamThatFitsExactlyIntoOneBuffer()
            throws IOException {
        final String input = repeat("b", ResourceUtil.BUFFER_SIZE);

        final String result = ResourceUtil.readAll(new StringReader(input));

        Assert.assertEquals(input, result);
    }

    @Test
    public void readAllShouldReadStreamThatSpansMultipleBuffers()
            throws IOException {
        final String input = repeat("c", ResourceUtil.BUFFER_SIZE * 2 + 1);

        final String result = ResourceUtil.readAll(new StringReader(input));

        Assert.assertEquals(input, result);
    }

    private String repeat(final String string, final int times) {
        return IntStream.range(0, times)
                .mapToObj(i -> string)
                .collect(Collectors.joining());
    }

}
