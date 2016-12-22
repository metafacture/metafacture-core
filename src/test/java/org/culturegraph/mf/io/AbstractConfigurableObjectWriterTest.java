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
package org.culturegraph.mf.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests shared by all back end implementations of {@link ObjectWriter}.
 *
 * @author Christoph Böhme
 *
 */
public abstract class AbstractConfigurableObjectWriterTest {

	private static final String HEADER = "(header)";
	private static final String FOOTER = "(footer)";
	private static final String SEPARATOR = "(sep)";

	private static final String DATA1 = "data-1";
	private static final String DATA2 = "data-2";
	private static final String DATA3 = "data-3";

	@Test
	public final void testShouldAppendNewLinesByDefault() throws IOException {
		final ConfigurableObjectWriter<String> writer = getWriter();

		// Use default configuration

		writer.process(DATA1);
		writer.process(DATA2);
		writer.process(DATA3);
		writer.closeStream();

		final String expectedResult =
				DATA1 + "\n" +
				DATA2 + "\n" +
				DATA3 + "\n";

		assertEquals(getOutput(), expectedResult);
	}

	@Test
	public final void testShouldOutputHeaderAtStreamStart() throws IOException {
		final ConfigurableObjectWriter<String> writer = getWriter();

		writer.setHeader(HEADER);

		writer.process(DATA1);
		writer.process(DATA2);
		writer.process(DATA3);
		writer.closeStream();

		final String expectedResult =
				HEADER +
				DATA1 + ObjectWriter.DEFAULT_SEPARATOR +
				DATA2 + ObjectWriter.DEFAULT_SEPARATOR +
				DATA3 +
				ObjectWriter.DEFAULT_FOOTER;

		assertEquals(getOutput(), expectedResult);
	}

	@Test
	public final void testShouldOutputFooterAtStreamEnd() throws IOException {
		final ConfigurableObjectWriter<String> writer = getWriter();

		writer.setFooter(FOOTER);

		writer.process(DATA1);
		writer.process(DATA2);
		writer.process(DATA3);
		writer.closeStream();

		final String expectedResult =
				ObjectWriter.DEFAULT_HEADER +
				DATA1 + ObjectWriter.DEFAULT_SEPARATOR +
				DATA2 + ObjectWriter.DEFAULT_SEPARATOR +
				DATA3 +
				FOOTER;

		assertEquals(getOutput(), expectedResult);
	}

	@Test
	public final void testShouldOutputSeparatorBetweenObjects() throws IOException {
		final ConfigurableObjectWriter<String> writer = getWriter();

		writer.setSeparator(SEPARATOR);

		writer.process(DATA1);
		writer.process(DATA2);
		writer.process(DATA3);
		writer.closeStream();

		final String expectedResult =
				ObjectWriter.DEFAULT_HEADER +
				DATA1 + SEPARATOR +
				DATA2 + SEPARATOR +
				DATA3 +
				ObjectWriter.DEFAULT_FOOTER;

		assertEquals(getOutput(), expectedResult);
	}

	@Test
	public final void testShouldOutputNoSeparatorifOnlyOneObjectIsOutput() throws IOException {
		final ConfigurableObjectWriter<String> writer = getWriter();

		writer.setHeader(HEADER);
		writer.setFooter(FOOTER);
		writer.setSeparator(SEPARATOR);

		writer.process(DATA1);
		writer.closeStream();

		final String expectedResult =
				HEADER +
				DATA1 +
				FOOTER;

		assertEquals(getOutput(), expectedResult);
	}

	@Test
	public final void testShouldOutputNothingIfNoObjectsAreProcessed() throws IOException {
		final ConfigurableObjectWriter<String> writer = getWriter();

		writer.setHeader(HEADER);
		writer.setFooter(FOOTER);
		writer.setSeparator(SEPARATOR);

		writer.closeStream();

		final String expectedResult = "";

		assertEquals(getOutput(), expectedResult);
	}

	protected abstract ConfigurableObjectWriter<String> getWriter();

	protected abstract String getOutput() throws IOException;

}
