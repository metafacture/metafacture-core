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
package org.culturegraph.mf.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.javaintegration.EventList.Event;
import org.culturegraph.mf.javaintegration.EventList;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;

/**
 * Tests for class {@link JsonDecoder}.
 *
 * @author Jens Wille
 *
 */
public final class JsonDecoderTest {

    private static final String RECORD =
        "{\"lit1\":\"value 1\",\" ent1\":{\"lit2\":\"value {x}\",\"lit\\\\3\":\"value 2 \"},\"lit4\":\"value '3'\",\"lit5\":null}";

    private static final String ARRAY_RECORD =
        "{\"arr1\":[\"val1\",\"val2\"],\"arr2\":[{\"lit1\":\"val1\",\"lit2\":\"val2\"},{\"lit3\":\"val3\"}],\"arr3\":[[{\"lit4\":\"val4\"}],[{\"lit5\":\"val5\"}]]}";

    private JsonDecoder decoder;

    private EventList eventReceiver;

    @Mock
    private ObjectReceiver<String> objectReceiver;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        decoder = new JsonDecoder();
        eventReceiver = new EventList();
        decoder.setReceiver(eventReceiver);
    }

    @After
    public void cleanup() {
        decoder.closeStream();
    }

    @Test
    public void testShouldProcessEmptyStrings() {
        assertEvents("", new EventList());
    }

    @Test
    public void testShouldProcessRecords() {
        final EventList eventList = new EventList();

        expectRecord(eventList);
        assertEvents(RECORD, eventList);
    }

    @Test
    public void testShouldProcessArrays() {
        final EventList eventList = new EventList();

        expectArray(eventList, "1");
        assertEvents(ARRAY_RECORD, eventList);
    }

    @Test
    public void testShouldProcessConcatenatedRecords() {
        final EventList eventList = new EventList();

        expectRecord(eventList);
        expectArray(eventList, "2");

        assertEvents(RECORD + "\n" + ARRAY_RECORD, eventList);
    }

    @Test
    public void testShouldProcessMultipleRecords() {
        final EventList eventList = new EventList();

        expectRecord(eventList);
        assertEvents(RECORD, eventList);

        eventList.resetStream();
        eventReceiver.resetStream();

        expectArray(eventList, "2");
        assertEvents(ARRAY_RECORD, eventList);
    }

    @Test
    public void testShouldOnlyParseObjects() {
        expectParseError("null", "Unexpected token 'VALUE_NULL'");
    }

    @Test
    public void testShouldNotParseIncompleteObjects() {
        expectParseError("{", "Unexpected end-of-input", JsonEOFException.class);
    }

    @Test
    public void testShouldNotParseTrailingContent() {
        expectParseError(RECORD + "null", "Unexpected token 'VALUE_NULL'");
    }

    @Test
    public void testShouldNotParseTrailingGarbage() {
        expectParseError(RECORD + "XXX", "Unrecognized token 'XXX'", JsonParseException.class);
    }

    @Test
    public void testShouldRoundtripRecords() {
        verifyRoundtrip(RECORD);
    }

    @Test
    public void testShouldRoundtripArrays() {
        verifyRoundtrip(ARRAY_RECORD);
    }

    @Test
    public void testShouldRoundtripMultipleRecords() {
        verifyRoundtrip(RECORD, ARRAY_RECORD);
    }

    private void assertEvents(final String string, final EventList eventList) {
        decoder.process(string);

        final Iterator<Event> expected = eventList.getEvents().iterator();
        final Iterator<Event> actual = eventReceiver.getEvents().iterator();

        while (expected.hasNext() && actual.hasNext()) {
            Assert.assertEquals(expected.next().toString(), actual.next().toString());
        }

        Assert.assertFalse("Missing events", expected.hasNext());
        Assert.assertFalse("Unexpected events", actual.hasNext());
    }

    private void expectParseError(final String string, final String msg, final Class cause) {
        thrown.expectCause(IsInstanceOf.instanceOf(cause));
        expectParseError(string, msg);
    }

    private void expectParseError(final String string, final String msg) {
        thrown.expect(MetafactureException.class);
        thrown.expectMessage(msg);

        decoder.process(string);
    }

    private void verifyRoundtrip(final String... strings) {
        decoder
            .setReceiver(new JsonEncoder())
            .setReceiver(objectReceiver);

        decoder.process(String.join("\n", strings));

        for (final String string : strings) {
            Mockito.verify(objectReceiver).process(string);
        }

        Mockito.verifyNoMoreInteractions(objectReceiver);
    }

    private void expectRecord(final EventList eventList) {
        eventList.startRecord("1");
            eventList.literal("lit1", "value 1");
            eventList.startEntity(" ent1");
                eventList.literal("lit2", "value {x}");
                eventList.literal("lit\\3", "value 2 ");
            eventList.endEntity();
            eventList.literal("lit4", "value '3'");
            eventList.literal("lit5", null);
        eventList.endRecord();
    }

    private void expectArray(final EventList eventList, final String id) {
        eventList.startRecord(id);
            eventList.startEntity("arr1[]");
                eventList.literal("1", "val1");
                eventList.literal("2", "val2");
            eventList.endEntity();
            eventList.startEntity("arr2[]");
                eventList.startEntity("1");
                    eventList.literal("lit1", "val1");
                    eventList.literal("lit2", "val2");
                eventList.endEntity();
                eventList.startEntity("2");
                    eventList.literal("lit3", "val3");
                eventList.endEntity();
            eventList.endEntity();
            eventList.startEntity("arr3[]");
                eventList.startEntity("1[]");
                    eventList.startEntity("1");
                        eventList.literal("lit4", "val4");
                    eventList.endEntity();
                eventList.endEntity();
                eventList.startEntity("2[]");
                    eventList.startEntity("1");
                        eventList.literal("lit5", "val5");
                    eventList.endEntity();
                eventList.endEntity();
            eventList.endEntity();
        eventList.endRecord();
    }

}
