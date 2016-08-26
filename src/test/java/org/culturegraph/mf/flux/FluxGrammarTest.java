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
package org.culturegraph.mf.flux;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Collections;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Flux grammar
 *
 * @author Christoph Böhme
 *
 */
public final class FluxGrammarTest {

	private static final Charset ENCODING = Charset.forName("UTF-8");
	private static final String TEST_FLOW =  "\"test\"|write(\"stdout\");";

	private static final String NO_OUTPUT = "";

	private ByteArrayOutputStream stdoutBuffer;
	private ByteArrayOutputStream stderrBuffer;

	@Before
	public void setup() {
		// Redirect standard out:
		stdoutBuffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdoutBuffer));

		// Redirect standard err:
		stderrBuffer = new ByteArrayOutputStream();
		System.setErr(new PrintStream(stderrBuffer));
	}

	@Test
	public void shouldAllowEmptyCommentInLastLineOfFile() throws RecognitionException, IOException {
		final String script = TEST_FLOW + "//";

		final InputStream stream = new ByteArrayInputStream(script.getBytes(ENCODING));
		FluxCompiler.compile(stream, Collections.<String, String>emptyMap());

		assertEquals(NO_OUTPUT, stdoutBuffer.toString());
		assertEquals(NO_OUTPUT, stderrBuffer.toString());
	}

	@Test
	public void shouldAllowEmptyCommentInFile() throws RecognitionException, IOException {
		final String script = TEST_FLOW + "//\n";

		final InputStream stream = new ByteArrayInputStream(script.getBytes(ENCODING));
		FluxCompiler.compile(stream, Collections.<String, String>emptyMap());

		assertEquals(NO_OUTPUT, stdoutBuffer.toString());
		assertEquals(NO_OUTPUT, stderrBuffer.toString());
	}

}
