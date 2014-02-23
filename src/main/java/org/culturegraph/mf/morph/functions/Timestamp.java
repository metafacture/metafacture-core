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
package org.culturegraph.mf.morph.functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.culturegraph.mf.exceptions.MorphDefException;

/**
 * This is providing an actual timestamp in Metamorph. By default it is
 * returning an UNIX timestamp. Other formats are possible as well a specific
 * timezone. <br>
 * usage:
 * <ul>
 * <li><code>&lt;timestamp /&gt;</code></li>
 * <li>
 * <code>&lt;timestamp format="yyyy-MM-dd'T'HH:mmZ" timezone="Europe/Berlin" /&gt;</code>
 * </li>
 * </ul>
 * 
 * @author Michael Büchner
 * @version 1.0
 */
public final class Timestamp extends AbstractSimpleStatelessFunction {

	private static final int TSCONV = 1000;
	private static final Set<String> LANGUAGES;
	private Locale locale = Locale.getDefault();
	private String format = "timestamp";
	private String timezone = "UTC";
	
	static {
		final Set<String> set = new HashSet<String>();
		Collections.addAll(set, Locale.getISOLanguages());
		LANGUAGES = Collections.unmodifiableSet(set);
	}

	@Override
	public String process(final String value) {
		if ("timestamp".equals(format)) {
			return Long.toString(System.currentTimeMillis() / TSCONV);
		}
		DateFormat df = null;
		try {
			df = new SimpleDateFormat(format, locale);
		} catch (IllegalArgumentException e) {
			throw new MorphDefException("The date/time format " + format + " is not supported. " + e.getMessage());
		}
		df.setTimeZone(TimeZone.getTimeZone(timezone));
		return df.format(new Date());
	}

	/**
	 * @param string
	 */
	public void setFormat(final String string) {
		this.format = string;
	}

	/**
	 * @param string
	 */
	public void setTimezone(final String string) {
		this.timezone = string;
	}

	/***
	 * @param language
	 */
	public void setLanguage(final String language) {
		if (!LANGUAGES.contains(language)) {
			throw new MorphDefException("Language " + language + " not supported.");
		}
		this.locale = new Locale(language);
	}
}
