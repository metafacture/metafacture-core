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
package org.culturegraph.mf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.culturegraph.mf.exceptions.MetafactureException;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>, Markus Michael Geipel
 *
 */
public final class ResourceUtil {

	private ResourceUtil() {
		// No instances allowed
	}

	/**
	 * First attempts to open open 'name' as a file. <br/>
	 * On fail attempts to open resource with name 'name'. <br/>
	 * On fail attempts to open 'name' as a URL.
	 *
	 * @param name
	 * @return
	 * @throws FileNotFoundException
	 *             if all attempts fail
	 */
	public static InputStream getStream(final String name) throws FileNotFoundException {
		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null");
		}
		final File file = new File(name);
		if (file.exists()) {
			return getStream(file);
		}

		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		if (stream != null) {
			return stream;
		}

		try {
			stream = new URL(name).openStream();
		} catch (final MalformedURLException e) {
			throwFileNotFoundException(name, e);
		} catch (final IOException e) {
			throwFileNotFoundException(name, e);
		}
		if (stream == null) {
			throwFileNotFoundException(name, null);
		}

		return stream;

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

	public static InputStream getStream(final File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	public static Reader getReader(final String name) throws FileNotFoundException {
		return new InputStreamReader(getStream(name));
	}

	public static Reader getReader(final File file) throws FileNotFoundException {
		return new InputStreamReader(getStream(file));
	}

	public static Reader getReader(final String name, final String encoding) throws FileNotFoundException,
			UnsupportedEncodingException {
		return new InputStreamReader(getStream(name), encoding);
	}

	public static Reader getReader(final File file, final String encoding) throws FileNotFoundException,
			UnsupportedEncodingException {
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
	 * @throws MalformedURLException
	 */
	public static URL getUrl(final String name) throws MalformedURLException {
		final File file = new File(name);
		if (file.exists()) {
			return getUrl(file);
		}

		final URL resourceUrl =
				Thread.currentThread().getContextClassLoader().getResource(name);
		if (resourceUrl != null) {
			return resourceUrl;
		}

		return new URL(name);
	}

	public static URL getUrl(final File file) throws MalformedURLException {
		return file.toURI().toURL();
	}

	public static Properties loadProperties(final String location) {
		try {
			return loadProperties(getStream(location));
		} catch (final IOException e) {
			throw new MetafactureException("'" + location + "' could not be loaded", e);
		}
	}

	public static Properties loadProperties(final InputStream stream) throws IOException {
		final Properties properties;
		properties = new Properties();
		properties.load(stream);
		return properties;
	}

	public static Properties loadProperties(final URL url) {
		try {
			return loadProperties(url.openStream());
		} catch (final IOException e) {
			throw new MetafactureException("'" + url.getPath() + "' could not be loaded", e);
		}
	}

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

	public static List<String> loadTextFile(final String location, final List<String> list) throws IOException {
		final BufferedReader reader = new BufferedReader(getReader(location));

		String line = reader.readLine();
		while (line != null) {
			list.add(line);
			line = reader.readLine();
		}

		return list;
	}

}
