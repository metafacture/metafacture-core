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
package org.culturegraph.mf.mangling;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link ObjectToLiteral}.
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectToLiteralTest {

	private static final String LITERAL_NAME = "myObject";
	private static final String OBJ_DATA = "This is a data object";

	private ObjectToLiteral<String> objectToLiteral;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		objectToLiteral = new ObjectToLiteral<String>();
		objectToLiteral.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		objectToLiteral.closeStream();
	}

	@Test
	public void testShouldUseObjectAsLiteralValue() {

		objectToLiteral.process(OBJ_DATA);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal(ObjectToLiteral.DEFAULT_LITERAL_NAME, OBJ_DATA);
		ordered.verify(receiver).endRecord();
	}

	@Test
	public void testShouldUseCustomLiteralName() {
		objectToLiteral.setLiteralName(LITERAL_NAME);

		objectToLiteral.process(OBJ_DATA);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).literal(LITERAL_NAME, OBJ_DATA);
		ordered.verify(receiver).endRecord();
	}

}
