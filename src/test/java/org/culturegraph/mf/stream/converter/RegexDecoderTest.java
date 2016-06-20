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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link RegexDecoder}.
 *
 * @author Christoph Böhme (rewrite)
 * @author Thomas Seidel
 *
 */
public final class RegexDecoderTest {

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldEmitStartAndEndRecordForMatchingInput() {
		final RegexDecoder regexDecoder = new RegexDecoder(".*");
		regexDecoder.setReceiver(receiver);

		regexDecoder.process("matching input");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(any());
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldIgnoreNonMatchingInput() {
		final RegexDecoder regexDecoder = new RegexDecoder("abc");
		regexDecoder.setReceiver(receiver);

		regexDecoder.process("non-matching input");

		verifyZeroInteractions(receiver);
	}

	@Test
	public void shouldEmitLiteralContainingUnmodifiedInputIfDefaultLiteralNameIsSet() {
		final RegexDecoder regexDecoder = new RegexDecoder(".*");
		regexDecoder.setReceiver(receiver);
		regexDecoder.setDefaultLiteralName("input");

		regexDecoder.process("foo=1234,bar=abcd");

		verify(receiver).literal("input", "foo=1234,bar=abcd");
	}

	@Test
	public void shouldUseGroupNameAsLiteralNameForNamedCaptureGroups() {
		final RegexDecoder regexDecoder = new RegexDecoder(
				"foo=(?<foo>[0-9]+),bar=(?<bar>[a-z]+)");
		regexDecoder.setReceiver(receiver);

		regexDecoder.process("foo=1234,bar=abcd");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).literal("foo", "1234");
		ordered.verify(receiver).literal("bar", "abcd");
	}


	@Test
	public void shouldOutputLiteralsForEachMatchOfPattern() {
		final RegexDecoder regexDecoder = new RegexDecoder(
				"foo=(?<foo>[0-9]+),bar=(?<bar>[a-z]+)");
		regexDecoder.setReceiver(receiver);

		regexDecoder.process("foo=1234,bar=abcd,foo=5678,bar=efgh");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).literal("foo", "1234");
		ordered.verify(receiver).literal("bar", "abcd");
		ordered.verify(receiver).literal("foo", "5678");
		ordered.verify(receiver).literal("bar", "efgh");
	}

	@Test
	public void shouldIgnoreNonMatchingPartsOfInputString() {
		final RegexDecoder regexDecoder = new RegexDecoder(
				"foo=(?<foo>[0-9]+)");
		regexDecoder.setReceiver(receiver);

		regexDecoder.process("foo=1234,bar=abcd,foo=5678,bar=efgh");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).literal("foo", "1234");
		ordered.verify(receiver, never()).literal("bar", "abcd");
		ordered.verify(receiver).literal("foo", "5678");
		ordered.verify(receiver, never()).literal("bar", "efgh");
	}

	@Test
	public void shouldUseCaptureGroupNamedIdAsRecordId() {
		final RegexDecoder regexDecoder = new RegexDecoder("RECORD-ID:(?<id>.*)");
		regexDecoder.setReceiver(receiver);

		regexDecoder.process("RECORD-ID:id-123");

		verify(receiver).startRecord("id-123");
	}

}
