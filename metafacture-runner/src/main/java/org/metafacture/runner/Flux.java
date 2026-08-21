/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metafacture.runner;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.flux.FluxCompiler;
import org.metafacture.flux.parser.FluxProgramm;
import org.metafacture.runner.util.DirectoryClassLoader;

import org.antlr.runtime.RecognitionException;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 */
@Command( // checkstyle-disable-line ClassFanOutComplexity|ParenPad
    name = "flux",
    description = "Runs the Flux.",
    mixinStandardHelpOptions = true,
    sortOptions = false,
    sortSynopsis = false,
    versionProvider = Flux.VersionProvider.class,
    descriptionHeading = "%n",
    optionListHeading = "%nOptions:%n",
    exitCodeListHeading = "%nExit codes:%n",
    exitCodeList = { "0:Successful program execution", "1:Internal software error", "2:Usage error" }
)
public final class Flux implements Callable<Integer> {

    public static final String PLUGINS_DIR_PROPERTY = "flux.pluginsdir";
    public static final String PROVIDED_DIR_PROPERTY = "flux.provideddir";

    private static final Pattern VAR_PATTERN = Pattern.compile("([^=]*)=(.*)");
    private static final String SCRIPT_HOME = "FLUX_DIR";

    private static final int LONG_OPTIONS_MAX_WIDTH = 42;

    private static final String DEPRECATION_NOTICE =
        "############################################################\n" +
        "# DEPRECATION NOTICE: Legacy parameters are deprecated and #\n" +
        "# will be removed in a future release! Please use the      #\n" +
        "# dedicated options instead; see `--help` for details.     #\n" +
        "############################################################\n";

    @ArgGroup(exclusive = true) // TODO: Add `multiplicity = "1"` after deprecation period is over.
    private FluxInput fluxInput;

    @Option(names = {"-v", "--var"}, paramLabel = "<name>=<value>", description = "Assign Flux variables (repeatable).")
    private final Map<String, String> vars = new HashMap<>();

    @Option(names = {"-l", "--list-commands"}, help = true, description = "List all available Flux commands and exit.")
    private boolean listCommands;

    @Parameters(hidden = true) // TODO: Remove catch-all parameter after deprecation period is over.
    private List<String> legacyParameters = new ArrayList<>();

    @Spec
    private Model.CommandSpec commandSpec;

    private Flux() {
        // No (public) instances allowed
    }

    /**
     * Runs the Flux.
     *
     * @param args the pathname of the flux file to run
     * @throws IOException if an I/O error occurs
     * @throws RecognitionException if an ANTLR error occurs
     */
    public static void main(final String[] args) throws IOException, RecognitionException {
        loadCustomJars();

        final CommandLine commandLine = new CommandLine(new Flux());
        commandLine.setUsageHelpLongOptionsMaxWidth(LONG_OPTIONS_MAX_WIDTH);

        System.exit(commandLine.execute(args));
    }

    @Override // checkstyle-disable-line CyclomaticComplexity|ReturnCount
    public Integer call() throws Exception {
        final CommandLine commandLine = commandSpec.commandLine();
        final File fluxFile;

        if (fluxInput != null || !vars.isEmpty() || listCommands) {
            // Mixed options and legacy arguments
            if (!legacyParameters.isEmpty()) {
                throw new UnmatchedArgumentException(commandLine, legacyParameters);
            }

            // Only dedicated options
            if (listCommands) {
                FluxProgramm.printHelp(System.out);
                return CommandLine.ExitCode.OK;
            }
            else if (fluxInput == null) { // TODO: Remove after deprecation period is over.
                throw new MissingParameterException(commandLine, commandSpec.args(),
                        "Error: Missing required argument (specify one of these): " +
                        commandSpec.args().get(0).group().synopsis());
            }
            else {
                fluxFile = fluxInput.fluxFile;
            }
        }
        else {
            System.err.println(DEPRECATION_NOTICE);

            // No arguments at all
            if (legacyParameters.isEmpty()) {
                FluxProgramm.printHelp(System.out);
                return CommandLine.ExitCode.USAGE;
            }

            // Only legacy arguments
            final Iterator<String> iter = legacyParameters.iterator();
            fluxFile = new File(iter.next());

            // get variable assignments
            while (iter.hasNext()) {
                final Matcher matcher = VAR_PATTERN.matcher(iter.next());
                if (!matcher.find()) {
                    FluxProgramm.printHelp(System.err);
                    return CommandLine.ExitCode.OK; // TODO: Exit code 2 (usage error) instead?
                }
                vars.put(matcher.group(1), matcher.group(2));
            }
        }

        if (fluxFile != null) {
            if (!fluxFile.exists()) {
                System.err.println("File not found: " + fluxFile);
                return CommandLine.ExitCode.SOFTWARE; // TODO: Exit code 2 (usage error) instead?
            }

            runFlux(fluxFile.getAbsoluteFile().getParent(), ResourceUtil.getStream(fluxFile));
        }
        else {
            runFlux(System.getProperty("user.dir"), new ByteArrayInputStream(fluxInput.fluxScript.getBytes(StandardCharsets.UTF_8)));
        }

        return CommandLine.ExitCode.OK;
    }

    private void runFlux(final String scriptHome, final InputStream inputStream) throws Exception {
        vars.put(SCRIPT_HOME, scriptHome + System.getProperty("file.separator"));

        try {
            // run parser and builder
            FluxCompiler.compile(inputStream, vars).start();
        }
        finally {
            inputStream.close();
        }
    }

    private static void loadCustomJars() {
        final DirectoryClassLoader dirClassLoader = new DirectoryClassLoader(getClassLoader());

        final String pluginsDir = System.getProperty(PLUGINS_DIR_PROPERTY);
        if (pluginsDir != null) {
            dirClassLoader.addDirectory(new File(pluginsDir));
        }
        final String providedDir = System.getProperty(PROVIDED_DIR_PROPERTY);
        if (providedDir != null) {
            dirClassLoader.addDirectory(new File(providedDir));
        }

        setClassLoader(dirClassLoader);
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static void setClassLoader(final ClassLoader classLoader) {
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private static class FluxInput {

        @Option(names = {"-f", "--file"}, required = true, description = "Specify the path to the Flux file.")
        private File fluxFile;

        @Option(names = {"-e", "--script"}, required = true, description = "Specify the inline Flux script.")
        private String fluxScript;

        private FluxInput() {
        }

    }

    /**
     * Provides a means to determine the Metafacture version.
     */
    public static class VersionProvider implements IVersionProvider {

        private static final String SYSTEM_PROPERTY_NAME = "org.metafacture.metafactureVersion";

        private static final String BUILD_PROPERTY_FILE = "build.properties";
        private static final String BUILD_PROPERTY_NAME = "version";

        /**
         * Creates an instance of {@link VersionProvider}.
         */
        public VersionProvider() {
        }

        @Override
        public String[] getVersion() throws Exception {
            return new String[]{"Metafacture Flux version " + getVersionString()};
        }

        /**
         * Determines the Metafacture version either from the system property
         * {@value SYSTEM_PROPERTY_NAME} or from the build properties file
         * {@value BUILD_PROPERTY_FILE}.
         *
         * @return the Metafacture version
         */
        public String getVersionString() { // checkstyle-disable-line ReturnCount
            final String systemVersion = System.getProperty(SYSTEM_PROPERTY_NAME);
            if (systemVersion != null) {
                return systemVersion;
            }

            try {
                final Properties properties = ResourceUtil.loadProperties(BUILD_PROPERTY_FILE);
                final String buildVersion = properties.getProperty(BUILD_PROPERTY_NAME);
                if (buildVersion != null) {
                    return buildVersion;
                }
            }
            catch (final IOException e) {
            }

            return "n/a";
        }

    }

}
