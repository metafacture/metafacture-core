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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.util.FileCompression;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for file compression in  class {@link FileOpener}.
 *
 * @author Christoph Böhme
 *
 */
@RunWith(Parameterized.class)
public final class FileOpenerCompressionTest {

	private static final String DATA =
			"This could have been a remarkable sentence.";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Mock
	private ObjectReceiver<Reader> receiver;

	private FileOpener fileOpener;

	private final String resourcePath;
	private final FileCompression compression;

	public FileOpenerCompressionTest(final String resourcePath,
			final FileCompression compression) {
		this.resourcePath = resourcePath;
		this.compression = compression;
	}

	@Parameters
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "compressed.txt", FileCompression.AUTO },
				{ "compressed.txt.bz2", FileCompression.AUTO },
				{ "compressed.txt.bzip2", FileCompression.AUTO },
				{ "compressed.txt.gz", FileCompression.AUTO },
				{ "compressed.txt.gzip", FileCompression.AUTO },
				{ "compressed.txt.xz", FileCompression.AUTO },
				{ "compressed.txt", FileCompression.NONE },
				{ "compressed.txt.bz2", FileCompression.BZIP2 },
				{ "compressed.txt.bzip2", FileCompression.BZIP2 },
				{ "compressed.txt.gz", FileCompression.GZIP },
				{ "compressed.txt.gzip", FileCompression.GZIP },
				{ "compressed.txt.xz", FileCompression.XZ },
			});
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		fileOpener = new FileOpener();
		fileOpener.setReceiver(receiver);
	}

	@Test
	public void testOpenCompressedFiles() throws IOException {
		final File file = copyResourceToTempFile();

		fileOpener.setCompression(compression);
		fileOpener.process(file.getAbsolutePath());

		final ArgumentCaptor<Reader> readerCaptor =
				ArgumentCaptor.forClass(Reader.class);
		verify(receiver).process(readerCaptor.capture());
		final String charsFromFile;
		try (Reader reader = readerCaptor.getValue()) {
			charsFromFile = IOUtils.toString(reader);
		}
		assertEquals(DATA, charsFromFile);
	}

	private File copyResourceToTempFile() throws IOException {
		final File file = tempFolder.newFile();
		try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
			try (OutputStream out = new FileOutputStream(file)) {
				IOUtils.copy(in, out);
			}
		}
		return file;
	}

}
