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

package org.metafacture.triples;

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.objects.Triple;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author markus geipel
 *
 */
public final class SortedTripleFileFacade {
    public static final int BUFFERSIZE = 2048;
    private final ObjectInputStream in;
    private final File file;
    private Triple triple;
    private boolean empty;

    /**
     * Constructs a SortedTripleFileFacade with a file. Reads a Triple from the
     * file.
     *
     * @param file the File to load Triples from
     * @throws IOException if Triple can't be loaded
     */
    public SortedTripleFileFacade(final File file) throws IOException {
        this.file = file;
        in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), BUFFERSIZE));
        next();
    }

    public boolean isEmpty() {
        return empty;
    }

    private void next() throws IOException {
        try {
            triple = Triple.read(in);
            empty = false;
        }
        catch (final EOFException e) {
            empty = true;
            triple = null;
        }
    }

    /**
     * Closes the {@link ObjectInputStream} and deletes the {@link #file} if it
     * exists.
     */
    public void close() {
        try {
            in.close();
        }
        catch (final IOException e) {
            throw new MetafactureException("Error closing input stream", e);
        }
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Peeks at a Triple at the top of the stack.
     *
     * @return the Triple at the top of the stack
     */
    public Triple peek() {
        if (isEmpty()) {
            return null;
        }
        return triple;
    }

    /**
     * Pops a Triple from the stack.
     *
     * @return the Triple at the top of the stack.
     * @throws IOException if the Triple can't be loaded
     */
    public Triple pop() throws IOException {
        final Triple nextTriple = peek();
        next();
        return nextTriple;
    }

}
