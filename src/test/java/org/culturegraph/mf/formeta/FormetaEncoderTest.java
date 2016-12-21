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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.formeta.formatter.FormatterStyle;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link FormetaEncoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class FormetaEncoderTest {

	private static final String CONCISE_RECORD =
			"1{lit1:value 1,' ent1'{lit2:value \\{x\\},lit\\\\3:'value 2 '}lit4:value \\'3\\'}";

	private static final String VERBOSE_RECORD =
			"1{ lit1: 'value 1', ' ent1'{ lit2: 'value {x}', 'lit\\\\3': 'value 2 ' }, lit4: 'value \\'3\\'' }";

	private static final String MULTILINE_RECORD =
			"'1' {\n" +
			"\t'lit1': 'value 1',\n" +
			"\t' ent1' {\n" +
			"\t\t'lit2': 'value {x}',\n" +
			"\t\t'lit\\\\3': 'value 2 '\n" +
			"\t},\n" +
			"\t'lit4': 'value \\'3\\''\n" +
			"}";

	private FormetaEncoder encoder;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		encoder = new FormetaEncoder();
		encoder.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		encoder.closeStream();
	}

	@Test
	public void testShouldOutputConciseRecordRepresentation() {
		encoder.setStyle(FormatterStyle.CONCISE);

		executeEvents();

		verify(receiver).process(CONCISE_RECORD);
	}

	@Test
	public void testShouldOutputVerboseRecordRepresentation() {
		encoder.setStyle(FormatterStyle.VERBOSE);

		executeEvents();

		verify(receiver).process(VERBOSE_RECORD);
	}

	@Test
	public void testShouldOutputMultilineRecordRepresentation() {
		encoder.setStyle(FormatterStyle.MULTILINE);

		executeEvents();

		verify(receiver).process(MULTILINE_RECORD);
	}

	@Test
	public void testShouldIgnoreIncompleteRecord() {
		encoder.setStyle(FormatterStyle.CONCISE);

		encoder.startRecord("incomplete");
		encoder.literal("lit", "value");
		encoder.startEntity("entity");
		executeEvents();

		verify(receiver).process(CONCISE_RECORD);
		verifyNoMoreInteractions(receiver);
	}

	private void executeEvents() {
		encoder.startRecord("1");
		encoder.literal("lit1", "value 1");
		encoder.startEntity(" ent1");
		encoder.literal("lit2", "value {x}");
		encoder.literal("lit\\3", "value 2 ");
		encoder.endEntity();
		encoder.literal("lit4", "value '3'");
		encoder.endRecord();
	}

}
