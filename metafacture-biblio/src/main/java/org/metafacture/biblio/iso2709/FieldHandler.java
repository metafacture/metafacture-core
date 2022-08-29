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

package org.metafacture.biblio.iso2709;

/**
 * Callback interface defining the events emitted by
 * {@link Record#processFields(FieldHandler)}.
 *
 * @author Christoph Böhme
 */
public interface FieldHandler {

    /**
     * Reference a field.
     *
     * @param tag             the tag
     * @param implDefinedPart the impl defined part
     * @param value           the value
     */
    void referenceField(char[] tag, char[] implDefinedPart, String value);

    /**
     * Starts a data field.
     *
     * @param tag             the tag
     * @param implDefinedPart the impl defined part
     * @param indicators      the indicators
     */
    void startDataField(char[] tag, char[] implDefinedPart, char[] indicators);

    /**
     * Ends the data field.
     */
    void endDataField();

    /**
     * Sets the impl defined part.
     *
     * @param implDefinedPart he impl defined part
     */
    void additionalImplDefinedPart(char[] implDefinedPart);

    /**
     * Sets the identifier to a value.
     *
     * @param identifier the identifier
     * @param value      the value
     */
    void data(char[] identifier, String value);

}
