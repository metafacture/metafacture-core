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

package org.metafacture.io;

import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Tests for class {@link ObjectStdoutWriter}.
 *
 * @author Christoph Böhme
 *
 */
public final class ObjectStdoutWriterTest
        extends AbstractConfigurableObjectWriterTest {

    private ObjectStdoutWriter<String> writer;

    private ByteArrayOutputStream stdoutBuffer;

    public ObjectStdoutWriterTest() {
    }

    @Before
    public void setup() {
        writer = new ObjectStdoutWriter<String>();

        // Redirect standard out:
        stdoutBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdoutBuffer));
    }

    @Override
    protected ConfigurableObjectWriter<String> getWriter() {
        return writer;
    }

    @Override
    protected String getOutput() throws IOException {
        System.out.flush();
        return stdoutBuffer.toString();
    }

}
