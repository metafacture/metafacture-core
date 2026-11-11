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

package org.metafacture.io;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectReceiver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link RecordReader}.
 *
 * @author Christoph Böhme
 *
 */
public final class RecordReaderTest {

    private static final String RECORD1 = "record1";
    private static final String RECORD2 = "record2";
    private static final String EMPTY_RECORD = "";
    private static final char SEPARATOR = ':';
    private static final char DEFAULT_SEPARATOR = '\u001d';

    private RecordReader recordReader;

    @Mock
    private ObjectReceiver<String> receiver;

    public RecordReaderTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        recordReader = new RecordReader();
        recordReader.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        recordReader.closeStream();
    }

    @Test
    public void testShouldProcessRecordsFollowedbySeparator() {
        recordReader.setSeparator(SEPARATOR);

        recordReader.process(new StringReader(
                RECORD1 + SEPARATOR +
                RECORD2 + SEPARATOR));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldProcessRecordsPrecededbySeparator() {
        recordReader.setSeparator(SEPARATOR);

        recordReader.process(new StringReader(
                SEPARATOR + RECORD1 +
                SEPARATOR + RECORD2));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldProcessRecordsSeparatedBySeparator() {
        recordReader.setSeparator(SEPARATOR);

        recordReader.process(new StringReader(
                RECORD1 + SEPARATOR +
                RECORD2));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldProcessSingleRecordWithoutSeparator() {
        recordReader.setSeparator(SEPARATOR);

        recordReader.process(new StringReader(
                RECORD1));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldNotEmitRecordIfInputIsEmpty() {
        recordReader.setSeparator(SEPARATOR);
        // Make sure empty records are
        // normally emitted:
        recordReader.setSkipEmptyRecords(false);

        recordReader.process(new StringReader(
                EMPTY_RECORD));

        Mockito.verifyZeroInteractions(receiver);
    }

    @Test
    public void testShouldSkipEmptyRecordsByDefault() {
        recordReader.setSeparator(SEPARATOR);

        recordReader.process(new StringReader(
                RECORD1 + SEPARATOR +
                EMPTY_RECORD + SEPARATOR +
                RECORD2));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldOutputEmptyRecordsIfConfigured() {
        recordReader.setSeparator(SEPARATOR);
        recordReader.setSkipEmptyRecords(false);

        recordReader.process(new StringReader(
                RECORD1 + SEPARATOR +
                EMPTY_RECORD + SEPARATOR +
                RECORD2));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(EMPTY_RECORD);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldOutputEmptyRecordsAtStartOfInputIfConfigured() {
        recordReader.setSeparator(SEPARATOR);
        recordReader.setSkipEmptyRecords(false);

        recordReader.process(new StringReader(
                EMPTY_RECORD + SEPARATOR +
                RECORD1 + SEPARATOR +
                RECORD2));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(EMPTY_RECORD);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldOutputEmptyRecordsAtEndOfInputIfConfigured() {
        recordReader.setSeparator(SEPARATOR);
        recordReader.setSkipEmptyRecords(false);

        recordReader.process(new StringReader(
                RECORD1 + SEPARATOR +
                RECORD2 + SEPARATOR +
                EMPTY_RECORD));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        ordered.verify(receiver).process(EMPTY_RECORD);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldUseGlobalSeparatorAsDefaultSeparator() {
        recordReader.process(new StringReader(
                RECORD1 + DEFAULT_SEPARATOR +
                RECORD2 + DEFAULT_SEPARATOR));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver).process(RECORD2);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void testShouldProcessMultipleReaders() {
        recordReader.setSeparator(SEPARATOR);

        recordReader.process(new StringReader(
                RECORD1 + SEPARATOR +
                RECORD2));
        recordReader.process(new StringReader(
                RECORD2 + SEPARATOR +
                RECORD1));

        final InOrder ordered = Mockito.inOrder(receiver);
        ordered.verify(receiver).process(RECORD1);
        ordered.verify(receiver, Mockito.times(2)).process(RECORD2);
        ordered.verify(receiver).process(RECORD1);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @Test
    public void issue584_shouldResetBufferOnException() {
        final String error = "ERROR:";
        final String success = "SUCCESS:";

        final List<String> actual = new ArrayList<>();
        final String[] expected = new String[]{error + RECORD1, success + RECORD2};

        recordReader.setReceiver(new DefaultObjectReceiver<String>() {
            @Override
            public void process(final String obj) {
                if (RECORD1.equals(obj)) {
                    throw new IllegalArgumentException(obj);
                }
                else {
                    actual.add(success + obj);
                }
            }
        });

        try {
            recordReader.process(new StringReader(RECORD1));
        }
        catch (final IllegalArgumentException e) {
            actual.add(error + e.getMessage());
        }

        recordReader.process(new StringReader(RECORD2));

        Assertions.assertArrayEquals(expected, actual.toArray());
    }

}
