/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

/**
 * Various utility methods for working with files, resources and streams.
 *
 * @author Christoph Böhme
 * @author Markus Michael Geipel
 *
 */
public final class ResourceUtil { // checkstyle-disable-line ClassDataAbstractionCoupling

    static final int BUFFER_SIZE = 4096;

    private ResourceUtil() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * First attempts to open a file with the provided name. On fail attempts to
     * open a resource identified by the name. On fail attempts to open a URL
     * identified by the name.
     *
     * @param name name of the file, resource or the URL to open
     * @return an input stream for reading the opened file, resource or URL
     * @throws FileNotFoundException if all attempts fail
     */
    public static InputStream getStream(final String name)
            throws FileNotFoundException {
        if (name == null) {
            throw new IllegalArgumentException("'name' must not be null");
        }
        final File file = new File(name);
        if (file.exists()) {
            return getStream(file);
        }

        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        if (stream == null) {
            try {
                stream = new URL(name).openStream();
            }
            catch (final IOException e) {
                throwFileNotFoundException(name, e);
            }
            if (stream == null) {
                throwFileNotFoundException(name, null);
            }
        }

        return stream;

    }

    /**
     * Gets an InputStream of a File.
     *
     * @param file the File.
     * @return the InputStream
     * @throws FileNotFoundException if the File couldn't be found
     */
    public static InputStream getStream(final File file)
            throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private static void throwFileNotFoundException(final String name,
            final Throwable t) throws FileNotFoundException {
        final FileNotFoundException e = new FileNotFoundException(
                "No file, resource or URL found: " + name);
        if (t != null) {
            e.initCause(t);
        }
        throw e;
    }

    /**
     * Gets a Reader. First attempts to open a file. On fail attempts to open the
     * resource with name. On fail attempts to open name as a URL.
     *
     * @param name the name of the resource
     * @return the Reader
     * @throws FileNotFoundException if the File couldn't be found
     */
    public static Reader getReader(final String name)
            throws FileNotFoundException {
        return new InputStreamReader(getStream(name));
    }

    /**
     * Gets a Reader from a File.
     *
     * @param file the File
     * @return the Reader
     * @throws FileNotFoundException if the File couldn't be found
     */
    public static Reader getReader(final File file) throws FileNotFoundException {
        return new InputStreamReader(getStream(file));
    }

    /**
     * Gets a Reader. First attempts to open a file. On fail attempts to open the
     * resource with name. On fail attempts to open name as a URL. Uses the given
     * {@link java.nio.charset.Charset charset} as encoding.
     *
     * @param name     the name of the resource
     * @param encoding the Charset
     * @return the Reader
     * @throws IOException if an I/O error occurs
     */
    public static Reader getReader(final String name, final String encoding)
            throws IOException {
        return new InputStreamReader(getStream(name), encoding);
    }

    /**
     * Gets a Reader from a File using {@link java.nio.charset.Charset charset}.
     *
     * @param file     the File
     * @param encoding the Charset
     * @return the Reader
     * @throws IOException if an I/O error occurs
     */
    public static Reader getReader(final File file, final String encoding)
            throws IOException {
        return new InputStreamReader(getStream(file), encoding);
    }

    /**
     * Attempts to return a URL for a file or resource. First, it is
     * checked whether {@code name} refers to an existing local file.
     * If not, the context class loader of the current thread is asked
     * if {@code name} refers to a resource. Finally, the method
     * attempts to interpret {@code name} as a URL.
     *
     * @param name reference to a file or resource maybe a URL
     * @return a URL referring to a file or resource
     * @throws MalformedURLException if the name is a failed URL
     */
    public static URL getUrl(final String name) throws MalformedURLException {
        final File file = new File(name);
        if (file.exists()) {
            return getUrl(file);
        }

        final URL resourceUrl =
                Thread.currentThread().getContextClassLoader().getResource(name);
        return resourceUrl != null ? resourceUrl : new URL(name);
    }

    /**
     * Gets an URL of a File.
     *
     * @param file the File
     * @return the URL
     * @throws MalformedURLException if malformed URL has occurred
     */
    public static URL getUrl(final File file) throws MalformedURLException {
        return file.toURI().toURL();
    }

    /**
     * Creates Properties based upon a location. First attempts to open a file. On
     * fail attempts to open the resource with name. On fail attempts to open name
     * as a URL.
     *
     * @param location the location of the resource
     * @return the Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties loadProperties(final String location)
            throws IOException {
        try (InputStream stream = getStream(location)) {
            return loadProperties(stream);
        }
    }

    /**
     * Loads properties from an InputStream.
     *
     * @param stream properties as InputStream
     * @return the Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties loadProperties(final InputStream stream)
            throws IOException {
        final Properties properties;
        properties = new Properties();
        properties.load(stream);
        return properties;
    }

    /**
     * Loads properties from a URL.
     *
     * @param url properties as URL
     * @return the Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties loadProperties(final URL url) throws IOException {
        return loadProperties(url.openStream());
    }

    /**
     * Loads a text file.
     *
     * @param location the filename
     * @return the content of the file
     * @throws IOException if an I/O error occurs
     */
    public static String loadTextFile(final String location) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(getReader(location));

        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }

        return builder.toString();
    }

    /**
     * * Loads a text file.
     *
     * @param location the filename
     * @param list a List of Strings to append the lines of the file to
     * @return the List of Strings with the lines of the file appended
     * @throws IOException if an I/O error occurs
     */
    public static List<String> loadTextFile(final String location,
            final List<String> list) throws IOException {
        final BufferedReader reader = new BufferedReader(getReader(location));

        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }

        return list;
    }

    /**
     * Reads an InputStream with the given Charset.
     *
     * @param inputStream the InputStream
     * @param encoding    the Charset
     * @return a String of the content of the InputStream
     * @throws IOException if an I/O error occurs
     */
    public static String readAll(final InputStream inputStream, final Charset encoding)
            throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, encoding)) {
            return readAll(reader);
        }
    }

    /**
     * Reads a Reader.
     *
     * @param reader the Reader
     * @return a String of the content of the Reader
     * @throws IOException if an I/O error occurs
     */
    public static String readAll(final Reader reader) throws IOException {
        final StringBuilder loadedText = new StringBuilder();
        try (Reader bufferedReader = new BufferedReader(reader)) {
            final CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);
            while (bufferedReader.read(buffer) > -1) {
                buffer.flip();
                loadedText.append(buffer);
                buffer.clear();
            }
            return loadedText.toString();
        }
    }

}
