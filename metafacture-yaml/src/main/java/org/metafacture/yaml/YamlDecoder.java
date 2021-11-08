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

package org.metafacture.yaml;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Decodes a record in YAML format.
 *
 * @author Jens Wille
 *
 */
@Description("Decodes YAML to metadata events.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-yaml")
public final class YamlDecoder extends DefaultObjectPipe<String, StreamReceiver> {

    public static final String DEFAULT_ARRAY_MARKER = YamlEncoder.ARRAY_MARKER;

    public static final String DEFAULT_ARRAY_NAME = "%d";

    public static final String DEFAULT_RECORD_ID = "%d";

    private final YAMLFactory yamlFactory = new YAMLFactory();

    private YAMLParser yamlParser;
    private String arrayMarker = DEFAULT_ARRAY_MARKER;
    private String arrayName = DEFAULT_ARRAY_NAME;
    private String recordId = DEFAULT_RECORD_ID;
    private int recordCount;

    /**
     * Creates an instance of {@link YamlDecoder}.
     */
    public YamlDecoder() {
    }

    /**
     * Sets the array marker. <strong>Default value:
     * {@value #DEFAULT_ARRAY_MARKER}</strong>
     *
     * @param arrayMarker the marker of the array
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
     * Sets the name of the array. <strong>Default value:
     * {@value #DEFAULT_ARRAY_NAME}</strong>
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
     * Sets the record ID. <strong>Default value:
     * {@value #DEFAULT_RECORD_ID}</strong>
     *
     * @param recordId the record ID
     */
    public void setRecordId(final String recordId) {
        this.recordId = recordId;
    }

    /**
     * Get the record ID.
     *
     * @return the record ID
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * Sets the record count.
     *
     * @param recordCount the record count.
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
     * Resets the record count.
     */
    public void resetRecordCount() {
        setRecordCount(0);
    }

    @Override
    public void process(final String yaml) {
        assert !isClosed();

        createParser(yaml);

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
                return new YAMLMapper(yamlFactory).writeValueAsString(doc);
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
            yamlParser = yamlFactory.createParser(string);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void closeParser() {
        try {
            yamlParser.close();
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void decode() throws IOException {
        while (yamlParser.nextToken() == JsonToken.START_OBJECT) {
            getReceiver().startRecord(String.format(recordId, ++recordCount));
            decodeObject();
            getReceiver().endRecord();
        }

        if (yamlParser.currentToken() != null) {
            throw new MetafactureException(new StringBuilder()
                    .append("Unexpected token '")
                    .append(yamlParser.currentToken())
                    .append("' at ")
                    .append(yamlParser.getCurrentLocation())
                    .toString());
        }
    }

    private void decodeObject() throws IOException {
        while (yamlParser.nextToken() == JsonToken.FIELD_NAME) {
            decodeValue(yamlParser.getCurrentName(), yamlParser.nextToken());
        }
    }

    private void decodeArray() throws IOException {
        int arrayCount = 0;

        while (yamlParser.nextToken() != JsonToken.END_ARRAY) {
            decodeValue(String.format(arrayName, ++arrayCount), yamlParser.currentToken());
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
                getReceiver().literal(name, yamlParser.getText());

                break;
        }
    }

}
