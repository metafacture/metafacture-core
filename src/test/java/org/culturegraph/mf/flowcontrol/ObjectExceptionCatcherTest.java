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
package org.culturegraph.mf.flowcontrol;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link ObjectExceptionCatcher}.
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectExceptionCatcherTest {

	@Mock
	private ObjectReceiver<String> exceptionThrowingModule;

	private ObjectExceptionCatcher<String> exceptionCatcher;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		doThrow(new TestException("Exception Message"))
				.when(exceptionThrowingModule).process(anyString());
		exceptionCatcher = new ObjectExceptionCatcher<>();
		exceptionCatcher.setReceiver(exceptionThrowingModule);
	}

	@Test
	public void shouldCatchException() {
		exceptionCatcher.process("data");

		// Test passed if no exception is thrown by ObjectReceiver#process(T)
	}

	/**
	 * A special exception to make sure the test is not passed accidentally on a
	 * different exception.
	 */
	private static final class TestException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		TestException(final String msg) {
			super(msg);
		}

	}

}
