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
import org.mockito.InOrder;
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
        verify(
            "3\t|\tc.*",
            "2\t|\tb.*",
            "1\t|\ta");
    }

    @Test
    public void testShouldListPathsNoCount() {
        lister.setCount(false);
        verify(
            "a",
            "b.*",
            "c.*");
    }

    @Test
    public void testShouldListPathsUseIndex() {
        lister.setIndex(true);
        verify(
            "1\t|\ta",
            "1\t|\tb.1",
            "1\t|\tb.2",
            "1\t|\tc.1",
            "1\t|\tc.2",
            "1\t|\tc.3");
    }

    @Test
    public void testShouldListPathsNoCountUseIndex() {
        lister.setCount(false);
        lister.setIndex(true);
        verify(
            "a",
            "b.1",
            "b.2",
            "c.1",
            "c.2",
            "c.3");
    }

    @Test
    public void testShouldListPathsSortedByFrequency() {
        verify(
            "3\t|\tc.*",
            "2\t|\tb.*",
            "1\t|\ta");
    }

    private void processRecord() {
        lister.setReceiver(receiver);
        lister.startRecord("");
        lister.literal("a", "");
        lister.literal("b", "");
        lister.literal("b", "");
        lister.literal("c", "");
        lister.literal("c", "");
        lister.literal("c", "");
        lister.endRecord();
        lister.closeStream();
    }

    private void verify(final String... result) throws MockitoAssertionError {
        processRecord();
        try {
            final InOrder ordered = Mockito.inOrder(receiver);
            for (final String r : result) {
                ordered.verify(receiver).process(r);
            }
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
