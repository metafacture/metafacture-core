/*
 * Copyright 2025 hbz NRW
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

import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.io.FileOpener;
import org.metafacture.io.ObjectStdoutWriter;
import org.metafacture.io.RecordReader;
import org.metafacture.json.JsonDecoder;
import org.metafacture.json.JsonEncoder;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class AbstractMetafixBenchmark extends FixParseBenchmark { // checkstyle-disable-line ClassDataAbstractionCoupling

    // TODO: Need to inject system properties into JMHTask's JavaExec process.
    //private static final boolean DEBUG_OUTPUT = Boolean.parseBoolean(System.getProperty("org.metafacture.metafix.debugBenchmarkOutput"));
    private static final boolean DEBUG_OUTPUT = false;

    private static final String INPUT = BASE + "/input/%s.json";

    private FileOpener fileOpener;
    private String inputFile;

    /**
     * Creates an instance of {@link AbstractMetafixBenchmark}.
     */
    public AbstractMetafixBenchmark() {
    }

    @Override
    public void setup() {
        super.setup();

        inputFile = String.format(INPUT, getInput());

        final Metafix metafix;
        try {
            metafix = new Metafix(getFixFile());
        }
        catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        if (DEBUG_OUTPUT) {
            metafix
                .setReceiver(new JsonEncoder())
                .setReceiver(new ObjectStdoutWriter<String>());
        }
        else {
            metafix
                .setReceiver(new DefaultStreamReceiver());
        }

        fileOpener = new FileOpener();
        fileOpener
            .setReceiver(new RecordReader())
            .setReceiver(new JsonDecoder())
            .setReceiver(metafix);
    }

    @Override
    protected void workload() {
        fileOpener.process(inputFile);
    }

    protected abstract String getInput();

}
