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

package org.metafacture.formeta.parser;

import org.metafacture.framework.StreamReceiver;

/**
 * Interface for event emitters.
 *
 * @author Christoph Böhme
 *
 */
public interface Emitter {

    /**
     * Sets the receiver.
     *
     * @param receiver the receiver
     */
    void setReceiver(StreamReceiver receiver);

    /**
     * The startGroup event.
     *
     * @param name         the name of the startGroup event
     * @param nestingLevel the nesting level of the startGroup event
     */
    void startGroup(String name, int nestingLevel);

    /**
     * The endGroup event.
     *
     * @param nestingLevel the nesting level
     */
    void endGroup(int nestingLevel);

    /**
     * The literal event.
     *
     * @param name         the name of the literal event
     * @param value        the value of the literal event
     * @param nestingLevel the nesting level of the literal event
     */
    void literal(String name, String value, int nestingLevel);

}
