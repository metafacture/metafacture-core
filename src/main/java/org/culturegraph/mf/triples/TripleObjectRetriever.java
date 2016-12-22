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
package org.culturegraph.mf.triples;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.culturegraph.mf.framework.objects.Triple;
import org.culturegraph.mf.framework.objects.Triple.ObjectType;

/**
 * Uses the object value of the triple as a URL and emits a new triple
 * in which the object value is replaced with the contents of the resource
 * identified by the URL.
 *
 * @author Christoph Böhme
 */
@Description("Uses the object value of the triple as a URL and emits a new triple "
		+ "in which the object value is replaced with the contents of the resource "
		+ "identified by the URL.")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("retrieve-triple-objects")
public final class TripleObjectRetriever
		extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> {

	private Charset defaultEncoding = StandardCharsets.UTF_8;

	/**
	 * Sets the default encoding to use when no encoding is
	 * provided by the server. The default setting is UTF-8.
	 *
	 * @param defaultEncoding new default encoding
	 */
	public void setDefaultEncoding(final String defaultEncoding) {
		this.defaultEncoding = Charset.forName(defaultEncoding);
	}

	/**
	 * Sets the default encoding to use when no encoding is
	 * provided by the server. The default setting is UTF-8.
	 *
	 * @param defaultEncoding new default encoding
	 */
	public void setDefaultEncoding(final Charset defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Returns the default encoding used when no encoding is
	 * provided by the server. The default setting is UTF-8.
	 *
	 * @return current default setting
	 */
	public String getDefaultEncoding() {
		return defaultEncoding.name();
	}

	@Override
	public void process(final Triple triple) {
		assert !isClosed();
		if (triple.getObjectType() != ObjectType.STRING) {
			return;
		}
		final String objectValue = retrieveObjectValue(triple.getObject());
		getReceiver().process(new Triple(triple.getSubject(), triple.getPredicate(),
				objectValue));
	}

	private String retrieveObjectValue(final String urlString) {
		try {
			final URL url = new URL(urlString);
			final URLConnection connection = url.openConnection();
			connection.connect();
			final String encodingName = connection.getContentEncoding();
			final Charset encoding = encodingName != null ?
					Charset.forName(encodingName) :
					defaultEncoding;
			try (InputStream inputStream = connection.getInputStream()) {
				return ResourceUtil.readAll(inputStream, encoding);
			}
		} catch (final IOException e) {
			throw new MetafactureException(e);
		}
	}

}
