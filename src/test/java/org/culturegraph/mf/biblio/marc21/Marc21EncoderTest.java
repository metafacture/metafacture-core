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
package org.culturegraph.mf.biblio.marc21;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link Marc21Encoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class Marc21EncoderTest {

	private static final String LEADER_LITERAL = "leader";

	private Marc21Encoder marc21Encoder;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		marc21Encoder = new Marc21Encoder();
		marc21Encoder.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		marc21Encoder.closeStream();
	}

	@Test
	public void shouldOutputTopLevelLiteralsAsReferenceFields() {
		marc21Encoder.startRecord("");
		marc21Encoder.literal("001", "identifier");
		marc21Encoder.endRecord();

		verify(receiver).process(
				matches(".*001001100000\u001eidentifier\u001e.*"));
	}

	@Test
	public void shouldOutputEntitiesAsDataFields() {
		marc21Encoder.startRecord("");
		marc21Encoder.startEntity("021a ");
		marc21Encoder.endEntity();
		marc21Encoder.endRecord();

		verify(receiver).process(matches(".*021000300000\u001ea \u001e.*"));
	}

	@Test
	public void shouldOutputLiteralsInEntitiesAsSubfields() {
		marc21Encoder.startRecord("");
		marc21Encoder.startEntity("021a ");
		marc21Encoder.literal("v", "Fritz");
		marc21Encoder.literal("n", "Bauer");
		marc21Encoder.endEntity();
		marc21Encoder.endRecord();

		verify(receiver).process(
				matches(".*021001700000\u001ea \u001fvFritz\u001fnBauer\u001e.*"));
	}

	@Test(expected = FormatException.class)
	public void shouldThrowFormatExceptionIfEntityNameLengthIsNotFive() {
		marc21Encoder.startRecord("");
		marc21Encoder.startEntity("012abc");
	}

	@Test
	public void issue231ShouldIgnoreTypeLiterals() {
		marc21Encoder.startRecord("");
		marc21Encoder.literal("type", "ignoreme");
		marc21Encoder.endRecord();

		verify(receiver).process(any(String.class));
	}

}
