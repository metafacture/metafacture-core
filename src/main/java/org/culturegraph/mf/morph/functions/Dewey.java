package org.culturegraph.mf.morph.functions;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * note: some parts are borrowed from https://github.com/sul-dlss/solrmarc-sw/blob/master/core/src/org/solrmarc/tools/CallNumUtils.java
 *
 * @author tgaengler
 */
public class Dewey extends AbstractSimpleStatelessFunction {

	private static final Pattern DEWEY_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d+)?.*");

	/**
	 * regular expression for Dewey classification.
	 *  Dewey classification is a three digit number (possibly missing leading
	 *   zeros) with an optional fraction portion.
	 */
	private static final String DEWEY_CLASS_REGEX = "\\d{1,3}(\\.\\d+)?";

	private static final String ENTIRE_CALL_NUM_REGEX = "(" + DEWEY_CLASS_REGEX + ").*";

	private static final Pattern ENTIRE_CALL_NUM_REGEX_PATTERN = Pattern.compile(ENTIRE_CALL_NUM_REGEX);

	private static final String ZERO      = "0";
	private static final String DOT       = ".";
	private static final String TRUE      = "true";
	public static final  String HASH_MARK = "############";

	private String  precision;
	private boolean addLeadingZeros;
	private String  errorString;

	public void setPrecision(final String precision) {

		this.precision = precision;
	}

	public void setAddLeadingZeros(final String addLeadingZeros) {

		this.addLeadingZeros = TRUE.equals(addLeadingZeros);
	}

	public void setErrorString(final String errorString) {

		this.errorString = errorString;
	}

	@Override
	protected String process(final String value) {

		if (!isValidDewey(value)) {

			// dewey number is not valid

			return errorString;
		}

		final String formattedDeweyNumber;
		final Integer digitsAfter;

		if (precision != null && !precision.trim().isEmpty()) {

			final float precisionFloat = convertToFloat(precision);

			if (!value.contains(DOT) || precisionFloat >= 1) {

				// no digits after decimal point

				digitsAfter = null;
			} else {

				// leave digits after decimal point as is

				digitsAfter = -1;
			}

			formattedDeweyNumber = formatDewey(value, precisionFloat);

			if (formattedDeweyNumber == null) {

				return errorString;
			}
		} else {

			formattedDeweyNumber = value;
			digitsAfter = -1;
		}

		return normalizeFloat(formattedDeweyNumber, 3, digitsAfter, addLeadingZeros);
	}

	private static String formatDewey(final String deweyNumberString, final float precision) {

		// Convert the numeric portion of the call number into a float:
		final String deweyB4Cutter = getDeweyB4Cutter(deweyNumberString);

		if (deweyB4Cutter == null) {

			return null;
		}

		final String deweyWALZ = cutLeadingZeros(deweyB4Cutter);

		final double currentVal = Double.parseDouble(deweyWALZ);

		final DeweyPrecisionType precisionType = DeweyPrecisionType.getByPrecision(precision);

		final DecimalFormat decimalFormat = precisionType.getPrecisionFormat();

		// Round the call number value to the specified precision:
		if (precision >= 1) {

			final Double roundVal = Math.floor(currentVal / precision) * precision;

			return roundVal.toString();
		} else {

			return decimalFormat.format(currentVal);
		}
	}

	private float convertToFloat(final String precisionString) {

		// Precision comes in as a string, but we need to convert it to a float:
		return Float.parseFloat(precisionString);
	}

	/**
	 * given a possible Dewey call number value, determine if it
	 *  matches the pattern of an Dewey call number
	 */
	private static boolean isValidDewey(final String possDeweyVal) {

		return possDeweyVal != null && DEWEY_PATTERN.matcher(possDeweyVal.trim()).matches();
	}

	/**
	 * return the portion of the Dewey call number string that occurs before the
	 *  Cutter.
	 */
	private static String getDeweyB4Cutter(final String callnum) {

		final Matcher matcher = ENTIRE_CALL_NUM_REGEX_PATTERN.matcher(callnum);

		if (!matcher.find()) {

			return null;
		}

		return matcher.group(1).trim();
	}

	/**
	 * normalizes numbers (can have decimal portion) to (digitsB4) before
	 *  the decimal (adding leading zeroes as necessary) and (digitsAfter
	 *  after the decimal.  In the case of a whole number, there will be no
	 *  decimal point.
	 * @param floatStr  the number, as a String
	 * @param digitsB4 - the number of characters the result should have before the
	 *   decimal point (leading zeroes will be added as necessary). A negative
	 *   number means leave whatever digits encountered as is; don't pad with leading zeroes.
	 * @param digitsAfter - the number of characters the result should have after
	 *   the decimal point.  A negative number means leave whatever fraction
	 *   encountered as is; don't pad with trailing zeroes (trailing zeroes in
	 *   this case will be removed)
	 * @throws NumberFormatException if string can't be parsed as a number
	 */
	public static String normalizeFloat(final String floatStr, final int digitsB4, final Integer digitsAfter, final boolean addLeadingZeros) {

		final double value = Double.valueOf(floatStr);

		final StringBuilder formatStrSB = new StringBuilder();

		formatStrSB.append(getFormatString(digitsB4, addLeadingZeros));

		if (digitsAfter != null) {

			formatStrSB.append(DOT).append(getFormatString(digitsAfter, addLeadingZeros));
		}

		final String formatStr = formatStrSB.toString();

		final DecimalFormat normFormat = new DecimalFormat(formatStr, DecimalFormatSymbols.getInstance(Locale.ENGLISH));

		final String norm = normFormat.format(value);

		if (!norm.endsWith(DOT)) {

			return norm;
		}

		return norm.substring(0, norm.length() - 1);
	}

	/**
	 * return a format string corresponding to the number of digits specified
	 * @param numDigits - the number of characters the result should have (to be padded
	 *  with zeroes as necessary). A negative number means leave whatever digits
	 *   encountered as is; don't pad with zeroes -- up to 12 characters.
	 */
	private static String getFormatString(final int numDigits, final boolean addLeadingZeros) {

		final StringBuilder b4 = new StringBuilder();

		if (numDigits < 0) {

			b4.append(HASH_MARK);
		} else if (numDigits > 0 && addLeadingZeros) {

			for (int i = 0; i < numDigits; i++) {

				b4.append(ZERO);
			}
		}

		return b4.toString();
	}

	private static String cutLeadingZeros(final String deweyString) {

		if (deweyString.endsWith(ZERO)) {

			// only 0 can be a dewey number as well

			return deweyString;
		}

		String dewey = deweyString;

		while (dewey.startsWith(ZERO)) {

			dewey = dewey.substring(1, dewey.length());
		}

		return dewey;
	}
}
