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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link FileOpener}.
 *
 * @author Christoph Böhme
 *
 */
public final class FileOpenerTest {

    private static final String DATA = "Überfacture";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private ObjectReceiver<Reader> receiver;

    @Captor
    private ArgumentCaptor<Reader> processedObject;

    @Test
    public void testUtf8IsDefaultEncoding() throws IOException {
        assumeFalse("Default encoding is UTF-8: It is not possible to test whether " +
                        "FileOpener sets the encoding to UTF-8 correctly.",
                StandardCharsets.UTF_8.equals(Charset.defaultCharset()));

        final File testFile = createTestFile();

        final FileOpener opener = new FileOpener();
        opener.setReceiver(receiver);
        opener.process(testFile.getAbsolutePath());
        opener.closeStream();

        verify(receiver).process(processedObject.capture());
        assertEquals(DATA, ResourceUtil.readAll(processedObject.getValue()));
    }

    @Test
    public void testNoDecompressConcatenated() throws IOException {
        testDecompressConcatenated(false);
    }

    @Test
    public void testDecompressConcatenated() throws IOException {
        testDecompressConcatenated(true);
    }

    private void testDecompressConcatenated(final boolean decompressConcatenated) throws IOException {
        final int maxBytes = (int) Math.pow(2, 16);  // BGZF max compressed block size
        final StringBuilder sb = new StringBuilder();

        try (InputStreamReader r = new InputStreamReader(getClass().getResourceAsStream("compressed.txt"))) {
            final String data = ResourceUtil.readAll(r);
            for (int i = 0; i < 1525; i++) {
                sb.append(data).append("\n");
            }
        }

        final String data = sb.toString();
        assertTrue(data.length() + " > " + maxBytes, data.length() > maxBytes);

        final File testFile = copyResourceToTempFile("compressed-large.txt.bgzf");

        final FileOpener opener = new FileOpener();
        opener.setDecompressConcatenated(decompressConcatenated);
        opener.setReceiver(receiver);
        opener.process(testFile.getAbsolutePath());
        opener.closeStream();

        verify(receiver).process(processedObject.capture());
        assertEquals(decompressConcatenated ? data : data.substring(0, maxBytes),
                ResourceUtil.readAll(processedObject.getValue()));
    }

    private File createTestFile() throws IOException {
        final File file = tempFolder.newFile();
        try (OutputStream stream = new FileOutputStream(file)) {
            stream.write(DATA.getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }

    private File copyResourceToTempFile(final String resourcePath) throws IOException {
        final File file = tempFolder.newFile();
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

}
