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
public final class MetafixListValuesTest {

    private MetafixListValues lister;

    @Mock
    private ObjectReceiver<String> receiver;

    public MetafixListValuesTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testShouldListPathsA() {
        lister = new MetafixListValues("a");
        verify("2\t aA");
    }

    @Test
    public void testShouldListPathsB() {
        lister = new MetafixListValues("b");
        verify(
            "2\t bB",
            "1\t bA");
    }

    @Test
    public void testShouldListPathsC() {
        lister = new MetafixListValues("c");
        verify(
            "3\t cC",
            "1\t cA",
            "1\t cB");
    }

    @Test
    public void testShouldListPathsDontCount() {
        lister = new MetafixListValues("c");
        lister.setCount(false);
        verify(
            "cC",
            "cB",
            "cA");
    }

    private void processRecord() {
        lister.setReceiver(receiver);
        lister.startRecord("");
        lister.literal("a", "aA");
        lister.literal("a", "aA");
        lister.literal("b", "bB");
        lister.literal("b", "bB");
        lister.literal("b", "bA");
        lister.literal("c", "cC");
        lister.literal("c", "cC");
        lister.literal("c", "cC");
        lister.literal("c", "cB");
        lister.literal("c", "cA");
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
