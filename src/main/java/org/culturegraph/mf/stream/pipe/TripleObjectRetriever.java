/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.pipe;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.Triple;
import org.culturegraph.mf.types.Triple.ObjectType;

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
public final class TripleObjectRetriever 
		extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> {

	private String defaultEncoding = "UTF-8";
	
	/**
	 * Returns the default encoding used when no encoding is
	 * provided by the server. The default setting is UTF-8.
	 * 
	 * @return current default setting
	 */
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/**
	 * Sets the default encoding to use when no encoding is 
	 * provided by the server. The default setting is UTF-8.
	 * 
	 * @param defaultEncoding new default encoding
	 */
	public void setDefaultEncoding(final String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	@Override
	public void process(final Triple triple) {
		assert !isClosed();
		
		if (triple.getObjectType() != ObjectType.STRING) {
			return;
		}

		final String objectValue;
		try {
			final URL url = new URL(triple.getObject());
			final URLConnection con = url.openConnection();
			String enc = con.getContentEncoding();
			if (enc == null) {
				enc = defaultEncoding;
			}
			objectValue = IOUtils.toString(con.getInputStream(), enc);
		} catch (IOException e) {
			throw new MetafactureException(e);
		}

		getReceiver().process(new Triple(triple.getSubject(), triple.getPredicate(), objectValue));
	}
	
}
