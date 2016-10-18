package org.culturegraph.mf.morph.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
public class ISSN extends AbstractSimpleStatelessFunction {

	private static final Logger LOG = LoggerFactory.getLogger(ISSN.class);

	private final static Pattern ISSN_PATTERN = Pattern.compile("^[0-9]{4}\\-?[0-9]{3}[0-9xX]$");
	private static final String  HYPHEN       = "-";

	private boolean format;
	private boolean check;
	private String  errorString;

	public void setFormat(final String formatStr) {

		format = Boolean.parseBoolean(formatStr);
	}

	public void setCheck(final String checkStr) {

		check = Boolean.parseBoolean(checkStr);
	}

	public void setErrorString(final String errorString) {

		this.errorString = errorString;
	}

	@Override
	protected String process(final String value) {

		if (!isValid(value)) {

			// input is not a valid ISSN

			return errorString;
		}

		final String normalizedValue = dehyphenate(value);

		if (check && !check(normalizedValue)) {

			// ISSN does not pass check

			return errorString;
		}

		if (format) {

			return format(normalizedValue);
		}

		return value;
	}

	private boolean isValid(final String input) {

		final Matcher matcher = ISSN_PATTERN.matcher(input);

		final boolean valid = matcher.find();

		if (!valid) {

			LOG.debug("input '{}' is not a valid ISSN", input);
		}

		return valid;
	}

	private String dehyphenate(final String input) {

		final StringBuilder sb = new StringBuilder(input);

		int i = sb.indexOf(HYPHEN);

		while (i > 0) {

			sb.deleteCharAt(i);
			i = sb.indexOf(HYPHEN);
		}

		return sb.toString();
	}

	private String format(final String input) {

		return input.substring(0, 4) + HYPHEN + input.substring(4, 7) + Character.toUpperCase(input.charAt(7));
	}

	private boolean check(final String input) {

		final String finalInput = input.substring(0, 7);
		Character existingCheckSum = input.charAt(7);

		int checkSumNumber = 0;
		int weight;
		int val;

		for (int i = 0; i < 7; i++) {

			val = finalInput.charAt(i) - '0';
			weight = 8 - i;
			checkSumNumber += weight * val;
		}

		int chk = checkSumNumber % 11;
		char checkSum = chk == 0 ? '0' : chk == 1 ? 'X' : (char) ((11 - chk) + '0');

		final boolean checkResult = checkSum == existingCheckSum;

		if (!checkResult) {

			LOG.debug("false check result for ISSN '{}': expected '{}', but was '{}'", input, existingCheckSum, checkSum);
		}

		return checkResult;
	}
}
