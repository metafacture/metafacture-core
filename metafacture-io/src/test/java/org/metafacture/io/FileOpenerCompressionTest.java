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

package org.metafacture.io;

import org.metafacture.framework.ObjectReceiver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

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

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectReceiver<Reader> receiver;

    private final String resourcePath;
    private final FileCompression compression;

    public FileOpenerCompressionTest(final String resourcePath, final FileCompression compression) {
        this.resourcePath = resourcePath;
        this.compression = compression;
    }

    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"compressed.txt", FileCompression.AUTO},
                {"compressed.txt.bgzf", FileCompression.AUTO},
                {"compressed.txt.bz2", FileCompression.AUTO},
                {"compressed.txt.bzip2", FileCompression.AUTO},
                {"compressed.txt.gz", FileCompression.AUTO},
                {"compressed.txt.gzip", FileCompression.AUTO},
                {"compressed.txt.xz", FileCompression.AUTO},
                {"compressed.txt", FileCompression.NONE},
                {"compressed.txt.bz2", FileCompression.BZIP2},
                {"compressed.txt.bzip2", FileCompression.BZIP2},
                {"compressed.txt.bgzf", FileCompression.GZIP},
                {"compressed.txt.gz", FileCompression.GZIP},
                {"compressed.txt.gzip", FileCompression.GZIP},
                {"compressed.txt.xz", FileCompression.XZ}
            });
    }

    @Test
    public void testOpenCompressedFiles() throws IOException {
        final File file = tempFolder.newFile();

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        TestHelpers.assertFile(receiver, DATA, file, o -> o.setCompression(compression));
    }

}
