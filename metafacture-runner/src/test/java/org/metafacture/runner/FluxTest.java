/*
 * Copyright 2026 hbz
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

package org.metafacture.runner;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for class {@link Flux}.
 *
 * @author Jens Wille
 */
public class FluxTest {

    private static final String DEPRECATION_NOTICE = "DEPRECATION NOTICE";
    private static final String FILE_NOT_FOUND = "File not found: ";
    private static final String LIST_COMMANDS_OUTPUT = "Available Flux commands";
    private static final String MISSING_PARAMETER = "Missing required parameter for option '%s'";
    private static final String UNMATCHED_ARGUMENT = "Unmatched argument at index";

    private AtomicBoolean ranFlux = new AtomicBoolean();
    private CommandLine commandLine;

    public FluxTest() {
    }

    @Before
    public void init() throws Exception {
        ranFlux.set(false);

        final Flux flux = Mockito.spy(Flux.class);
        Mockito.doAnswer(AdditionalAnswers.answerVoid((s, i) -> ranFlux.set(true))).when(flux).runFlux(Mockito.any(), Mockito.any());

        commandLine = new CommandLine(flux);
    }

    @Test
    public void shouldAcceptFluxFileOption() {
        Stream.of("-f", "--file").forEach(o -> assertCommand(0, true, (String) null, null, o, getTempFile()));
    }

    @Test
    public void shouldRejectFluxFileOptionWithoutParameter() {
        assertCommand(2, false, null, String.format(MISSING_PARAMETER, "--file"), "-f");
    }

    @Test
    public void shouldRejectMissingFluxFile() {
        final String file = "no-such-file";
        assertCommand(1, false, null, FILE_NOT_FOUND + file, "-f", file);
    }

    @Test
    public void shouldAcceptFluxScriptOption() {
        Stream.of("-e", "--script").forEach(o -> assertCommand(0, true, (String) null, null, o, "..."));
    }

    @Test
    public void shouldRejectFluxScriptOptionWithoutParameter() {
        assertCommand(2, false, null, String.format(MISSING_PARAMETER, "--script"), "-e");
    }

    @Test
    public void shouldRejectSimultaneousFluxFileAndFluxScriptOption() {
        assertCommand(2, false, null, "Error: --file=<fluxFile>, --script=<fluxScript> are mutually exclusive (specify only one)", "-f", "some-file", "-e", "...");
    }

    @Test
    public void shouldAcceptVarOption() {
        Stream.of("-v", "--var").forEach(o -> assertCommand(0, true, (String) null, null, "-e", "...", o, "var1=value1"));
    }

    @Test
    public void shouldAcceptMultipleVarOptions() {
        assertCommand(0, true, (String) null, null, "-f", getTempFile(), "-v", "var1=value1", "-v", "var2=value2");
    }

    @Test
    public void shouldRejectInvalidVarOption() {
        assertCommand(2, false, null, "Value for option option '--var' (<name>=<value>) should be in KEY=VALUE format but was var-without-value", "-e", "...", "-v", "var-without-value");
    }

    @Test
    public void shouldRejectVarOptionWithoutFluxInput() {
        assertCommand(2, false, null, "Error: Missing required argument (specify one of these): [-f=<fluxFile> | -e=<fluxScript>]", "-v", "var1=value1");
    }

    @Test
    public void shouldAcceptListCommandsOption() {
        Stream.of("-l", "--list-commands").forEach(o -> assertCommand(0, false, LIST_COMMANDS_OUTPUT, null, o));
    }

    @Test
    public void shouldAcceptListCommandsOptionWithAdditionalOptions() {
        assertCommand(0, false, LIST_COMMANDS_OUTPUT, null, "-l", "-e", "...");
    }

    @Test
    public void shouldRejectValidOptionWithAdditionalArgument() {
        assertCommand(2, false, null, UNMATCHED_ARGUMENT, "-f", "some-file", "some-other-file");
    }

    @Test
    public void shouldRejectInvalidOption() {
        assertCommand(2, false, null, "Unknown option: '-x'", "-x");
    }

    @Test
    public void shouldAcceptLegacyNoArguments() {
        assertCommand(2, false, LIST_COMMANDS_OUTPUT, DEPRECATION_NOTICE);
    }

    @Test
    public void shouldAcceptLegacyFluxFileArgument() {
        assertCommand(0, true, null, DEPRECATION_NOTICE, getTempFile());
    }

    @Test
    public void shouldRejectLegacyFluxFileArgumentWithValidOption() {
        assertCommand(2, false, null, UNMATCHED_ARGUMENT, "some-file", "-f", "some-other-file");
    }

    @Test
    public void shouldRejectMissingLegacyFluxFile() {
        final String file = "no-such-file";
        assertCommand(1, false, null, List.of(DEPRECATION_NOTICE, FILE_NOT_FOUND + file), file);
    }

    @Test
    public void shouldAcceptLegacyFluxFileAndVarsArguments() {
        assertCommand(0, true, null, DEPRECATION_NOTICE, getTempFile(), "var1=value1", "var2=value2");
    }

    @Test
    public void shouldRejectLegacyFluxFileAndInvalidVarsArgument() {
        assertCommand(0, false, null, List.of(DEPRECATION_NOTICE, LIST_COMMANDS_OUTPUT), getTempFile(), "var-without-value");
    }

    private void assertCommand(final int expectedExitCode, final boolean expectedRanFlux, final String expectedOut, final String expectedErr, final String... args) {
        assertCommand(expectedExitCode, expectedRanFlux, expectedOut != null ? List.of(expectedOut) : null, expectedErr != null ? List.of(expectedErr) : null, args);
    }

    private void assertCommand(final int expectedExitCode, final boolean expectedRanFlux, final List<String> expectedOut, final List<String> expectedErr, final String... args) {
        final StringWriter out = new StringWriter();
        final StringWriter err = new StringWriter();
        commandLine.setOut(new PrintWriter(out));
        commandLine.setErr(new PrintWriter(err));

        final int actualExitCode = commandLine.execute(args);
        final boolean actualRanFlux = ranFlux.get();
        final String actualOut = out.toString();
        final String actualErr = err.toString();

        Assert.assertEquals(expectedExitCode, actualExitCode);
        Assert.assertEquals(expectedRanFlux, actualRanFlux);
        assertStrings(actualOut, expectedOut);
        assertStrings(actualErr, expectedErr);
    }

    private void assertStrings(final String actual, final List<String> expected) {
        if (expected != null) {
            MatcherAssert.assertThat(actual, CoreMatchers.allOf(expected.stream().map(CoreMatchers::containsString).collect(Collectors.toList())));
        }
        else {
            Assert.assertEquals(actual, "");
        }
    }

    private String getTempFile() {
        try {
            final File tempFile = File.createTempFile("FluxTest", "");
            tempFile.deleteOnExit();
            return tempFile.getPath();
        }
        catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
