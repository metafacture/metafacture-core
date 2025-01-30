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

package org.metafacture.flowcontrol;

import org.metafacture.framework.ObjectReceiver;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link CloseSuppressor}.
 *
 * @author Markus Geipel
 *
 */
public final class CloseSuppressorTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectReceiver<Object> receiver;

    public CloseSuppressorTest() {
    }

    @Test
    public void testSuppression() {
        final CloseSuppressor<Object> supressor = new CloseSuppressor<>(3);
        supressor.setReceiver(receiver);
        supressor.closeStream();
        supressor.closeStream();
        Mockito.verifyZeroInteractions(receiver);
        supressor.closeStream();
        Mockito.verify(receiver).closeStream();
        supressor.closeStream();
        Mockito.verifyNoMoreInteractions(receiver);
    }

}
