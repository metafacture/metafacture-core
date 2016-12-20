/*
 * Copyright 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.metamorph.functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.culturegraph.mf.metamorph.api.MorphBuildException;
import org.culturegraph.mf.metamorph.api.helpers.AbstractSimpleStatelessFunction;

/**
 * This function creates a timestamp. By default it returns a unix
 * timestamp. Other formats are and different
 * timezones can be specified.
 *
 * Examples for using the function in Metamorph:
 * <ul>
 * 	<li>Generate a default unix timestamp: <code>&lt;timestamp /&gt;</code></li>
 * 	<li>Generate a nicely formatted timestamp using central european time:
 * 	<code>&lt;timestamp format="yyyy-MM-dd'T'HH:mmZ" timezone="Europe/Berlin" /&gt;</code></li>
 * </ul>
 *
 * @author Michael Büchner
 */
public final class Timestamp extends AbstractSimpleStatelessFunction {

	public static final String FORMAT_TIMESTAMP = "timestamp";

	public static final String DEFAULT_FORMAT = FORMAT_TIMESTAMP;
	public static final String DEFAULT_TIMEZONE = "UTC";

	private static final int MS_PER_SECOND = 1000;
	private static final Set<String> SUPPORTED_LANGUAGES;

	private String format = DEFAULT_FORMAT;
	private String timezone = DEFAULT_TIMEZONE;
	private Locale locale = Locale.getDefault();

	static {
		final Set<String> set = new HashSet<String>();
		Collections.addAll(set, Locale.getISOLanguages());
		SUPPORTED_LANGUAGES = Collections.unmodifiableSet(set);
	}

	@Override
	public String process(final String value) {
		if (FORMAT_TIMESTAMP.equals(format)) {
			return Long.toString(System.currentTimeMillis() / MS_PER_SECOND);
		}
		final DateFormat dateFormat;
		try {
			dateFormat = new SimpleDateFormat(format, locale);
		} catch (final IllegalArgumentException e) {
			throw new MorphBuildException("The date/time format '" + format + "' is not supported. ", e);
		}
		dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		return dateFormat.format(new Date());
	}

	public void setFormat(final String format) {
		this.format = format;
	}

	public void setTimezone(final String timezone) {
		this.timezone = timezone;
	}

	public void setLanguage(final String language) {
		if (!SUPPORTED_LANGUAGES.contains(language)) {
			throw new MorphBuildException("Language '" + language + "' not supported.");
		}
		this.locale = new Locale(language);
	}

}
