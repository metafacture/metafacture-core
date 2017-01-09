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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.metamorph.api.helpers.AbstractReadOnlyMap;

/**
 * A map which resolves its keys by doing a REST request and returning the
 * response as value.
 *
 * @author Markus Michael Geipel
 * @author Philipp v. Böselager
 */
public final class RestMap extends AbstractReadOnlyMap<String, String> {

	private static final Pattern VAR_PATTERN = Pattern.compile("${key}", Pattern.LITERAL);
	private String charsetName = "UTF-8";
	private String url;

	public RestMap() {
	}

	public RestMap(String url) {
		this.url = url;
	}

	@Override
	public String get(final Object key) {
		final Matcher matcher = VAR_PATTERN.matcher(url);
		try {
			String urlString = matcher.replaceAll(key.toString());
			return readFromUrl(urlString);
		} catch (IOException | URISyntaxException e) {
			// There was no data result for the given URL
			return null;
		}
	}

	private String readFromUrl(final String url) throws IOException, URISyntaxException {
		InputStream inputStream = new URL(new URI(url.replace(" ", "%20")).toASCIIString()).openConnection()
				.getInputStream();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream, Charset.forName(charsetName)));
			StringBuilder stringBuffer = new StringBuilder();
			int value;
			while ((value = reader.read()) != -1) {
				stringBuffer.append((char) value);
			}
			return stringBuffer.toString();
		} finally {
			inputStream.close();
		}
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public void setCharsetName(String name) {
		charsetName = name;
	}

}
