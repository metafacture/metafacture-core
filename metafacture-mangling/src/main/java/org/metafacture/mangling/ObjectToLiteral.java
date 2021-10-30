/*
 * Copyright 2013, 2021 Deutsche Nationalbibliothek and others
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

package org.metafacture.mangling;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Outputs a record containing the input object as literal.
 *
 * @param <T> input object type
 * @author Christoph Böhme, Fabian Steeg
 */
@Description("Outputs a record containing the input object as literal")
@Out(StreamReceiver.class)
@FluxCommand("object-to-literal")
public final class ObjectToLiteral<T> extends
        DefaultObjectPipe<T, StreamReceiver> {

    public static final String DEFAULT_LITERAL_NAME = "obj";
    public static final String DEFAULT_RECORD_ID = "";

    private String literalName = DEFAULT_LITERAL_NAME;
    private String recordId = DEFAULT_RECORD_ID;
    private int recordCount;

    /**
     * Creates an instance of {@link ObjectToLiteral}.
     */
    public ObjectToLiteral() {
    }

    /**
     * Sets the literal name.
     *
     * @param literalName the literal name
     */
    public void setLiteralName(final String literalName) {
        this.literalName = literalName;
    }

    /**
     * Sets the record ID.
     *
     * @param recordId the record ID
     */
    public void setRecordId(final String recordId) {
        this.recordId = recordId;
    }

    /**
     * Gets the literal name.
     *
     * @return the literal name
     */
    public String getLiteralName() {
        return literalName;
    }

    /**
     * Gets the record ID.
     *
     * @return the record ID
     */
    public String getRecordId() {
        return recordId;
    }

    @Override
    public void process(final T obj) {
        assert obj != null;
        assert !isClosed();
        getReceiver().startRecord(String.format(recordId, ++recordCount));
        getReceiver().literal(literalName, obj.toString());
        getReceiver().endRecord();
    }

}
