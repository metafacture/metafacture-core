/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.runner.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.culturegraph.mf.exceptions.MetafactureException;

/**
 * A class loader which allows adding directories to the class
 * path instead of class-files or jar-files.
 *
 * @author Christoph Böhme
 *
 */
public final class DirectoryClassLoader extends URLClassLoader {

	private static final String JAR_FILE_EXTENSION = ".jar";
	private static final String CLASS_FILE_EXTENSION = ".class";

	private static final FilenameFilter JAR_AND_CLASS_FILTER =
			new FilenameFilter() {

				@Override
				public boolean accept(final File dir, final String name) {
					return name.endsWith(JAR_FILE_EXTENSION)
							|| name.endsWith(CLASS_FILE_EXTENSION);
				}
			};

	public DirectoryClassLoader(final ClassLoader parent) {
		super(new URL[0], parent);
	}

	public void addDirectory(final File dir) {
		for (final File file : dir.listFiles(JAR_AND_CLASS_FILTER)) {
			try {
				addURL(file.toURI().toURL());
			} catch (final MalformedURLException e) {
				throw new MetafactureException("Could not add " + file + " to class loader", e);
			}
		}
	}

}