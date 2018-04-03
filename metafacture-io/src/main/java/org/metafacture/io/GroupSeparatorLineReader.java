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

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Scanner;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

@Description("Processes input from a reader line by line, with group separator as line end.")
@In(Reader.class)
@Out(String.class)
@FluxCommand("as-gs-lines")
public final class GroupSeparatorLineReader extends DefaultObjectPipe<Reader, ObjectReceiver<String>>
{

    private static final int BUFFER_SIZE = 1024 * 1024 * 16;

    private static final String GroupSeparator = "\u001D";

    @Override
    public void process(final Reader reader) {
        assert !isClosed();
        assert null!=reader;
        process(reader, getReceiver());
    }

    public static void process(final Reader reader, final ObjectReceiver<String> receiver)
    {
        Scanner scanner = new Scanner(new BufferedReader(reader, BUFFER_SIZE));
        scanner.useDelimiter(GroupSeparator);

        try
        {
            while (scanner.hasNext())
            {
                String record = scanner.next();
                if (record.startsWith("\n"))
                {
                    record = record.substring(1);
                }
                if (!record.trim().isEmpty())
                {
                    receiver.process(record + GroupSeparator);
                }
            }
        }
        catch (Exception e)
        {
            throw new MetafactureException(e);
        }
    }
}
