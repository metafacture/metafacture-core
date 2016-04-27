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
package org.culturegraph.mf.stream.converter;

import static org.junit.Assert.fail;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.stream.sink.EventList;
import org.culturegraph.mf.stream.sink.StreamValidator;
import org.junit.Test;


/**
 * Test {@link RegexDecoderTest}.
 *
 * @author Thomas Seidel, Christoph Böhme
 *
 */
public final class RegexDecoderTest {

	private static final String INPUT = "abc42defxyzzyghi23jklxxxxmno";

	private static final String GROUP_NAME_1 = "foo";
	private static final String GROUP_NAME_2 = "bar";

	private static final String DEFAULT_LITERAL_NAME = "defaultLiteralName";

	private static final String RECORD_ID = "28";

	@Test
	public void testRegex() {
		final EventList expected = new EventList();

		expected.startRecord("");
		expected.literal(DEFAULT_LITERAL_NAME, INPUT);
		expected.literal(GROUP_NAME_1, "42");
		expected.literal(GROUP_NAME_2, "xyzzy");
		expected.literal(GROUP_NAME_1, "23");
		expected.literal(GROUP_NAME_2, "xxxx");
		expected.endRecord();
		expected.closeStream();

		final RegexDecoder regexDecoder = new RegexDecoder("(?<foo>[0-9]+)[a-w]+(?<bar>[x-z]+)");
		regexDecoder.setDefaultLiteralName(DEFAULT_LITERAL_NAME);
		final StreamValidator validator = new StreamValidator(expected.getEvents());

		regexDecoder.setReceiver(validator);

		try {
			regexDecoder.process(INPUT);
			regexDecoder.closeStream();
		} catch (FormatException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testRecordId() {
		final EventList expected = new EventList();

		expected.startRecord(RECORD_ID);
		expected.literal(RegexDecoder.ID_CAPTURE_GROUP, RECORD_ID);
		expected.literal("data", "test");
		expected.endRecord();
		expected.closeStream();

		final RegexDecoder regexDecoder = new RegexDecoder("^RECORD-ID:(?<id>.*?),DATA:(?<data>.*?)$");
		final StreamValidator validator = new StreamValidator(expected.getEvents());

		regexDecoder.setReceiver(validator);

		try {
			regexDecoder.process("RECORD-ID:28,DATA:test");
			regexDecoder.closeStream();
		} catch (FormatException e) {
			fail(e.toString());
		}

	}

	@Test
	public void testIgnoreNonMatching() {
		final EventList expected = new EventList();

		expected.startRecord("");
		expected.literal("l", "v1");
		expected.endRecord();
		expected.startRecord("");
		expected.literal("l", "v2");
		expected.endRecord();
		expected.closeStream();

		final RegexDecoder regexDecoder = new RegexDecoder("^l:(?<l>.*?)$");
		final StreamValidator validator = new StreamValidator(expected.getEvents());

		regexDecoder.setReceiver(validator);

		try {
			regexDecoder.process("l:v1");
			regexDecoder.process("garbage should be ignored");
			regexDecoder.process("l:v2");
			regexDecoder.closeStream();
		} catch (FormatException e) {
			fail(e.toString());
		}
	}

}
