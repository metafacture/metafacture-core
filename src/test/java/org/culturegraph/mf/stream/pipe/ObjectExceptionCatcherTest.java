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
package org.culturegraph.mf.stream.pipe;



import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Tests for class {@link ObjectExceptionCatcher}.
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectExceptionCatcherTest {

	private static final String OBJECT_STRING = "TEST OBJECT REPRESENTATION";
	private static final String EXCEPTION_MESSAGE = "TEST EXCEPTION MESSSAGE";

	/**
	 * A special exception to make sure the test
	 * is not passed accidentally on a different
	 * exception.
	 */
	private static final class TestException
		extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public TestException(final String msg) {
			super(msg);
		}

	}

	private ObjectExceptionCatcher<String> systemUnderTest;

	@Mock
	private ObjectReceiver<String> exceptionThrowingModule;

	@Mock
	private Appender logAppender;
	@Captor
	private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		doThrow(new TestException(EXCEPTION_MESSAGE))
				.when(exceptionThrowingModule).process(anyString());

		final Logger logger = Logger.getLogger(ObjectExceptionCatcher.class);
		logger.addAppender(logAppender);

		systemUnderTest = new ObjectExceptionCatcher<String>();
		systemUnderTest.setReceiver(exceptionThrowingModule);
	}

	@After
	public void cleanup() {
		final Logger logger = Logger.getLogger(ObjectExceptionCatcher.class);
		logger.removeAppender(logAppender);
	}

	@Test
	public void shouldCatchAndLogException() {
		systemUnderTest.process(OBJECT_STRING);

		verify(logAppender).doAppend(loggingEventCaptor.capture());
		final LoggingEvent loggingEvent = loggingEventCaptor.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.ERROR));
		assertThat(loggingEvent.getRenderedMessage(), containsString(EXCEPTION_MESSAGE));
		assertThat(loggingEvent.getRenderedMessage(), containsString(OBJECT_STRING));
	}

	@Test
	public void shouldLogStackTraceIfConfigured() {
		systemUnderTest.setLogStackTrace(true);

		systemUnderTest.process(OBJECT_STRING);

		verify(logAppender, times(2)).doAppend(loggingEventCaptor.capture());
		final LoggingEvent loggingEvent = loggingEventCaptor.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.ERROR));
		assertThat(loggingEvent.getRenderedMessage(), containsString("Stack Trace"));
	}

}
