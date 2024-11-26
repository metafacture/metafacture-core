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

package org.metafacture.flux.parser;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Helper class to start a pipe with a {@link String}
 *
 * @author Markus Michael Geipel
 */
public final class StringSender extends DefaultObjectPipe<Object, ObjectReceiver<String>> {

    private final String string;

    /**
     * Creates an instance of {@link StringSender} with the given string.
     *
     * @param string the string
     */
    public StringSender(final String string) {
        this.string = string;
    }

    @Override
    public void process(final Object notUsed) {
        if (notUsed == null) {
            getReceiver().process(string);
        }
        else {
            throw new IllegalArgumentException("Parameter not used. Must be null");
        }
    }

}
