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

package org.metafacture.formatting;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Emits a <i>preamble</i> string before the first string object in the
 * stream and an <i>epilogue</i> string before the end of the stream.
 * <p>
 * The preamble and epilogue strings are only emitted if an object is received.
 * If the preamble or epilogue string is empty, the respective string is not
 * emitted.
 *
 * @author Markus Geipel
 * @author Christoph Böhme
 *
 */
@Description("Adds a String preamle and/or epilogue to the stream")
@In(String.class)
@Out(String.class)
@FluxCommand("add-preamble-epilogue")
public final class PreambleEpilogueAdder extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    private String preamble = "";
    private String epilogue = "";

    private boolean objectsReceived;

    /**
     * Creates an instance of {@link PreambleEpilogueAdder}.
     */
    public PreambleEpilogueAdder() {
    }

    /**
     * Sets the <i>preamble</i> string which is emitted before the first object.
     * <p>
     * The default preamble is an empty string. That means by default no
     * preamble is emitted.
     * <p>
     * The parameter may only be changed before the first object is processed
     * otherwise the change has no effect.
     *
     * @param preamble the preamble string
     */
    public void setPreamble(final String preamble) {
        this.preamble = preamble;
    }

    /**
     * Gets the preamble.
     *
     * @return the preamble
     */
    public String getPreamble() {
        return preamble;
    }

    /**
     * Sets the <i>epilogue</i> string which is emitted after the last object.
     * <p>
     * The default epilogue string is an empty string. That means by default no
     * epilogue is emitted.
     * <p>
     * The parameter may be changed at any time. Its becomes effective when a
     * <i>close-stream</i> event is received.
     *
     * @param epilogue the epilogue string
     */
    public void setEpilogue(final String epilogue) {
        this.epilogue = epilogue;
    }

    /**
     * Gets the epilogue.
     *
     * @return the epilogue
     */
    public String getEpilogue() {
        return epilogue;
    }

    @Override
    public void process(final String obj) {
        if (!objectsReceived && !preamble.isEmpty()) {
            getReceiver().process(preamble);
        }
        objectsReceived = true;
        getReceiver().process(obj);
    }

    @Override
    protected void onCloseStream() {
        if (objectsReceived && !epilogue.isEmpty()) {
            getReceiver().process(epilogue);
        }
    }

    @Override
    protected void onResetStream() {
        objectsReceived = false;
    }

}
