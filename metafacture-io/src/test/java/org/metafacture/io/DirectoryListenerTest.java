/*
 * Copyright 2026, hbz
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for class {@link DirectoryListener}.
 *
 * @author Pascal Christoph (dr0i)
 */
public final class DirectoryListenerTest {

    private static final DirectoryListener DIRECTORY_LISTENER = new DirectoryListener();
    private static final int MAX_MILLISECONDS_WAITING_OF_THREAD = 3000;
    private static final String FILE_NAME = "test";
    private static final String SUBDIRECTORY_NAME = "subdir";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String pathToDirectory;
    private String pathToSubdirectory;

    @Mock
    private ObjectReceiver<String> receiver;

    public DirectoryListenerTest() {
    }

    @Before
    public void setup() {
        pathToDirectory = tempFolder.getRoot() + File.separator;
        DIRECTORY_LISTENER.setReceiver(receiver);
        DIRECTORY_LISTENER.process(pathToDirectory);
        pathToSubdirectory = pathToDirectory + SUBDIRECTORY_NAME + File.separator;
    }

    @Test
    public void testFileOccurs() {
        final String pathToTestfile = pathToDirectory + FILE_NAME;
        createFile(pathToTestfile);
        Mockito.verify(receiver, org.mockito.Mockito.timeout(MAX_MILLISECONDS_WAITING_OF_THREAD)).process(pathToTestfile);
    }

    @Test
    public void testFileOccursInSubdirectory() throws InterruptedException {
        createDirectory(pathToSubdirectory);
        final String pathToTestfile = pathToSubdirectory + FILE_NAME;
        Thread.sleep(100); // because of https://bugs.openjdk.org/browse/JDK-8202759
        createFile(pathToTestfile);
        Mockito.verify(receiver, org.mockito.Mockito.timeout(MAX_MILLISECONDS_WAITING_OF_THREAD)).process(pathToTestfile);
    }

    @Test
    public void testDontProcessDirectoryWithoutFiles() throws InterruptedException {
        final String pathToTestfile = pathToDirectory + SUBDIRECTORY_NAME;
        Thread.sleep(400); // because of https://bugs.openjdk.org/browse/JDK-8202759
        createFile(pathToTestfile);
        Mockito.verify(receiver, org.mockito.Mockito.timeout(MAX_MILLISECONDS_WAITING_OF_THREAD).times(0)).process(pathToTestfile);
    }

    @Test
    public void testTriggerShutdown() throws InterruptedException {
        final String pathToTestfile = pathToDirectory + DirectoryListener.TRIGGER_SHUTDOWN_FILENAME;
        createFile(pathToTestfile);
        Mockito.verify(receiver, org.mockito.Mockito.timeout(MAX_MILLISECONDS_WAITING_OF_THREAD).times(0)).process(
                pathToDirectory);
        Thread.sleep(100);
        Assert.assertTrue(DIRECTORY_LISTENER.isClosed());
    }

    private void createFile(final String path) {
        final File testFile = new File(path);
        try {
            testFile.createNewFile();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectory(final String dir) {
        try {
            Files.createDirectory(Path.of(dir));
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
