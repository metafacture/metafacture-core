/*
 * Copyright 2019 Pascal Christoph (hbz)
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

import static org.mockito.Mockito.inOrder;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link LineRecorder}.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
public final class LineRecorderTest {

    private static final String RECORD1_PART1 = "a1";
    private static final String RECORD1_PART2 = "a2";
    private static final String RECORD1_ENDMARKER = "\n";

    private static final String RECORD2_PART1 = "b1";
    private static final String RECORD2_PART2 = "b2";
    private static final String RECORD2_ENDMARKER = "\n";

    private static final String RECORD3_PART1 = "c1";
    private static final String RECORD3_PART2 = "c2";
    private static final String RECORD3_ENDMARKER = "EOR";

    private static final String LINE_SEPARATOR = "\n";

    private LineRecorder lineRecorder;

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        lineRecorder = new LineRecorder();
        lineRecorder.setReceiver(receiver);
    }

    @Test
    public void shouldEmitRecords() {
        lineRecorder.process(RECORD1_PART1);
        lineRecorder.process(RECORD1_PART2);
        lineRecorder.process(RECORD1_ENDMARKER);

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process(
                RECORD1_PART1 +
                LINE_SEPARATOR +
                RECORD1_PART2 +
                LINE_SEPARATOR);

        lineRecorder.process(RECORD2_PART1);
        lineRecorder.process(RECORD2_PART2);
        lineRecorder.process(RECORD2_ENDMARKER);

        ordered.verify(receiver).process(
                RECORD2_PART1 +
                LINE_SEPARATOR +
                RECORD2_PART2 +
                LINE_SEPARATOR);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    public void shouldEmitRecordWithNonDefaultRecordMarker() {
        lineRecorder.setRecordMarkerRegexp(RECORD3_ENDMARKER);
        lineRecorder.process(RECORD3_PART1);
        lineRecorder.process(RECORD3_PART2);
        lineRecorder.process(RECORD3_ENDMARKER);

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process(
                RECORD3_PART1 +
                LINE_SEPARATOR +
                RECORD3_PART2 +
                LINE_SEPARATOR);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    public void shouldEmitLastRecordWithoutRecordMarkerWhenClosingStream() {
        lineRecorder.process(RECORD3_PART1);
        lineRecorder.process(RECORD3_PART2);
        lineRecorder.closeStream();
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process(
                RECORD3_PART1 +
                LINE_SEPARATOR +
                RECORD3_PART2 +
                LINE_SEPARATOR);
        ordered.verify(receiver).closeStream();
        ordered.verifyNoMoreInteractions();
    }

    @Test(expected = AssertionError.class)
    public void shouldEmitAsseertionErorWhenProcessingAfterStreamIsClosed() {
            lineRecorder.closeStream();
            lineRecorder.process("");
    }

}
