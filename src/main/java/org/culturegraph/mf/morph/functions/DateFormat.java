package org.culturegraph.mf.morph.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.culturegraph.mf.exceptions.MorphDefException;

/**
 * This is providing a way to format date/time strings in Metamorph. By default
 * the input format is dd.MM.yyyy and the output format is dd. MMMM yyyy.<br>
 * usage:
 * <ul>
 * <li><code>&lt;dateformat /&gt;</code></li>
 * <li>
 * <code>&lt;dateformat inputformat="yyyy-MM-dd" outputformat="dd.MM.yyyy" /&gt;</code>
 * </li>
 * </ul>
 * 
 * @author Michael Büchner
 * @version 1.0
 */
public class DateFormat extends AbstractSimpleStatelessFunction {

	private static final Set<String> LANGUAGES;
	private Locale locale = Locale.getDefault();
	private String dateInputFormat = "dd.MM.yyyy";
	private String dateOutputFormat = "dd. MMMM yyyy";

	static {
		final Set<String> set = new HashSet<String>();
		Collections.addAll(set, Locale.getISOLanguages());
		LANGUAGES = Collections.unmodifiableSet(set);
	}

	@Override
	public final String process(final String value) {

		// convert to Java Date
		String ret = value;
		try {
			final java.text.DateFormat dfi = new SimpleDateFormat(dateInputFormat, locale);
			final java.util.Date date = dfi.parse(value);
			final SimpleDateFormat dfo = new SimpleDateFormat(dateOutputFormat, locale);
			ret = dfo.format(date);
		} catch (IllegalArgumentException iae) {
			throw new MorphDefException("The date/time format is not supported. " + iae.getMessage());
		} catch (ParseException e) {
			return value; // that didn't work
		}
		return ret;
	}

	public final void setInputFormat(final String string) {
		this.dateInputFormat = string;
	}

	public final void setOutputFormat(final String string) {
		this.dateOutputFormat = string;
	}

	public final void setLanguage(final String language) {
		if (!LANGUAGES.contains(language)) {
			throw new MorphDefException("Language " + language + " not supported.");
		}
		this.locale = new Locale(language);
	}
}
