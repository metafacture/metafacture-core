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

package org.metafacture.strings;

import org.metafacture.framework.ObjectReceiver;

/**
 * Concatenates all received Strings.
 *
 * @author markus geipel
 */
public final class StringConcatenator implements ObjectReceiver<String> {

    private StringBuilder builder = new StringBuilder();
    private String separator = "";

    /**
     * Creates an instance of {@link StringConcatenator}.
     */
    public StringConcatenator() {
    }

    @Override
    public void resetStream() {
        reset();
    }

    /**
     * Sets a separator.
     *
     * @param separator the separator
     */
    public void setSeparator(final String separator) {
        this.separator = separator;
    }

    @Override
    public void closeStream() {
        // nothing to do
    }

    @Override
    public void process(final String obj) {
        builder.append(separator);
        builder.append(obj);
    }

    /**
     * Resets the content.
     */
    public void reset() {
        builder = new StringBuilder();
    }

    /**
     * Gets the string.
     *
     * @return the string
     */
    public String getString() {
        return builder.toString();
    }

}
