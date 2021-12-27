/* Copyright 2019 Pascal Christoph (hbz)
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectPipe;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;

/**
 * Collects strings and emits them as records when a line matches the pattern.
 * Appends to every incoming line a line feed so that the original structure is
 * preserved.
 *
 * @author Pascal Christoph (dr0i).
 *
 */
@Description("Collects strings and emits them as records when a line matches the pattern or the stream is closed.")
@In(String.class)
@Out(String.class)
@FluxCommand("lines-to-records")
public final class LineRecorder implements ObjectPipe<String, ObjectReceiver<String>> {

    private static final int SB_CAPACITY = 4096 * 7;
    // empty line is the default
    private String recordMarkerRegexp = "^\\s*$";
    private StringBuilder record = new StringBuilder(SB_CAPACITY);
    private ObjectReceiver<String> receiver;
    private boolean isClosed;

    /**
     * Creates an instance of {@link LineRecorder}.
     */
    public LineRecorder() {
    }

    /**
     * Sets the record marker regexp.
     *
     * @param regexp the regexp
     */
    public void setRecordMarkerRegexp(final String regexp) {
        recordMarkerRegexp = regexp;
    }

    @Override
    public void process(final String line) {
        assert !isClosed();
        if (line.matches(recordMarkerRegexp)) {
            getReceiver().process(record.toString());
            record = new StringBuilder(SB_CAPACITY);
        }
        else {
            record.append(line + "\n");
        }
    }

    private boolean isClosed() {
        return isClosed;
    }

    @Override
    public void resetStream() {
        record = new StringBuilder(SB_CAPACITY);
    }

    @Override
    public void closeStream() {
        assert !isClosed();
        getReceiver().process(record.toString());
        getReceiver().closeStream();
        isClosed = true;
    }

    @Override
    public <R extends ObjectReceiver<String>> R setReceiver(final R newReceiver) {
        receiver = newReceiver;
        return newReceiver;
    }

    /**
     * Returns a reference to the downstream module.
     *
     * @return reference to the downstream module
     */
    protected ObjectReceiver<String> getReceiver() {
        return receiver;
    }

}
