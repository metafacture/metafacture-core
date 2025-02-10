/*
 * Copyright 2013, 2014, 2021 Deutsche Nationalbibliothek et al
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

package org.metafacture.metamorph.maps;

import org.metafacture.io.FileOpener;
import org.metafacture.metamorph.api.MorphExecutionException;
import org.metafacture.metamorph.api.helpers.AbstractReadOnlyMap;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a {@link Map} based on files. Can be a single file or a
 * comma-separated list of files.
 *
 * The default {@link #setEncoding encoding} is UTF-8.
 * The default {@link #setSeparator separator} is {@code \t}.
 * The default {@link #setKeyColumn keyColumn} is {@code 0}.
 * The default {@link #setValueColumn valueColumn} is {@code 1}.
 *
 * <p>By setting {@link #setAllowEmptyValues allowEmptyValues} to {@code true},
 * the values in the {@link Map} can be empty; thus enabling e.g.
 * {@link org.metafacture.metamorph.functions.SetReplace} to remove matching
 * keys.
 *
 * <p>By setting {@link #setExpectedColumns expectedColumns} to
 * {@code -1}, the number of columns is not checked.
 *
 * <p><strong>Important:</strong> Otherwise, all lines that are not split into
 * the expected number of parts by the separator are ignored!
 *
 * @author Markus Michael Geipel
 */
public final class FileMap extends AbstractReadOnlyMap<String, String> implements Closeable {

    private final FileOpener fileOpener = new FileOpener();
    private final Map<String, String> map = new HashMap<>();

    private ArrayList<String> filenames = new ArrayList<>();
    private Matcher ignoreMatcher;
    private Pattern split = Pattern.compile("\t", Pattern.LITERAL);
    private boolean allowEmptyValues;
    private boolean isUninitialized = true;
    private int expectedColumns;
    private int keyColumn;
    private int valueColumn = 1;

    /**
     * Creates an instance of {@link FileMap}.
     */
    public FileMap() {
    }

    private void init() {
        loadFiles();
        isUninitialized = false;
    }

    /**
     * Sets whether to allow empty values in the {@link Map} or ignore these
     * entries.
     *
     * <strong>Default value: false</strong>
     *
     * @param allowEmptyValues true if empty values in the Map are allowed
     */
    public void setAllowEmptyValues(final boolean allowEmptyValues) {
        this.allowEmptyValues = allowEmptyValues;
    }

    /**
     * Sets number of expected columns; lines with different number of columns
     * are ignored. Set to {@code -1} to disable the check and allow arbitrary
     * number of columns.
     *
     * <strong>Default value: calculated from {@link #setKeyColumn key} and
     * {@link #setValueColumn value} columns</strong>
     *
     * @param expectedColumns number of expected columns
     */
    public void setExpectedColumns(final int expectedColumns) {
        this.expectedColumns = expectedColumns;
    }

    /**
     * Sets the pattern which determines whether a line should be ignored.
     *
     * @param ignorePattern a Java regular expression
     */
    public void setIgnorePattern(final String ignorePattern) {
        this.ignoreMatcher = Pattern.compile(ignorePattern).matcher("");
    }

    /**
     * Sets a comma separated list of files which provides the {@link Map}.
     *
     * @param files a comma separated list of files
     */
    public void setFiles(final String files) {
        Collections.addAll(filenames, files.split("\\s*,\\s*"));
    }

    /**
     * Sets a file which provides the {@link Map}.
     * @param file the file
     */
    public void setFile(final String file) {
        Collections.addAll(filenames, file);
    }

    /**
     * Sets the encoding used to open the resource.
     *
     * @param encoding new encoding
     */
    public void setEncoding(final String encoding) {
        fileOpener.setEncoding(encoding);
    }

    /**
     * Sets the compression of the file.
     *
     * @param compression the name of the compression
     */
    public void setCompression(final String compression) {
        fileOpener.setCompression(compression);
    }

    /**
     * Flags whether to use decompress concatenated file compression.
     *
     * @param decompressConcatenated true if file compression should decompress concatenated
     */
    public void setDecompressConcatenated(final boolean decompressConcatenated) {
        fileOpener.setDecompressConcatenated(decompressConcatenated);
    }

    private void loadFiles() {
        filenames.forEach(this::loadFile);
    }

    private void loadFile(final String file) {
        try (
                InputStream stream = openStream(file);
                Reader reader = fileOpener.open(stream);
                BufferedReader br = new BufferedReader(reader)
        ) {
            final int minColumns = Math.max(keyColumn, valueColumn) + 1;
            final int expColumns = expectedColumns != 0 ? expectedColumns : minColumns;

            String line;
            while ((line = br.readLine()) != null) {
                if (ignore(line)) {
                    continue;
                }

                final String[] parts = allowEmptyValues ? split.split(line, -1) : split.split(line);
                if (ignore(parts.length, minColumns, expColumns)) {
                    continue;
                }

                map.put(parts[keyColumn], parts[valueColumn]);
            }
        }
        catch (final IOException | UncheckedIOException e) {
            throw new MorphExecutionException("filemap: cannot read map file", e);
        }
    }

    private boolean ignore(final String line) {
        return line.isEmpty() || ignoreMatcher != null && ignoreMatcher.reset(line).matches();
    }

    private boolean ignore(final int partsLength, final int minColumns, final int expColumns) {
        return partsLength < minColumns || expColumns > 0 && partsLength != expColumns;
    }

    private InputStream openStream(final String file) {
        return openAsFile(file)
            .orElseGet(() -> openAsResource(file)
                .orElseGet(() -> openAsUrl(file)
                    .orElseThrow(() -> new MorphExecutionException("File not found: " + file))));
    }

    private Optional<InputStream> openAsFile(final String file) {
        try {
            return Optional.of(new FileInputStream(file));
        }
        catch (final FileNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<InputStream> openAsResource(final String file) {
        return Optional.ofNullable(Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(file));
    }

    private Optional<InputStream> openAsUrl(final String file) {
        final URL url;
        try {
            url = new URL(file);
        }
        catch (final MalformedURLException e) {
            return Optional.empty();
        }
        try {
            return Optional.of(url.openStream());
        }
        catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Sets the separator.
     *
     * <strong>Default value: {@code \t}</strong>
     *
     * @param delimiter the separator
     */
    public void setSeparator(final String delimiter) {
        split = Pattern.compile(delimiter, Pattern.LITERAL);
    }

    /**
     * Sets the key column (0-based).
     *
     * <strong>Default value: {@code 0}</strong>
     *
     * @param keyColumn the key column
     */
    public void setKeyColumn(final int keyColumn) {
        this.keyColumn = keyColumn;
    }

    /**
     * Sets the value column (0-based).
     *
     * <strong>Default value: {@code 1}</strong>
     *
     * @param valueColumn the value column
     */
    public void setValueColumn(final int valueColumn) {
        this.valueColumn = valueColumn;
    }

    @Override
    public String get(final Object key) {
        if (isUninitialized) {
            init();
        }
        return map.get(key);
    }

    @Override
    public Set<String> keySet() {
        if (isUninitialized) {
            init();
        }
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public void close() throws IOException {
        map.clear();
        fileOpener.closeStream();
    }
}
