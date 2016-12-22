/*
 *  Copyright 2015 Lars G. Svensson
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
package org.culturegraph.mf.biblio;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link AseqDecoder}.
 *
 * @author Lars G. Svensson
 *
 */
public final class AseqDecoderTest {

	private static final String LEADER_LITERAL = "LDR";

	private static final String RECORD_ID = "001304760";

	private static final String FIELD_LDR = " LDR   L 00235nM2.01000024------h";

	private static final String FIELD_001_a_TEST = " 001   L $$atest";

	private static final String FIELD_200_TEST = "001304760 200   L $$kAckermann-Gemeinde$$9(DE-588)39042-2";

	private static final String FIELD_MARKER = "\n";

	private AseqDecoder aseqDecoder;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.aseqDecoder = new AseqDecoder();
		this.aseqDecoder.setReceiver(this.receiver);
	}

	@After
	public void cleanup() {
		this.aseqDecoder.closeStream();
	}

	@Test
	public void shouldReturnRecordId() {
		this.aseqDecoder.process(RECORD_ID + FIELD_LDR);

		final InOrder ordered = inOrder(this.receiver);
		ordered.verify(this.receiver).startRecord(RECORD_ID);
	}

	@Test
	public void testShouldParseRecordStartingWithRecordMarker() {
		this.aseqDecoder.process(RECORD_ID + FIELD_LDR);

		final InOrder ordered = inOrder(this.receiver);
		ordered.verify(this.receiver).startRecord(RECORD_ID);
		verifyLdrTest(ordered);
		ordered.verify(this.receiver).endRecord();
	}

	@Test
	public void testShouldParseRecordWithTwoFields() {
		this.aseqDecoder.process(RECORD_ID + FIELD_LDR + FIELD_MARKER
				+ RECORD_ID + FIELD_001_a_TEST + FIELD_MARKER + FIELD_200_TEST);
		final InOrder ordered = inOrder(this.receiver);
		ordered.verify(this.receiver).startRecord(RECORD_ID);
		verifyLdrTest(ordered);
		verify001_a_Test(ordered);
		verify200(ordered);
		ordered.verify(this.receiver).endRecord();
	}

	private void verify200(final InOrder ordered) {
		ordered.verify(this.receiver).startEntity("200");
		ordered.verify(this.receiver, never())
				.literal("0", "01304760 200   L ");
		ordered.verify(this.receiver).literal("k", "Ackermann-Gemeinde");
		ordered.verify(this.receiver).literal("9", "(DE-588)39042-2");
		ordered.verify(this.receiver).endEntity();
	}

	private void verify001_a_Test(final InOrder ordered) {
		ordered.verify(this.receiver).startEntity("001");
		ordered.verify(this.receiver).literal("a", "test");
		ordered.verify(this.receiver).endEntity();
	}

	private void verifyLdrTest(final InOrder ordered) {
		ordered.verify(this.receiver).literal(LEADER_LITERAL,
				"00235nM2.01000024------h");
	}

}
