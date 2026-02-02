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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Listens to a directory and passes occurring filenames to the receiver.
 * If a file named {@value TRIGGER_SHUTDOWN_FILENAME} appears the process
 * is closed.
 * Keep bug @see <a href="https://bugs.openjdk.org/browse/JDK-8202759">JDK-8202759</a>
 * in mind: if files occur too fast the files may be missed by the watcher.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("Listens to a directory and passes occurring filenames to the receiver. " +
        "If a file named 'shutdownEtlNow' appears the process " +
        "is closed." +
        "Keep bug https://bugs.openjdk.org/browse/JDK-8202759 " +
        "in mind: if files occur too fast the files may be missed by the watcher.")
@In(String.class)
@Out(String.class)
@FluxCommand("listen-directory")
public final class DirectoryListener extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    /* This special filename triggers the end of listing and closes the module */
    public static final String TRIGGER_SHUTDOWN_FILENAME = "shutdownEtlNow";
    private static final WatchService WATCHER;

    static {
        try {
            WATCHER = FileSystems.getDefault().newWatchService();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<WatchKey, Path> KEYS = new HashMap<>();

    /**
     * Creates an instance of {@link DirectoryListener} if no IOException occurs.
     */
    public DirectoryListener() {
    }

    @Override
    public void process(final String directory) {

        final Path dir = Path.of(directory);
        try {
            registerAll(dir);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        start(directory);
    }

    private void start(final String directory) {
        final DirectoryWatcher directoryWatcher = new DirectoryWatcher();
        directoryWatcher.setDirectory(directory);
        final Thread thread = new Thread(directoryWatcher);
        thread.start();
    }

    /**
     * Register the given directory with the WatchService.
     *
     * @param dir the directory to register
     */
    private void register(final Path dir) throws IOException {
        final WatchKey key = dir.register(WATCHER, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE, java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY);
        System.out.println("Add directory to watch: " + dir);
        KEYS.put(key, dir);
    }

    /**
     * Register the given directory, and all its subdirectories, with the
     * WatchService.
     *
     * @param start root directory for registering all (sub)directories
     */
    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    final class DirectoryWatcher implements Runnable {
        private String directory;

        DirectoryWatcher() {
        }

        private void setDirectory(final String directory) {
            this.directory = directory;
        }

        public void run() {

            while (true) {
                final WatchKey key;
                try {
                    key = WATCHER.take();
                }
                catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                final Path dir = KEYS.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!");
                    continue;
                }

                for (final WatchEvent<?> event : key.pollEvents()) {
                    // an OVERFLOW event can occur if events are lost or discarded
                    if (event.kind() == java.nio.file.StandardWatchEventKinds.OVERFLOW) {
                        throw new OpenFailed("Overflow event occurred on directory " + directory);
                    }
                    System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");

                    @SuppressWarnings("unchecked")
                    final Path fileName = ((WatchEvent<Path>) event).context();
                    final Path absolutePath = dir.resolve(fileName);

                    processFile(fileName, absolutePath);
                }
                // reset key and remove from set if directory no longer accessible
                final boolean valid = key.reset();
                if (!valid) {
                    KEYS.remove(key);
                    // all directories are inaccessible
                    if (KEYS.isEmpty()) {
                        break;
                    }
                }
            }
        }

        private void processFile(final Path fileName, final Path absolutePath) {
            if (Files.isDirectory(absolutePath, LinkOption.NOFOLLOW_LINKS)) {
                try {
                    registerAll(absolutePath);
                }
                catch (final IOException e) {
                    throw new OpenFailed("IOException event occurred on directory " + directory, e);
                }
            }
            else {
                if (fileName.toString().equals(TRIGGER_SHUTDOWN_FILENAME)) {
                    closeStream();
                    Thread.currentThread().interrupt();
                }
                else {
                    getReceiver().process(absolutePath.toString());
                }
            }
        }
    }
}
