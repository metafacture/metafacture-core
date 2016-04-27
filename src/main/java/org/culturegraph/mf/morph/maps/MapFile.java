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
package org.culturegraph.mf.morph.maps;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.MorphException;
import org.culturegraph.mf.util.ResourceUtil;

/**
 * Provides a {@link Map} based on a file. The file is supposed to be UTF-8
 * encoded. The separator is by default \t. <strong>Important:</strong> Lines
 * that are not split in two parts by the separator are ignored!
 *
 * @author "Markus Michael Geipel"
 *
 */
public final class MapFile extends AbstractReadOnlyMap<String, String> {

	private final Map<String, String> map = new HashMap<String, String>();

	private Pattern split = Pattern.compile("\t", Pattern.LITERAL);

	public void setFiles(final String files) {
		final String[] parts = files.split("\\s*,\\s*");
		for (final String part : parts) {
			setFile(part);
		}
	}

	public void setFile(final String file) {
		final BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(
					ResourceUtil.getStream(file), "UTF-8"));

			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) {
						continue;
					}
					final String[] parts = split.split(line);
					if (parts.length == 2) {
						map.put(parts[0], parts[1]);
					}
				}

			} finally {
				reader.close();
			}

		} catch (final UnsupportedEncodingException e) {
			throw new MorphException(e);
		} catch (final FileNotFoundException e) {
			throw new MorphException("resource '" + file + "' not found", e);
		} catch (final IOException e) {
			throw new MorphException(e);
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
