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

package org.metafacture.metamorph.api;

/**
 * Describes a location in a Metamorph script. Instances of this interface are
 * returned by {@link KnowsSourceLocation}.
 *
 * @author Christoph Böhme
 */
public interface SourceLocation {
    /**
     * Gets the filename.
     *
     * @return the filename
     */
    String getFileName();

    /**
     * Gest the start position.
     *
     * @return the start position as {@link Position}
     */
    Position getStartPosition();

    /**
     * Gets the end position.
     *
     * @return the end position as {@link Position}
     */
    Position getEndPosition();

    /**
     * Describes a position in a file by line and column number.
     *
     * @author Christoph Böhme
     */
    interface Position {
        /**
         * Gest the line number.
         *
         * @return the line number
         */
        int getLineNumber();

        /**
         * Gest the column number.
         *
         * @return the column number.
         */
        int getColumnNumber();

    }

}
