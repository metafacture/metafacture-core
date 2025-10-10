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

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class FixParseBenchmark extends AbstractBenchmark {

    protected static final String BASE = "src/jmh/resources/org/metafacture/metafix";

    private static final String FIXES = BASE + "/fixes/%s" + Metafix.FIX_EXTENSION;

    private String fixFile;

    @Param({ // checkstyle-disable-line AnnotationUseStyle
        "nothing",
        "alma"
    })
    private String fixDef;

    /**
     * Creates an instance of {@link FixParseBenchmark}.
     */
    public FixParseBenchmark() {
    }

    /**
     * Performs the setup.
     */
    @Setup
    public void setup() {
        fixFile = String.format(FIXES, fixDef);
    }

    @Override
    protected void workload() {
        FixStandaloneSetup.parseFix(getFixFile());
    }

    protected String getFixFile() {
        return fixFile;
    }

}
