/*
 * Copyright 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.pipe;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link RecordBounderyRemover}.
 *
 * @author Christoph Böhme
 */
public class RecordBounderyRemoverTest {

	@Mock
	private StreamReceiver receiver;

	private RecordBounderyRemover bounderyRemover;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		bounderyRemover = new RecordBounderyRemover();
		bounderyRemover.setReceiver(receiver);
	}

	@Test
	public void shouldRemoveStartAndEndRecordEvents() {
		bounderyRemover.startRecord("1");
		bounderyRemover.endRecord();

		verifyZeroInteractions(receiver);
	}

	@Test
	public void shouldForwardAllOtherEvents() {
		bounderyRemover.startRecord("1");
		bounderyRemover.startEntity("entity");
		bounderyRemover.literal("literal", "value");
		bounderyRemover.endEntity();
		bounderyRemover.endRecord();
		bounderyRemover.closeStream();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("entity");
		ordered.verify(receiver).literal("literal", "value");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).closeStream();
	}

}
