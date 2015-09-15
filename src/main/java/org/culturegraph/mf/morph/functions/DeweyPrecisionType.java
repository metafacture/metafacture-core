package org.culturegraph.mf.morph.functions;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author tgaengler
 */
public enum DeweyPrecisionType {

	P100(100, new DecimalFormat("###", DecimalFormatSymbols.getInstance(Locale.ENGLISH))),
	P10(10, new DecimalFormat("##", DecimalFormatSymbols.getInstance(Locale.ENGLISH))),
	P1(1, new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH))),
	P10PART(Float.valueOf("0.1"), new DecimalFormat("###.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH))),
	P100PART(Float.valueOf("0.01"), new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH)));

	private final float precision;

	private final DecimalFormat precisionFormat;

	DeweyPrecisionType(final float precisionArg, final DecimalFormat precisionFormatArg) {

		precision = precisionArg;
		precisionFormat = precisionFormatArg;
	}

	public float getPrecision() {

		return precision;
	}

	public DecimalFormat getPrecisionFormat() {

		precisionFormat.setRoundingMode(RoundingMode.DOWN);

		return precisionFormat;
	}

	public static DeweyPrecisionType getByPrecision(final float precision) {

		for (final DeweyPrecisionType deweyPrecisionType : DeweyPrecisionType.values()) {

			if (deweyPrecisionType.precision == precision) {

				return deweyPrecisionType;
			}
		}

		throw new IllegalArgumentException("" + precision);
	}

	@Override
	public String toString() {

		return "" + precision;
	}
}
