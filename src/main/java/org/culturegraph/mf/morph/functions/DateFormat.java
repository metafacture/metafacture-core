/*
 *  Copyright 2014 Deutsche Nationalbibliothek
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.culturegraph.mf.exceptions.MorphDefException;

/**
 * Format date/time strings in Metamorph. By default the input format is
 * dd.MM.yyyy and the output format is dd. MMMM yyyy.
 *
 * Examples of using this function in Metamorph:
 * <ul>
 *     <li>Default date format: <code>&lt;dateformat /&gt;</code></li>
 *     <li>Read ISO-dates and generate German style dates:
 *     <code>&lt;dateformat inputformat="yyyy-MM-dd" outputformat="dd.MM.yyyy" /&gt;</code></li>
 * </ul>
 *
 * @author Michael Büchner
 */
public class DateFormat extends AbstractSimpleStatelessFunction {

	public static final String DEFAULT_INPUT_FORMAT = "dd.MM.yyyy";
	public static final DateFormats DEFAULT_OUTPUT_FORMAT = DateFormats.LONG;

	private static final Set<String> SUPPORTED_LANGUAGES;

	private String inputFormat = DEFAULT_INPUT_FORMAT;
	private DateFormats outputFormat = DEFAULT_OUTPUT_FORMAT;
	private Locale outputLocale = Locale.getDefault();

	/**
	 * Supported date formats. Maps to the date formats in
	 * {@link java.text.DateFormat}.
	 *
	 * @author Christoph Böhme
	 */
	public enum DateFormats {
		FULL(java.text.DateFormat.FULL),
		LONG(java.text.DateFormat.LONG),
		MEDIUM(java.text.DateFormat.MEDIUM),
		SHORT(java.text.DateFormat.SHORT);

		private final int formatId;

		DateFormats(final int formatId) {
			this.formatId = formatId;
		}

		int getFormatId() {
			return formatId;
		}

	}

	static {
		final Set<String> set = new HashSet<String>();
		Collections.addAll(set, Locale.getISOLanguages());
		SUPPORTED_LANGUAGES = Collections.unmodifiableSet(set);
	}

	@Override
	public final String process(final String value) {
		String result = value;
		try {
			final java.text.DateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
			final Date date = inputDateFormat.parse(value);
			result = java.text.DateFormat.getDateInstance(outputFormat.getFormatId(), outputLocale).format(date);
		} catch (final IllegalArgumentException e) {
			throw new MorphDefException("The date/time format is not supported.", e);
		} catch (final ParseException e) {
			// Nothing to do
		}
		return result;
	}

	public final void setInputFormat(final String inputFormat) {
		this.inputFormat = inputFormat;
	}

	public final void setOutputFormat(final DateFormats outputFormat) {
		this.outputFormat = outputFormat;
	}

	public final void setLanguage(final String language) {
		if (!SUPPORTED_LANGUAGES.contains(language)) {
			throw new MorphDefException("Language '" + language + "' not supported.");
		}
		this.outputLocale = new Locale(language);
	}

}
