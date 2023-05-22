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

import org.metafacture.framework.ObjectReceiver;

import org.junit.Before;
import org.junit.Test;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.base.MockitoAssertionError;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Tests for {@link CsvEncoder}.
 *
 * @author eberhardtj (j.eberhardt@dnb.de)
 * @author Pascal Christoph (dr0i)
 * @author Jens Wille
 */
public final class CsvEncoderTest {

    @Mock
    private ObjectReceiver<String> receiver;
    private static final String LITERAL1 = "column 1";
    private static final String LITERAL2 = "column 2";
    private static final String RECORD_ID1 = "1";
    private static final String RECORD_ID2 = "2";
    private static final String RECORD_ID3 = "3";
    private static final String VALUE1 = "a";
    private static final String VALUE2 = "b";
    private static final String VALUE3 = "c";
    private static final String VALUE4 = "d";
    private static final String VALUE5 = "e";
    private static final String VALUE6 = "f";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CsvEncoder encoder = new CsvEncoder();
        encoder.setReceiver(receiver);
    }


    @Test
    public void shouldReceiveSingleRecord() {
        assertEncode(i -> {
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
        }, "\"a\",\"b\"");
    }

    @Test
    public void shouldHaveNoQuotes() {
        assertEncode(i -> {
            i.setNoQuotes(true);
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
        }, "a,b");
    }

    @Test
    public void shouldReceiveSingleRecordWithHeader() {
        assertEncode(i -> {
            i.setIncludeHeader(true);
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
        }, "\"column 1\",\"column 2\"", "\"a\",\"b\"");
    }

    @Test
    public void shouldReceiveSingleRecordWithRecordId() {
        assertEncode(i -> {
            i.setIncludeRecordId(true);
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
        }, "\"1\",\"a\",\"b\"");
    }

    @Test
    public void shouldReceiveSingleRecordWithRecordIdAndHeader() {
        assertEncode(i -> {
            i.setIncludeRecordId(true);
            i.setIncludeHeader(true);
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
        }, "\"record id\",\"column 1\",\"column 2\"", "\"1\",\"a\",\"b\"");
    }

    @Test
    public void shouldReceiveThreeRows() {
        assertEncode(i -> {
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
            i.startRecord(RECORD_ID2);
            i.literal(LITERAL1, VALUE3);
            i.literal(LITERAL2, VALUE4);
            i.endRecord();
            i.startRecord(RECORD_ID3);
            i.literal(LITERAL1, VALUE5);
            i.literal(LITERAL2, VALUE6);
            i.endRecord();
        }, "\"a\",\"b\"", "\"c\",\"d\"", "\"e\",\"f\"");
    }

    @Test
    public void shouldUseTabulatorAsSeparator() {
        assertEncode(i -> {
            i.setSeparator('\t');
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.endRecord();
        }, "\"a\"\t\"b\"");
    }

    @Test
    public void shouldNotCreateNestedCsvInColumn() {
        assertEncode(i -> {
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.literal(LITERAL2, VALUE3);
            i.literal(LITERAL2, VALUE4);
            i.endRecord();
        }, "\"a\",\"b\",\"c\",\"d\"");
    }

    @Test
    public void shouldRepeatHeaderForRepeatedColumns() {
        assertEncode(i -> {
            i.setIncludeHeader(true);
            i.startRecord(RECORD_ID1);
            i.literal(LITERAL1, VALUE1);
            i.literal(LITERAL2, VALUE2);
            i.literal(LITERAL2, VALUE3);
            i.literal(LITERAL1, VALUE4);
            i.literal(LITERAL2, VALUE5);
            i.endRecord();
        }, "\"column 1\",\"column 2\",\"column 2\",\"column 1\",\"column 2\"", "\"a\",\"b\",\"c\",\"d\",\"e\"");
    }

    private void assertEncode(final Consumer<CsvEncoder> in, final String... out) {
        final InOrder ordered = Mockito.inOrder(receiver);

        final CsvEncoder csvEncoder = new CsvEncoder();
        csvEncoder.setReceiver(receiver);
        in.accept(csvEncoder);

        try {
            Arrays.stream(out).forEach(s -> ordered.verify(receiver).process(s));

            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }

    }
}
