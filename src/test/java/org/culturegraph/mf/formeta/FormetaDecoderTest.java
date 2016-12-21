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
package org.culturegraph.mf.formeta;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link FormetaDecoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class FormetaDecoderTest {

	private static final String RECORD =
			"1{lit1:value 1,' ent1'{lit2:value \\{x\\},lit\\\\3:'value 2 '}lit4:value \\'3\\'}";

	private FormetaDecoder decoder;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		decoder = new FormetaDecoder();
		decoder.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		decoder.closeStream();
	}

	@Test
	public void testShouldProcessRecords() {
		decoder.process(RECORD);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("lit1", "value 1");
		ordered.verify(receiver).startEntity(" ent1");
		ordered.verify(receiver).literal("lit2", "value {x}");
		ordered.verify(receiver).literal("lit\\3", "value 2 ");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).literal("lit4", "value '3'");
		ordered.verify(receiver).endRecord();
	}

}
