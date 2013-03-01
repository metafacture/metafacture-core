/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * Opens a {@link URLConnection} and passes a reader to the receiver.
 * 
 * @author Christoph Böhme
 * 
 */
@Description("Opens a http resource.")
@In(String.class)
@Out(java.io.Reader.class)
public final class HttpOpener 
		extends DefaultObjectPipe<String, ObjectReceiver<Reader>> implements Opener {

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
	public void process(final String urlStr) {
		try {
			final URL url = new URL(urlStr);
			final URLConnection con = url.openConnection();
			String enc = con.getContentEncoding();
			if (enc == null) {
				enc = defaultEncoding;
			}
			getReceiver().process(new InputStreamReader(con.getInputStream(), enc));
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}
}
