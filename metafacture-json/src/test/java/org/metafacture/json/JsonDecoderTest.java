/*
 * Copyright 2017 hbz
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

package org.metafacture.json;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link JsonDecoder}.
 *
 * @author Jens Wille
 *
 */
public final class JsonDecoderTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private StreamReceiver receiver;

    private JsonDecoder jsonDecoder;

    @Before
    public void init() {
        jsonDecoder = new JsonDecoder();
        jsonDecoder.setReceiver(receiver);
    }

    @Test
    public void testShouldProcessEmptyStrings() {
        jsonDecoder.process("");

        verifyZeroInteractions(receiver);
    }

    @Test
    public void testShouldProcessRecords() {
        jsonDecoder.process(
            "{" +
                "\"lit1\":\"value 1\"," +
                "\" ent1\":{" +
                    "\"lit2\":\"value {x}\"," +
                    "\"lit\\\\3\":\"value 2 \"" +
                "}," +
                "\"lit4\":\"value '3'\"," +
                "\"lit5\":null" +
            "}");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit1", "value 1");
        ordered.verify(receiver).startEntity(" ent1");
        ordered.verify(receiver).literal("lit2", "value {x}");
        ordered.verify(receiver).literal("lit\\3", "value 2 ");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).literal("lit4", "value '3'");
        ordered.verify(receiver).literal("lit5", null);
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldProcessArrays() {
        jsonDecoder.process(
            "{" +
                "\"arr1\":[\"val1\",\"val2\"]," +
                "\"arr2\":[" +
                    "{" +
                        "\"lit1\":\"val1\"," +
                        "\"lit2\":\"val2\"" +
                    "},{" +
                        "\"lit3\":\"val3\"" +
                    "}" +
                "]," +
                "\"arr3\":[" +
                    "[" +
                        "{\"lit4\":\"val4\"}" +
                    "],[" +
                        "{\"lit5\":\"val5\"}" +
                    "]" +
                "]" +
            "}");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).startEntity("arr1[]");
        ordered.verify(receiver).literal("1", "val1");
        ordered.verify(receiver).literal("2", "val2");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("arr2[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit1", "val1");
        ordered.verify(receiver).literal("lit2", "val2");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("2");
        ordered.verify(receiver).literal("lit3", "val3");
        ordered.verify(receiver, times(2)).endEntity();
        ordered.verify(receiver).startEntity("arr3[]");
        ordered.verify(receiver).startEntity("1[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit4", "val4");
        ordered.verify(receiver, times(2)).endEntity();
        ordered.verify(receiver).startEntity("2[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit5", "val5");
        ordered.verify(receiver, times(3)).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldNotProcessBooleans() {
        jsonDecoder.process(
            "{" +
                "\"lit1\":false," +
                "\"lit2\":\"true\"," +
                "\"arr1\":[{\"lit3\":true,\"lit4\":\"false\"}]," +
                "\"arr2\":[false,true,\"false\"]" +
            "}"
        );

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit1", "false");
        ordered.verify(receiver).literal("lit2", "true");
        ordered.verify(receiver).startEntity("arr1[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit3", "true");
        ordered.verify(receiver).literal("lit4", "false");
        ordered.verify(receiver, times(2)).endEntity();
        ordered.verify(receiver).startEntity("arr2[]");
        ordered.verify(receiver).literal("1", "false");
        ordered.verify(receiver).literal("2", "true");
        ordered.verify(receiver).literal("3", "false");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldProcessBooleansIfEnabled() {
        jsonDecoder.setBooleanMarker("~");
        jsonDecoder.process(
            "{" +
                "\"lit1\":false," +
                "\"lit2\":\"true\"," +
                "\"arr1\":[{\"lit3\":true,\"lit4\":\"false\"}]," +
                "\"arr2\":[false,true,\"false\"]" +
            "}"
        );

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit1~", "false");
        ordered.verify(receiver).literal("lit2", "true");
        ordered.verify(receiver).startEntity("arr1[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit3~", "true");
        ordered.verify(receiver).literal("lit4", "false");
        ordered.verify(receiver, times(2)).endEntity();
        ordered.verify(receiver).startEntity("arr2[]");
        ordered.verify(receiver).literal("1~", "false");
        ordered.verify(receiver).literal("2~", "true");
        ordered.verify(receiver).literal("3", "false");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldNotProcessNumbers() {
        jsonDecoder.process(
            "{" +
                "\"lit1\":23," +
                "\"lit2\":\"4.2\"," +
                "\"arr1\":[{\"lit3\":4.2,\"lit4\":\"23\"}]," +
                "\"arr2\":[23,4.2,\"23\"]" +
            "}"
        );

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit1", "23");
        ordered.verify(receiver).literal("lit2", "4.2");
        ordered.verify(receiver).startEntity("arr1[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit3", "4.2");
        ordered.verify(receiver).literal("lit4", "23");
        ordered.verify(receiver, times(2)).endEntity();
        ordered.verify(receiver).startEntity("arr2[]");
        ordered.verify(receiver).literal("1", "23");
        ordered.verify(receiver).literal("2", "4.2");
        ordered.verify(receiver).literal("3", "23");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldProcessNumbersIfEnabled() {
        jsonDecoder.setNumberMarker("#");
        jsonDecoder.process(
            "{" +
                "\"lit1\":23," +
                "\"lit2\":\"4.2\"," +
                "\"arr1\":[{\"lit3\":4.2,\"lit4\":\"23\"}]," +
                "\"arr2\":[23,4.2,\"23\"]" +
            "}"
        );

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit1#", "23");
        ordered.verify(receiver).literal("lit2", "4.2");
        ordered.verify(receiver).startEntity("arr1[]");
        ordered.verify(receiver).startEntity("1");
        ordered.verify(receiver).literal("lit3#", "4.2");
        ordered.verify(receiver).literal("lit4", "23");
        ordered.verify(receiver, times(2)).endEntity();
        ordered.verify(receiver).startEntity("arr2[]");
        ordered.verify(receiver).literal("1#", "23");
        ordered.verify(receiver).literal("2#", "4.2");
        ordered.verify(receiver).literal("3", "23");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldProcessConcatenatedRecords() {
        jsonDecoder.process(
            "{\"lit\": \"record 1\"}\n" +
                "{\"lit\": \"record 2\"}");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit", "record 1");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("2");
        ordered.verify(receiver).literal("lit", "record 2");
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldProcessRecordsInArrayField() {
        jsonDecoder.setRecordPath("$.data");
        jsonDecoder.process(
                "{\"data\":[" + "{\"lit\": \"record 1\"}," +
                        "{\"lit\": \"record 2\"}" + "]}");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit", "record 1");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("2");
        ordered.verify(receiver).literal("lit", "record 2");
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldProcessRecordsInArrayRoot() {
        jsonDecoder.setRecordPath("$");
        jsonDecoder.process(
                "[" + "{\"lit\": \"record 1\"}," +
                        "{\"lit\": \"record 2\"}" + "]");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit", "record 1");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("2");
        ordered.verify(receiver).literal("lit", "record 2");
        ordered.verify(receiver).endRecord();
    }

    @Test(expected=MetafactureException.class)
    public void testRootArrayNoRecordPath() {
        jsonDecoder.process(
                "[" + "{\"lit\": \"record 1\"}," +
                        "{\"lit\": \"record 2\"}" + "]");
    }

    @Test
    public void testShouldProcessMultipleRecords() {
        jsonDecoder.process("{\"lit\": \"record 1\"}");
        jsonDecoder.process("{\"lit\": \"record 2\"}");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit", "record 1");
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).startRecord("2");
        ordered.verify(receiver).literal("lit", "record 2");
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void testShouldOnlyParseObjects() {
        exception.expect(MetafactureException.class);
        exception.expectMessage("Unexpected token 'VALUE_NULL'");

        jsonDecoder.process("null");
    }

    @Test
    public void testShouldNotParseIncompleteObjects() {
        exception.expect(MetafactureException.class);
        exception.expectMessage("Unexpected end-of-input");

        jsonDecoder.process("{");
    }

    @Test
    public void testShouldNotParseTrailingContent() {
        exception.expect(MetafactureException.class);
        exception.expectMessage("Unexpected token 'VALUE_NULL'");

        jsonDecoder.process("{\"lit\":\"value\"}null");
    }

    @Test
    public void testShouldNotParseTrailingGarbage() {
        exception.expect(MetafactureException.class);
        exception.expectMessage("Unrecognized token 'XXX'");

        jsonDecoder.process("{\"lit\":\"value\"}XXX");
    }

    @Test
    public void testShouldNotParseComments() {
        exception.expect(MetafactureException.class);
        exception.expectMessage("Unexpected character ('/' (code 47))");

        Assert.assertFalse(jsonDecoder.getAllowComments());

        jsonDecoder.process("//{\"lit\":\"value\"}");
    }

    @Test
    public void testShouldParseCommentsIfEnabled() {
        jsonDecoder.setAllowComments(true);
        Assert.assertTrue(jsonDecoder.getAllowComments());

        jsonDecoder.process("//{\"lit\":\"value\"}");

        verifyZeroInteractions(receiver);
    }

    @Test
    public void testShouldNotParseInlineComments() {
        exception.expect(MetafactureException.class);
        exception.expectMessage("Unexpected character ('/' (code 47))");

        Assert.assertFalse(jsonDecoder.getAllowComments());

        jsonDecoder.process("{\"lit\":/*comment*/\"value\"}");
    }

    @Test
    public void testShouldParseInlineCommentsIfEnabled() {
        jsonDecoder.setAllowComments(true);
        Assert.assertTrue(jsonDecoder.getAllowComments());

        jsonDecoder.process("{\"lit\":/*comment*/\"value\"}");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("lit", "value");
        ordered.verify(receiver).endRecord();
    }

}
