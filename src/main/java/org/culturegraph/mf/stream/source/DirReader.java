/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.source;

import java.io.File;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Reads a directory and emits all filenames found.
 * 
 * @author Markus Michael Geipel
 */
@In(String.class)
@Out(String.class)
@Description("Reads a directory and emits all filenames found.")
public final class DirReader extends DefaultObjectPipe<String, ObjectReceiver<String>> {

	private boolean recursive;

	public void setRecursive(final boolean recursive) {
		this.recursive = recursive;
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
		final File[] files = dir.listFiles();
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
