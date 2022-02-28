/*
 * Copyright 2022 hbz NRW
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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
//import org.openjdk.jmh.profile.GCProfiler;
//import org.openjdk.jmh.profile.StackProfiler;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.RunnerException;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

@Fork(2)
@Warmup(iterations = 2)
@Measurement(iterations = 4) // checkstyle-disable-line MagicNumber
@BenchmarkMode(Mode.Throughput) // AverageTime
@OutputTimeUnit(TimeUnit.MILLISECONDS) // SECONDS
@State(Scope.Thread)
public class MetafixBenchmark {

    // TODO: Need to inject system properties into JMHTask's JavaExec process.
    //private static final boolean DEBUG_OUTPUT = Boolean.parseBoolean(System.getProperty("org.metafacture.metafix.debugBenchmarkOutput"));
    private static final boolean DEBUG_OUTPUT = false;

    private static final String BASE = "src/jmh/resources/org/metafacture/metafix";

    private static final String FIXES = BASE + "/fixes/%s" + Metafix.FIX_EXTENSION;
    private static final String INPUT = BASE + "/input/%s.json";

    @Param({ // checkstyle-disable-line AnnotationUseStyle
        "nothing"
    })
    private String fixDef;

    @Param({ // checkstyle-disable-line AnnotationUseStyle
        "empty"
    })
    private String input;

    private String fixFile;
    private String inputFile;

    private FileOpener fileOpener;

    public MetafixBenchmark() {
    }

    @Setup
    public void setup() throws FileNotFoundException {
        fixFile = String.format(FIXES, fixDef);
        inputFile = String.format(INPUT, input);

        final Metafix metafix = new Metafix(fixFile);

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

    @Benchmark
    public void baseline() {
        // this method was intentionally left blank.
    }

    @Benchmark
    public void parse() {
        FixStandaloneSetup.parseFix(fixFile);
    }

    @Benchmark
    public void process() {
        fileOpener.process(inputFile);
    }

    /*
    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(MetafixBenchmark.class.getSimpleName())
            .addProfiler(StackProfiler.class)
            //.addProfiler(GCProfiler.class)
            .build();

        new Runner(opt).run();
    }
    */

}
