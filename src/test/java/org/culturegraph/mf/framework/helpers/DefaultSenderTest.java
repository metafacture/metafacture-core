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
package org.culturegraph.mf.framework.helpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Test;

/**
 * Tests for class {@link DefaultSender}.
 *
 * @author Markus M Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class DefaultSenderTest {

	@Test
	public void shouldCallOnCloseStreamOnlyOnce() {
		final DefaultSender<StreamReceiver> defaultSender =
				spy(new DefaultSender<>());

		verify(defaultSender, never()).onCloseStream();
		assertFalse(defaultSender.isClosed());

		defaultSender.closeStream();

		verify(defaultSender, times(1)).onCloseStream();
		assertTrue(defaultSender.isClosed());

		defaultSender.closeStream();

		verify(defaultSender, times(1)).onCloseStream();
		assertTrue(defaultSender.isClosed());
	}

}
