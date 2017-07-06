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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

import java.io.IOException;

/**
 * Decodes a record in JSON format.
 *
 * @author Jens Wille
 *
 */
public final class JsonDecoder extends DefaultObjectPipe<String, StreamReceiver> {

    public static final String DEFAULT_ARRAY_MARKER = JsonEncoder.ARRAY_MARKER;

    public static final String DEFAULT_ARRAY_NAME = "%d";

    public static final String DEFAULT_RECORD_ID = "%d";

    private final JsonFactory jsonFactory = new JsonFactory();

    private JsonParser jsonParser;
    private String arrayMarker;
    private String arrayName;
    private String recordId;
    private int recordCount;

    public JsonDecoder() {
        super();

        setArrayMarker(DEFAULT_ARRAY_MARKER);
        setArrayName(DEFAULT_ARRAY_NAME);
        setRecordId(DEFAULT_RECORD_ID);

        resetRecordCount();
    }

    public void setArrayMarker(final String arrayMarker) {
        this.arrayMarker = arrayMarker;
    }

    public String getArrayMarker() {
        return arrayMarker;
    }

    public void setArrayName(final String arrayName) {
        this.arrayName = arrayName;
    }

    public String getArrayName() {
        return arrayName;
    }

    public void setRecordId(final String recordId) {
        this.recordId = recordId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordCount(final int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void resetRecordCount() {
        setRecordCount(0);
    }

    @Override
    public void process(final String string) {
        assert !isClosed();

        createParser(string);

        try {
            decode();
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
        finally {
            closeParser();
        }
    }

    @Override
    protected void onResetStream() {
        resetRecordCount();
    }

    private void createParser(final String string) {
        try {
            jsonParser = jsonFactory.createParser(string);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void closeParser() {
        try {
            jsonParser.close();
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void decode() throws IOException {
        while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
            getReceiver().startRecord(String.format(recordId, ++recordCount));
            decodeObject();
            getReceiver().endRecord();
        }

        if (jsonParser.currentToken() != null) {
            throw new MetafactureException(new StringBuilder()
                    .append("Unexpected token '")
                    .append(jsonParser.currentToken())
                    .append("' at ")
                    .append(jsonParser.getCurrentLocation())
                    .toString());
        }
    }

    private void decodeObject() throws IOException {
        while (jsonParser.nextToken() == JsonToken.FIELD_NAME) {
            decodeValue(jsonParser.getCurrentName(), jsonParser.nextToken());
        }
    }

    private void decodeArray() throws IOException {
        int arrayCount = 0;

        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            decodeValue(String.format(arrayName, ++arrayCount), jsonParser.currentToken());
        }
    }

    private void decodeValue(final String name, final JsonToken token) throws IOException {
        switch (token) {
            case START_OBJECT:
                getReceiver().startEntity(name);
                decodeObject();
                getReceiver().endEntity();

                break;
            case START_ARRAY:
                getReceiver().startEntity(name + arrayMarker);
                decodeArray();
                getReceiver().endEntity();

                break;
            case VALUE_NULL:
                getReceiver().literal(name, null);

                break;
            default:
                getReceiver().literal(name, jsonParser.getText());

                break;
        }
    }

}
