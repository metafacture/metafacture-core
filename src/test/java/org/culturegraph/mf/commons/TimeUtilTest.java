/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

import org.junit.Test;

/**
 * Tests for {@link TimeUtil}.
 *
 * @author Christoph Böhme
 *
 */
public final class TimeUtilTest {

	@Test
	public void testShouldFormatNanoseconds() {
		final long duration = 29 * TimeUtil.NANOSECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("29ns", formattedDuration);
	}

	@Test
	public void testShouldFormatMicroseconds() {
		final long duration = 28 * TimeUtil.MICROSECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("28µs", formattedDuration);
	}

	@Test
	public void testShouldFormatSeconds() {
		final long duration = 10 * TimeUtil.SECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("10s", formattedDuration);
	}

	@Test
	public void testShouldFormatMinutes() {
		final long duration = 58 * TimeUtil.MINUTES;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("58min", formattedDuration);
	}

	@Test
	public void testShouldFormatMicrosecondsPlusNanoseconds() {
		final long duration = 28 * TimeUtil.MICROSECONDS + 9 * TimeUtil.NANOSECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("28µs 9ns", formattedDuration);
	}

	@Test
	public void testShouldFormatSecondsPlusMilliseconds() {
		final long duration = 10 * TimeUtil.SECONDS + 8 * TimeUtil.MILLISECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("10s 8ms", formattedDuration);
	}

	@Test
	public void testShouldRoundDownIfRemainderLessThanHalf() {
		final long duration = 23 * TimeUtil.MINUTES + 1 * TimeUtil.SECONDS + 499 * TimeUtil.MILLISECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("23min 1s", formattedDuration);
	}

	@Test
	public void testShouldRoundDownIfNanosecondsLessThanHalf() {
		final long duration = 23 * TimeUtil.MILLISECONDS + 1 * TimeUtil.MICROSECONDS + 499 * TimeUtil.NANOSECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("23ms 1µs", formattedDuration);
	}

	@Test
	public void testShouldIgnoreSmallerQualifiersWhenRounding() {
		final long duration = 23 * TimeUtil.MINUTES + 1 * TimeUtil.SECONDS + 501 * TimeUtil.MICROSECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("23min 1s", formattedDuration);
	}

	@Test
	public void testShouldRoundUpIfRemainderGreaterThanHalf() {
		final long duration = 42 * TimeUtil.MINUTES + 1 * TimeUtil.SECONDS + 501 * TimeUtil.MILLISECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("42min 2s", formattedDuration);
	}

	@Test
	public void testShouldRoundDownIfNanosecondsGreaterThanHalf() {
		final long duration = 42 * TimeUtil.MILLISECONDS + 1 * TimeUtil.MICROSECONDS + 501 * TimeUtil.NANOSECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("42ms 2µs", formattedDuration);
	}

	@Test
	public void testShouldRoundUpIfRemainderIsMidway() {
		final long duration = 42 * TimeUtil.MINUTES + 1 * TimeUtil.SECONDS + 500 * TimeUtil.MILLISECONDS;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("42min 2s", formattedDuration);
	}

	@Test
	public void testShouldNotFailIfDurationIsZero() {
		final long duration = 0L;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("0s", formattedDuration);
	}

	/**
	 *  Test for issue #82: TimerBase throws ArrayIndexOutOfBoundsException
	 *  if the measured duration is longer than one hour.
	 */
	@Test
	public void testShouldNotFailIfDurationNeedsLargestQuantifier() {
		final long duration = 1 * TimeUtil.HOURS + 1 * TimeUtil.MINUTES;

		final String formattedDuration = TimeUtil.formatDuration(duration);

		assertEquals("1h 1min", formattedDuration);
	}

}
