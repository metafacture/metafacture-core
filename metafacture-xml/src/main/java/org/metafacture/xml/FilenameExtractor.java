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

        /**
         * Default constructor
         */
        public FilenameUtil() {
        }

        /**
         * Ensures that path exists. If not, make it.
         *
         * @param path
         *            the path of the to be stored file
         */
        public void ensurePathExists(final File path) {
            final File parent = path.getAbsoluteFile().getParentFile();
            parent.mkdirs();
        }

        /**
         * Sets the encoding used to open the resource.
         *
         * @param encoding
         *            new encoding
         */
        public void setEncoding(final String encoding) {
            this.encoding = encoding;
        }

        /**
         * Returns the encoding used to open the resource.
         *
         * @return current default setting
         */
        public String getEncoding() {
            return encoding;
        }

        /**
         * Sets the suffix of the files.
         *
         * @param fileSuffix
         *              the suffix of the files
         */
        public void setFileSuffix(final String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        /**
         * Gets the suffix of the file.
         *
         * @return the suffix of the file
         */
        public String getFileSuffix() {
            return fileSuffix;
        }

        /**
         * Sets the filename.
         *
         * @param filename
         *              the name of the file
         */
        public void setFilename(final String filename) {
            this.filename = filename;
        }

        /**
         * Gets the filename.
         *
         * @return the name of the file
         *
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Sets the property which is used as the base of the filename.
         * Recommended properties are identifiers.
         *
         * @param property
         *              the property which will be used for the base name of the file
         */
        public void setProperty(final String property) {
            this.property = property;
        }

        /**
         * Gets the property which is the base of the filename.
         *
         * @return the property which is the base name of the file
         */
        public String getProperty() {
            return property;
        }

        /**
         * Sets the target path.
         *
         * @param target
         *            the basis directory in which the files are stored
         */
        public void setTarget(final String target) {
            this.target = target;
        }

        /**
         * Gets the target path.
         *
         * @return the basis directory in which the files are stored
         */
        public String getTarget() {
            return target;
        }

        /**
         * Sets the end of the index in the filename to extract the name of a
         * subfolder.
         *
         * @param endIndex
         *            this marks the index' end
         */
        public void setEndIndex(final int endIndex) {
            this.endIndex = endIndex;
        }

        /**
         * Gets the end of the index in the filename to extract the name of a
         * subfolder.
         *
         * @return the marker of the index' end
         */
        public int getEndIndex() {
            return endIndex;
        }

        /**
         * Sets the beginning of the index in the filename to extract the name of
         * the subfolder.
         *
         * @param startIndex
         *            This marks the index' beginning
         */
        public void setStartIndex(final int startIndex) {
            this.startIndex = startIndex;
        }

        /**
         * Gets the beginning of the index in the filename to extract the name of
         * the subfolder.
         *
         * @return the marker of the index' beginning
         */
        public int getStartIndex() {
            return startIndex;
        }

    }

}
