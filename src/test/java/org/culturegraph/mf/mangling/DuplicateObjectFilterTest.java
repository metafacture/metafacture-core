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

/**
 *
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
 * Tests for class {@link DuplicateObjectFilter}.
 *
 * @author Alexander Haffner
 *
 */
public final class DuplicateObjectFilterTest {

	private static final String OBJECT1 = "Object 1";
	private static final String OBJECT2 = "Object 2";

	private DuplicateObjectFilter<String> duplicateObjectFilter;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		duplicateObjectFilter = new DuplicateObjectFilter<String>();
		duplicateObjectFilter.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		duplicateObjectFilter.closeStream();
	}

	@Test
	public void testShouldEliminateDuplicateObjects() {
		duplicateObjectFilter.process(OBJECT1);
		duplicateObjectFilter.process(OBJECT1);
		duplicateObjectFilter.process(OBJECT2);

		verify(receiver).process(OBJECT1);
		verify(receiver).process(OBJECT2);
		verifyNoMoreInteractions(receiver);
	}

}
