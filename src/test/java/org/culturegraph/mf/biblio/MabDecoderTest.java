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
package org.culturegraph.mf.biblio;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link MabDecoder}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (rewrite)
 *
 */
public final class MabDecoderTest {

	@Mock
	private StreamReceiver receiver;

	private MabDecoder mabDecoder;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mabDecoder = new MabDecoder();
		mabDecoder.setReceiver(receiver);
	}

	@Test
	public void shouldParseMabRecord() {
		mabDecoder.process("00068nM2.01200024      h" +
				"001 1234\u001E" +
				"705a\u001FaSubfield 1\u001FbSubfield 2\u001E" +
				"705b\u001FcSubfield 3\u001FdSubfield 4\u001E" +
				"\u001D");

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1234");
		ordered.verify(receiver).literal("001", "1234");
		ordered.verify(receiver).startEntity("705a");
		ordered.verify(receiver).literal("a", "Subfield 1");
		ordered.verify(receiver).literal("b", "Subfield 2");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("705b");
		ordered.verify(receiver).literal("c", "Subfield 3");
		ordered.verify(receiver).literal("d", "Subfield 4");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void shouldSkipWhitespaceOnlyInput() {
		mabDecoder.process("   ");
		mabDecoder.closeStream();

		verify(receiver).closeStream();
		verifyNoMoreInteractions(receiver);
	}

}
