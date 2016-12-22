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
package org.culturegraph.mf.strings;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link LineSplitter}.
 *
 * @author Christoph Böhme
 *
 */
public final class LineSplitterTest {

	private static final String PART1 = "One";
	private static final String PART2 = "Two";
	private static final String PART3 = "Three";

	private LineSplitter lineSplitter;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		lineSplitter = new LineSplitter();
		lineSplitter.setReceiver(receiver);
	}

	@Test
	public void shouldSplitInputStringAtNewLines() {
		lineSplitter.process(PART1 + "\n" + PART2 + "\n" + PART3);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(PART1);
		ordered.verify(receiver).process(PART2);
		ordered.verify(receiver).process(PART3);
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldPassInputWithoutNewLinesUnchanged() {
		lineSplitter.process(PART1);

		verify(receiver).process(PART1);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void shouldOutputEmptyStringsForSequencesOfNewLines() {
		lineSplitter.process(PART1 + "\n\n" + PART2);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(PART1);
		ordered.verify(receiver).process("");
		ordered.verify(receiver).process(PART2);
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldOutputEmptyStringForNewLinesAtStartOfTheInput() {
		lineSplitter.process("\n" + PART1);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process("");
		ordered.verify(receiver).process(PART1);
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void shouldNotOutputEmptyStringForNewLinesAtEndOfTheInput() {
		lineSplitter.process(PART1 + "\n");

		verify(receiver).process(PART1);
		verifyNoMoreInteractions(receiver);
	}

}
