/*
 *  Copyright 2014 hbz, Fabian Steeg
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
package org.culturegraph.mf.csv;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link CsvDecoder}.
 *
 * @author Fabian steeg (fsteeg)
 *
 */
public final class CsvDecoderTest {

	private CsvDecoder decoder;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		decoder = new CsvDecoder(',');
		decoder.setHasHeader(true);
		decoder.setReceiver(receiver);
		decoder.process("h1,h2,h3");
	}

	@After
	public void cleanup() {
		decoder.closeStream();
	}

	@Test
	public void testSimple() {
		decoder.process("a,b,c");
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("h1", "a");
		ordered.verify(receiver).literal("h2", "b");
		ordered.verify(receiver).literal("h3", "c");
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testQuoted() {
		decoder.process("a,\"b1,b2,b3\",c");
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("h1", "a");
		ordered.verify(receiver).literal("h2", "b1,b2,b3");
		ordered.verify(receiver).literal("h3", "c");
		ordered.verify(receiver).endRecord();
	}

}
