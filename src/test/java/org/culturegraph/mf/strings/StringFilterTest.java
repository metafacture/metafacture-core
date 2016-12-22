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
package org.culturegraph.mf.strings;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link StringFilter}.
 *
 * @author Christoph Böhme
 *
 */
public final class StringFilterTest {

	private static final String[] RECORDS = {
		"Record 1: Data",
		"Record 42: Data",
		"Record 3: Data",
	};

	private static final String PATTERN = "\\d\\d";

	private StringFilter filter;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		filter = new StringFilter(PATTERN);
		filter.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		filter.closeStream();
	}

	@Test
	public void testShouldPassMatchingInput() {
		filter.setPassMatches(true);

		filter.process(RECORDS[0]);
		filter.process(RECORDS[1]);
		filter.process(RECORDS[2]);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORDS[1]);
		verifyNoMoreInteractions(receiver);
	}

	@Test
	public void testShouldPassNonMatchingInput() {
		filter.setPassMatches(false);

		filter.process(RECORDS[0]);
		filter.process(RECORDS[1]);
		filter.process(RECORDS[2]);

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(RECORDS[0]);
		ordered.verify(receiver).process(RECORDS[2]);
		verifyNoMoreInteractions(receiver);
	}

}
