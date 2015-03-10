/*
 *  Copyright 2014 Christoph Böhme
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.iso2709;

import static org.junit.Assert.assertEquals;

import org.culturegraph.mf.iso2709.RecordFormat;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link RecordFormat}.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordFormatTest {

	private static final int TOO_LARGE_LENGTH = 10;

	private RecordFormat recordFormat;

	@Before
	public void setup() {
		recordFormat = new RecordFormat();
	}

	@Test
	public void shouldSetIndicatorLength() {
		recordFormat.setIndicatorLength(2);
		assertEquals(2, recordFormat.getIndicatorLength());
	}
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorLengthLessThanZero() {
		recordFormat.setIndicatorLength(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorLengthIsGreaterThanNine() {
		recordFormat.setIndicatorLength(TOO_LARGE_LENGTH);
	}

	@Test
	public void shouldSetIdentifierLength() {
		recordFormat.setIdentifierLength(2);
		assertEquals(2, recordFormat.getIdentifierLength());
	}
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierLengthLessThanZero() {
		recordFormat.setIdentifierLength(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierLengthIsGreaterThanNine() {
		recordFormat.setIdentifierLength(TOO_LARGE_LENGTH);
	}

	@Test
	public void shouldSetFieldStartLength() {
		recordFormat.setFieldStartLength(2);
		assertEquals(2, recordFormat.getFieldStartLength());
	}
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldStartLengthLessThanOne() {
		recordFormat.setFieldStartLength(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldStartLengthIsGreaterThanNine() {
		recordFormat.setFieldStartLength(TOO_LARGE_LENGTH);
	}

	@Test
	public void shouldSetFieldLengthLength() {
		recordFormat.setFieldLengthLength(2);
		assertEquals(2, recordFormat.getFieldLengthLength());
	}
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldLengthLengthLessThanOne() {
		recordFormat.setFieldLengthLength(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldLengthLengthIsGreaterThanNine() {
		recordFormat.setFieldLengthLength(TOO_LARGE_LENGTH);
	}


	@Test
	public void shouldSetImplDefinedPartLength() {
		recordFormat.setImplDefinedPartLength(2);
		assertEquals(2, recordFormat.getImplDefinedPartLength());
	}
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfSetImplDefinedPartLengthLessThanZero() {
		recordFormat.setImplDefinedPartLength(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfSetImplDefinedPartLengthIsGreaterThanNine() {
		recordFormat.setImplDefinedPartLength(TOO_LARGE_LENGTH);
	}

}
