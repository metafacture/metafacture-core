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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;

/**
 * Tests for class {@link DirectoryListener}.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
public final class DirectoryListenerTest {

    private static final String DIRECTORY_NAME = "tmp";
    private static final String FILE_NAME = "test";
    private static final DirectoryListener dl = new DirectoryListener();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private ObjectReceiver<String> receiver;

    public DirectoryListenerTest() {
    }

    @Test
    public void testFileOccurs() throws IOException {
        final File file = tempFolder.newFile("pchbz");
        dl.setReceiver(receiver);
        dl.process("/tmp");
    }


    private File createTestFile() throws IOException {
        final File file = tempFolder.newFile();
        file.createNewFile();
        return file;
    }

    private File copyResourceToTempFile(final String resourcePath) throws IOException {
        final File file = tempFolder.newFile(FILE_NAME);
        return file;
    }

}
