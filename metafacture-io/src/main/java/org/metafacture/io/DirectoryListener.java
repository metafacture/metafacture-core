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

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * listens to a directory and passes occurring filenames to the receiver.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description("Listens on a directory. Output filenames if they occur.")
@In(String.class)
@Out(String.class)
@FluxCommand("listen-directory")
public final class DirectoryListener extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    WatchService watcher;

    public DirectoryListener(){
        try {
            watcher=FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(final String directory) {
        Path dir = Path.of(directory);

        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == OVERFLOW) {
                    throw new OpenFailed("Overflow event occured on directory " + directory);
                }

                // The filename is the context of the event.
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                try {
                    Path child = dir.resolve(filename);
                    getReceiver().process(child.toRealPath().toString()); //also resolves symbolic links
                } catch (IOException x) {
                    System.err.println(x);
                    continue;
                }

            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }


    }

}
