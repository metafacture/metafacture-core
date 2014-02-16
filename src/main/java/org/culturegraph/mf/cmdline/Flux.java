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
package org.culturegraph.mf.cmdline;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.RecognitionException;
import org.culturegraph.mf.flux.FluxCompiler;
import org.culturegraph.mf.flux.parser.FluxProgramm;
import org.culturegraph.mf.util.ResourceUtil;

/**
 * @author Markus Michael Geipel
 *
 */
public final class Flux {
	public static final String MODULES_DIR = "modules";

	private static final String JAR_FILE_EXTENSION = ".jar";
	private static final String CLASS_FILE_EXTENSION = ".class";

	private static final Pattern VAR_PATTERN = Pattern.compile("([^=]*)=(.*)");
	private static final String SCRIPT_HOME = "FLUX_DIR";

	private static final String MODULES_DIR_ARG = "-modules=";

	private Flux() {
		// no instances
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws RecognitionException
	 * @throws URISyntaxException
	 */
	public static void main(final String[] args) throws IOException, RecognitionException {

		loadModules(getModulesDir(args));

		final int fileArg;
		if (args.length > 0 && args[0].startsWith(MODULES_DIR_ARG)) {
			fileArg = 1;
		} else {
			fileArg = 0;
		}

		if (args.length < (fileArg + 1)) {
			FluxProgramm.printHelp(System.out);
			System.exit(2);
		} else {

			final File fluxFile = new File(args[fileArg]);
			if (!fluxFile.exists()) {
				System.err.println("File not found: " + args[fileArg]);
				System.exit(1);
				return;
			}

			// get variable assignments
			final Map<String, String> vars = new HashMap<String, String>();
			vars.put(SCRIPT_HOME, fluxFile.getAbsoluteFile().getParent() + System.getProperty("file.separator"));

			for (int i = fileArg + 1; i < args.length; ++i) {
				if (args[i].startsWith(MODULES_DIR_ARG)) {
					continue;
				}
				final Matcher matcher = VAR_PATTERN.matcher(args[i]);
				if (!matcher.find()) {
					FluxProgramm.printHelp(System.err);
					return;
				}
				vars.put(matcher.group(1), matcher.group(2));
			}

			// run parser and builder
			FluxCompiler.compile(ResourceUtil.getStream(fluxFile), vars).start();
		}
	}

	private static File getModulesDir(final String[] args) {
		File modulesDir = new File(MODULES_DIR);

		File programDir = null;
		try {
			programDir = new File(Flux.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (final URISyntaxException e) {
			// Ignore the programDir, if it is not available
		}
		if (programDir != null) {
			if (programDir.getName().endsWith(JAR_FILE_EXTENSION)) {
				programDir = programDir.getParentFile();
			}
			modulesDir = new File(programDir, MODULES_DIR);
		}

		if (args.length > 0 && args[0].startsWith(MODULES_DIR_ARG)) {
			modulesDir = new File(args[0].substring(MODULES_DIR_ARG.length()));
		}

		return modulesDir;
	}

	private static void loadModules(final File modulesDir) throws MalformedURLException {
		if (modulesDir.exists()) {
			final FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.endsWith(JAR_FILE_EXTENSION) || name.endsWith(CLASS_FILE_EXTENSION);
				}
			};
			final List<URL> moduleURLs = new LinkedList<URL>();
			for (final File file : modulesDir.listFiles(filter)) {
				moduleURLs.add(file.getAbsoluteFile().toURI().toURL());
			}
			final URLClassLoader moduleLoader = new URLClassLoader(moduleURLs.toArray(new URL[0]), Thread
					.currentThread().getContextClassLoader());
			Thread.currentThread().setContextClassLoader(moduleLoader);
		}
	}

}