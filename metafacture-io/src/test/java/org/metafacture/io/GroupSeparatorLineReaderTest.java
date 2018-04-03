/*
 * Copyright 2018 Deutsche Nationalbibliothek
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
package org.metafacture.io;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.inOrder;

public class GroupSeparatorLineReaderTest
{
    private GroupSeparatorLineReader lineReader;
    private final String GROUP_SEPARATOR = "\u001D";

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        lineReader = new GroupSeparatorLineReader();
        lineReader.setReceiver(receiver);
    }

    @Test
    public void readMultipleGroupSeparatorSeparatedLines()
    {
        lineReader.process(new StringReader("a\na" + GROUP_SEPARATOR + "A" + GROUP_SEPARATOR + "\n" +
                "B" + GROUP_SEPARATOR + "\n" + "C" + GROUP_SEPARATOR + "\n"));

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("a\na" + GROUP_SEPARATOR);
        ordered.verify(receiver).process("A" + GROUP_SEPARATOR);
        ordered.verify(receiver).process("B" + GROUP_SEPARATOR);
        ordered.verify(receiver).process("C" + GROUP_SEPARATOR);
        ordered.verifyNoMoreInteractions();
    }

}
