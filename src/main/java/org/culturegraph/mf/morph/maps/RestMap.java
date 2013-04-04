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
package org.culturegraph.mf.morph.maps;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.MetafactureException;


/**
 * NOT WORKING YET
 * 
 * @author "Markus Michael Geipel"
 *
 */
public final class RestMap extends AbstractReadOnlyMap<String, String>{
	
	private static final Pattern VAR_PATTERN = Pattern.compile("${key}", Pattern.LITERAL);
	private String url;
	
	public void setUrl(final String url) {
		this.url = url;
	}
		
	

	@Override
	public String get(final Object key) {
		final Matcher matcher = VAR_PATTERN.matcher(url);
		try {
			final URL url = new URL(matcher.replaceAll(key.toString()));
			final URLConnection con = url.openConnection();
			//TODO correctly read from connection!
			return (String)con.getContent();
			
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

}
