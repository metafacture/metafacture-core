/*
 * Copyright 2017, 2021 hbz
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Decodes a record in JSON format.
 *
 * @author Jens Wille
 *
 */
@Description("Decodes JSON to metadata events. The \'recordPath\' option can be used to set a JsonPath " +
    "to extract a path as JSON - or to split the data into multiple JSON documents.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-json")
public final class JsonDecoder extends DefaultObjectPipe<String, StreamReceiver> {

    public static final String DEFAULT_ARRAY_MARKER = JsonEncoder.ARRAY_MARKER;

    public static final String DEFAULT_ARRAY_NAME = "%d";

    public static final String DEFAULT_RECORD_ID = "%d";

    public static final String DEFAULT_ROOT_PATH = "";

    private final JsonFactory jsonFactory = new JsonFactory();

    private JsonParser jsonParser;
    private String arrayMarker = DEFAULT_ARRAY_MARKER;
    private String arrayName = DEFAULT_ARRAY_NAME;
    private String recordId = DEFAULT_RECORD_ID;
    private String recordPath = DEFAULT_ROOT_PATH;

    private int recordCount;

    /**
     * Creates an instance of {@link JsonDecoder}.
     */
    public JsonDecoder() {
    }

    /**
     * Flags wether to allow comments.
     *
     * @param allowComments true if comments should be allowed
     */
    public void setAllowComments(final boolean allowComments) {
        jsonFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, allowComments);
    }

    /**
     * Checks if comments are allowed.
     *
     * @return true if comments are allowed
     */
    public boolean getAllowComments() {
        return jsonFactory.isEnabled(JsonParser.Feature.ALLOW_COMMENTS);
    }

    /**
     * Sets the array marker.
     *
     * @param arrayMarker the array marker
     */
    public void setArrayMarker(final String arrayMarker) {
        this.arrayMarker = arrayMarker;
    }

    /**
     * Gets the array marker.
     *
     * @return the array marker
     */
    public String getArrayMarker() {
        return arrayMarker;
    }

    /**
     * Sets the name of the array.
     *
     * @param arrayName the name of the array
     */
    public void setArrayName(final String arrayName) {
        this.arrayName = arrayName;
    }

    /**
     * Gets the name of the array.
     *
     * @return the name of the array
     */
    public String getArrayName() {
        return arrayName;
    }

    /**
     * Sets the ID of the record.
     *
     * @param recordId the ID of the record
     */
    public void setRecordId(final String recordId) {
        this.recordId = recordId;
    }

    /**
     * Gets the ID of the record.
     *
     * @return the ID of the record
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * Sets the record count.
     *
     * @param recordCount the record count
     */
    public void setRecordCount(final int recordCount) {
        this.recordCount = recordCount;
    }

    /**
     * Gets the record count.
     *
     * @return the record count
     */
    public int getRecordCount() {
        return recordCount;
    }

    /**
     * Sets the record path.
     *
     * @param recordPath the record path
     */
    public void setRecordPath(final String recordPath) {
        this.recordPath = recordPath;
    }

    /**
     * Gets the record path.
     *
     * @return the record path
     */
    public String getRecordPath() {
        return recordPath;
    }

    /**
     * Resets the record count.
     */
    public void resetRecordCount() {
        setRecordCount(0);
    }

    @Override
    public void process(final String json) {
        assert !isClosed();
        if (recordPath.isEmpty()) {
            processRecord(json);
        }
        else {
            matches(JsonPath.read(json, recordPath)).forEach(record -> {
                processRecord(record);
            });
        }
    }

    private void processRecord(final String record) {
        createParser(record);
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

    private Stream<String> matches(final Object obj) {
        final List<?> records = (obj instanceof List<?>) ? ((List<?>) obj) : Arrays.asList(obj);
        return records.stream().map(doc -> {
            try {
                return new ObjectMapper().writeValueAsString(doc);
            }
            catch (final JsonProcessingException e) {
                e.printStackTrace();
                return doc.toString();
            }
        });
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
