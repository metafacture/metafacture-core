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
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T>
 *            object type
 *
 * @author Markus Geipel
 * @author Christoph Böhme
 *
 */
@Description("Writes objects to one (or more) file(s)")
@In(Object.class)
@Out(Void.class)
@FluxCommand("write-files")
public final class ObjectFileWriter<T> extends AbstractObjectWriter<T>  {

    private static final String VAR = "${i}";
    private static final Pattern VAR_PATTERN = Pattern.compile(VAR, Pattern.LITERAL);

    private String path;
    private int count;
    private Writer writer;
    private boolean appendIfFileExists;
    private boolean firstObject = true;
    private boolean closed;

    private String encoding = "UTF-8";
    private FileCompression compression = FileCompression.AUTO;

    /**
     * Sets the destination of a file to write objects to.
     *
     * @param path the path to be written to
     */
    public ObjectFileWriter(final String path) {
        this.path = path;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    @Override
    public FileCompression getCompression() {
        return compression;
    }

    @Override
    public void setCompression(final FileCompression compression) {
        this.compression = compression;
    }

    @Override
    public void setCompression(final String compression) {
        setCompression(FileCompression.valueOf(compression.toUpperCase()));
    }

    @Override
    public void process(final T obj) {
        assert !closed;
        final String objStr = obj.toString();
        if (!objStr.isEmpty()) {
            try {
                if (firstObject) {
                    getWriter().write(getHeader());
                    firstObject = false;
                }
                else {
                    getWriter().write(getSeparator());
                }
                getWriter().write(objStr);
            }
            catch (final IOException e) {
                throw new MetafactureException(e);
            }
        }
    }

    @Override
    public void resetStream() {
        closeStream();
        ++count;
        startNewFile();
    }

    @Override
    public void closeStream() {
        if (!closed) {
            try {
                if (!firstObject) {
                    getWriter().write(getFooter());
                }
                getWriter().close();
            }
            catch (final IOException e) {
                throw new MetafactureException(e);
            }
            finally {
                closed = true;
            }
        }
    }

    /**
     * Controls whether to open files in append mode if they exist.
     * <p>
     * The default value is {@code false}.
     * <p>
     * This property can be changed anytime during processing. It becomes
     * effective the next time a new output file is opened.
     *
     * @param appendIfFileExists true if new data should be appended,
     *                           false to overwrite the existing file
     */
    public void setAppendIfFileExists(final boolean appendIfFileExists) {
        this.appendIfFileExists = appendIfFileExists;
    }

    private void startNewFile() {
        final Matcher matcher = VAR_PATTERN.matcher(this.path);
        final String currentPath = matcher.replaceAll(String.valueOf(count));
        try {
            final OutputStream file = new FileOutputStream(currentPath, appendIfFileExists);
            try {
                final OutputStream compressor = compression.createCompressor(file, currentPath);
                try {
                    writer = new OutputStreamWriter(compressor, encoding);
                    firstObject = true;
                    closed = false;
                }
                catch (final IOException e) {
                    compressor.close();
                    throw e;
                }
            }
            catch (final IOException e) {
                file.close();
                throw e;
            }
        }
        catch (final IOException e) {
            throw new MetafactureException("Error creating file '" + currentPath + "'.", e);
        }
    }

    private Writer getWriter() {
        if (writer == null) {
            startNewFile();

            final Matcher matcher = VAR_PATTERN.matcher(this.path);
            if (!matcher.find()) {
                this.path = this.path + VAR;
            }
        }

        return writer;
    }

}
