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
package org.culturegraph.mf.metamorph.maps;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.culturegraph.mf.metamorph.api.MorphExecutionException;
import org.culturegraph.mf.metamorph.api.helpers.AbstractReadOnlyMap;

/**
 * Provides a {@link Map} based on a file. The file is supposed to be UTF-8
 * encoded. The separator is by default \t. <strong>Important:</strong> Lines
 * that are not split in two parts by the separator are ignored!
 *
 * @author Markus Michael Geipel
 */
public final class FileMap extends AbstractReadOnlyMap<String, String> {

	private final Map<String, String> map = new HashMap<String, String>();

	private Pattern split = Pattern.compile("\t", Pattern.LITERAL);

	public void setFiles(final String files) {
		final String[] parts = files.split("\\s*,\\s*");
		for (final String part : parts) {
			setFile(part);
		}
	}

	public void setFile(final String file) {
		try (
				final InputStream stream = openStream(file);
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream, StandardCharsets.UTF_8))
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				final String[] parts = split.split(line);
				if (parts.length == 2) {
					map.put(parts[0], parts[1]);
				}
			}
		} catch (final IOException | UncheckedIOException e) {
			throw new MorphExecutionException("filemap: cannot read map file", e);
		}
	}

	private InputStream openStream(String file) {
		return openAsFile(file)
				.orElseGet(() -> openAsResource(file)
						.orElseGet(() -> openAsUrl(file)
								.orElseThrow(() -> new MorphExecutionException(
										"File not found: " + file))));
	}

	private Optional<InputStream> openAsFile(String file) {
		try {
			return Optional.of(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return Optional.empty();
		}
	}

	private Optional<InputStream> openAsResource(String file) {
		return Optional.ofNullable(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream(file));
	}

	private Optional<InputStream> openAsUrl(String file) {
		final URL url;
		try {
			url = new URL(file);
		} catch (MalformedURLException e) {
			return Optional.empty();
		}
		try {
			return Optional.of(url.openStream());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void setSeparator(final String delimiter) {
		split = Pattern.compile(delimiter, Pattern.LITERAL);
	}

	@Override
	public String get(final Object key) {
		return map.get(key);
	}

}
