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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
//import org.openjdk.jmh.profile.GCProfiler;
//import org.openjdk.jmh.profile.StackProfiler;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.RunnerException;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(2)
@Warmup(iterations = 2)
@Measurement(iterations = 4) // checkstyle-disable-line MagicNumber
@BenchmarkMode(Mode.Throughput) // AverageTime
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public abstract class AbstractBenchmark {

    public AbstractBenchmark() {
    }

    @Benchmark
    public void benchmark() {
        workload();
    }

    protected abstract void workload();

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
