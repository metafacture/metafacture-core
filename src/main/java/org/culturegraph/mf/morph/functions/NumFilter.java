package org.culturegraph.mf.morph.functions;

import org.culturegraph.mf.morph.NamedValueSource;

/**
 * @author tgaengler
 */
public class NumFilter extends AbstractFunction {

	private NumMatcher matcher;

	@Override
	public void receive(final String name, final String value, final NamedValueSource source, final int recordCount,
			final int entityCount) {

		try {

			final double valueNumber = Double.valueOf(value);

			final int compareResult = Double.compare(valueNumber, matcher.getMatchNumber());

			final NumMatcherType matcherType = matcher.getType();

			boolean matched = false;

			switch (matcherType) {

				case GREATER_THEN:
				case LESS_THEN:
				case EQUALS:

					if (compareResult == matcherType.getCompareValue()) {

						matched = true;
					}

					break;
				case GREATER_THEN_OR_EQUALS:

					if (compareResult >= matcherType.getCompareValue()) {

						matched = true;
					}

					break;
				case LESS_THEN_OR_EQUALS:

					if (compareResult <= matcherType.getCompareValue()) {

						matched = true;
					}

					break;
			}

			if (matched) {

				getNamedValueReceiver().receive(name, value, this, recordCount, entityCount);
			}
		} catch (final NumberFormatException e) {

			throw new IllegalArgumentException(String.format("cannot execute numeric filter; value '%s' is not a valid number.", value));
		}

	}

	public void setExpression(final String expression) {

		this.matcher = determineMatcher(expression);
	}

	private NumMatcher determineMatcher(final String expression) {

		if (expression == null) {

			throw new IllegalArgumentException("numeric filter expression must be set");
		}

		final String trimmedExpression = expression.trim();

		if (trimmedExpression.length() < 2) {

			throw new IllegalArgumentException("numeric filter must contain at least two characters");
		}

		final NumMatcherType type = NumMatcherType.getByExpression(trimmedExpression);

		final String matchNumberPart = trimmedExpression.substring(type.getComparator().length(), trimmedExpression.length());

		final String trimmedMatchNumberPart = matchNumberPart.trim();

		try {

			final double matchNumber = Double.valueOf(trimmedMatchNumberPart);

			return new NumMatcher(type, matchNumber);
		} catch (final NumberFormatException e) {

			throw new IllegalArgumentException(
					String.format("numeric filter must consists of a comparator and number - number part '%s' is not valid.",
							trimmedMatchNumberPart));
		}

	}

	private class NumMatcher {

		private NumMatcherType type;
		private double         matchNumber;

		public NumMatcher(final NumMatcherType typeArg, final double matchNumberArg) {

			type = typeArg;
			matchNumber = matchNumberArg;
		}

		public NumMatcherType getType() {

			return type;
		}

		public double getMatchNumber() {

			return matchNumber;
		}

		@Override public String toString() {

			return type + " " + matchNumber;
		}
	}

	private enum NumMatcherType {

		GREATER_THEN(Constants.GREATER_THEN, 1),
		LESS_THEN(Constants.LESS_THEN, -1),
		EQUALS(Constants.EQUALS, 0),
		GREATER_THEN_OR_EQUALS(Constants.GREATER_THEN_OR_EQUALS, 0),
		LESS_THEN_OR_EQUALS(Constants.LESS_THEN_OR_EQUALS, 0);

		private String comparator;
		private int    compareValue;

		NumMatcherType(final String comparatorArg, final int compareValueArg) {

			comparator = comparatorArg;
			compareValue = compareValueArg;
		}

		public String getComparator() {

			return comparator;
		}

		public int getCompareValue() {

			return compareValue;
		}

		public static NumMatcherType getByComparator(final String comparator) {

			for (final NumMatcherType numMatcherType : NumMatcherType.values()) {

				if (numMatcherType.comparator.equals(comparator)) {

					return numMatcherType;
				}
			}

			throw new IllegalArgumentException(comparator);
		}

		public static NumMatcherType getByExpression(final String expression) {

			if (expression.startsWith(Constants.GREATER_THEN_OR_EQUALS)) {

				return NumMatcherType.GREATER_THEN_OR_EQUALS;
			} else if (expression.startsWith(Constants.LESS_THEN_OR_EQUALS)) {

				return NumMatcherType.LESS_THEN_OR_EQUALS;
			} else if (expression.startsWith(Constants.GREATER_THEN)) {

				return NumMatcherType.GREATER_THEN;
			} else if (expression.startsWith(Constants.LESS_THEN)) {

				return NumMatcherType.LESS_THEN;
			} else if (expression.startsWith(Constants.EQUALS)) {

				return NumMatcherType.EQUALS;
			}

			throw new IllegalArgumentException(
					String.format("numeric filter must consists of a comparator and number - comparator part in '%s' is not valid.", expression));
		}

		@Override
		public String toString() {

			return comparator;
		}

		private static class Constants {

			public static final String GREATER_THEN           = ">";
			public static final String LESS_THEN              = "<";
			public static final String EQUALS                 = "==";
			public static final String GREATER_THEN_OR_EQUALS = ">=";
			public static final String LESS_THEN_OR_EQUALS    = "<=";
		}
	}
}
