package org.culturegraph.mf.morph.functions;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tgaengler
 */
public class DeweyTest {

	/**
	 * Input String (DEWEY) : 123.456

	 Function:
	 dewey-part(<DEWEY>,<PART>)
	 PART : 100er, 10er, 1er, 10tel, 100tel

	 Result:
	 100er = 100
	 10er = 120
	 1er = 123
	 10tel = 123.4
	 100tel = 123.45
	 */

	private static final String DEWEY1 = "123.456";
	private static final String DEWEY2 = "3.456";
	private static final String NON_VALID_DEWEY = "ABC";
	private static final String NON_VALID_DEWEY2 = "123.456.789";

	private static final String PRECISION_100      = "100";
	private static final String PRECISION_10       = "10";
	private static final String PRECISION_1        = "1";
	private static final String PRECISION_10_PART  = "0.1";
	private static final String PRECISION_100_PART = "0.01";

	@Test
	public void precision100DeweyTest() {

		processDewey(DEWEY1, PRECISION_100, true, "something went wrong", "100");
	}

	@Test
	public void precision10DeweyTest() {

		processDewey(DEWEY1, PRECISION_10, true, "something went wrong", "120");
	}

	@Test
	public void precision1DeweyTest() {

		processDewey(DEWEY1, PRECISION_1, true, "something went wrong", "123");
	}

	@Test
	public void precision10PartDeweyTest() {

		processDewey(DEWEY1, PRECISION_10_PART, true, "something went wrong", "123.4");
	}

	@Test
	public void precision100PartDeweyTest() {

		processDewey(DEWEY1, PRECISION_100_PART, true, "something went wrong", "123.45");
	}

	/**
	 * i.e. simply validation + add leading zeros
	 */
	@Test
	public void noPrecisionDeweyTest() {

		processDewey(DEWEY1, null, true, "something went wrong", "123.456");
	}

	@Test
	public void addLeadingZerosTest() {

		processDewey(DEWEY2, null, true, "something went wrong", "003.456");
	}

	@Test
	public void addLeadingZerosTest2() {

		processDewey(DEWEY2, PRECISION_10_PART, true, "something went wrong", "003.4");
	}

	@Test
	public void nonValidDeweyTest() {

		processDewey(NON_VALID_DEWEY, null, true, "something went wrong", "something went wrong");
	}

	@Test
	public void nonValidDeweyTest2() {

		boolean hitExecption = false;

		try {

			processDewey(NON_VALID_DEWEY2, null, true, "something went wrong", "something went wrong");
		} catch (final NumberFormatException e) {

			Assert.assertEquals("multiple points", e.getMessage());

			hitExecption = true;
		}

		Assert.assertTrue(hitExecption);
	}

	private static void processDewey(final String inputString, final String precision, final boolean addLeadingZeros, final String errorString,
			final String expectedResult) {

		final Dewey dewey = new Dewey();
		dewey.setPrecision(precision);
		dewey.setAddLeadingZeros(Boolean.valueOf(addLeadingZeros).toString());
		dewey.setErrorString(errorString);

		final String actualResult = dewey.process(inputString);

		Assert.assertEquals(expectedResult, actualResult);
	}

}
