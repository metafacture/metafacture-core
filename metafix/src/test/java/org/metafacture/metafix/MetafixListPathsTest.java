/*
 * Copyright 2023 Fabian Steeg, hbz
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

package org.metafacture.metafix;

import org.metafacture.framework.ObjectReceiver;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.base.MockitoAssertionError;

/**
 * Tests for class {@link MetafixListPaths}.
 *
 * @author Fabian Steeg
 *
 */
public final class MetafixListPathsTest {

    private MetafixListPaths lister;

    @Mock
    private ObjectReceiver<String> receiver;

    public MetafixListPathsTest() {
        MockitoAnnotations.initMocks(this);
        lister = new MetafixListPaths();
    }

    @Test
    public void testShouldListPaths() {
        processRecord();
        verify("a.*\t 3");
    }

    @Test
    public void testShouldListPathsNoCount() {
        lister.setCount(false);
        processRecord();
        verify("a.*");
    }

    @Test
    public void testShouldListPathsUseIndex() {
        lister.setIndex(true);
        processRecord();
        verify("a.1\t 1");
        verify("a.2\t 1");
        verify("a.3\t 1");
    }

    @Test
    public void testShouldListPathsNoCountUseIndex() {
        lister.setCount(false);
        lister.setIndex(true);
        processRecord();
        verify("a.1");
        verify("a.2");
        verify("a.3");
    }

    private void processRecord() {
        lister.setReceiver(receiver);
        lister.startRecord("");
        lister.literal("a", "A");
        lister.literal("a", "B");
        lister.literal("a", "C");
        lister.endRecord();
        lister.closeStream();
    }

    private void verify(final String result) throws MockitoAssertionError {
        try {
            Mockito.verify(receiver).process(result);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
