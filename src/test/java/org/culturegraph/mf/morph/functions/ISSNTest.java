package org.culturegraph.mf.morph.functions;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tgaengler
 */
public class ISSNTest {

	private static final String DEFAULT_ISSN   = "0724-8679";
	private static final String ISSN_2         = "07248679";
	private static final String ISSN_3         = "0361-526X";
	private static final String INVALID_ISSN   = "0724-8675";
	private static final String INVALID_ISSN_2 = "0724-86757";
	private static final String DEFAULT_ERROR  = "something went wrong";

	@Test
	public void validISSNWithoutFormatAndCheckTest() {

		final String inputString = DEFAULT_ISSN;
		final boolean format = false;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithoutFormatAndWithCheckTest() {

		final String inputString = DEFAULT_ISSN;
		final boolean format = false;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithFormatAndWithoutCheckTest() {

		final String inputString = DEFAULT_ISSN;
		final boolean format = true;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithFormatAndCheckTest() {

		final String inputString = DEFAULT_ISSN;
		final boolean format = true;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validNonFormattedISSNWithoutFormatAndCheckTest() {

		final String inputString = ISSN_2;
		final boolean format = false;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validNonFormattedISSNWithoutFormatAndWithCheckTest() {

		final String inputString = ISSN_2;
		final boolean format = false;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validNonFormattedISSNWithFormatAndWithoutCheckTest() {

		final String inputString = ISSN_2;
		final boolean format = true;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = DEFAULT_ISSN;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validNonFormattedISSNWithFormatAndCheckTest() {

		final String inputString = ISSN_2;
		final boolean format = true;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = DEFAULT_ISSN;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithXAtTheEndWithoutFormatAndCheckTest() {

		final String inputString = ISSN_3;
		final boolean format = false;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithXAtTheEndWithoutFormatAndWithCheckTest() {

		final String inputString = ISSN_3;
		final boolean format = false;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithXAtTheEndWithFormatAndWithoutCheckTest() {

		final String inputString = ISSN_3;
		final boolean format = true;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = ISSN_3;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	@Test
	public void validISSNWithXAtTheEndWithFormatAndCheckTest() {

		final String inputString = ISSN_3;
		final boolean format = true;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = ISSN_3;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	/**
	 * result won't be checked, just validated against regex (hence, it's valid)
	 */
	@Test
	public void validInvalidISSNWithoutFormatAndCheckTest() {

		final String inputString = INVALID_ISSN;
		final boolean format = false;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	/**
	 * error string will be returned, because check failed
	 */
	@Test
	public void validInvalidISSNWithoutFormatAndWithCheckTest() {

		final String inputString = INVALID_ISSN;
		final boolean format = false;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = errorString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	/**
	 * result won't be checked, just validated against regex (hence, it's valid) + formatted at the end (but it's already formatted)
	 */
	@Test
	public void validInvalidISSNWithFormatAndWithoutCheckTest() {

		final String inputString = INVALID_ISSN;
		final boolean format = true;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = inputString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	/**
	 * error string will be returned, because check failed
	 */
	@Test
	public void validInvalidISSNWithFormatAndCheckTest() {

		final String inputString = INVALID_ISSN;
		final boolean format = true;
		final boolean check = true;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = errorString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	/**
	 * error string will be returned, because it is not a valid ISSN re. the given regex
	 */
	@Test
	public void validInvalidISSN2WithoutFormatAndCheckTest() {

		final String inputString = INVALID_ISSN_2;
		final boolean format = false;
		final boolean check = false;
		final String errorString = DEFAULT_ERROR;
		final String expectedResult = errorString;

		processISSN(inputString, format, check, errorString, expectedResult);
	}

	private static void processISSN(final String inputString, final Boolean format, final Boolean check, final String errorString,
			final String expectedResult) {

		final ISSN issn = new ISSN();

		if (format != null) {

			issn.setFormat(format.toString());
		}

		if (check != null) {

			issn.setCheck(check.toString());
		}

		issn.setErrorString(errorString);

		final String actualResult = issn.process(inputString);

		Assert.assertEquals(expectedResult, actualResult);
	}
}
