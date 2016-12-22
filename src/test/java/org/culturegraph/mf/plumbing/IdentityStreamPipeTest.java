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
package org.culturegraph.mf.plumbing;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link IdentityStreamPipe}.
 *
 * @author Christoph Böhme
 *
 */
public final class IdentityStreamPipeTest {

	private static final String RECORD_ID1 = "Re1";
	private static final String RECORD_ID2 = "Re2";
	private static final String LITERAL_NAME1 = "Li1";
	private static final String LITERAL_NAME2 = "Li2";
	private static final String LITERAL_VALUE1 = "Va1";
	private static final String LITERAL_VALUE2 = "Va2";
	private static final String ENTITY1 = "En1";

	private IdentityStreamPipe identityPipe;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		identityPipe = new IdentityStreamPipe();
		identityPipe.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		identityPipe.closeStream();
	}

	@Test
	public void testShouldNotChangeTheStreamInAnyWay() {
		identityPipe.startRecord(RECORD_ID1);
		identityPipe.literal(LITERAL_NAME1, LITERAL_VALUE1);
		identityPipe.startEntity(ENTITY1);
		identityPipe.literal(LITERAL_NAME2, LITERAL_VALUE2);
		identityPipe.endEntity();
		identityPipe.endRecord();
		identityPipe.startRecord(RECORD_ID2);
		identityPipe.literal(LITERAL_NAME1, LITERAL_VALUE1);
		identityPipe.literal(LITERAL_NAME1, LITERAL_VALUE1);
		identityPipe.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord(RECORD_ID1);
		ordered.verify(receiver).literal(LITERAL_NAME1, LITERAL_VALUE1);
		ordered.verify(receiver).startEntity(ENTITY1);
		ordered.verify(receiver).literal(LITERAL_NAME2, LITERAL_VALUE2);
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord(RECORD_ID2);
		ordered.verify(receiver, times(2)).literal(LITERAL_NAME1, LITERAL_VALUE1);
		ordered.verify(receiver).endRecord();
	}

}
