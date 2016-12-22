/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.culturegraph.mf.commons.ResourceUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for class {@link ObjectFileWriter}.
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectFileWriterTest
		extends AbstractConfigurableObjectWriterTest {

	private static final String DATA = "Überfacture";

	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	private File file;
	private ObjectFileWriter<String> writer;

	@Before
	public void setup() throws IOException {
		file = tempFolder.newFile();
		writer = new ObjectFileWriter<String>(file.getAbsolutePath());
	}

	@Test
	public void shouldWriteUTF8EncodedOutput() throws IOException {
		assumeFalse("Default encoding is UTF-8: It is not possible to test whether " +
						"ObjectFileWriter sets the encoding to UTF-8 correctly.",
				StandardCharsets.UTF_8.equals(Charset.defaultCharset()));

		writer.process(DATA);
		writer.closeStream();

		final byte[] bytesWritten = Files.readAllBytes(file.toPath());
		assertArrayEquals((DATA + "\n").getBytes(StandardCharsets.UTF_8),
				bytesWritten); // FileObjectWriter appends new lines
	}

	@Override
	protected ConfigurableObjectWriter<String> getWriter() {
		return writer;
	}

	@Override
	protected String getOutput() throws IOException {
		final Charset encoding = Charset.forName(writer.getEncoding());
		try (InputStream inputStream = new FileInputStream(file)) {
			return ResourceUtil.readAll(inputStream, encoding);
		}
	}

}
