/*
 * Copyright 2019 hbz
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
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Collects strings and emits them as records when a line matches the pattern.
 * Appends to every incoming line a line feed so that the original structure is
 * preserved.
 *
 * @author Pascal Christoph (dr0i).
 *
 */
@Description("Collects strings and emits them as records when a line matches the pattern.")
@In(String.class)
@Out(String.class)
@FluxCommand("lines-to-records")
public final class LineRecorder
        extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    private final int SB_CAPACITY = 4096 * 7;
    private String recordMarkerRegexp = "^\\s*$"; // empty line is default
    StringBuilder record = new StringBuilder(SB_CAPACITY);

    public void setRecordMarkerRegexp(final String regexp) {
        this.recordMarkerRegexp = regexp;
    }

    @Override
    public void process(final String line) {
        assert !isClosed();
        if (line.matches(recordMarkerRegexp)) {
            getReceiver().process(record.toString());
            record = new StringBuilder(SB_CAPACITY);
        } else
            record.append(line + "\n");
    }

}
