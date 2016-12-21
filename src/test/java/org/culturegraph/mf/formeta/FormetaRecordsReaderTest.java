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
package org.culturegraph.mf.formeta;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.StringReader;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link FormetaRecordsReader}.
 *
 * @author Christoph Böhme
 *
 */
public final  class FormetaRecordsReaderTest {

	private static final String SINGLE_RECORD = "l: v";

	private static final String RECORD_LITERAL = " l: v,";
	private static final String RECORD_GROUP = " r{l: v}";
	private static final String RECORD_NESTED_GROUP = " r{ e { l: v } }";
	private static final String RECORD_QUOTED_LITERAL = " 'l x': v,";
	private static final String RECORD_LEFT_BRACE_IN_QUOTES = " '{': l,";
	private static final String RECORD_RIGHT_BRACE_IN_QUOTES = " r{ l: '}' }";
	private static final String RECORD_COLON_IN_QUOTES = " ':': v,";
	private static final String RECORD_COMMA_IN_QUOTES = " l: ',v:v',";
	private static final String RECORD_ESCAPED_LEFT_BRACE = " \\{: v,";
	private static final String RECORD_ESCAPED_RIGHT_BRACE = " r{ l: \\} }";
	private static final String RECORD_ESCAPED_COLON = " \\:: v,";
	private static final String RECORD_ESCAPED_COMMA = " l: \\,v\\:v,";
	private static final String RECORD_ESCAPED_QUOTE = " '\\',': v";

	private FormetaRecordsReader formetaRecordsReader;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		formetaRecordsReader = new FormetaRecordsReader();
		formetaRecordsReader.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		formetaRecordsReader.closeStream();
	}

	@Test
	public void shouldProcessSingleRecord() {
		final StringReader reader = new StringReader(SINGLE_RECORD);

		formetaRecordsReader.process(reader);

		verify(receiver).process(SINGLE_RECORD);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldSplitBetweenTopLevelElements() {
		final String records = RECORD_LITERAL +
				RECORD_GROUP +
				RECORD_NESTED_GROUP +
				RECORD_QUOTED_LITERAL +
				RECORD_LEFT_BRACE_IN_QUOTES +
				RECORD_RIGHT_BRACE_IN_QUOTES +
				RECORD_COLON_IN_QUOTES +
				RECORD_COMMA_IN_QUOTES +
				RECORD_ESCAPED_LEFT_BRACE +
				RECORD_ESCAPED_RIGHT_BRACE +
				RECORD_ESCAPED_COLON +
				RECORD_ESCAPED_COMMA +
				RECORD_ESCAPED_QUOTE;

		final StringReader reader = new StringReader(records);

		formetaRecordsReader.process(reader);

		verify(receiver).process(RECORD_LITERAL);
		verify(receiver).process(RECORD_GROUP);
		verify(receiver).process(RECORD_NESTED_GROUP);
		verify(receiver).process(RECORD_QUOTED_LITERAL);
		verify(receiver).process(RECORD_LEFT_BRACE_IN_QUOTES);
		verify(receiver).process(RECORD_RIGHT_BRACE_IN_QUOTES);
		verify(receiver).process(RECORD_COLON_IN_QUOTES);
		verify(receiver).process(RECORD_COMMA_IN_QUOTES);
		verify(receiver).process(RECORD_ESCAPED_LEFT_BRACE);
		verify(receiver).process(RECORD_ESCAPED_RIGHT_BRACE);
		verify(receiver).process(RECORD_ESCAPED_COLON);
		verify(receiver).process(RECORD_ESCAPED_COMMA);
		verify(receiver).process(RECORD_ESCAPED_QUOTE);
		verifyNoMoreInteractions(receiver);
	}
}
