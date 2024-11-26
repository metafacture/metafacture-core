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

package org.metafacture.fix;

import org.metafacture.fix.parser.FixLexer;
import org.metafacture.fix.parser.FixParser;
import org.metafacture.fix.parser.RecordTransformer;
import org.metafacture.fix.parser.RecordTransformerBuilder;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Creates a flow based on a flux script.
 *
 * @author Fabian Steeg
 */
// This is based on FluxCompiler, but using the ANTLR Fix draft. To be replaced by the Metafix class from metafacture-fix.
// See https://github.com/metafacture/metafacture-fix/blob/master/metafix/src/main/java/org/metafacture/metafix/Metafix.java
public final class Metafix {
    private Metafix() {
        // no instances
    }

    /**
     * Compiles the Fix to a RecordTransformer.
     *
     * @see RecordTransformer
     * @param fix the Fix
     * @param vars the variables of the Fix
     * @return the RecordTransformer
     * @throws RecognitionException if an ANTLR exception occurs
     * @throws IOException          if an I/O error occurs
     */
    public static RecordTransformer compile(final InputStream fix, final Map<String, String> vars) throws RecognitionException, IOException {
        return compileFix(compileAst(fix), vars);
    }

    private static CommonTreeNodeStream compileAst(final InputStream flowDef) throws IOException, RecognitionException {
        final FixParser parser = new FixParser(new CommonTokenStream(new FixLexer(new ANTLRInputStream(flowDef))));
        return new CommonTreeNodeStream(parser.fix().getTree());
    }

    private static RecordTransformer compileFix(final CommonTreeNodeStream treeNodes, final Map<String, String> vars)
            throws RecognitionException {
        final RecordTransformerBuilder builder = new RecordTransformerBuilder(treeNodes);
        return builder.transformer();
    }
}
