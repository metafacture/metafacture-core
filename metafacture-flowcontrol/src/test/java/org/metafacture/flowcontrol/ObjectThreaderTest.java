/*
 * Copyright 2019 Pascal Christoph, hbz.
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
package org.metafacture.flowcontrol;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atLeast;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.flowcontrol.ObjectThreader;
import org.metafacture.framework.ObjectReceiver;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link ObjectThreader} (which itself uses
 * {@link org.metafacture.flowcontrol.ObjectPipeDecoupler} to thread receivers).
 *
 * @author Pascal Christoph (dr0i)
 *
 */
public final class ObjectThreaderTest {

	@Mock
	private ObjectReceiver<String> receiverThread1;
	@Mock
	private ObjectReceiver<String> receiverThread2;

	private final ObjectThreader<String> objectThreader = new ObjectThreader<>();
	private static final int ACTIVE_THREADS_AT_BEGINNING = Thread.getAllStackTraces().keySet().size();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		objectThreader//
				.addReceiver(receiverThread1)//
				.addReceiver(receiverThread2);
	}

	@Test
	public void shouldSplitAllObjectsToAllThreadedDownStreamReceivers() throws InterruptedException {
		objectThreader.process("a");
		objectThreader.process("b");
		objectThreader.process("a");
		objectThreader.process("c");
		// check if two more threads were indeed created
		assertThat(Thread.getAllStackTraces().keySet().size() - ACTIVE_THREADS_AT_BEGINNING).isEqualTo(2);
		objectThreader.closeStream();
		// verify thread 1
		verify(receiverThread1, atLeast(2)).process("a");
		verify(receiverThread1, atMost(0)).process("b");
		verify(receiverThread1, atMost(0)).process("c");
		// verify thread 2
		verify(receiverThread2, atMost(0)).process("a");
		verify(receiverThread2, atLeast(1)).process("b");
		verify(receiverThread2, atLeast(1)).process("c");
	}

}
