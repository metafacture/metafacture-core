/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.sink;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for class {@link ObjectFileWriter}.
 * 
 * @author Christoph Böhme
 */
public final class ObjectFileWriterTest {

	private static final String OUTPUT = "Überfacture";
	
	private static final String UTF8_MESSAGE = 
			"Default encoding is UTF-8: It is not possible to test whether " +
			"ObjectFileWriter sets the encoding to UTF-8 correctly.";
	
	// NO CHECKSTYLE VisibilityModifier FOR 3 LINES:
	// JUnit requires rules to be public
	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void testOutputIsUTF8Encoded() throws IOException {
		final Charset charsetUTF8 = Charset.forName("UTF-8");
		assumeThat(UTF8_MESSAGE, Charset.defaultCharset(), not(equalTo(charsetUTF8)));

		final File file = tempFolder.newFile();
		
		final ObjectFileWriter<String> writer = new ObjectFileWriter<String>(file.getAbsolutePath());
		writer.process(OUTPUT);
		writer.closeStream();
		
		final byte[] expected = (OUTPUT + "\n").getBytes(charsetUTF8); // ObjectFileWriter appends new lines
		final InputStream stream = new FileInputStream(file);
		try {
			int i = 0;
			int b;
			while((b = stream.read()) != -1) {
				assertTrue("File contains more data than expected", i < expected.length);
				assertEquals("File contains unexpected characters", expected[i], (byte)b);
				i += 1;
			}
		} finally {
			stream.close();
		}
	}
	
}
