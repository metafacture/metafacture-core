/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.commons;

/**
 * Defines static methods for method argument validation.
 *
 * @author Christoph Böhme
 *
 */
public final class Require {

	private Require() {
		// No instances allowed
	}

	/**
	 * Throws an {@link IllegalArgumentException} if {@code object} is
	 * {@literal null}.
	 *
	 * @return {@code object}
	 */
	public static <T> T notNull(final T object) {
		return notNull(object, "parameter must not be null");
	}

	/**
	 * Throws an {@link IllegalArgumentException} if {@code object} is
	 * {@literal null}.
	 *
	 * @param message
	 *            exception message
	 * @return {@code object}
	 */
	public static <T> T notNull(final T object, final String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}

	/**
	 * Throws an {@link IllegalArgumentException} if {@code value} is negative.
	 *
	 * @return {@code value}
	 */
	public static int notNegative(final int value) {
		return notNegative(value, "parameter must not be negative");
	}

	/**
	 * Throws an {@link IllegalArgumentException} if {@code value} is negative.
	 *
	 * @param message
	 *            exception message
	 * @return {@code value}
	 */
	public static int notNegative(final int value, final String message) {
		if (value < 0) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	/**
	 * Throws an {@link IllegalArgumentException} if {@code condition} is false.
	 */
	public static void that(final boolean condition) {
		that(condition, "parameter is not valid");
	}

	/**
	 * Throws an {@link IllegalArgumentException} if {@code condition} is false.
	 *
	 * @param message
	 *            exception message
	 */
	public static void that(final boolean condition, final String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Throws an {@link IndexOutOfBoundsException} if {@code index} is negative
	 * or equal to or greater than {@code arrayLength}.
	 *
	 * @return {@code index}
	 */
	public static int validArrayIndex(final int index, final int arrayLength) {
		return validArrayIndex(index, arrayLength, "array index out of range");
	}

	/**
	 * Throws an {@link IndexOutOfBoundsException} if {@code index} is negative
	 * or equal to or greater than {@code arrayLength}.
	 *
	 * @param message
	 *            exception message
	 * @return {@code index}
	 */
	public static int validArrayIndex(final int index, final int arrayLength,
			final String message) {
		if (index < 0 || index >= arrayLength) {
			throw new IndexOutOfBoundsException(message);
		}
		return index;
	}

	/**
	 * Throws an {@link IndexOutOfBoundsException} if {@code sliceFrom} or
	 * {@code sliceLength} is negative or the sum of both is greater than
	 * {@code arrayLength}. Note that this means that a slice of length zero
	 * starting at array length is a valid slice.
	 */
	public static void validArraySlice(final int sliceFrom,
			final int sliceLength, final int arrayLength) {
		validArraySlice(sliceFrom, sliceLength, arrayLength,
				"array slice out of range");
	}

	/**
	 * Throws an {@link IndexOutOfBoundsException} if {@code sliceFrom} or
	 * {@code sliceLength} is negative or the sum of both is greater than
	 * {@code arrayLength}. Note that this means that a slice of length zero
	 * starting at array length is a valid slice.
	 *
	 *
	 * @param message
	 *            exception message
	 */
	public static void validArraySlice(final int sliceFrom,
			final int sliceLength, final int arrayLength, final String message) {
		if (sliceFrom < 0 || sliceLength < 0) {
			throw new IndexOutOfBoundsException(message);
		}
		if (sliceFrom + sliceLength > arrayLength) {
			throw new IndexOutOfBoundsException(message);
		}
	}

}
