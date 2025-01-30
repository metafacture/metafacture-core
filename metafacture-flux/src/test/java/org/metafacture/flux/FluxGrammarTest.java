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

import org.metafacture.commons.reflection.ReflectionException;
import org.metafacture.flux.parser.FluxProgramm;

import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Tests for the Flux grammar.
 *
 * @author Christoph Böhme
 */
public final class FluxGrammarTest {

    private ByteArrayOutputStream stdoutBuffer;
    private ByteArrayOutputStream stderrBuffer;

    public FluxGrammarTest() {
    }

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

        FluxCompiler.compile(createInputStream(script), Collections.emptyMap());

        Assert.assertEquals("", stderrBuffer.toString());
        Assert.assertEquals("", stdoutBuffer.toString());
    }

    @Test
    public void shouldAllowEmptyCommentInFile()
            throws RecognitionException, IOException {
        final String script = "\"test\"|print; //\n";

        FluxCompiler.compile(createInputStream(script), Collections.emptyMap());

        Assert.assertEquals("", stderrBuffer.toString());
        Assert.assertEquals("", stdoutBuffer.toString());
    }

    @Test
    public void shouldReplaceJavaEscapeSequences()
            throws IOException, RecognitionException {
        final String script =
                "\"quot=\\\" octal1=\\7 octal2=\\60 octal3=\\103 unicode=\\u00f8 tab=[\\t]\"" +
                        "|print;";

        final FluxProgramm program = FluxCompiler.compile(
                createInputStream(script), Collections.emptyMap());
        program.start();

        Assert.assertEquals("", stderrBuffer.toString());
        Assert.assertEquals("quot=\" octal1=\7 octal2=0 octal3=C unicode=\u00f8 tab=[\t]\n",
                stdoutBuffer.toString());
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenSemicolonInFlowIsMissing()
        throws RecognitionException, IOException {
        final String script = "\"test\"|print";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("mismatched input '<EOF>' expecting ';' in Flux", e.getMessage());
            throw e;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenSemicolonInVarDefIsMissing()
        throws RecognitionException, IOException {
        final String script = "foo=42";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("mismatched input '<EOF>' expecting ';' in Flux", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ReflectionException.class)
    public void issue421_shouldThrowReflectionExceptionWhenCommandIsNotFound()
        throws RecognitionException, IOException {
        final String script = "\"test\"|prin;";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final ReflectionException e) {
            Assert.assertEquals("Class not found: prin", e.getMessage());
            throw e;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenInputIsMissingAfterPipe1()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("no viable alternative at input '<EOF>' in Flux", e.getMessage());
            throw e;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenInputIsMissingAfterPipe2()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|;";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("no viable alternative at input ';' in Flux", e.getMessage());
            throw e;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenTeeStructureOccursWithouATeeCommand()
        throws RecognitionException, IOException {
        final String script = "\"test\"|{print}{print} ;";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("Flow cannot be split without a tee-element.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldThrowFluxParseExceptionWhenTeeIsNotASender()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|print|object-tee|{print}{print} ;";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("org.metafacture.io.ObjectStdoutWriter is not a sender", e.getMessage());
            throw e;
        }
    }

    @Test(expected = FluxParseException.class)
    public void issue421_shouldInsertMissingSymbolsWhenTeeIsStructurallyInvalid()
        throws RecognitionException, IOException {
        final String script =  "\"test\"|object-tee|{object-tee{print{print} ;";
        try {
            FluxCompiler.compile(createInputStream(script), Collections.emptyMap());
            final String tmp = stdoutBuffer.toString();
        }
        catch (final FluxParseException e) {
            Assert.assertEquals("missing '}' at '{' in Flux", e.getMessage());
            throw e;
        }
    }

    private ByteArrayInputStream createInputStream(final String script) {
        return new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
    }

}
