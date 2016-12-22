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
package org.culturegraph.mf.biblio.iso2709;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for class {@link RecordFormat}.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordFormatTest {

	@Test
	public void shouldCreateRecordFormatFromValidValues() {
		final RecordFormat recordFormat = RecordFormat.create()
				.withIndicatorLength(1)
				.withIdentifierLength(1)
				.withFieldLengthLength(1)
				.withFieldStartLength(1)
				.withImplDefinedPartLength(1)
				.build();
		assertEquals(1, recordFormat.getIndicatorLength());
		assertEquals(1, recordFormat.getIdentifierLength());
		assertEquals(1, recordFormat.getFieldLengthLength());
		assertEquals(1, recordFormat.getFieldStartLength());
		assertEquals(1, recordFormat.getImplDefinedPartLength());
	}
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorLengthLessThanZero() {
		RecordFormat.create().withIndicatorLength(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIndicatorLengthIsGreaterThanNine() {
		RecordFormat.create().withIndicatorLength(10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierLengthLessThanZero() {
		RecordFormat.create().withIdentifierLength(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfIdentifierLengthIsGreaterThanNine() {
		RecordFormat.create().withIdentifierLength(10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldStartLengthLessThanOne() {
		RecordFormat.create().withFieldStartLength(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldStartLengthIsGreaterThanNine() {
		RecordFormat.create().withFieldStartLength(10);
	}


	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldLengthLengthLessThanOne() {
		RecordFormat.create().withFieldLengthLength(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfFieldLengthLengthIsGreaterThanNine() {
		RecordFormat.create().withFieldLengthLength(10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfImplDefinedPartLengthLessThanZero() {
		RecordFormat.create().withImplDefinedPartLength(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionIfImplDefinedPartLengthIsGreaterThanNine() {
		RecordFormat.create().withImplDefinedPartLength(10);
	}

}
