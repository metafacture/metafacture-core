/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.files;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Reads a directory and emits all filenames found.
 *
 * @author Markus Michael Geipel
 * @author Fabian Steeg (fsteeg)
 */
@In(String.class)
@Out(String.class)
@Description("Reads a directory and emits all filenames found.")
@FluxCommand("read-dir")
public final class DirReader extends DefaultObjectPipe<String, ObjectReceiver<String>> {

	private boolean recursive;

	private String filenameFilterPattern = null;

	public void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}

	public void setFilenamePattern(final String filenameFilterPattern) {
		this.filenameFilterPattern = filenameFilterPattern;
	}

	@Override
	public void process(final String dir) {
		final File file = new File(dir);
		if (file.isDirectory()) {
			dir(file);
		} else {
			getReceiver().process(dir);
		}
	}

	private void dir(final File dir) {
		final ObjectReceiver<String> receiver = getReceiver();
		final File[] files = filenameFilterPattern == null ? dir.listFiles()
				: dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return name.matches(filenameFilterPattern);
					}
				});
		Arrays.sort(files);
		for (File file : files) {
			if (file.isDirectory()) {
				if (recursive) {
					dir(file);
				}
			} else {
				receiver.process(file.getAbsolutePath());
			}
		}
	}
}
