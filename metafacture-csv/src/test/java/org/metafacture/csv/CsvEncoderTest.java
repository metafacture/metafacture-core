/*
 *  Copyright 2018-2023 Deutsche Nationalbibliothek et al
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metafacture.csv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.inOrder;

/**
 * Tests for {@link CsvEncoder}.
 *
 * @author eberhardtj (j.eberhardt@dnb.de)
 * @author Pascal Christoph (dr0i)
 */
public final class CsvEncoderTest {

    private CsvEncoder encoder;

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        encoder = new CsvEncoder();
        encoder.setIncludeHeader(false);
        encoder.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        encoder.closeStream();
    }

    @Test
    public void shouldReceiveSingleRecord() {
        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"a\",\"b\"");
    }

    @Test
    public void shouldHaveNoQuotes() {
        encoder.setNoQuotes(true);
        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("a,b");
    }

    @Test
    public void shouldReceiveSingleRecordWithHeader() {
        encoder.setIncludeHeader(true);

        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"column 1\",\"column 2\"");
        ordered.verify(receiver).process("\"a\",\"b\"");
    }

    @Test
    public void shouldReceiveSingleRecordWithRecordId() {
        encoder.setIncludeRecordId(true);

        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"1\",\"a\",\"b\"");
    }

    @Test
    public void shouldReceiveSingleRecordWithRecordIdAndHeader() {
        encoder.setIncludeRecordId(true);
        encoder.setIncludeHeader(true);

        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"record id\",\"column 1\",\"column 2\"");
        ordered.verify(receiver).process("\"1\",\"a\",\"b\"");
    }

    @Test
    public void shouldReceiveThreeRows() {
        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.startRecord("2");
        encoder.literal("column 1", "c");
        encoder.literal("column 2", "d");
        encoder.endRecord();
        encoder.startRecord("3");
        encoder.literal("column 1", "e");
        encoder.literal("column 2", "f");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"a\",\"b\"");
        ordered.verify(receiver).process("\"c\",\"d\"");
        ordered.verify(receiver).process("\"e\",\"f\"");
    }

    @Test
    public void shouldUseTabulatorAsSeparator() {
        encoder.setSeparator('\t');

        encoder.startRecord("1");
        encoder.literal("column 1", "a");
        encoder.literal("column 2", "b");
        encoder.endRecord();
        encoder.startRecord("2");
        encoder.literal("column 1", "c");
        encoder.literal("column 2", "d");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"a\"\t\"b\"");
        ordered.verify(receiver).process("\"c\"\t\"d\"");
    }

    @Test
    public void shouldNotCreateNestedCsvInColumn() {
        encoder.startRecord("1");
        encoder.literal("name", "a");
        encoder.literal("alias", "a1");
        encoder.literal("alias", "a2");
        encoder.literal("alias", "a3");
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("\"a\",\"a1\",\"a2\",\"a3\"");
    }

}
