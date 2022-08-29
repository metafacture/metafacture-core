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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import org.apache.commons.io.input.BOMInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Opens a file and passes a reader for it to the receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Opens a file.")
@In(String.class)
@Out(Reader.class)
@FluxCommand("open-file")
public final class FileOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private String encoding = "UTF-8";
    private FileCompression compression = FileCompression.AUTO;
    private boolean decompressConcatenated = FileCompression.DEFAULT_DECOMPRESS_CONCATENATED;

    /**
     * Creates an instance of {@link FileOpener}.
     */
    public FileOpener() {
    }

    /**
     * Returns the encoding used to open the resource.
     *
     * @return current default setting
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding used to open the resource.
     *
     * @param encoding new encoding
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the file compression.
     *
     * @return the {@link FileCompression}
     */
    public FileCompression getCompression() {
        return compression;
    }

    /**
     * Sets the compression of the file.
     *
     * @param compression the {@link FileCompression}
     */
    public void setCompression(final FileCompression compression) {
        this.compression = compression;
    }

    /**
     * Sets the compression of the file.
     *
     * @param compression the name of the compression
     */
    public void setCompression(final String compression) {
        setCompression(FileCompression.valueOf(compression.toUpperCase()));
    }

    /**
     * Checks whether the file compression is set to decompress concatenated.
     *
     * @return true if file compression should be decompresses concatenated
     */
    public boolean getDecompressConcatenated() {
        return decompressConcatenated;
    }

    /**
     * Flags whether to use decompress concatenated file compression.
     *
     * @param decompressConcatenated true if file compression should decompress concatenated
     */
    public void setDecompressConcatenated(final boolean decompressConcatenated) {
        this.decompressConcatenated = decompressConcatenated;
    }

    /**
     * Opens a file.
     *
     * @param file the file
     * @return a Reader
     * @throws IOException if an I/O error occurs
     */
    public Reader open(final String file) throws IOException {
        return open(new FileInputStream(file));
    }

    /**
     * Opens a file stream.
     *
     * @param stream the stream
     * @return a Reader
     * @throws IOException if an I/O error occurs
     */
    public Reader open(final InputStream stream) throws IOException {
        try {
            final InputStream decompressor = compression.createDecompressor(stream, decompressConcatenated);
            try {
                return new InputStreamReader(new BOMInputStream(decompressor), encoding);
            }
            catch (final IOException | MetafactureException e) {
                decompressor.close();
                throw e;
            }
        }
        catch (final IOException | MetafactureException e) {
            stream.close();
            throw e;
        }
    }

    @Override
    public void process(final String file) {
        try {
            getReceiver().process(open(file));
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

}
