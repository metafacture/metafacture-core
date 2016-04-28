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
package org.culturegraph.mf.stream.source;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.stream.pipe.ObjectBuffer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests {@link FileOpener}
 *
 * @author ChristophBöhme
 *
 */
public final class FileOpenerTest {

	private static final String DATA = "Überfacture";

	private static final String UTF8_MESSAGE =
			"Default encoding is UTF-8: It is not possible to test " +
			"whether FileOpener sets the encoding to UTF-8 correctly.";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testUtf8IsDefaultEncoding() throws IOException {
		final Charset charsetUTF8 = Charset.forName("UTF-8");

		assumeThat(UTF8_MESSAGE, Charset.defaultCharset(), not(equalTo(charsetUTF8)));

		final File file = tempFolder.newFile();

		final OutputStream stream = new FileOutputStream(file);
		try { stream.write(DATA.getBytes(charsetUTF8)); }
		finally { stream.close(); }

		final FileOpener opener = new FileOpener();
		final ObjectBuffer<Reader> buffer = new ObjectBuffer<Reader>();
		opener.setReceiver(buffer);
		opener.process(file.getAbsolutePath());
		opener.closeStream();

		final Reader reader = buffer.pop();
		final String charsFromFile;
		try { charsFromFile = IOUtils.toString(reader); }
		finally { reader.close(); }

		assertEquals(DATA, charsFromFile);
	}

}
