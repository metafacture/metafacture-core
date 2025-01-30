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

package org.metafacture.framework.helpers;

import org.metafacture.framework.StreamReceiver;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for class {@link DefaultSender}.
 *
 * @author Markus M Geipel
 * @author Christoph Böhme (refactored to Mockito)
 *
 */
public final class DefaultSenderTest {

    public DefaultSenderTest() {
    }

    @Test
    public void shouldCallOnCloseStreamOnlyOnce() {
        final DefaultSender<StreamReceiver> defaultSender =
                Mockito.spy(new DefaultSender<>());

        Mockito.verify(defaultSender, Mockito.never()).onCloseStream();
        Assert.assertFalse(defaultSender.isClosed());

        defaultSender.closeStream();

        Mockito.verify(defaultSender, Mockito.times(1)).onCloseStream();
        Assert.assertTrue(defaultSender.isClosed());

        defaultSender.closeStream();

        Mockito.verify(defaultSender, Mockito.times(1)).onCloseStream();
        Assert.assertTrue(defaultSender.isClosed());
    }

}
