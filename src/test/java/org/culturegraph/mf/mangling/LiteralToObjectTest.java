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
package org.culturegraph.mf.mangling;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link LiteralToObject}.
 *
 * @author Christoph Böhme
 *
 */
public final class LiteralToObjectTest {

	private static final String LITERAL_NAME = "extract_this";
	private static final String LITERAL_VALUE1 =
			"I've been extracted from a record";
	private static final String LITERAL_VALUE2 =
			"I've been extracted from a record, too";

	@Mock
	private ObjectReceiver<String> receiver;

	private LiteralToObject literalToObject;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		literalToObject = new LiteralToObject();
		literalToObject.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		literalToObject.closeStream();
	}

	@Test
	public void shouldEmitLiteralValueAsObject() {
		literalToObject.setPattern(LITERAL_NAME);

		literalToObject.startRecord("");
		literalToObject.literal("L1", "V1");
		literalToObject.literal(LITERAL_NAME, LITERAL_VALUE1);
		literalToObject.literal("L2", "V2");
		literalToObject.endRecord();

		verify(receiver).process(LITERAL_VALUE1);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldEmitValueOfNestedLiteralsAsObject() {
		literalToObject.setPattern(LITERAL_NAME);

		literalToObject.startRecord("");
		literalToObject.startEntity("En1");
		literalToObject.literal(LITERAL_NAME, LITERAL_VALUE1);
		literalToObject.endEntity();
		literalToObject.endRecord();

		verify(receiver).process(LITERAL_VALUE1);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldUseRegExForMatchingLiteralNames() {
		literalToObject.setPattern("^ex_\\d$");

		literalToObject.startRecord("");
		literalToObject.literal("ex_1", LITERAL_VALUE1);
		literalToObject.literal("L1", "V1");
		literalToObject.literal("ex_2", LITERAL_VALUE2);
		literalToObject.endRecord();

		verify(receiver).process(LITERAL_VALUE1);
		verify(receiver).process(LITERAL_VALUE2);
		verifyNoMoreInteractions(receiver);
	}

}
