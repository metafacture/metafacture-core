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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 *
 * Tests for file compression in class {@link ObjectFileWriter}.
 *
 * @author Christoph Böhme
 *
 */
@RunWith(Parameterized.class)
public final class ObjectFileWriterCompressionTest {

	private static final String DATA = "This could have been a remarkable sentence.";

	private static final String FILENAME_NONE = "compressed.txt";
	private static final String FILENAME_BZ2 = "compressed.txt.bz2";
	private static final String FILENAME_BZIP2 = "compressed.txt.bzip2";
	private static final String FILENAME_GZ = "compressed.txt.gz";
	private static final String FILENAME_GZIP = "compressed.txt.gzip";
	private static final String FILENAME_XZ = "compressed.txt.xz";

	private static final byte[] MAGIC_BYTES_NONE = { 'T', 'h', 'i', 's' };
	private static final byte[] MAGIC_BYTES_BZIP2 = { 'B', 'Z', 'h' };
	private static final byte[] MAGIC_BYTES_GZIP = { (byte)0x1f, (byte)0x8b };
	private static final byte[] MAGIC_BYTES_XZ = { (byte)0xfd, '7', 'z', 'X', 'Z', (byte)0x00 };

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final String fileName;
	private final FileCompression compression;
	private final byte[] magicBytes;

	public ObjectFileWriterCompressionTest(final String fileName, final FileCompression compression,
			final byte[] magicBytes) {
		this.fileName = fileName;
		this.compression = compression;
		this.magicBytes = magicBytes;
	}

	@Parameters
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ FILENAME_NONE, FileCompression.AUTO, MAGIC_BYTES_NONE },
				{ FILENAME_BZ2, FileCompression.AUTO, MAGIC_BYTES_BZIP2 },
				{ FILENAME_BZIP2, FileCompression.AUTO, MAGIC_BYTES_BZIP2 },
				{ FILENAME_GZ, FileCompression.AUTO, MAGIC_BYTES_GZIP },
				{ FILENAME_GZIP, FileCompression.AUTO, MAGIC_BYTES_GZIP },
				{ FILENAME_XZ, FileCompression.AUTO, MAGIC_BYTES_XZ },
				{ FILENAME_NONE, FileCompression.NONE, MAGIC_BYTES_NONE },
				{ FILENAME_BZ2, FileCompression.BZIP2, MAGIC_BYTES_BZIP2 },
				{ FILENAME_GZ, FileCompression.GZIP, MAGIC_BYTES_GZIP },
				{ FILENAME_XZ, FileCompression.XZ, MAGIC_BYTES_XZ },
			});
	}

	@Test
	public void shouldWriteCompressedFiles() throws IOException {
		// This test only looks at the magic byte sequences in the
		// files to decide whether a compressed file was written.

		final File file = tempFolder.newFile(fileName);

		final ObjectFileWriter<String> writer = new ObjectFileWriter<String>(file.getAbsolutePath());
		writer.setCompression(compression);
		writer.process(DATA);
		writer.closeStream();

		assertArrayEquals(magicBytes, readMagicBytes(file, magicBytes.length));
	}

	private byte[] readMagicBytes(final File file, final int magicBytesLength)
			throws IOException {
		final byte[] fileContents = Files.readAllBytes(file.toPath());
		return Arrays.copyOf(fileContents, magicBytesLength);
	}

}
