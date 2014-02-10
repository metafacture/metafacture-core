package org.culturegraph.mf.morph.functions;

import java.text.ParseException;
import java.util.Locale;

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

	private Locale outputLocale = Locale.getDefault();
	private String dateInputFormat = "dd.MM.yyyy";
	private int dateOutputFormat = java.text.DateFormat.LONG;

	static {
	}

	@Override
	public final String process(final String value) {   
        
		String ret = value;		
		try {
			final java.text.SimpleDateFormat dfi = new java.text.SimpleDateFormat(dateInputFormat);
			final java.util.Date date = dfi.parse(value);
			final java.text.DateFormat dfo = java.text.DateFormat.getDateInstance(dateOutputFormat, outputLocale);
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
		if(string.equalsIgnoreCase("FULL")) dateOutputFormat = java.text.DateFormat.FULL;
		else if(string.equalsIgnoreCase("LONG")) dateOutputFormat = java.text.DateFormat.LONG;
		else if(string.equalsIgnoreCase("MEDIUM")) dateOutputFormat = java.text.DateFormat.MEDIUM;
		else if(string.equalsIgnoreCase("SHORT")) dateOutputFormat = java.text.DateFormat.SHORT;
	}

	public final void setLanguage(final String string) {
        this.outputLocale = Locale.getDefault();
        for(final Locale l : Locale.getAvailableLocales()) {
        	if(string.equals(l.toString())) {
        		this.outputLocale = l;
        		break;
        	}
        }
	}
}
