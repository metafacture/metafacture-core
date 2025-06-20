/*
 * Copyright 2024 Tobias Bülte, hbz
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
 * Tests for class {@link FindFixPaths}.
 *
 * @author Tobias Bülte
 *
 */
@ExtendWith(MockitoExtension.class)
public final class FindFixPathsTest {

    private final FindFixPaths finder = new FindFixPaths(".*ETL.*");

    @Mock
    private ObjectReceiver<String> receiver;

    public FindFixPathsTest() {
    }

    @Test
    public void testShouldFindPaths() {
        verify(
                "a\t|\tAn ETL test",
                "c.2\t|\tETL what?");
    }

    private void processRecord() {
        finder.setReceiver(receiver);
        finder.startRecord("1");
        finder.literal("a", "An ETL test");
        finder.literal("b", "");
        finder.literal("b", "Dummi");
        finder.literal("b", "Dog");
        finder.literal("c", "");
        finder.literal("c", "ETL what?");
        finder.endRecord();
        finder.startRecord("2");
        finder.literal("a", "An another test");
        finder.literal("b", "");
        finder.literal("b", "Dummi");
        finder.literal("b", "Dog");
        finder.literal("c", "");
        finder.literal("c", "ETL what?");
        finder.endRecord();
        finder.closeStream();
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
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
