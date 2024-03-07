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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for class {@link ListFixValues}.
 *
 * @author Fabian Steeg
 *
 */
@ExtendWith(MockitoExtension.class)
public final class ListFixValuesTest {

    private ListFixValues lister;

    @Mock
    private ObjectReceiver<String> receiver;

    public ListFixValuesTest() {
    }

    @Test
    public void testShouldListPathsSimple() {
        lister = new ListFixValues("a");
        verify("2\t|\taA");
    }

    @Test
    public void testShouldListPathsWildcard() {
        lister = new ListFixValues("a.*");
        verify("2\t|\taA");
    }

    @Test
    public void testShouldListPathsIndex() {
        lister = new ListFixValues("a.1");
        verify("1\t|\taA");
    }

    @Test
    public void testShouldListPathsTwoValues() {
        lister = new ListFixValues("b");
        verify(
                "2\t|\tbB",
                "1\t|\tbA");
    }

    @Test
    public void testShouldListPathsTwoValuesWildcard() {
        lister = new ListFixValues("b.*");
        verify(
            "2\t|\tbB",
            "1\t|\tbA");
    }

    @Test
    public void testShouldListPathsTwoValuesIndex() {
        lister = new ListFixValues("b.1");
        verify(
            "1\t|\tbB");
    }

    @Test
    public void testShouldListPathsSortCount() {
        lister = new ListFixValues("c");
        verify(
            "3\t|\tcC",
            "1\t|\tcA",
            "1\t|\tcB");
    }

    @Test
    public void testShouldListPathsDontCount() {
        lister = new ListFixValues("c");
        lister.setCount(false);
        verify(
            "cA",
            "cB",
            "cC");
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
        lister.literal("c", "cB");
        lister.literal("c", "cC");
        lister.literal("c", "cA");
        lister.literal("c", "cC");
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
            ordered.verify(receiver, Mockito.times(2)).closeStream();
            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
