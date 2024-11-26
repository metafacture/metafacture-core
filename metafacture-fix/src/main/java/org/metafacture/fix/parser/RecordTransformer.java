/*
 * Copyright 2024 Fabian Steeg, hbz
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

package org.metafacture.fix.parser;

/**
 * @author Fabian Steeg
 *
 */
// For Flux, the equivalent of this class is `FluxProgramm`. In metafacture-fix, we have RecordTransformer, to be rewritten for ANTLR here.
// See https://github.com/metafacture/metafacture-fix/blob/master/metafix/src/main/java/org/metafacture/metafix/RecordTransformer.java
@SuppressWarnings("checkstyle:MissingCtor")
public final class RecordTransformer {
    /**
     * @param name The method name
     * @param param The param passed to the method
     */
    // sample method called from the tree grammar / RecordTransformerBuilder
    public void processMethod(final String name, final String param) {
        System.out.printf("Called processMethod: %s with %s\n", name, param);
    }

}
