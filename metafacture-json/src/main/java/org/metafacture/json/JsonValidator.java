/*
 * Copyright 2021, 2022 Fabian Steeg, hbz
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
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Validate JSON against a given schema, pass only valid input to the receiver.
 *
 * @author Fabian Steeg (fsteeg)
 */
@Description("Validate JSON against a given schema, send only valid input to the receiver. Pass the schema location to validate against. " +
        "Set `schemaRoot` for resolving sub-schemas referenced in `$id` or `$ref` (defaults to the classpath root: `/`). " +
        "Write valid and/or invalid output to locations specified with `writeValid` and `writeInvalid`. " +
        "Set the JSON key for the record ID value with `idKey` (for logging output, defaults to `id`).")
@In(String.class)
@Out(String.class)
@FluxCommand("validate-json")
public final class JsonValidator extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonValidator.class);
    private static final String DEFAULT_SCHEMA_ROOT = "/";
    private static final String DEFAULT_ID_KEY = "id";
    private String schemaUrl;
    private Schema schema;
    private long fail;
    private long success;
    private FileWriter writeInvalid;
    private FileWriter writeValid;
    private String schemaRoot = DEFAULT_SCHEMA_ROOT;
    private String idKey = DEFAULT_ID_KEY;

    /**
     * @param url The URL of the schema to validate against.
     */
    public JsonValidator(final String url) {
        this.schemaUrl = url;
    }

    /**
     * @param schemaRoot The root location for resolving sub-schemas referenced in '$id' or '$ref'.
     */
    public void setSchemaRoot(final String schemaRoot) {
        this.schemaRoot = schemaRoot;
    }

    /**
     * @param writeValid The location to write valid data to.
     */
    public void setWriteValid(final String writeValid) {
        this.writeValid = fileWriter(writeValid);
    }

    /**
     * @param writeInvalid The location to write invalid data to.
     */
    public void setWriteInvalid(final String writeInvalid) {
        this.writeInvalid = fileWriter(writeInvalid);
    }

    /**
     * @param idKey The JSON key for the record ID value.
     */
    public void setIdKey(final String idKey) {
        this.idKey = idKey;
    }

    @Override
    public void process(final String json) {
        try {
            validate(json, new JSONObject(json) /* throws JSONException on syntax error */);
        }
        catch (final JSONException e) {
            handleInvalid(json, null, e.getMessage());
        }
    }

    private void validate(final String json, final JSONObject object) {
        try {
            initSchema();
            schema.validate(object); // throws ValidationException if invalid
            getReceiver().process(json);
            ++success;
            write(json, writeValid);
        }
        catch (final ValidationException e) {
            handleInvalid(json, object, e.getAllMessages().toString());
        }
    }

    @Override
    protected void onCloseStream() {
        close(writeInvalid);
        close(writeValid);
        LOG.debug("Success: {}, Fail: {}", success, fail);
        super.onCloseStream();
    }

    private void initSchema() {
        if (schema != null) {
            return;
        }
        try (InputStream inputStream = getClass().getResourceAsStream(schemaUrl)) {
            schema = SchemaLoader.builder()
                    .schemaJson(new JSONObject(new JSONTokener(inputStream)))
                    .schemaClient(SchemaClient.classPathAwareClient())
                    .resolutionScope("classpath://" + schemaRoot)
                    .build().load().build();
        }
        catch (final IOException | JSONException e) {
            throw new MetafactureException(e.getMessage(), e);
        }
    }

    private FileWriter fileWriter(final String fileLocation) {
        try {
            return new FileWriter(fileLocation);
        }
        catch (final IOException e) {
            throw new MetafactureException(e.getMessage(), e);
        }
    }

    private void handleInvalid(final String json, final JSONObject object,
            final String errorMessage) {
        LOG.info("Invalid JSON: {} in {}", errorMessage, object != null ? object.opt(idKey) : json);
        ++fail;
        write(json, writeInvalid);
    }

    private void write(final String json, final FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.append(json);
                fileWriter.append("\n");
            }
            catch (final IOException e) {
                throw new MetafactureException(e.getMessage(), e);
            }
        }
    }

    private void close(final FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            }
            catch (final IOException e) {
                throw new MetafactureException(e.getMessage(), e);
            }
        }
    }

}
