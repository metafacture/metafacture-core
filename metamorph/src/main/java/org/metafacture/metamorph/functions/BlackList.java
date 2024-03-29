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

package org.metafacture.metamorph.functions;

/**
 * A function which checks whether the received value
 * is blacklisted.
 *
 * @author Markus Michael Geipel
 */
public final class BlackList extends AbstractLookup {

    /**
     * Creates an instance of {@link BlackList}.
     */
    public BlackList() {
    }

    @Override
    public String process(final String key) {
        final String returnValue = lookup(key);
        if (returnValue == null) {
            return key;
        }
        return null;
    }

}
