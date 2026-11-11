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

package org.metafacture.formeta.formatter;

/**
 * Interface for formatters.
 *
 * @author Christoph Böhme
 *
 */
public interface Formatter {

    /**
     * The reset event.
     */
    void reset();

    /**
     * The startGroup event.
     *
     * @param name the name of the startGroup
     */
    void startGroup(String name);

    /**
     * The literal endGroup event.
     */
    void endGroup();

    /**
     * The literal event.
     *
     * @param name  the name of the literal
     * @param value the value of the literal
     */
    void literal(String name, String value);

}
