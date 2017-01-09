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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.culturegraph.mf.metamorph.api.MorphBuildException;
import org.culturegraph.mf.metamorph.api.helpers.AbstractSimpleStatelessFunction;

/**
 * Format date/time strings in Metamorph. By default the input format is
 * dd.MM.yyyy and the output format is {@link java.text.DateFormat.Field#LONG}.
 * <p>
 * The Attribute removeLeadingZeros will remove all leading zeros from all
 * numbers in the output date.
 * <p>
 * The attribute era is used to specify if the date is BC or AD. Default
 * value is AUTO. To understand that, three short examples:
 * <ul>
 *   <li>Input: 20.07.356 (era=BC)
 *   <li>Output (German location): 20. Juli 0356 v. Chr.
 *   <li>Input: 20.07.356 (era=AD,removeLeadingZeros=true)
 *   <li>Output (German location): 20. Juli 356
 *   <li>Input: 20.07.-356 (era=AUTO)
 *   <li>Output (German location): 20. Juli 0357 v. Chr. (there is NO year 0;
 *       see ISO 8601, Proleptic Gregorian Calendar)
 * </ul>
 * Examples of using this function in Metamorph:
 * <ul>
 *   <li>Default date format: {@code <dateformat />}
 *   <li>Read ISO-dates and generate German style dates:
 *   {@code <dateformat inputformat="yyyy-MM-dd" outputformat="dd.MM.yyyy" />}
 * </ul>
 *
 * @author Michael Büchner
 */
public class DateFormat extends AbstractSimpleStatelessFunction {

	public static final String DEFAULT_INPUT_FORMAT = "dd.MM.yyyy";
	public static final DateFormats DEFAULT_OUTPUT_FORMAT = DateFormats.LONG;
	public static final boolean DEFAULT_REMOVE_LEADING_ZEROS = false;
	public static final Era DEFAULT_ERA = Era.AUTO;

	private static final Set<String> SUPPORTED_LANGUAGES;

	private String inputFormat = DEFAULT_INPUT_FORMAT;
	private DateFormats outputFormat = DEFAULT_OUTPUT_FORMAT;
	private Era era = DEFAULT_ERA;
	private boolean removeLeadingZeros = DEFAULT_REMOVE_LEADING_ZEROS;
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

	/**
	 * Supported eras (basically AUTO, AD and BC). Maps to
	 * {@link java.util.GregorianCalendar}.
	 *
	 * @author Michael Büchner
	 */
	public enum Era {
		AD(GregorianCalendar.AD),
		BC(GregorianCalendar.BC),
		AUTO(-1);

		private final int eraId;

		Era(final int eraId) {
			this.eraId = eraId;
		}

		int getEraId() {
			return eraId;
		}

	}

	static {
		final Set<String> set = new HashSet<String>();
		Collections.addAll(set, Locale.getISOLanguages());
		SUPPORTED_LANGUAGES = Collections.unmodifiableSet(set);
	}

	@Override
	public final String process(final String value) {
		String result;
		try {
			final Calendar c = Calendar.getInstance();
			final SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
			c.setTime(sdf.parse(value));
			if (era == Era.BC) {
				c.set(Calendar.ERA, GregorianCalendar.BC);
			} else if (era == Era.AD) {
				c.set(Calendar.ERA, GregorianCalendar.AD);
			}

			final SimpleDateFormat sdfp = (SimpleDateFormat) java.text.DateFormat.getDateInstance(outputFormat.getFormatId(), outputLocale);
			String p = sdfp.toPattern();
			if (c.get(Calendar.ERA) == GregorianCalendar.BC) {
				p = p.replace("yyyy", "yyyy G");
			}

			final SimpleDateFormat sdfo = new SimpleDateFormat(p, outputLocale);
			result = sdfo.format(c.getTime());

			if (removeLeadingZeros) {
				result = result.replaceAll("([0]{1,})([0-9]{1,})", "$2");
			}

		} catch (final IllegalArgumentException e) {
			throw new MorphBuildException("The date/time format is not supported.", e);
		} catch (final Exception e) {
			result = value;
		}
		return result;
	}

	public final void setInputFormat(final String inputFormat) {
		this.inputFormat = inputFormat;
	}

	public final void setOutputFormat(final DateFormats outputFormat) {
		this.outputFormat = outputFormat;
	}

	public final void setEra(final Era era) {
		this.era = era;
	}

	public final void setRemoveLeadingZeros(final boolean removeLeadingZeros) {
		this.removeLeadingZeros = removeLeadingZeros;
	}

	public final void setLanguage(final String language) {
		if (!SUPPORTED_LANGUAGES.contains(language)) {
			throw new MorphBuildException("Language '" + language + "' not supported.");
		}
		this.outputLocale = new Locale(language);
	}
}
