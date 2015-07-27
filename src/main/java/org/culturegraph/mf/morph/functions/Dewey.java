package org.culturegraph.mf.morph.functions;

import java.util.regex.Pattern;

/**
 * note: some parts are borrowed from https://github.com/sul-dlss/solrmarc-sw/blob/master/core/src/org/solrmarc/tools/CallNumUtils.java
 *
 * @author tgaengler
 */
public class Dewey extends AbstractSimpleStatelessFunction {

	private static final Pattern DEWEY_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d+)?.*");

	// cutter is a single letter followed by digits.
	// there may be a space before a cutter
	// there should be a period, which is followed by a single letter
	//   the period is sometimes missing
	// For Dewey callnumber, there may be a slash instead of a cutter,
	//  or there might be NO cutter
	private static final String BEGIN_CUTTER_REGEX = "( +|(\\.[A-Z])| */)";

	private boolean addLeadingZeros;
	private String  errorString;

	public void setAddLeadingZeros(final String addLeadingZeros) {

		this.addLeadingZeros = "true".equals(addLeadingZeros);
	}

	public void setErrorString(final String errorString) {

		this.errorString = errorString;
	}

	@Override protected String process(final String value) {

		if (!isValidDewey(value)) {

			return errorString;
		}

		if (addLeadingZeros) {

			return addLeadingZeros(value);
		}

		return value;
	}

	/**
	 * given a possible Dewey call number value, determine if it
	 *  matches the pattern of an Dewey call number
	 */
	private static boolean isValidDewey(final String possDeweyVal) {

		return possDeweyVal != null && DEWEY_PATTERN.matcher(possDeweyVal.trim()).matches();
	}

	/**
	 * adds leading zeros to a dewey call number, when they're missing.
	 * @param deweyCallNum
	 * @return the dewey call number with leading zeros
	 */
	private static String addLeadingZeros(final String deweyCallNum) {

		String result = deweyCallNum;

		final String b4Cutter = getPortionBeforeCutter(deweyCallNum);

		// TODO: could call Utils.normalizeFloat(b4Cutter.trim(), 3, -1);
		// but still need to add back part after cutter

		if (b4Cutter == null) {

			return result;
		}

		final String b4dec;

		final int decIx = b4Cutter.indexOf(".");

		if (decIx >= 0) {

			b4dec = deweyCallNum.substring(0, decIx).trim();
		} else {

			b4dec = b4Cutter.trim();
		}

		switch (b4dec.length()) {
			case 1:

				result = "00" + deweyCallNum;

				break;
			case 2:

				result = "0" + deweyCallNum;

				break;
			default:

				// nothing to do ???
		}

		return result;
	}

	/**
	 * return the portion of the call number string that occurs before the
	 *  Cutter, NOT including any class suffixes occuring before the cutter
	 */
	private static String getPortionBeforeCutter(final String callnum) {

		final String[] pieces = callnum.split(BEGIN_CUTTER_REGEX);

		if (pieces.length == 0 || pieces[0] == null || pieces[0].length() == 0) {

			return null;
		}

		return pieces[0].trim();
	}

}
