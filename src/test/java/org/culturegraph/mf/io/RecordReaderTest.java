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
package org.culturegraph.mf.io;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.StringReader;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link RecordReader}.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordReaderTest {

	private static final String RECORD1 = "record1";
	private static final String RECORD2 = "record2";
	private static final String EMPTY_RECORD = "";
	private static final char SEPARATOR = ':';
	private static final char DEFAULT_SEPARATOR = '\u001d';

	private RecordReader recordReader;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		recordReader = new RecordReader();
		recordReader.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		recordReader.closeStream();
	}

	@Test
	public void testShouldProcessRecordsFollowedbySeparator() {
		recordReader.setSeparator(SEPARATOR);

		recordReader.process(new StringReader(
				RECORD1 + SEPARATOR +
				RECORD2 + SEPARATOR));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldProcessRecordsPrecededbySeparator() {
		recordReader.setSeparator(SEPARATOR);

		recordReader.process(new StringReader(
				SEPARATOR + RECORD1 +
				SEPARATOR + RECORD2));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldProcessRecordsSeparatedBySeparator() {
		recordReader.setSeparator(SEPARATOR);

		recordReader.process(new StringReader(
				RECORD1 + SEPARATOR +
				RECORD2));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldProcessSingleRecordWithoutSeparator() {
		recordReader.setSeparator(SEPARATOR);

		recordReader.process(new StringReader(
				RECORD1));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldNotEmitRecordIfInputIsEmpty() {
		recordReader.setSeparator(SEPARATOR);
		// Make sure empty records are
		// normally emitted:
		recordReader.setSkipEmptyRecords(false);

		recordReader.process(new StringReader(
				EMPTY_RECORD));

		verifyZeroInteractions(receiver);
	}

	@Test
	public void testShouldSkipEmptyRecordsByDefault() {
		recordReader.setSeparator(SEPARATOR);

		recordReader.process(new StringReader(
				RECORD1 + SEPARATOR +
				EMPTY_RECORD + SEPARATOR +
				RECORD2));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldOutputEmptyRecordsIfConfigured() {
		recordReader.setSeparator(SEPARATOR);
		recordReader.setSkipEmptyRecords(false);

		recordReader.process(new StringReader(
				RECORD1 + SEPARATOR +
				EMPTY_RECORD + SEPARATOR +
				RECORD2));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(EMPTY_RECORD);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldOutputEmptyRecordsAtStartOfInputIfConfigured() {
		recordReader.setSeparator(SEPARATOR);
		recordReader.setSkipEmptyRecords(false);

		recordReader.process(new StringReader(
				EMPTY_RECORD + SEPARATOR +
				RECORD1 + SEPARATOR +
				RECORD2));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(EMPTY_RECORD);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldOutputEmptyRecordsAtEndOfInputIfConfigured() {
		recordReader.setSeparator(SEPARATOR);
		recordReader.setSkipEmptyRecords(false);

		recordReader.process(new StringReader(
				RECORD1 + SEPARATOR +
				RECORD2 + SEPARATOR +
				EMPTY_RECORD));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		ordered.verify(receiver).process(EMPTY_RECORD);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldUseGlobalSeparatorAsDefaultSeparator() {
		recordReader.process(new StringReader(
				RECORD1 + DEFAULT_SEPARATOR +
				RECORD2 + DEFAULT_SEPARATOR));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver).process(RECORD2);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldProcessMultipleReaders() {
		recordReader.setSeparator(SEPARATOR);

		recordReader.process(new StringReader(
				RECORD1 + SEPARATOR +
				RECORD2));
		recordReader.process(new StringReader(
				RECORD2 + SEPARATOR +
				RECORD1));

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORD1);
		ordered.verify(receiver, times(2)).process(RECORD2);
		ordered.verify(receiver).process(RECORD1);
		verifyNoMoreInteractions(receiver);

	}

}
