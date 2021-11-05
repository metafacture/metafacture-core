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

package org.metafacture.metamorph.maps;

import org.metafacture.metamorph.api.MorphExecutionException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * Provides an unmodifiable {@link Map} based on a file. The file is supposed to
 * be UTF-8 encoded. The default separator is {@code \t}.
 * <strong>Important:</strong> Lines that are not split in two parts by the
 * separator are ignored!
 *
 * @author Markus Michael Geipel
 */
public final class FileMap extends HashMap<String, String> {

    private  Map<String, String> unmodifiableMap;

    private Pattern split = Pattern.compile("\t", Pattern.LITERAL);

    /**
     * Creates an instance of {@link FileMap}.
     */
    public FileMap() {
    }

    /**
     * Sets a comma separated list of files which are then passed to
     * {@link #setFile}.
     *
     * @param files a comma separated list of files
     */
    public void setFiles(final String files) {
        final String[] parts = files.split("\\s*,\\s*");
        for (final String part : parts) {
            setFile(part);
        }
    }

    /**
     * Provides a {@link Map} based on a file. The file is supposed to be UTF-8
     * encoded. The default separator is {@code \t}. <strong>Important:</strong>
     * Lines that are not split in two parts by the separator are ignored!
     *
     * @param file the file
     */
    public void setFile(final String file) {
        final Map<String, String> modifiableMap = new HashMap<>();
        try (
                InputStream stream = openStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                final String[] parts = split.split(line);
                if (parts.length == 2) {
                    modifiableMap.put(parts[0], parts[1]);
                }
            }
        }
        catch (final IOException | UncheckedIOException e) {
            throw new MorphExecutionException("filemap: cannot read map file", e);
        }
        unmodifiableMap = Collections.unmodifiableMap(modifiableMap);
    }

    private InputStream openStream(final String file) {
        return openAsFile(file)
                .orElseGet(() -> openAsResource(file)
                        .orElseGet(() -> openAsUrl(file)
                                .orElseThrow(() -> new MorphExecutionException(
                                        "File not found: " + file))));
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
     * <strong>Default value: {@code \t} </strong>
     *
     * @param delimiter the separator
     */
    public void setSeparator(final String delimiter) {
        split = Pattern.compile(delimiter, Pattern.LITERAL);
    }

    @Override
    public String get(final Object key) {
        return unmodifiableMap.get(key);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> s = unmodifiableMap.entrySet();
        s.clear();
        return unmodifiableMap.entrySet();
    }

}
