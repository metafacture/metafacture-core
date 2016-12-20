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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Tests for class {@link Require}.
 *
 * @author Christoph Böhme
 *
 */
public final class RequireTest {

	@Test(expected = IllegalArgumentException.class)
	public void notNullShouldThrowIllegalArgumentExceptionIfArgIsNull() {
		Require.notNull(null);
	}

	@Test
	public void notNullShouldReturnArgIfArgIsNotNull() {
		final Object obj = new Object();
		assertSame(obj, Require.notNull(obj));
	}

	@Test(expected = IllegalArgumentException.class)
	public void notNegativeShouldThrowIllegalArgumentExceptionIfArgIsNegative() {
		Require.notNegative(-1);
	}

	@Test
	public void notNegativeShouldReturnArgIfArgIsNotNegative() {
		assertEquals(0, Require.notNegative(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void thatShouldFailIfArgumentIsFalse() {
		Require.that(false);
	}

	@Test
	public void thatShouldDoNothingIfArgumentIsTrue() {
		Require.that(true);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void validArrayIndexShouldThrowIndexOutOfBoundsExceptionIfIndexIsNegative() {
		Require.validArrayIndex(-1, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void validArrayIndexShouldThrowIndexOutOfBoundsExceptionIfIndexIsGreaterThanArrayLength() {
		Require.validArrayIndex(2, 2);
	}

	@Test
	public void validArrayIndexShouldDoNothingIfIndexIsWithinArrayBounds() {
		assertEquals(1, Require.validArrayIndex(1, 2));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void validArraySliceShouldThrowIndexOutOfBoundsExceptionIfIndexIsNegative() {
		Require.validArraySlice(-1, 1, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void validArraySliceShouldThrowIndexOutOfBoundsExceptionIfLengthIsNegative() {
		Require.validArraySlice(0, -1, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void validArraySliceShouldThrowIndexOutOfBoundsExceptionIfIndexPlusLengthIsGreaterThanArrayLength() {
		Require.validArraySlice(1, 2, 2);
	}

	@Test
	public void validArraySliceShouldDoNothingIfIndexAndLengthAreWithinArrayBounds() {
		Require.validArraySlice(0, 1, 2);
	}

}
