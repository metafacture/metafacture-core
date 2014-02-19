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
package org.culturegraph.mf.runner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.RecognitionException;
import org.culturegraph.mf.flux.FluxCompiler;
import org.culturegraph.mf.flux.parser.FluxProgramm;
import org.culturegraph.mf.runner.util.DirectoryClassLoader;
import org.culturegraph.mf.util.ResourceUtil;

/**
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public final class Flux {

	public static final String PLUGINS_DIR_PROPERTY = "flux.pluginsdir";
	public static final String PROVIDED_DIR_PROPERTY = "flux.provideddir";

	private static final Pattern VAR_PATTERN = Pattern.compile("([^=]*)=(.*)");
	private static final String SCRIPT_HOME = "FLUX_DIR";

	private Flux() {
		// No instances allowed
	}

	public static void main(final String[] args) throws IOException, RecognitionException {

		loadCustomJars();

		if (args.length < (1)) {
			FluxProgramm.printHelp(System.out);
			System.exit(2);
		} else {

			final File fluxFile = new File(args[0]);
			if (!fluxFile.exists()) {
				System.err.println("File not found: " + args[0]);
				System.exit(1);
				return;
			}

			// get variable assignments
			final Map<String, String> vars = new HashMap<String, String>();
			vars.put(SCRIPT_HOME, fluxFile.getAbsoluteFile().getParent()
					+ System.getProperty("file.separator"));

			for (int i = 1; i < args.length; ++i) {
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

}