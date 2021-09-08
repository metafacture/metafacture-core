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

package org.metafacture.xml;

import java.io.File;

/**
 * Provides basic methods for constructing filenames out of entities.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
public interface FilenameExtractor extends RecordIdentifier {

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
    void setEncoding(String encoding);

    /**
     * Sets the end of the index in the filename to extract the name of the
     * subfolder.
     *
     * @param endIndex
     *            This marks the index' end.
     */
    void setEndIndex(int endIndex);

    /**
     * Sets the file's suffix.
     *
     * @param fileSuffix
     *            the suffix used for the to be generated files
     */
    void setFileSuffix(String fileSuffix);

    /**
     * Sets the beginning of the index in the filename to extract the name of
     * the subfolder.
     *
     * @param startIndex
     *            This marks the index' beginning.
     */
    void setStartIndex(int startIndex);

    /**
     * Sets the target path.
     *
     * @param target
     *            the basis directory in which the files are stored
     */
    void setTarget(String target);

    /**
     * Provides functions and fields that all {@link FilenameExtractor}
     * implementing classes may found useful.
     *
     * @author Pascal Christoph (dr0i)
     *
     */
    class FilenameUtil {

        private String encoding = "UTF-8";
        private String fileSuffix;
        private String filename = "test";
        private String property;
        private String target = "tmp";
        private int endIndex;
        private int startIndex;

        FilenameUtil() {
        }

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

        public void setEncoding(final String encoding) {
            this.encoding = encoding;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setFileSuffix(final String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        public String getFileSuffix() {
            return fileSuffix;
        }

        public void setFilename(final String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public void setProperty(final String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        public void setTarget(final String target) {
            this.target = target;
        }

        public String getTarget() {
            return target;
        }

        public void setEndIndex(final int endIndex) {
            this.endIndex = endIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public void setStartIndex(final int startIndex) {
            this.startIndex = startIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

    }

}
