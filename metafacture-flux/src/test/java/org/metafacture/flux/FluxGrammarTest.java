/*
 * Copyright 2014 Deutsche Nationalbibliothek
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

package org.metafacture.flux;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.commons.reflection.ReflectionException;
import org.metafacture.flux.parser.FluxProgramm;

/**
 * Tests for the Flux grammar.
 *
 * @author Christoph Böhme
 */
public final class FluxGrammarTest {

    private ByteArrayOutputStream stdoutBuffer;
    private ByteArrayOutputStream stderrBuffer;

    @Before
    public void setup() {
        // Redirect standard out:
        stdoutBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdoutBuffer));

        // Redirect standard err:
        stderrBuffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderrBuffer));
    }

    @Test
    public void shouldAllowEmptyCommentInLastLineOfFile()
            throws RecognitionException, IOException {
        final String script = "\"test\"|print; //";

        FluxCompiler.compile(createInputStream(script), emptyMap());

        assertEquals("", stderrBuffer.toString());
        assertEquals("", stdoutBuffer.toString());
    }

    @Test
    public void shouldAllowEmptyCommentInFile()
            throws RecognitionException, IOException {
        final String script = "\"test\"|print; //\n";

        FluxCompiler.compile(createInputStream(script), emptyMap());

        assertEquals("", stderrBuffer.toString());
        assertEquals("", stdoutBuffer.toString());
    }

    @Test
    public void shouldReplaceJavaEscapeSequences()
            throws IOException, RecognitionException {
        final String script =
                "\"quot=\\\" octal1=\\7 octal2=\\60 octal3=\\103 unicode=\\u00f8 tab=[\\t]\"" +
                        "|print;";

        final FluxProgramm program = FluxCompiler.compile(
                createInputStream(script), emptyMap());
        program.start();

        assertEquals("", stderrBuffer.toString());
        assertEquals("quot=\" octal1=\7 octal2=0 octal3=C unicode=\u00f8 tab=[\t]\n",
                stdoutBuffer.toString());
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenSemicolonInFlowIsMissing()
        throws RecognitionException, IOException {
        final String script = "\"test\"|print";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (FluxParseException fpe) {
            assertEquals("mismatched input '<EOF>' expecting ';' in Flux", fpe.getMessage());
            throw fpe;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenSemicolonInVarDefIsMissing()
        throws RecognitionException, IOException {
        final String script = "foo=42";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (FluxParseException re) {
            assertEquals("mismatched input '<EOF>' expecting ';' in Flux", re.getMessage());
            throw re;
        }
    }

    @Test(expected = ReflectionException.class)
    public void issue421_shouldThrowReflectionExceptionWhenCommandIsNotFound()
        throws RecognitionException, IOException {
        final String script = "\"test\"|prin;";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (ReflectionException re) {
             assertEquals("Class not found: prin", re.getMessage());
            throw re;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenInputIsMissingAfterPipe1()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (FluxParseException re) {
            assertEquals("no viable alternative at input '<EOF>' in Flux", re.getMessage());
            throw re;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenInputIsMissingAfterPipe2()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|;";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (FluxParseException re) {
            assertEquals("no viable alternative at input ';' in Flux", re.getMessage());
            throw re;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenTeeStructureOccursWithouATeeCommand()
        throws RecognitionException, IOException {
        final String script = "\"test\"|{print}{print} ;";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (FluxParseException re) {
            assertEquals("Flow cannot be split without a tee-element.", re.getMessage());
            throw re;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenTeeIsNotASender()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|print|object-tee|{print}{print} ;";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
        } catch (FluxParseException re) {
            assertEquals("org.metafacture.io.ObjectStdoutWriter is not a sender", re.getMessage());
            throw re;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldInsertMissingSymbolsWhenTeeIsStructurallyInvalid()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|object-tee|{object-tee{print{print} ;";
        try {
            FluxCompiler.compile(createInputStream(script), emptyMap());
            String tmp=stdoutBuffer.toString();
        } catch (FluxParseException re) {
            assertEquals("missing '}' at '{' in Flux", re.getMessage());
            throw re;
        }
    }

    private ByteArrayInputStream createInputStream(String script) {
        return new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
    }

}
