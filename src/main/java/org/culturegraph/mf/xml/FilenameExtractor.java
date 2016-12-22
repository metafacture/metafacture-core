/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.xml;

import java.io.File;

/**
 * Provides basic methods for constructing filenames out of entities.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
public interface FilenameExtractor extends RecordIdentifier {

	/**
	 * Provides functions and fields that all {@link FilenameExtractor}
	 * implementing classes may found useful.
	 *
	 * @author Pascal Christoph (dr0i)
	 *
	 */
	class FilenameUtil {
		public String target = "tmp";
		public String encoding = "UTF-8";
		public String property = null;
		public String fileSuffix = null;
		public int startIndex = 0;
		public int endIndex = 0;
		public String filename = "test";

		/**
		 * Ensures that path exists. If not, make it.
		 *
		 * @param path
		 *            the path of the to be stored file.
		 */
		public void ensurePathExists(final File path) {
			final File parent = path.getAbsoluteFile().getParentFile();
			parent.mkdirs();
		}
	}

	/**
	 * Returns the encoding used to open the resource.
	 *
	 * @return current default setting
	 */
	String getEncoding();

	/**
	 * Sets the encoding used to open the resource.
	 *
	 * @param encoding
	 *            new encoding
	 */
	void setEncoding(final String encoding);

	/**
	 * Sets the end of the index in the filename to extract the name of the
	 * subfolder.
	 *
	 * @param endIndex
	 *            This marks the index' end.
	 */
	void setEndIndex(final int endIndex);

	/**
	 * Sets the file's suffix.
	 *
	 * @param fileSuffix
	 *            the suffix used for the to be generated files
	 */
	void setFileSuffix(final String fileSuffix);

	/**
	 * Sets the beginning of the index in the filename to extract the name of
	 * the subfolder.
	 *
	 * @param startIndex
	 *            This marks the index' beginning.
	 */
	void setStartIndex(final int startIndex);

	/**
	 * Sets the target path.
	 *
	 * @param target
	 *            the basis directory in which the files are stored
	 */
	void setTarget(final String target);

}
